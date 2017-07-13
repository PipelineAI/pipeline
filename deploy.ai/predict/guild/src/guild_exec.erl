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

-module(guild_exec).

-export([init/0, run_link/2, run_quiet/1, run/1, run/2, run_capture/1,
         run_capture/2, send/2, stop_and_wait/2, apply_user_opts/2]).

init() ->
    ok = application:ensure_started(erlexec).

run_link(Args, Opts) ->
    exec:run_link(Args, Opts).

run_quiet(Args) ->
    case exec:run(Args, [sync, stdout, stderr]) of
        {ok, _} -> ok;
        {error, Err} -> error({exec_run, Err, Args})
    end.

run(Args) ->
    run(Args, []).

run(Args, RunOpts) ->
    Opts = [sync, {stdout, fun console/3}, {stderr, fun console/3}|RunOpts],
    exec:run(Args, Opts).

console(stdout, _Pid, Bin) ->
    io:put_chars(standard_io, Bin);
console(stderr, _Pid, Bin) ->
    io:put_chars(standard_error, Bin).

run_capture(Args) ->
    run_capture(Args, []).

run_capture(Args, RunOpts) ->
    Opts = [sync, stdout, stderr|RunOpts],
    exec:run(Args, Opts).

send(Pid, Bin) ->
    exec:send(Pid, Bin).

stop_and_wait(Pid, Timeout) ->
    exec:stop_and_wait(Pid, Timeout).

apply_user_opts(Opts, Acc) ->
    lists:foldl(fun maybe_user_opt/2, Acc, Opts).

maybe_user_opt({cwd, Dir}, Acc) -> [{cd, Dir}|Acc];
maybe_user_opt({env, Env}, Acc) -> [{env, Env}|Acc];
maybe_user_opt(Other, _Acc)     -> error({cmd_opt, Other}).
