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

-module(guild_test_buffer).

-export([start_link/0, add/2, get_all/1, stop/1]).

-export([handle_msg/3]).

start_link() -> e2_service:start_link(?MODULE, []).
add(Buf, X)  -> e2_service:call(Buf, {add, X}).
get_all(Buf) -> e2_service:call(Buf, get_all).
stop(Buf)    -> e2_service:call(Buf, stop).

handle_msg({add, X}, _From, Buf) -> {reply, ok, [X|Buf]};
handle_msg(get_all,  _From, Buf) -> {reply, lists:reverse(Buf), Buf};
handle_msg(stop,     _From, Buf) -> {stop, normal, ok, Buf}.
