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

-module(guild_install_cmd).

-export([parser/0, main/2]).

parser() ->
    cli:parser(
      "guild install",
      "[OPTION]... PACKAGE [PACKAGE]...",
      "Installs a Guild package. PACKAGE may be a package archive or a "
      "package name. You may specify multiple packages.",
      [],
      [{pos_args, {1, any}}]).

%% ===================================================================
%% Main
%% ===================================================================

main(_Opts, Args) ->
    guild_app:init_support([exec]),
    Bin = guild_app:priv_bin("guild-install"),
    lists:foreach(fun(Pkg) -> install_pkg(Pkg, Bin) end, Args).

install_pkg(Pkg, Bin) ->
    Dest = dest_for_pkg(Pkg),
    Args = [Bin, Pkg, Dest],
    Result = guild_cmd_support:exec_run(Args, []),
    exit_on_error(Result).

dest_for_pkg(Pkg) ->
    Pattern = "([^/]+)\\.pkg\\.tar\\.xz",
    case re:run(Pkg, Pattern, [{capture, [1], list}]) of
        {match, [Name]} ->
            user_package_path(Name);
        nomatch ->
            invalid_package(Pkg)
    end.

user_package_path(Name) ->
    filename:join(guild_app:pkg_dir(), Name).

invalid_package(Pkg) ->
    guild_cli:cli_error(
      io_lib:format(
        "~s does not appear to be a valid Guild package", [Pkg])).

exit_on_error(ok) -> ok;
exit_on_error({error, N}) -> throw({error, N}).
