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

-module(guild_run).

-export([new/2, id/1, dir/1, attrs/1, attr/2, attr/3, int_attr/2,
         int_attr/3, is_run/1, run_for_rundir/1, runs_for_project/1,
         runs_for_runroots/1, timestamp/0, timestamp_to_now/1]).

-record(run, {id, dir, attrs}).

-define(rundir_marker, "guild.d").

%% ===================================================================
%% New
%% ===================================================================

new(Dir, Attrs) ->
    #run{id=id_for_dir(Dir), dir=Dir, attrs=Attrs}.

id_for_dir(Dir) -> erlang:crc32(Dir).

%% ===================================================================
%% Run attrs
%% ===================================================================

id(#run{id=Id}) -> Id.

dir(#run{dir=Dir}) -> Dir.

attrs(#run{attrs=Attrs}) -> Attrs.

attr(#run{attrs=Attrs}, Name) ->
    case lists:keyfind(Name, 1, Attrs) of
        {_, Val} -> {ok, Val};
        false -> error
    end.

attr(Run, Name, Default) ->
    case attr(Run, Name) of
        {ok, Val} -> Val;
        error -> Default
    end.

int_attr(Run, Name) ->
    try_convert(attr(Run, Name), fun binary_to_integer/1).

int_attr(Run, Name, Default) ->
    try_convert(attr(Run, Name), fun binary_to_integer/1, Default).

try_convert({ok, Bin}, F) ->
    try F(Bin) of Val -> {ok, Val} catch _:_ -> error end;
try_convert(error, _F) ->
    error.

try_convert(Resp, F, Default) ->
    case try_convert(Resp, F) of
        {ok, Val} -> Val;
        error -> Default
    end.

%% ===================================================================
%% Is run
%% ===================================================================

is_run(Dir) ->
    filelib:is_dir(run_marker(Dir)).

run_marker(Dir) ->
    filename:join(Dir, ?rundir_marker).

%% ===================================================================
%% Run for rundir
%% ===================================================================

run_for_rundir(RunDir) ->
    case is_run(RunDir) of
        true -> {ok, new(RunDir, read_meta_attrs(RunDir))};
        false -> error
    end.

read_meta_attrs(RunDir) ->
    MetaDir = guild_rundir:meta_dir(RunDir),
    case file:list_dir(MetaDir) of
        {ok, Files} -> attrs_for_files(MetaDir, Files);
        {error, _} -> []
    end.

attrs_for_files(Dir, Files) ->
    lists:foldl(
      fun(Name, Acc) -> acc_attr_for_file(Dir, Name, Acc) end,
      [], Files).

acc_attr_for_file(Dir, Name, Acc) ->
    case file:read_file(filename:join(Dir, Name)) of
        {ok, Bin} -> [{Name, Bin}|Acc];
        {error, _} -> Acc
    end.

%% ===================================================================
%% Runs for project
%% ===================================================================

runs_for_project(Project) ->
    runs_for_runroots(guild_project_util:all_runroots(Project)).

%% ===================================================================
%% Runs for run roots
%% ===================================================================

runs_for_runroots(RunRoots) ->
    lists:foldl(fun acc_runs_for_runroot/2, [], RunRoots).

acc_runs_for_runroot(RunRoot, Acc) ->
    lists:foldl(fun acc_run/2, Acc, find_rundirs(RunRoot)).

find_rundirs(RunRoot) ->
    Markers = filelib:wildcard("*/" ?rundir_marker, RunRoot),
    [filename:join(RunRoot, filename:dirname(Marker)) || Marker <- Markers].

acc_run(RunDir, Acc) ->
    {ok, Run} = run_for_rundir(RunDir),
    [Run|Acc].

%% ===================================================================
%% Timestamp (used for any time associated with a run)
%% ===================================================================

timestamp() ->
    erlang:system_time(milli_seconds).

timestamp_to_now(Timestamp) ->
    Mega = Timestamp div 1000000000,
    Seconds = Timestamp rem 1000000000 div 1000,
    Micro = Timestamp rem 1000,
    {Mega, Seconds, Micro}.
