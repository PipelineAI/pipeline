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

-module(guild_trace).

-export([init_from_opts/1, init_from_env/1]).

init_from_opts(Opts) ->
    lists:foreach(fun apply_trace/1, parse_trace_specs(trace_opt(Opts))).

trace_opt(Opts) ->
    proplists:get_value(trace, Opts).

init_from_env(false) ->
    ok;
init_from_env(Env) ->
    lists:foreach(fun apply_trace/1, parse_trace_specs(Env)).

parse_trace_specs(undefined) ->
    [];
parse_trace_specs(Str) ->
    [trace_spec(Token) || Token <- string:tokens(Str, ",")].

trace_spec(Str) ->
    [list_to_atom(Token) || Token <- string:tokens(Str, ":")].

apply_trace([M])    -> e2_debug:trace_module(M);
apply_trace([M, F]) -> e2_debug:trace_function(M, F).
