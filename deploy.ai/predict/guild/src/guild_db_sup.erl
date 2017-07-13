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

-module(guild_db_sup).

-behavior(e2_supervisor).

-export([start_link/0, start_child/1]).

start_link() ->
    e2_supervisor:start_link(?MODULE, [], [registered]).

start_child(Spec) ->
    e2_supervisor:start_child(?MODULE, Spec).
