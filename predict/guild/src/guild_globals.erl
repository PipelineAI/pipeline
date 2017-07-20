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
%% guild_globals - hack to workaround erlydtl's lack of context to tags/filters
%%
%% For actual global support, we should use something like
%% mochi_global. This service state is ephemeral making it useless in
%% a real system.

-module(guild_globals).

-behavior(e2_service).

-export([start_link/0, put/2, get/1]).

-export([handle_msg/3]).

start_link() ->
    e2_service:start_link(?MODULE, #{}, [registered]).

put(Name, Val) ->
    e2_service:call(?MODULE, {put, Name, Val}).

get(Name) ->
    e2_service:call(?MODULE, {get, Name}).

handle_msg({get, Name}, _, Vals) ->
    {reply, maps:find(Name, Vals), Vals};
handle_msg({put, Name, Val}, _, Vals) ->
    {reply, ok, maps:put(Name, Val, Vals)}.
