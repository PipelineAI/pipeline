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

-module(guild_exec_monitor_task).

-behavior(e2_service).

-export([start_link/3]).

-export([init/1, handle_task/1, handle_msg/3]).

-record(state, {op, op_stop_timeout, exec_pid, exec_ospid}).

start_link(Op, OSPid, OpStopTimeout) ->
    e2_task:start_link(?MODULE, [Op, OSPid, OpStopTimeout]).

init([Op, OSPid, OpStopTimeout]) ->
    process_flag(trap_exit, true),
    guild_proc:reg(optask, self()),
    {ok, #state{op=Op, op_stop_timeout=OpStopTimeout, exec_ospid=OSPid}}.

handle_task(#state{exec_ospid=OSPid}=State) ->
    guild_cli:out("Guild monitoring process ~b~n", [OSPid]),
    handle_monitored_ospid(monitor_ospid(State), State).

monitor_ospid(#state{exec_ospid=OSPid}) ->
    exec:manage(OSPid, [monitor, monitor_kill_opt()]).

monitor_kill_opt() ->
    %% We're exploiting some poorly defined behavior in the erlexec
    %% lib to prevent it from killing the monitored process. By
    %% default when erlexec monitors a process it kills it when the
    %% monitoring process (us) exits. This is obviously catasrophic in
    %% our case. By setting kill here to some non process killing
    %% signal we end up circumventing this behavior.
    {kill, "_"}.

handle_monitored_ospid({ok, ExecPid, _OSPid}, State) ->
    {wait_for_msg, State#state{exec_pid=ExecPid}};
handle_monitored_ospid({error, not_found}, _State) ->
    {stop, normal}.

handle_msg({'DOWN', _, process, Pid, _}, noreply, #state{exec_pid=Pid}=State) ->
    handle_exec_exit(State).

handle_exec_exit(#state{op=Op, op_stop_timeout=Timeout, exec_ospid=OSPid}) ->
    guild_cli:out("Guild stopping operation (process ~b exited)~n", [OSPid]),
    guild_op:stop(Op, Timeout),
    {stop, normal}.
