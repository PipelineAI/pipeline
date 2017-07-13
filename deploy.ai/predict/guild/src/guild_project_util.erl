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

-module(guild_project_util).

-export([runroot/1, runroot/2, all_runroots/1, flags/2, run_model/2,
         strip_sections/2, strip_meta/1]).

-define(default_runroot, "runs").

%% ===================================================================
%% Runroot
%% ===================================================================

runroot(Project) ->
    runroot(undefined, Project).

runroot(Section, Project) ->
    {ok, Root} =
        guild_util:find_apply(
          [fun() -> section_runroot(Section) end,
           fun() -> project_runroot(Project) end,
           fun() -> user_configured_runroot() end,
           fun() -> {ok, ?default_runroot} end],
          []),
    filename:absname(Root, guild_project:dir(Project)).

section_runroot(undefined) ->
    error;
section_runroot(Section) ->
    guild_project:section_attr(Section, "runroot").

project_runroot(Project) ->
    guild_project:attr(Project, ["project"], "runroot").

user_configured_runroot() ->
    guild_user:config(["defaults"], "runroot").

%% ===================================================================
%% All runroots
%% ===================================================================

all_runroots(Project) ->
    guild_util:fold_apply(
      [fun(S) -> apply_project_sections_runroot(Project, S) end,
       fun(S) -> apply_project_runroot(Project, S) end,
       fun(S) -> sets:to_list(S) end],
      sets:new()).

apply_project_sections_runroot(Project, Set) ->
    Sections = guild_project:sections(Project, []),
    ProjectDir = guild_project:dir(Project),
    Apply =
        fun(Section, Acc) ->
            apply_project_section_runroot(Section, ProjectDir, Acc)
        end,
    lists:foldl(Apply, Set, Sections).

apply_project_section_runroot(Section, ProjectDir, Set) ->
    case section_runroot(Section) of
        {ok, Root} ->
            AbsRoot = filename:absname(Root, ProjectDir),
            sets:add_element(AbsRoot, Set);
        error ->
            Set
    end.

apply_project_runroot(Project, Set) ->
    sets:add_element(runroot(Project), Set).

%% ===================================================================
%% Flags
%% ===================================================================

flags(Section, Project) ->
    Path = flags_path_for_section(Section),
    guild_project:section_attr_union(Project, Path).

flags_path_for_section({[_, Name|_], _}) ->
    [["flags", cmdline], ["flags", profile], ["flags", Name], ["flags"]];
flags_path_for_section({[_], _}) ->
    [["flags", cmdline], ["flags", profile], ["flags"]].

%% ===================================================================
%% Run model
%% ===================================================================

run_model(Run, Project) ->
    case guild_run:attr(Run, "model") of
        error      -> default_model(Project);
        {ok, <<>>} -> default_model(Project);
        {ok, Name} -> named_model(Project, binary_to_list(Name))
    end.

default_model(Project) ->
    guild_project:section(Project, ["model"]).

named_model(Project, Name) ->
    guild_project:section(Project, ["model", Name]).

%% ===================================================================
%% Strip sections
%% ===================================================================

strip_sections(Bin, Type) ->
    StripHeaderPattern = strip_header_pattern(Type),
    HeaderPattern = header_start_pattern(),
    strip_sections(split_lines(Bin), StripHeaderPattern, HeaderPattern).

strip_header_pattern(Type) ->
    {ok, Re} = re:compile(["^\\[\\s*", Type, "(\\s+.*)?\\]"]),
    Re.

header_start_pattern() ->
    {ok, Re} = re:compile("^\\["),
    Re.

split_lines(Bin) ->
    LinePattern = "(.+(\r\n|\n|\r|\032)*)",
    case re:run(Bin, LinePattern, [global, {capture, [1], list}]) of
        {match, Parts} -> Parts;
        nomatch -> ""
    end.

strip_sections(Lines, StripHeader, AnyHeader) ->
    strip_acc(Lines, StripHeader, AnyHeader, false, []).

strip_acc([Line|Rest], StripH, AnyH, Stripping, Acc) ->
    case {match(Line, StripH), match(Line, AnyH), Stripping} of
        {true, _, _} -> strip_acc(Rest, StripH, AnyH, true, Acc);
        {_, true, _} -> strip_acc(Rest, StripH, AnyH, false, [Line|Acc]);
        {_, _, true} -> strip_acc(Rest, StripH, AnyH, true, Acc);
        {_, _, _}    -> strip_acc(Rest, StripH, AnyH, false, [Line|Acc])
    end;
strip_acc([], _StripH, _AnyH, _Stripping, Acc) ->
    lists:reverse(Acc).

match(Subject, Pattern) ->
    re:run(Subject, Pattern, [{capture, none}]) == match.

%% ===================================================================
%% Strip meta
%% ===================================================================

strip_meta(Bin) ->
    [Line || Line <- split_lines(Bin), not is_meta(Line)].

is_meta(Line) ->
    re:run(Line, "^\\s*#\\+", [{capture, none}]) == match.
