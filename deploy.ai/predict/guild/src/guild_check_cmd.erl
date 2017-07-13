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

-module(guild_check_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild check",
      "[OPTION]...",
      "Check Guild setup.",
      [],
      [{pos_args, 0}]).

%% ===================================================================
%% Main
%% ===================================================================

main(_Opts, []) ->
    guild_app:init_support([exec]),
    print_guild_info(),
    print_erlang_info(),
    print_tensorflow_info(),
    print_psutil_info(),
    print_nvidia_tools_info().

print_guild_info() ->
    io:format(user, "guild_version:          ~s~n", [guild:version()]),
    io:format(user, "guild_home:             ~s~n", [guild_home()]).

guild_home() ->
    filename:dirname(guild_app:priv_dir()).

print_erlang_info() ->
    io:format(
      user, "otp_version:            ~s~n",
      [erlang:system_info(otp_release)]).

print_tensorflow_info() ->
    CheckBin = guild_app:priv_bin("tensorflow-check"),
    handle_check_exec(exec(CheckBin), tensorflow).

print_psutil_info() ->
    CheckBin = guild_app:priv_bin("psutil-check"),
    handle_check_exec(exec(CheckBin), psutil).

print_nvidia_tools_info() ->
    CheckBin = guild_app:priv_bin("nvidia-tools-check"),
    handle_check_exec(exec(CheckBin), nvidia_tools).

exec(Cmd) ->
    exec:run(Cmd, [stdout, stderr, sync]).

handle_check_exec({ok, Result}, _Src) ->
    print_result(Result);
handle_check_exec({error, ErrResult}, Src) ->
    Status = proplists:get_value(exit_status, ErrResult),
    Err = proplists:get_value(stderr, ErrResult, ""),
    Out = proplists:get_value(stdout, ErrResult),
    print_error(Src, Status, Err, Out).

print_result(Result) ->
    case proplists:get_value(stdout, Result) of
        undefined -> ok;
        Out -> io:format(user, Out, [])
    end.

print_error(Src, Status, Err, Out) ->
    maybe_print_out(Out),
    io:format(
      standard_error,
      "ERROR (~s): ~s (exit status ~p)~n",
      [Src, Err, Status]),
    erlang:halt(1).

maybe_print_out(undefined) -> ok;
maybe_print_out(Out) ->
    io:format(user, Out, []).
