%% Copyright 2016-2017 TensorHub, Inc.
%%
%% Licensed under the Apache License, Version 2.0 (the "License");
%% you may not use this file except in compliance with the License.
%% You may obtain a copy of the License at
%%
%% http://www.apache.org/licenses/LICENSE-2.0
%%
%% Unless required by applicable law or agreed to in writing, software
%% distributed under the License is distributed on an "AS IS" BASIS,
%% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%% See the License for the specific language governing permissions and
%% limitations under the License.

-module(guild_view_cmd).

-export([parser/0, main/2]).

-define(default_port, 6333).
-define(default_refresh_interval, 5).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild view",
      "[OPTION]...",
      "Start a web based app to view and interact with project runs.\n"
      "\n"
      "When the server is running, open your browser on the specified port "
      "(default is " ++ integer_to_list(?default_port)
      ++ ") - e.g. http://localhost:" ++ integer_to_list(?default_port) ++ ".\n"
      "\n"
      "To log server requests, use --logging.\n"
      "\n"
      "To modify the refresh interval (default is "
      ++ integer_to_list(?default_refresh_interval)
      ++ " seconds), use --interval. This is useful for "
      "longer running operations that don't need to be refreshed often.",
      view_options() ++ guild_cmd_support:project_options(),
      [{pos_args, 0}]).

view_options() ->
    [{port, "-p, --port",
      fmt("HTTP server port (default is ~b)", [?default_port]),
      [{metavar, "PORT"}]},
     {interval, "-n, --interval",
      fmt("refresh interval in seconds (default is ~b)",
          [?default_refresh_interval]),
      [{metavar, "SECONDS"}]},
     {logging, "-l, --logging",
      "enable logging", [flag]},
     %% view --debug flag is shorthand for global --debug and --reload
     {debug, "--debug", "Run with debug settings", [hidden, flag]},
     {tf_demo, "--tf-demo", "Use tf-demo data", [hidden, flag]}].

fmt(Msg, Data) -> io_lib:format(Msg, Data).

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, []) ->
    apply_debug_opts(Opts),
    Project = guild_cmd_support:project_from_opts(Opts),
    guild_app:init_support([exec]),
    TBInfo = start_tensorboard(Project, Opts),
    View = init_project_view(Project, Opts, TBInfo),
    Port = guild_cmd_support:port_opt(Opts, ?default_port),
    Server = start_http_server(View, Port, Opts),
    guild_cli:out("Guild View running on port ~b~n", [Port]),
    wait_for_server_and_terminate(Server, Opts).

apply_debug_opts(Opts) ->
    maybe_apply_debug_opts(proplists:get_bool(debug, Opts)).

maybe_apply_debug_opts(true) ->
    guild_reloader:init_from_opts([reload]),
    ok = application:ensure_started(sasl);
maybe_apply_debug_opts(false) ->
    ok.

start_tensorboard(Project, Opts) ->
    LogDir = tensorboard_logdir(Project),
    Port = guild_util:free_port(),
    TBChild = {guild_tf_tensorboard, start_link, [LogDir, Port]},
    handle_tensorboard_start(guild_app:start_child(TBChild), Port, Opts).

tensorboard_logdir(Project) ->
    string:join(guild_project_util:all_runroots(Project), ",").

handle_tensorboard_start({ok, _}, Port, Opts) ->
    maybe_report_tensorboard_port(proplists:get_bool(debug, Opts), Port),
    tb_info(Port);
handle_tensorboard_start({error, Err}, _Port, _Opts) ->
    report_tensorboard_error(Err),
    tb_info(undefined).

maybe_report_tensorboard_port(true, Port) ->
    guild_cli:out("TensorBoard running on port ~b~n", [Port]);
maybe_report_tensorboard_port(false, _Port) ->
    ok.

tb_info(Port) ->
    #{port => Port}.

report_tensorboard_error(Err) ->
    guild_cli:out(
      io_lib:format(
        "Unable to start TensorBoard (~p)\n"
        "TensorBoard integration will be disabled\n",
        [Err])).

init_project_view(Project, Opts, TBInfo) ->
    Settings = view_settings(Opts, TBInfo),
    {ok, View} = guild_view_sup:start_view(Project, Settings),
    View.

view_settings(Opts, TBInfo) ->
    TF = apply_tf_demo_mode(TBInfo, Opts),
    #{refreshInterval => interval_opt(Opts),
      tensorboard => TF}.

apply_tf_demo_mode(M, Opts) ->
    M#{demo => proplists:get_bool(tf_demo, Opts)}.

interval_opt(Opts) ->
    validate_interval(
      cli_opt:int_val(
        interval, Opts, ?default_refresh_interval,
        "invalid value for --interval")).

validate_interval(I) when I > 0 -> I;
validate_interval(_) -> throw({error, "invalid value for --interval"}).

start_http_server(View, Port, Opts) ->
    guild_http:init_mime_types(),
    case guild_view_http:start_server(View, Port, Opts) of
        {ok, Server} ->
            Server;
        {error, {{listen, eaddrinuse}, _Stack}} ->
            port_in_use_error(Port)
    end.

port_in_use_error(Port) ->
    guild_cli:cli_error(
      io_lib:format(
        "port ~b is being used by another application\n"
        "Try 'guild view --port PORT' with a different port.",
        [Port])).

wait_for_server_and_terminate(Pid, MainOpts) ->
    guild_proc:reg(Pid),
    Exit = guild_proc:wait_for({proc, Pid}),
    handle_server_exit(Exit, MainOpts).

handle_server_exit({_, normal}, _MainOpts) ->
    guild_cli:out("Server stopped by user~n");
handle_server_exit({_, Other}, MainOpts) ->
    guild_log:internal("Restarting server due to error: ~p~n", [Other]),
    main(MainOpts, []).
