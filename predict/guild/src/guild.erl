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

-module(guild).

-export([main/0, start/0, stop/0, restart/0, version/0]).

main() ->
    Args = init:get_plain_arguments(),
    start(),
    handle_args(Args).

start() ->
    {ok, _} = application:ensure_all_started(?MODULE).

stop() ->
    application:stop(?MODULE).

restart() ->
    stop(),
    start().

handle_args(Args) ->
    Parser = guild_cli:parser(),
    Exit = cli:main(Args, Parser, {guild_cli, main, []}),
    stop(),
    erlang:halt(Exit).

version() ->
    {ok, Vsn} = application:get_key(?MODULE, vsn),
    maybe_apply_git_commit(Vsn).

maybe_apply_git_commit(Vsn) ->
    case os:getenv("GUILD_GIT_COMMIT") of
        false -> Vsn;
        Commit -> [Vsn, " (", Commit, ")"]
    end.
