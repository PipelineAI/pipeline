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

-module(guild_log).

-export([internal/1, internal/2, warn/2]).

internal(Output) ->
    io:put_chars(standard_error, Output).

internal(Msg, Data) ->
    io:format(standard_error, Msg, Data).

warn(Msg, Data) ->
    io:format(standard_error, Msg, Data).
