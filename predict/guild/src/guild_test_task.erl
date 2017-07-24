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

-module(guild_test_task).

-behavior(e2_task).

-export([start_link/2]).

-export([init/1, handle_task/1, handle_msg/3]).

-record(state, {dir, n}).

-define(max_writes, 10).

start_link(OpPid, Repeat) ->
    e2_task:start_link(?MODULE, [OpPid], [{repeat, Repeat}]).

init([Op]) ->
    erlang:monitor(process, Op),
    guild_proc:reg(optask, self()),
    Dir = guild_op:opdir(Op),
    {ok, #state{dir=Dir, n=0}}.

handle_task(#state{dir=Dir, n=N}=S) when N < ?max_writes ->
    write_test_file(Dir, N),
    {repeat, S#state{n=N+1}};
handle_task(_) ->
    {stop, normal}.

write_test_file(Dir, N) ->
    Path = test_file_path(Dir, N),
    ok = file:write_file(Path, <<>>).

test_file_path(Dir, N) ->
    Name = io_lib:format("test-~2..0b", [N]),
    filename:join(Dir, Name).

handle_msg({'DOWN', _Ref, process, _OpPid, _Reason}, _From, _State) ->
    {stop, normal}.
