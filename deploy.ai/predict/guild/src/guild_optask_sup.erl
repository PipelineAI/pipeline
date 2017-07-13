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

-module(guild_optask_sup).

-export([start_link/0, start_task/2, tasks/0]).

-behavior(e2_task_supervisor).

start_link() ->
    e2_task_supervisor:start_link(?MODULE, {erlang, apply, []}, [registered]).

start_task({M, F, A}, OpPid) ->
    e2_task_supervisor:start_task(?MODULE, [M, F, [OpPid|A]]).

tasks() ->
    supervisor:which_children(?MODULE).
