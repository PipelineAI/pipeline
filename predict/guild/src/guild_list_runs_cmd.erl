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

-module(guild_list_runs_cmd).

-export([parser/0, main/2]).

-define(true_filter, fun(_) -> true end).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild list-runs",
      "[OPTION]...",
      "List project runs.",
      list_runs_opts() ++ guild_cmd_support:project_options(),
      [{pos_args, 0}]).

list_runs_opts() ->
    [{completed, "--completed",
      "show only completed runs", [flag]},
     {terminated, "--terminated",
      "show only runs that were stopped by the user", [flag]},
     {error, "--error",
      "show only runs that stopped due to an error", [flag]},
     {error_or_terminated, "--error-or-terminated",
      "show only runs that were stopped by the user or due to an error",
      [flag]},
     {with_export, "--with-export",
      "show only runs an exported model", [flag]}].

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, []) ->
    Project = guild_cmd_support:project_from_opts(Opts),
    guild_app:init_support([exec]),
    print_runs(runs_for_project(Project), Opts).

runs_for_project(Project) ->
    [{Run, run_status(Run)} || Run <- guild_run:runs_for_project(Project)].

run_status(R) ->
    case guild_run_util:run_status(R) of
        running -> running;
        crashed -> terminated;
        stopped ->
            case guild_run:attr(R, "exit_status") of
                {ok, <<"0">>} -> completed;
                {ok, _} -> error;
                error -> error
            end
    end.

print_runs(Runs, Opts) ->
    Filtered = lists:filter(run_filter(Opts), Runs),
    lists:foreach(fun print_run/1, Filtered).

run_filter(Opts) ->
    Filters =
        [status_filter(
           [completed],
           proplists:get_bool(completed, Opts)),
         status_filter(
           [terminated],
           proplists:get_bool(terminated, Opts)),
         status_filter(
           [error],
           proplists:get_bool(error, Opts)),
         status_filter(
           [error, terminated],
           proplists:get_bool(error_or_terminated, Opts)),
         exports_filter(Opts)
        ],
    fun(Run) -> apply_filters(Run, Filters) end.

status_filter(Tests, true) ->
    fun({_Run, Status}) -> lists:member(Status, Tests) end;
status_filter(_Status, false) ->
    ?true_filter.

exports_filter(Opts) ->
    case proplists:get_bool(with_export, Opts) of
        true -> fun(Run) -> has_export(Run) end;
        false -> ?true_filter
    end.

has_export({Run, _Status}) ->
    Export = filename:join(guild_run:dir(Run), "model/export.meta"),
    filelib:is_file(Export).

apply_filters(Run, [F|Rest]) ->
    case F(Run) of
        true -> apply_filters(Run, Rest);
        false -> false
    end;
apply_filters(_Run, []) ->
    true.

print_run({Run, Status}) ->
    Dir = guild_run:dir(Run),
    guild_cli:closeable_out("~s\t~s~n", [Dir, Status]).
