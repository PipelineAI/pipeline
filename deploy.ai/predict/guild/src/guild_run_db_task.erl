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
%% guild_run_db_task
%%
%% Initializes the rundb for an operation. Closes the db when the op
%% terminates.

-module(guild_run_db_task).

-behavior(e2_task).

-export([start_link/1]).

-export([init/1, handle_task/1, handle_msg/3]).

-record(state, {op, rundir}).

start_link(Op) ->
    e2_task:start_link(?MODULE, [Op], []).

init([Op]) ->
    monitor(process, Op),
    guild_proc:reg(optask, self()),
    {ok, init_state(Op)}.

init_state(Op) ->
    RunDir = guild_op:opdir(Op),
    #state{op=Op, rundir=RunDir}.

handle_task(State) ->
    open_db(State),
    {stop, normal}.

open_db(#state{rundir=RunDir}) ->
    ok = guild_run_db:open(RunDir, [create_if_missing]).

handle_msg({'DOWN', _, process, Op, _}, noreply, #state{op=Op}=State) ->
    handle_op_exit(State).

handle_op_exit(#state{rundir=RunDir}) ->
    guild_run_db:close(RunDir),
    {stop, normal}.
