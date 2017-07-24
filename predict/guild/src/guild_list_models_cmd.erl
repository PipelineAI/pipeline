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

-module(guild_list_models_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild list-models",
      "[OPTION]...",
      "List project models.",
      guild_cmd_support:project_options(),
      [{pos_args, 0}]).

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, []) ->
    Project = guild_cmd_support:project_from_opts(Opts),
    print_models(guild_project:sections(Project, ["model"])).

print_models(Models) ->
    lists:foreach(fun print_model/1, Models).

print_model(M) ->
    Pos = print_model_name(M),
    maybe_print_model_desc(M, Pos),
    guild_cli:out("\n").

print_model_name(M) ->
    Name = guild_model:name_for_project_section(M, "?"),
    guild_cli:out(io_lib:format("~s", [Name])),
    length(Name).

-define(desc_inset, 20).

maybe_print_model_desc(M, Pos) ->
    case guild_project:section_attr(M, "description") of
        {ok, Desc} ->
            guild_cmd_support:cli_out_spaces(?desc_inset - Pos + 1),
            guild_cli:out(Desc);
        error ->
            ok
    end.
