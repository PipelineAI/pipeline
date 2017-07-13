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
%% guild_op
%%
%% Interface to operations. Operations are implemented by behavior
%% modules (typically ending in '_op' - e.g. guild_train_op,
%% guild_eval_op, etc.)
%%
%% Op implementations should provide a function that creates a static
%% op definition consisting of a {Module, InitState} definition. This
%% should be used in static information calls (see below). Callbacks
%% that apply to static state will be made as
%% Module:Function(InitState).
%%
%% Process state is initialized via the init/1 callback, as with
%% typical processes.
%%
%% Once the task is started, callbacks will be made using the process
%% state.
%%
%% This split interface is used to support behavior prior to starting
%% the task (static state), in addition to traditional process
%% callbacks, which are made with process state.

-module(guild_op).

-export([start_link/2, stop/2, cmd_preview/1, opdir/1, ospid/1]).

-export([init/1, handle_task/1, handle_msg/3]).

-export([behaviour_info/1]).

-record(state, {mod, mod_state, opdir, exec_ospid, exec_pid,
                stream_handlers, stdout_buf, stderr_buf}).

%% ===================================================================
%% Behavior callbacks
%% ===================================================================

behaviour_info(callbacks) ->
    [{cmd_preview, 1},
     {init, 1},
     {cmd, 1},
     {opdir, 1},
     {meta, 1},
     {tasks, 1}].

%% ===================================================================
%% Start / init
%% ===================================================================

start_link(Name, {Mod, InitState}) ->
    e2_task:start_link(?MODULE, [Mod, InitState], [{registered, Name}]).

init([Mod, InitState]) ->
    case Mod:init(InitState) of
        {ok, ModState} ->
            init(Mod, ModState);
        Other ->
            Other
    end.

init(Mod, ModState) ->
    guild_proc:reg(operation, self()),
    process_flag(trap_exit, true),
    {ok, init_state(Mod, ModState)}.

init_state(Mod, ModState) ->
    #state{mod=Mod, mod_state=ModState}.

%% ===================================================================
%% API
%% ===================================================================

stop(OpPid, Timeout) ->
    e2_task:stop(OpPid, Timeout).

cmd_preview({Mod, ModInitState}) ->
    Mod:cmd_preview(ModInitState).

opdir(Op) ->
    gproc:get_value({p, l, opdir}, Op).

ospid(Op) ->
    gproc:get_value({p, l, ospid}, Op).

%% ===================================================================
%% Task
%% ===================================================================

handle_task(State) ->
    try guild_util:fold_apply(op_steps(), State) of
        Next -> {wait_for_msg, Next}
    catch
        exit:{stop, Reason} -> {stop, Reason}
    end.

op_steps() ->
    [fun init_app_support/1,
     fun init_opdir/1,
     fun init_error_log/1,
     fun write_meta/1,
     fun start_exec/1,
     fun start_core_tasks/1,
     fun start_op_tasks/1].

%% ===================================================================
%% App support
%% ===================================================================

init_app_support(State) ->
    guild_app:init_support([exec, json]),
    State.

%% ===================================================================
%% Op dir
%% ===================================================================

init_opdir(#state{mod=M, mod_state=MS}=State) ->
    case M:opdir(MS) of
        {ok, OpDir, NextMS} ->
            init_opdir(OpDir, State#state{mod_state=NextMS});
        {ignore, NextMS} ->
            State#state{mod_state=NextMS}
    end.

init_opdir(OpDir, State) ->
    guild_rundir:init(OpDir),
    gproc:add_local_property(opdir, OpDir),
    State#state{opdir=OpDir}.

%% ===================================================================
%% Error log
%% ===================================================================

init_error_log(#state{opdir=undefined}=State) ->
    State;
init_error_log(#state{opdir=Dir}=State) ->
    LogPath = guild_rundir:guild_file(Dir, "errors.log"),
    error_logger:logfile({open, LogPath}),
    State.

%% ===================================================================
%% Meta
%% ===================================================================

write_meta(#state{opdir=undefined}=State) ->
    State;
write_meta(#state{mod=M, mod_state=MS}=State) ->
    case M:meta(MS) of
        {ok, Meta, NextMS} ->
            write_meta(Meta, State#state{mod_state=NextMS});
        {ignore, NextMS} ->
            State#state{mod_state=NextMS};
        {stop, Reason} ->
            exit({stop, Reason})
    end.

write_meta(Meta, #state{opdir=Dir}=State) ->
    guild_rundir:write_attrs(Dir, Meta),
    State.

%% ===================================================================
%% Start exec
%% ===================================================================

start_exec(#state{mod=M, mod_state=MS}=State) ->
    case M:cmd(MS) of
        {ok, Args, Env, Cwd, NextMS} ->
            start_exec(Args, Env, Cwd, State#state{mod_state=NextMS});
        {stop, Reason} ->
            exit({stop, Reason})
    end.

start_exec(Args, Env, Cwd, State) ->
    start_exec_(Args, Env, Cwd, reset_exec_support(State)).

reset_exec_support(State) ->
    State#state{
      stdout_buf=guild_util:new_input_buffer(),
      stderr_buf=guild_util:new_input_buffer(),
      stream_handlers=init_stream_handlers(State)}.

init_stream_handlers(State) ->
    Specs = stream_handler_specs(State),
    Inits = guild_op_support:op_stream_handlers(Specs),
    [Init(self()) || Init <- Inits].

stream_handler_specs(#state{opdir=undefined}) ->
    [console];
stream_handler_specs(_) ->
    [console, run_db_output].

start_exec_(Args, Env, Cwd, State) ->
    Opts = [{env, Env}, {cd, Cwd}, stdout, stderr],
    {ok, Pid, OSPid} = guild_exec:run_link(Args, Opts),
    gproc:add_local_property(ospid, OSPid),
    State#state{exec_pid=Pid, exec_ospid=OSPid}.

%% ===================================================================
%% Core tasks
%% ===================================================================

start_core_tasks(#state{opdir=undefined}=State) ->
    State;
start_core_tasks(State) ->
    start_tasks(
      [{guild_run_status_task, start_link, []},
       {guild_run_db_task, start_link, []}]),
    State.

start_tasks(Tasks) ->
    lists:foreach(fun start_task/1, Tasks).

start_task(TaskSpec) ->
    {ok, _} = guild_optask_sup:start_task(TaskSpec, self()).

%% ===================================================================
%% Op tasks
%% ===================================================================

start_op_tasks(#state{mod=M, mod_state=MS}=State) ->
    case M:tasks(MS) of
        {ok, Tasks, NextMS} ->
            start_tasks(Tasks),
            State#state{mod_state=NextMS};
        {ignore, NextMS} ->
            State#state{mod_state=NextMS};
        {stop, Reason} ->
            exit({stop, Reason})
    end.

%% ===================================================================
%% Messages
%% ===================================================================

handle_msg({Stream, OSPid, Bin}, _From, #state{exec_ospid=OSPid}=State) ->
    handle_input(Stream, Bin, State);
handle_msg({'EXIT', Pid, Reason}, _From, #state{exec_pid=Pid}) ->
    handle_exec_exit(Reason);
handle_msg({stop, Timeout}, _From, State) ->
    handle_stop(Timeout, State).

handle_input(Stream, Bin, State) ->
    {Lines, Next} = stream_input(Stream, Bin, State),
    handle_stream_lines(Stream, Lines, State),
    {noreply, Next}.

stream_input(stdout, Bin, #state{stdout_buf=Buf}=S) ->
    {Lines, NextBuf} = guild_util:input(Buf, Bin),
    {Lines, S#state{stdout_buf=NextBuf}};
stream_input(stderr, Bin, #state{stderr_buf=Buf}=S) ->
    {Lines, NextBuf} = guild_util:input(Buf, Bin),
    {Lines, S#state{stderr_buf=NextBuf}}.

handle_stream_lines(Stream, Lines, #state{stream_handlers=Handlers}) ->
    dispatch_to_stream_handlers({Stream, Lines}, Handlers).

dispatch_to_stream_handlers(Msg, Handlers) ->
    lists:foreach(fun(H) -> call_stream_handler(H, Msg) end, Handlers).

call_stream_handler(F, Msg) when is_function(F) -> F(Msg);
call_stream_handler({M, F, A}, Msg) -> M:F([Msg|A]).

handle_exec_exit(Reason) ->
    {stop, Reason}.

handle_stop(Timeout, #state{exec_pid=Pid}=State) ->
    exec:stop_and_wait(Pid, Timeout),
    {stop, normal, ok, State}.
