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

-module(guild_json).

-export([init/0, encode/1, try_encode/1, try_decode/1]).

init() ->
    ok = application:ensure_started(jiffy).

encode(Term) ->
    jiffy:encode(Term).

try_encode(Term) ->
    try jiffy:encode(Term) of
        Bin -> {ok, Bin}
    catch
        throw:{error, Err} -> {error, Err}
    end.

try_decode(Bin) ->
    try jiffy:decode(Bin) of
        Term -> {ok, Term}
    catch
        throw:{error, Err} -> {error, Err}
    end.
