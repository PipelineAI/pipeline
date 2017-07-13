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

-module(guild_log_http).

-export([create_app/1, handle/2]).

-record(state, {start, method, path}).

create_app(Upstream) -> {?MODULE, handle, [Upstream]}.

handle(Upstream, Env0) ->
    {State, Env} = init_state(Env0),
    handle_app_result(catch psycho:call_app(Upstream, Env), State).

init_state(Env) ->
    Method = psycho:env_val(request_method, Env),
    Path = psycho:env_val(request_path, Env),
    Start = erlang:timestamp(),
    {#state{start=Start, method=Method, path=Path}, Env}.

handle_app_result(Result, State) ->
    #state{start=Start, method=Method, path=Path} = State,
    Date = psycho_datetime:iso8601(Start),
    Time = request_time(Start),
    Status = try_response_status(Result),
    io:format(
      standard_error,
      "[~s] ~s ~s ~b ~b~n",
      [Date, Method, Path, Status, Time]),
    Result.

request_time(Start) ->
    micro(erlang:timestamp()) - micro(Start).

micro({M, S, U}) ->
    M * 1000000000000 + S * 1000000 + U.

try_response_status({{Status, _}, _, _}) -> Status;
try_response_status(_) -> 0.
