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

-module(guild_tf_tensorboard).

-behavior(e2_task).

-export([start_link/2, stop/1]).

-export([handle_task/1, handle_msg/3]).

-record(state, {logdir, port, ospid}).

start_link(LogDir, Port) ->
    State = #state{logdir=LogDir, port=Port},
    e2_task:start_link(?MODULE, State, []).

stop(TB) ->
    e2_task:call(TB, stop).

handle_task(S) ->
    Next = start_tensorboard(S),
    {wait_for_msg, Next}.

handle_msg(stop, _From, #state{ospid=OSPid}=S) ->
    exec:stop(OSPid),
    {stop, normal, ok, S}.

start_tensorboard(#state{logdir=LogDir, port=Port}=S) ->
    Args =
        [tensorboard_bin(),
         "--logdir", LogDir,
         "--port",
         integer_to_list(Port)],
    Opts = [],
    {ok, _Pid, OSPid} = exec:run_link(Args, Opts),
    S#state{ospid=OSPid}.

tensorboard_bin() ->
    guild_app:priv_bin("tensorboard").
