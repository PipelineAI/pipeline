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

-module(guild_data_reader).

-export([flags/1, attrs/1, series/3, output/1, series_keys/1,
         compare/2]).

%% ===================================================================
%% Flags
%% ===================================================================

flags(Run) ->
    Db = run_db(Run),
    case guild_run_db:flags(Db) of
        {ok, Flags} -> format_flags(Flags);
        {error, Err} -> error({db_dlags, Err, Run})
    end.

format_flags(Flags) -> maps:from_list(Flags).

%% ===================================================================
%% Attrs
%% ===================================================================

attrs(Run) ->
    Db = run_db(Run),
    case guild_run_db:attrs(Db) of
        {ok, Attrs} -> format_attrs(Attrs);
        {error, Err} -> error({db_attrs, Err, Run})
    end.

format_attrs(Attrs) -> maps:from_list(Attrs).

%% ===================================================================
%% Series
%% ===================================================================

series(Run, Pattern, Max) ->
    Db = run_db(Run),
    case guild_run_db:series(Db, Pattern) of
        {ok, Series} -> format_series(reduce_series(Series, Max));
        {error, Err} -> error({db_series, Err, Run, Pattern})
    end.

reduce_series(Series, all) ->
    Series;
reduce_series(Series, Max) ->
    [{Key, guild_util:reduce_to(Vals, Max)} || {Key, Vals} <- Series].

format_series(Series) ->
    maps:from_list(Series).

%% ===================================================================
%% Output
%% ===================================================================

output(Run) ->
    Db = run_db(Run),
    case guild_run_db:output(Db) of
        {ok, Output} -> format_output(Output);
        {error, Err} -> error({db_output, Err, Run})
    end.

format_output(Output) ->
    [[Time div 1000, stream_id(Stream), Val]
     || {Time, Stream, Val} <- Output].

stream_id(stdout) -> 0;
stream_id(stderr) -> 1;
stream_id(_) -> null.

%% ===================================================================
%% Series keys
%% ===================================================================

series_keys(Runs) ->
    Keys = lists:foldl(fun series_keys_acc/2, sets:new(), Runs),
    sets:to_list(Keys).

series_keys_acc(Run, Acc) ->
    Keys = run_series_keys(Run),
    sets:union(Acc, sets:from_list(Keys)).

run_series_keys(Run) ->
    Db = run_db(Run),
    case guild_run_db:series_keys(Db) of
        {ok, Keys} -> Keys;
        {error, Err} -> error({db_series_keys, Err, Run})
    end.

%% ===================================================================
%% Compare
%% ===================================================================

compare(Runs, Sources) ->
    [run_compare(Run, Sources) || Run <- Runs].

run_compare(Run, Sources) ->
    maps:from_list(
      [{run, guild_run_util:format_run(Run)}
       |[{source_key(Source), run_source(Source, Run)}
         || Source <- Sources]]).

source_key(Name) -> list_to_binary(Name).

run_source("flags", Run)           -> flags(Run);
run_source("attrs", Run)           -> attrs(Run);
run_source("output", Run)          -> output(Run);
run_source("series/" ++ Path, Run) -> series(Run, Path, all);
run_source(Other, _Run)            -> error({run_source, Other}).

%% ===================================================================
%% Utils / support
%% ===================================================================

run_db(Run) ->
    RunDir = guild_run:dir(Run),
    case guild_run_db:open(RunDir) of
        ok -> RunDir;
        {error, missing} -> error({db_missing, Run})
    end.
