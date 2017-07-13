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

-module(guild_cli).

-export([parser/0, main/1, cli_error/1, cli_error/2, out/1, out/2,
         out_par/1, out_par/2, closeable_out/2, warn/1, warn/2]).

-define(default_exit_code, 2).
-define(page_width, 79).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:command_parser(
      "guild",
      "[OPTION]... COMMAND [ARG]...",
      "",
      global_opts(),
      parser_commands(),
      [{version, guild:version()}]).

global_opts() ->
    [{trace,     "--trace",     "trace a module/function", [hidden]},
     {reload,    "--reload",    "reload modified modules", [hidden, flag]},
     {observer,  "--observer",  "run observer",            [hidden, flag]},
     {debug,     "--debug",     "enable detailed logging", [hidden, flag]}].

parser_commands() ->
    parser_commands(
      ["check",
       "cmds-json",
       "delete-eval",
       "delete-run",
       "evaluate",
       "init",
       "install",
       "list-attrs",
       "list-evals",
       "list-models",
       "list-packages",
       "list-runs",
       "list-series",
       "package",
       "prepare",
       "serve",
       "status",
       "train",
       "uninstall",
       "view"]).

parser_commands(Names) ->
    Info = [{Name, cmd_info(Name)} || Name <- Names],
    [{Name, Desc, M:parser()} || {Name, {M, Desc}} <- Info].

cmd_info("check")       -> {guild_check_cmd, "check Guild setup"};
cmd_info("cmds-json")   -> {guild_cmds_json_cmd, "commands JSON (hidden)"};
cmd_info("delete-eval") -> {guild_delete_eval_cmd, "deletes an evaluation"};
cmd_info("delete-run")  -> {guild_delete_run_cmd, "deletes a run"};
cmd_info("evaluate")    -> {guild_eval_cmd, "evaluate a trained model"};
cmd_info("init")        -> {guild_init_cmd, "initialize a Guild project"};
cmd_info("install")     -> {guild_install_cmd, "install a Guild package"};
cmd_info("list-attrs")  -> {guild_list_attrs_cmd, "list run attributes"};
cmd_info("list-evals")  -> {guild_list_evals_cmd, "list run evaluations"};
cmd_info("list-models") -> {guild_list_models_cmd, "list project models"};
cmd_info("list-packages") -> {guild_list_packages_cmd, "list installed packages"};
cmd_info("list-runs")   -> {guild_list_runs_cmd, "list project runs"};
cmd_info("list-series") -> {guild_list_series_cmd, "list run series names"};
cmd_info("package")     -> {guild_package_cmd, "create a Guild package"};
cmd_info("prepare")     -> {guild_prepare_cmd, "prepare model for training"};
cmd_info("serve")       -> {guild_serve_cmd, "serve a trained model"};
cmd_info("status")      -> {guild_status_cmd, "train a model"};
cmd_info("train")       -> {guild_train_cmd, "show project status"};
cmd_info("uninstall")   -> {guild_uninstall_cmd, "uninstall Guild packages"};
cmd_info("view")        -> {guild_view_cmd, "start Guild View"}.

%% ===================================================================
%% Main
%% ===================================================================

main({Cmd, Opts, Args}) ->
    init_tty_error_logger(),
    apply_global_opts(Opts),
    {M, _Desc} = cmd_info(Cmd),
    handle_main_result(M:main(Opts, Args)).

handle_main_result(Result) ->
    guild_proc:wait_for({scope, global}),
    Result.

%% ===================================================================
%% TTY error logger
%% ===================================================================

init_tty_error_logger() ->
    error_logger:tty(false),
    error_logger:add_report_handler(guild_error_logger_tty).

%% ===================================================================
%% Global opts
%% ===================================================================

apply_global_opts(Opts) ->
    guild_trace:init_from_opts(Opts),
    guild_reloader:init_from_opts(Opts),
    maybe_start_sasl(Opts),
    guild_observer:maybe_start_from_opts(Opts).

maybe_start_sasl(Opts) ->
    case proplists:get_bool(debug, Opts) of
        true -> start_sasl();
        false -> ok
    end.

start_sasl() ->
    ok = application:ensure_started(sasl).

%% ===================================================================
%% Generate CLI error
%% ===================================================================

cli_error(ExitCode) when is_integer(ExitCode) ->
    cli:main_error(ExitCode);
cli_error(Msg) ->
    cli:main_error(?default_exit_code, Msg).

cli_error(ExitCode, Msg) ->
   cli:main_error(ExitCode, Msg).

%% ===================================================================
%% Output
%% ===================================================================

out(Msg) ->
    out(Msg, []).

out(Msg, Data) ->
    io:format(standard_io, Msg, Data).

out_par(Msg) ->
    out_par(Msg, []).

out_par(Msg, Data) ->
    Text = io_lib:format(Msg, Data),
    io:format(standard_io, wrap(Text), []).

closeable_out(Msg, Data) ->
    try
        out(Msg, Data)
    catch
        error:terminated -> erlang:halt(0)
    end.

wrap(Text) ->
    prettypr:format(prettypr:text_par(Text), ?page_width, ?page_width).

warn(Msg) ->
    warn(Msg, []).

warn(Msg, Data) ->
    io:format(standard_error, Msg, Data).
