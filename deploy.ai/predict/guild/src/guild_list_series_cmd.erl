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

-module(guild_list_series_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild list-series",
      "[OPTION]... [RUNDIR]",
      "List series for a run in RUNIDR or the latest using --latest-run.\n"
      "\n"
      "Use 'guild list-runs' to list runs that can be used for RUNDIR.",
      guild_cmd_support:project_options([latest_run]),
      [{pos_args, {0, 1}}]).

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, Args) ->
    RunDir = guild_cmd_support:run_db_for_args(Opts, Args),
    print_stat_names(guild_run_db:series_keys(RunDir)).

print_stat_names({ok, Names}) ->
    Print = fun(N) -> io:format("~s~n", [N]) end,
    lists:foreach(Print, Names).
