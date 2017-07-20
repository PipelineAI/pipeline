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
%% guild_run_status_task
%%
%% Writes op status information as guild attrs.
%%
%% Expected behavior in time order:
%%
%% - Monitor op process
%% - Write LOCK containing our (beam process) PID
%% - On op exit (DOWN) write op exit attrs (status and stopped) and
%%   delete LOCK

-module(guild_run_status_task).

-behavior(e2_task).

-export([start_link/1]).

-export([init/1, handle_task/1, handle_msg/3]).

-record(state, {op, rundir}).

%% ===================================================================
%% Start / init
%% ===================================================================

start_link(Op) ->
    e2_task:start_link(?MODULE, [Op], []).

init([Op]) ->
    monitor(process, Op),
    guild_proc:reg(optask, self()),
    {ok, init_state(Op)}.

init_state(Op) ->
    RunDir = guild_op:opdir(Op),
    #state{op=Op, rundir=RunDir}.

%% ===================================================================
%% Task impl
%% ===================================================================

handle_task(#state{rundir=RunDir}=State) ->
    write_lock(RunDir),
    {wait_for_msg, State}.

write_lock(RunDir) ->
    ok = file:write_file(lock_file(RunDir), run_pid()).

lock_file(RunDir) ->
    guild_rundir:guild_file(RunDir, "LOCK").

run_pid() ->
    %% Use our pid as a proxy for the run
    os:getpid().

%% ===================================================================
%% Messages
%% ===================================================================

handle_msg({'DOWN', _, process, Op, Reason}, noreply, #state{op=Op}=State) ->
    handle_op_exit(Reason, State).

handle_op_exit(ExitReason, #state{rundir=RunDir}) ->
    try
        write_exit_attrs(RunDir, ExitReason),
        delete_lock(RunDir)
    catch
        error:enoent -> log_rundir_deleted()
    end,
    {stop, normal}.

write_exit_attrs(RunDir, ExitReason) ->
    Attrs =
        [{stopped, guild_run:timestamp()},
         {exit_status, op_exit_status(ExitReason)}],
    guild_rundir:write_attrs(RunDir, Attrs).

op_exit_status(normal)           -> 0;
op_exit_status({exit_status, N}) -> N.

delete_lock(RunDir) ->
    ok = file:delete(lock_file(RunDir)).

log_rundir_deleted() ->
    guild_log:internal(
      "Error updating run attributes (was RUNDIR deleted?)~n", []).
