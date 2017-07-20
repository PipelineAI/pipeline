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
%%
%% guild_eval
%%
%% Represents an evaluation operation for a run.

-module(guild_eval).

-export([is_eval/1, evals_for_rundir/1]).

is_eval(Dir) ->
    %% Use guild_run:is_run/1 as it's the same structural test.
    guild_run:is_run(Dir).

evals_for_rundir(Dir) ->
    Pattern = filename:join(Dir, "eval-*"),
    filelib:wildcard(binary_to_list(Pattern)).
