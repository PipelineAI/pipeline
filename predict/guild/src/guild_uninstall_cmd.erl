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

-module(guild_uninstall_cmd).

-export([parser/0, main/2]).

parser() ->
    cli:parser(
      "guild uninstall",
      "[OPTION]... PACKAGE [PACKAGE]...",
      "Uninstalls one or more packages. PACKAGE may contain a version "
      "number to uninstall a specific version. If a version number is not "
      "specified, Guild will uninstall all installed versions of the "
      "package.",
      [],
      [{pos_args, {1, any}}]).

%% ===================================================================
%% Main
%% ===================================================================

main(_Opts, Args) ->
    guild_app:init_support([exec]),
    Bin = guild_app:priv_bin("guild-uninstall"),
    lists:foreach(fun(Pkg) -> uninstall_matches(Pkg, Bin) end, Args).

uninstall_matches(Pkg, Bin) ->
    Paths = match_packages(Pkg),
    error_if_no_paths(Paths, Pkg),
    lists:foreach(fun(Path) -> uninstall_pkg_path(Path, Bin) end, Paths).

match_packages(Pkg) ->
    BasePath = filename:join(guild_app:pkg_dir(), Pkg),
    Matches = filelib:wildcard([BasePath, "-*"]),
    lists:sort(Matches).

error_if_no_paths([], Pkg) ->
    guild_cli:cli_error(
      io_lib:format(
        "cannot find an installed package matching '~s'",
        [Pkg]));
error_if_no_paths(_, _) ->
    ok.

uninstall_pkg_path(Path, Bin) ->
    Args = [Bin, Path],
    Result = guild_cmd_support:exec_run(Args, []),
    exit_on_error(Result).

exit_on_error(ok) -> ok;
exit_on_error({error, N}) -> throw({error, N}).
