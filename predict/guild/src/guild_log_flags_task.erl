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
%% guild_log_flags_task
%%
%% Writes op flags to the rundb.

-module(guild_log_flags_task).

-behavior(e2_task).

-export([start_link/2]).

-export([init/1, handle_task/1]).

-record(state, {rundir, flags}).

start_link(Op, Flags) ->
    e2_task:start_link(?MODULE, [Op, Flags], []).

init([Op, Flags]) ->
    guild_proc:reg(optask, self()),
    RunDir = guild_op:opdir(Op),
    {ok, #state{rundir=RunDir, flags=Flags}}.

handle_task(#state{rundir=RunDir, flags=Flags}) ->
    guild_run_db:log_flags(RunDir, Flags),
    {stop, normal}.
