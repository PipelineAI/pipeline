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

-module(guild_status_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild status",
      "[OPTION]...",
      "Print project status.",
      guild_cmd_support:project_options(),
      [{pos_args, 0}]).

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, []) ->
    Dir = guild_cmd_support:project_dir_from_opts(Opts),
    handle_project(guild_project:from_dir(Dir), Dir).

handle_project({ok, _Project}, Dir) ->
    print_project_status(Dir),
    ok;
handle_project({error, Err}, Dir) ->
    print_project_error(Err, Dir),
    error.

print_project_status(Dir) ->
    guild_cli:out(
      "~s is a Guild project.~n",
      [guild_cmd_support:project_dir_desc(Dir)]).

print_project_error(missing_project_file, Dir) ->
    guild_cli:out(
      "~s is not a Guild project.~n"
      "Try 'guild init~s' to create one or "
      "'guild --help' for more information.~n",
      [guild_cmd_support:project_dir_desc(Dir),
       guild_cmd_support:project_dir_opt(Dir)]);
print_project_error(Other, Dir) ->
    guild_cli:out(
      "~s~n",
      [guild_cmd_support:project_error_msg(Other, Dir)]).
