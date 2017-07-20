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

-module(guild_cmds_json_cmd).

-export([parser/0, main/2]).

-define(bin(L), iolist_to_binary(L)).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild cmds-json",
      "[OPTION]",
      "Print Guild commands as JSON.\n",
      [],
      [hidden, {pos_args, 0}]).

%% ===================================================================
%% Main
%% ===================================================================

main([], []) ->
    P = guild_cli:parser(),
    Cmds = format_commands(cli_parser:commands(P)),
    io:format(user, "~s~n", [guild_json:encode(Cmds)]).

format_commands(Cmds) ->
    maps:from_list(
      [{format_name(Name), format_cmd(P)}
       || {Name, _, P} <- Cmds, cli_parser:visible(P)]).

format_name(N) ->
    ?bin(re:replace(N, "-", "_", [global])).

format_cmd(P) ->
    #{
       prog => ?bin(cli_parser:prog(P)),
       usage => ?bin(cli_parser:usage(P)),
       desc => ?bin(cli_parser:desc(P)),
       options => format_options(cli_parser:options(P))
     }.

format_options(Opts) ->
    [format_option(Opt) || Opt <- Opts, cli_opt:visible(Opt)].

format_option(Opt) ->
    #{
       formatted_name => ?bin(cli_help:format_opt_name(Opt)),
       desc => ?bin(cli_opt:desc(Opt))
     }.
