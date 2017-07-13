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

-module(guild_proc).

-export([start_link/0, reg/1, reg/2, wait_for/1]).

-export([init/1, handle_msg/3]).

-record(state, {waiting_on, waiters}).

-define(noscope, '$noscope').

%% ===================================================================
%% Start / init
%% ===================================================================

start_link() ->
    e2_service:start_link(?MODULE, [], [registered]).

init([]) ->
    erlang:process_flag(trap_exit, true),
    {ok, init_state()}.

init_state() ->
    #state{waiting_on=[], waiters=[]}.

%% ===================================================================
%% Client API
%% ===================================================================

reg(Proc) ->
    reg(?noscope, Proc).

reg(Scope, Proc) ->
    e2_service:call(?MODULE, {register, Scope, Proc}).

wait_for({scope, Scope}) ->
    e2_service:call(?MODULE, {wait_for_scope, Scope});
wait_for({proc, Proc}) ->
    e2_service:call(?MODULE, {wait_for_proc, Proc}).

%% ===================================================================
%% Msg dispatch
%% ===================================================================

handle_msg({register, Scope, Proc}, _From, State) ->
    handle_register(Scope, Proc, State);
handle_msg({wait_for_scope, Scope}, From, State) ->
    handle_wait_for_scope(procs_for_scope(Scope, State), Scope, From, State);
handle_msg({wait_for_proc, Proc}, From, State) ->
    handle_wait_for_proc(waiting_on(Proc, State), Proc, From, State);
handle_msg({'DOWN', _, process, Proc, Reason}, noreply, State) ->
    handle_proc_down(strip_node(Proc), Reason, State).

strip_node({Proc, _Node}) -> Proc;
strip_node(Proc) -> Proc.

%% ===================================================================
%% Register
%% ===================================================================

handle_register(Scope, Proc, State) ->
    {reply, ok, add_waiting_on(Proc, Scope, State)}.

add_waiting_on(Proc, Scope, #state{waiting_on=WOs}=S) ->
    erlang:monitor(process, Proc),
    S#state{waiting_on=[{Scope, Proc}|WOs]}.

%% ===================================================================
%% Wait for scope
%% ===================================================================

procs_for_scope(Target, #state{waiting_on=Procs}) ->
    [Proc || {Scope, Proc} <- Procs, Scope == Target].

handle_wait_for_scope([], _Scope, _From, State) ->
    {reply, ok, State};
handle_wait_for_scope(_Procs, Scope, From, State) ->
    {noreply, add_waiter({scope, Scope, From}, State)}.

add_waiter(W, #state{waiters=Ws}=S) ->
    S#state{waiters=[W|Ws]}.

%% ===================================================================
%% Wait for proc
%% ===================================================================

waiting_on(Proc, #state{waiting_on=Procs}) ->
    lists:keymember(Proc, 2, Procs).

handle_wait_for_proc(false, _Proc, _From, State) ->
    {reply, {error, noproc}, State};
handle_wait_for_proc(true, Proc, From, State) ->
    {noreply, add_waiter({proc, Proc, From}, State)}.

%% ===================================================================
%% Notify waiters
%% ===================================================================

handle_proc_down(Proc, Reason, State) ->
    NextState =
        guild_util:fold_apply(
          [fun(S) -> remove_waiting_on(Proc, S) end,
           fun(S) -> remove_and_notify_proc_waiters(Proc, Reason, S) end,
           fun(S) -> remove_and_notify_scope_waiters(S) end],
          State),
    {noreply, NextState}.

remove_waiting_on(Target, #state{waiting_on=Procs}=S) ->
    Kept = lists:filter(fun({_, Proc}) -> Proc /= Target end, Procs),
    S#state{waiting_on=Kept}.

remove_and_notify_proc_waiters(Proc, Reason, State) ->
    {Removed, Kept} = partition_waiters_on_proc(Proc, State),
    notify_proc_waiters(Removed, Proc, Reason),
    State#state{waiters=Kept}.

partition_waiters_on_proc(Proc, #state{waiters=Ws}) ->
    lists:partition(fun(W) -> waiter_on_proc(W, Proc) end, Ws).

waiter_on_proc({proc, Proc, _From}, Proc) -> true;
waiter_on_proc(_Watier, _Proc) -> false.

notify_proc_waiters(Waiters, Proc, Reason) ->
    Reply = fun({proc, _, From}) -> e2_service:reply(From, {Proc, Reason}) end,
    lists:foreach(Reply, Waiters).

remove_and_notify_scope_waiters(State) ->
    {Removed, Kept} = partition_waiters_without_scope(State),
    notify_scope_waiters(Removed),
    State#state{waiters=Kept}.

partition_waiters_without_scope(#state{waiters=Ws, waiting_on=Procs}) ->
    lists:partition(fun(W) -> waiter_without_scope(W, Procs) end, Ws).

waiter_without_scope({scope, Scope, _From}, WaitingOn) ->
    not lists:keymember(Scope, 1, WaitingOn);
waiter_without_scope({proc, _Proc, _From}, _WaitingOn) ->
    false.

notify_scope_waiters(Waiters) ->
    Reply = fun({scope, _, From}) -> e2_service:reply(From, ok) end,
    lists:foreach(Reply, Waiters).
