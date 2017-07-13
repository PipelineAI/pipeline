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

-module(guild_list_packages_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild list-packages",
      "[OPTION]...",
      "List installed packages.",
      [],
      [{pos_args, 0}]).

%% ===================================================================
%% Main
%% ===================================================================

main([], []) ->
    print_packages(installed_packages()).

installed_packages() ->
    [package_info(Dir) || Dir <- package_dirs()].

package_dirs() ->
    Home = guild_app:pkg_dir(),
    Paths = [filename:join(Home, Name) || Name <- list_dir(Home)],
    Sorted = lists:sort(Paths),
    lists:filter(fun filelib:is_dir/1, Sorted).

list_dir(Dir) ->
    case file:list_dir(Dir) of
        {ok, Names} -> Names;
        {error, enoent} -> []
    end.

package_info(Path) ->
    Basename = filename:basename(Path),
    #{
       path => Path,
       basename => Basename
     }.

print_packages(Pkgs) ->
    lists:foreach(fun print_package/1, Pkgs).

print_package(#{basename:=Basename}) ->
    io:format("~s~n", [Basename]).
