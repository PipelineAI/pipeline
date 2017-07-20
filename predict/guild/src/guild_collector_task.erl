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

-module(guild_collector_task).

-behavior(e2_task).

-export([start_link/3]).

-export([init/1, handle_task/1, handle_msg/3]).

-record(state, {op, cmd, rundir, op_ospid, exec_pid, exec_ospid,
                buf, stderr_handler, waiting, stopping}).

-define(default_repeat, 5000).
-define(stop_exec_timeout, 1000).
-define(stop_task_timeout, 5000).

%% ===================================================================
%% Start / init
%% ===================================================================

start_link(Op, CollectorScript, Opts) ->
    Cmd = local_exe_cmd(CollectorScript),
    {TaskOpts, CollectorOpts} = e2_task_impl:split_options(?MODULE, Opts),
    SafeTaskOpts = ensure_repeat(TaskOpts),
    e2_task:start_link(?MODULE, [Op, Cmd, CollectorOpts], SafeTaskOpts).

local_exe_cmd(Exe) ->
    {[guild_app:priv_bin(Exe)], []}.

ensure_repeat(Opts) ->
    case proplists:get_value(repeat, Opts) of
        undefined -> [{repeat, ?default_repeat}|Opts];
        _ -> Opts
    end.

init([Op, Cmd, Opts]) ->
    process_flag(trap_exit, true),
    monitor(process, Op),
    guild_proc:reg(optask, self()),
    {ok, init_state(Op, Cmd, Opts)}.

init_state(Op, Cmd, Opts) ->
    RunDir = guild_op:opdir(Op),
    OpOsPid = guild_op:ospid(Op),
    #state{
       op=Op,
       cmd=Cmd,
       rundir=RunDir,
       op_ospid=OpOsPid,
       buf=guild_collector_protocol:new_input_buffer(),
       stderr_handler=stderr_handler_opt(Opts),
       waiting=false}.

stderr_handler_opt(Opts) ->
    case proplists:get_value(stderr_handler, Opts) of
        {F, _}=H
          when is_function(F) -> H;
        undefined             -> default_stderr_handler();
        Other                 -> error({invalid_stderr_handler, Other})
    end.

default_stderr_handler() ->
    {fun(Bin, _) -> guild_log:internal(Bin) end, not_used}.

%% ===================================================================
%% Task
%% ===================================================================

handle_task(#state{exec_pid=undefined}=State) ->
    Next = start_exec(State),
    handle_task(Next);
handle_task(State) ->
    Next = maybe_send_request(State),
    {repeat, Next}.

start_exec(#state{cmd={Args, UserOpts}}=State) ->
    Opts = exec_opts(UserOpts, State),
    {ok, Pid, OSPid} = guild_exec:run_link(Args, Opts),
    State#state{exec_pid=Pid, exec_ospid=OSPid}.

exec_opts(UserOpts, State) ->
    {BaseEnv, RestUserOpts} = split_env(UserOpts),
    Env = apply_task_env(BaseEnv, State),
    guild_exec:apply_user_opts(
      RestUserOpts,
      [stdout, stderr, stdin, {env, Env}]).

split_env(Opts) ->
    TakeEnv = fun({env, _}) -> true; (_) -> false end,
    case lists:splitwith(TakeEnv, Opts) of
        {[], Rest} -> {[], Rest};
        {[{env, Env}|_], Rest} -> {Env, Rest}
    end.

apply_task_env(Env0, #state{rundir=RunDir, op_ospid=Pid}) ->
    [{"RUNDIR", RunDir},
     {"OP_PID", integer_to_list(Pid)}
     |Env0].

maybe_send_request(#state{waiting=false, exec_pid=Pid}=S) ->
    guild_exec:send(Pid, <<"\n">>),
    S#state{waiting=true};
maybe_send_request(#state{waiting=true}=State) ->
    State.

%% ===================================================================
%% Message dispatch
%% ===================================================================

handle_msg({stdout, OSPid, Bin}, noreply, #state{exec_ospid=OSPid}=State) ->
    handle_stdout(Bin, State);
handle_msg({stderr, OSPid, Bin}, noreply, #state{exec_ospid=OSPid}=State) ->
    handle_stderr(Bin, State);
handle_msg({'DOWN', _, process, Op, _}, noreply, #state{op=Op}=State) ->
    handle_op_exit(State);
handle_msg({'EXIT', Pid, Reason}, noreply, #state{exec_pid=Pid}=State) ->
    handle_exec_exit(Reason, State).

%% ===================================================================
%% Stdout
%% ===================================================================

handle_stdout(Bin, State) ->
    handle_decoded(decode_input(Bin, State), State).

decode_input(Bin, #state{buf=Buf}) ->
    guild_collector_protocol:input(Buf, Bin).

handle_decoded({Decoded, Buf}, State) ->
    handle_decoded_(Decoded, State#state{buf=Buf}).

handle_decoded_([{_Time, eof}|Rest], State) ->
    handle_eof(Rest, State);
handle_decoded_([Decoded|Rest], State) ->
    log_decoded(Decoded, State),
    handle_decoded_(Rest, State);
handle_decoded_([], State) ->
    {noreply, State}.

handle_eof(_RestDecoded, #state{stopping=true, exec_pid=Pid}=State) ->
    guild_exec:stop_and_wait(Pid, ?stop_exec_timeout),
    {stop, normal, State};
handle_eof(RestDecoded, State) ->
    handle_decoded_(RestDecoded, State#state{waiting=false}).

%% ===================================================================
%% Stderr
%% ===================================================================

handle_stderr(Bin, #state{stderr_handler={Handle, HState}}=State) ->
    NextHState = Handle(Bin, HState),
    {noreply, State#state{stderr_handler={Handle, NextHState}}}.

%% ===================================================================
%% Log decoded
%% ===================================================================

log_decoded({Time, {kv, KVs}}, State) ->
    log_keyvals(Time, KVs, State);
log_decoded({_Time, {ktsv, KTSVs}}, State) ->
    log_keytsvs(KTSVs, State);
log_decoded({_, {other, Term}}, _State) ->
    guild_log:internal("Invalid collector response: ~p~n", [Term]);
log_decoded({_, {invalid, Bin}}, _State) ->
    guild_log:internal("Invalid collector output: ~p~n", [Bin]).

log_keyvals(Time, KVs, #state{rundir=RunDir}) ->
    KTSVs = [{Key, [[Time, 0, Val]]} || {Key, Val} <- KVs],
    handle_db_result(guild_run_db:log_series_values(RunDir, KTSVs)).

log_keytsvs(KTSVs, #state{rundir=RunDir}) ->
    handle_db_result(guild_run_db:log_series_values(RunDir, KTSVs)).

handle_db_result(ok) -> ok;
handle_db_result({error, read_only}) ->
    guild_log:internal(
      "Error logging series values: read_only (was RUNDIR deleted?)~n", []);
handle_db_result({error, Err}) ->
    guild_log:internal(
      "Error logging series values: ~p~n", [Err]).

%% ===================================================================
%% Operation exited - run a final time
%% ===================================================================

handle_op_exit(State) ->
    e2_task:run_once(self()),
    timer:kill_after(?stop_task_timeout),
    {noreply, State#state{stopping=true}}.

%% ===================================================================
%% Exec exited
%% ===================================================================

%% Collectors may exit deliberately for various reasons - e.g. when
%% required software is not available or the environment doesn't
%% otherwise support collection. This is a controlled exit and is
%% indicated by an exit code of 127 (32512 encoded). Collectors should
%% print an appropriate message to stderr as an indicator to the user,
%% however, the exit should not be treated as an error.
%%
-define(controlled_exit_status, 32512).

handle_exec_exit(normal, State) ->
    {stop, normal, State};
handle_exec_exit({exit_status, ?controlled_exit_status}, State) ->
    {stop, normal, State};
handle_exec_exit({exit_status, Status}, State) ->
    {stop, {exec_exit, exec:status(Status)}, State}.
