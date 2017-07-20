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

-module(guild_delete_run_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild delete-run",
      "[OPTION]... [RUNDIR]",
      "Remove a run by deleting RUNDIR or the latest using --latest-run.\n"
      "\n"
      "Use 'guild list-runs' to list runs that can be used for RUNDIR.\n",
      guild_cmd_support:project_options([latest_run]),
      [{pos_args, {0, 1}}]).

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, Args) ->
    Project = guild_cmd_support:project_from_opts(Opts),
    RunDir = guild_cmd_support:rundir_from_args(Args, Opts, Project),
    maybe_remove_rundir(guild_run:is_run(RunDir), RunDir).

maybe_remove_rundir(true, Dir) ->
    remove_dir(Dir);
maybe_remove_rundir(false, Dir) ->
    print_invalid_rundir(Dir).

remove_dir(Dir) ->
    guild_cli:out("Deleting ~s~n", [Dir]),
    "" = os:cmd(["rm -rf ", quote_dir(Dir)]),
    ok.

quote_dir(Dir) ->
    Escaped = re:replace(Dir, "\"", "\\\\\"", [global, {return, list}]),
    "\"" ++ Escaped ++ "\"".

print_invalid_rundir(Dir) ->
    guild_cli:cli_error(
      io_lib:format(
        "'~s' is not a Guild run directory~n"
        "Try 'guild list-runs' for a list.",
        [Dir])).
