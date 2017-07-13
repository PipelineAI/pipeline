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

-module(inifile).

-export([load/1, load/2, parse/1]).

-record(ps, {sec, secs, meta, lnum}).

load(File) ->
    load_files_acc([File], []).

load(File, Includes) ->
    load_files_acc(Includes ++ [File], []).

load_files_acc([F|Rest], Acc) ->
    handle_read_file(file:read_file(F), Rest, Acc);
load_files_acc([], Acc) ->
    {ok, Acc}.

handle_read_file({ok, Bin}, Rest, Acc) ->
    handle_parsed_file(parse(Bin, Acc), Rest);
handle_read_file({error, Err}, _Rest, _Acc) ->
    {error, Err}.

handle_parsed_file({ok, Sections}, Rest) ->
    load_files_acc(Rest, Sections);
handle_parsed_file({error, Err}, _Rest) ->
    {error, Err}.

parse(Bin) ->
    parse(Bin, []).

parse(Bin, Sections0) ->
    parse_lines(split_lines(Bin), init_parse_state(Sections0)).

init_parse_state(Secs) ->
    #ps{sec=undefined, secs=Secs, meta=[], lnum=1}.

split_lines(Bin) ->
    re:split(Bin, "\r\n|\n|\r|\032", [{return, list}]).

parse_lines([Line|Rest], PS) ->
    parse_line(strip_leading_and_trailing_spaces(Line), Rest, PS);
parse_lines([], PS) ->
    {ok, finalize_parse(PS)}.

strip_leading_and_trailing_spaces(Str) ->
    string:strip(Str, both).

parse_line("", Rest, PS) ->
    parse_lines(Rest, incr_lnum(PS));
parse_line(";"++_, Rest, PS) ->
    parse_lines(Rest, incr_lnum(PS));
parse_line("#+"++Meta, Rest, PS) ->
    parse_lines(Rest, incr_lnum(add_meta(Meta, PS)));
parse_line("#"++_, Rest, PS) ->
    parse_lines(Rest, incr_lnum(PS));
parse_line("["++_=Line, Rest, PS) ->
    handle_section_parse(parse_section_line(Line), Rest, PS);
parse_line("@"++Directive, Rest, PS) ->
    handle_directive(parse_directive(Directive), Rest, PS);
parse_line(Line0, Rest0, PS0) ->
    case read_line_continuations(Line0, Rest0, PS0) of
        {ok, {Line, Rest, PS}} ->
            handle_attr_parse(parse_attr_line(Line), Rest, PS);
        {error, Err} ->
            {error, Err}
    end.

add_meta("", PS) ->
    PS;
add_meta(Raw, #ps{meta=Meta}=PS) ->
    PS#ps{meta=[parse_meta(Raw)|Meta]}.

parse_meta(S) ->
    [meta_token(Part) || Part <- split_meta(S)].

split_meta(S) ->
    Pattern = "\"(.*?)\"|([^\s]+)",
    Opts = [global, {capture, all_but_first, list}],
    {match, Parts} = re:run(S, Pattern, Opts),
    Parts.

meta_token(["", Word]) -> Word;
meta_token([Quoted]) -> Quoted.

-define(
   section_pattern,
   "^\\[\\s*([^ ]+)"
   "(?:\\s+\"(.+?)\")?"
   "(?:\\s+\"(.+?)\")?"
   "\\s*]$").

parse_section_line(Line) ->
    case re:run(Line, ?section_pattern, [{capture, all_but_first, list}]) of
        {match, Keys} -> {ok, {Keys, []}};
        nomatch -> {error, syntax}
    end.

handle_section_parse({ok, Section}, Rest, PS) ->
    parse_lines(Rest, incr_lnum(add_section(Section, PS)));
handle_section_parse({error, syntax}, _Rest, #ps{lnum=LNum}) ->
    {error, {syntax, LNum}}.

add_section(New, #ps{sec=undefined}=PS) ->
    PS#ps{sec=New};
add_section(New, #ps{sec=Cur, secs=Secs}=PS) ->
    PS#ps{sec=New, secs=[finalize_section(Cur)|Secs]}.

finalize_section({Name, Attrs}) ->
    {Name, Attrs}.

parse_directive("attrs "++Attrs) ->
    case parse_keyspec(Attrs) of
        {ok, Keys} -> {ok, {attrs, Keys}};
        {error, Err} -> {error, Err}
    end;
parse_directive(_) ->
    {error, directive}.

parse_keyspec(Spec) ->
    Line = ["[", Spec, "]"],
    case re:run(Line, ?section_pattern, [{capture, all_but_first, list}]) of
        {match, Keys} -> {ok, Keys};
        nomatch -> {error, syntax}
    end.

handle_directive({ok, {attrs, Keys}}, Rest, PS) ->
    handle_attrs_directive(Keys, Rest, PS);
handle_directive({error, syntax}, _Rest, #ps{lnum=LNum}) ->
    {error, {syntax, LNum}};
handle_directive({error, directive}, _Rest, #ps{lnum=LNum}) ->
    {error, {directive, LNum}}.

handle_attrs_directive(_Keys, _Rest, #ps{sec=undefined, lnum=LNum}) ->
    {error, {no_section_for_attrs_directive, LNum}};
handle_attrs_directive(Keys, Rest, #ps{sec=Sec, secs=Secs}=PS) ->
    NextSec = apply_attrs(Keys, Sec, Secs),
    parse_lines(Rest, incr_lnum(PS#ps{sec=NextSec})).

apply_attrs(Keys, Sec, Secs) ->
    case find_section(Keys, Secs) of
        {ok, {_, Attrs}} -> apply_attrs(Attrs, Sec);
        error -> Sec
    end.

find_section(Keys, [{Keys, _}=S|_]) -> {ok, S};
find_section(Keys, [_|Rest]) -> find_section(Keys, Rest);
find_section(_, []) -> error.

apply_attrs(NewAttrs, {Keys, CurAttrs}) ->
    {Keys, NewAttrs ++ CurAttrs}.

read_line_continuations(Line, Rest, PS) ->
    {ok, Pattern} = re:compile("(.*?)\\\\$"),
    read_line_continuations_acc(Line, Rest, PS, Pattern, []).

read_line_continuations_acc(Line, Rest, PS, Pattern, Acc) ->
    case re:run(Line, Pattern, [{capture, all_but_first, list}]) of
        {match, [Part]} ->
            handle_line_continuation(Part, Rest, PS, Pattern, Acc);
        nomatch ->
            finalize_line_continuation(Line, Acc, Rest, PS)
    end.

handle_line_continuation(Part, [NextLine|NextRest], PS, Pattern, Acc) ->
    read_line_continuations_acc(
      NextLine, NextRest, incr_lnum(PS), Pattern, [Part|Acc]);
handle_line_continuation(_Part, [], #ps{lnum=Num}, _Pattern, _Acc) ->
    {error, {eof, Num}}.

finalize_line_continuation(Line, Acc, Rest, PS) ->
    {ok, {lists:reverse([Line|Acc]), Rest, PS}}.

parse_attr_line(Line) ->
    Pattern = "([^\\s]+)\\s*[:=]\\s*(.*)",
    case re:run(Line, Pattern, [{capture, all_but_first, list}]) of
        {match, [Name, Val]} -> {ok, {Name, Val}};
        nomatch -> error
    end.

handle_attr_parse({ok, _}, _Rest, #ps{sec=undefined, lnum=LNum}) ->
    {error, {no_section_for_attr, LNum}};
handle_attr_parse({ok, Attr}, Rest, PS) ->
    parse_lines(Rest, incr_lnum(add_attr(Attr, PS)));
handle_attr_parse(error, _Rest, #ps{lnum=LNum}) ->
    {error, {syntax, LNum}}.

add_attr(Attr, #ps{sec={Name, Attrs}}=PS) ->
    PS#ps{sec={Name, [Attr|Attrs]}}.

finalize_parse(PS) ->
    apply_meta(finalize_sections(PS), PS).

finalize_sections(#ps{sec=undefined, secs=Acc}) ->
    lists:reverse(Acc);
finalize_sections(#ps{sec=Sec, secs=Acc}) ->
    lists:reverse([finalize_section(Sec)|Acc]).

apply_meta(Sections, #ps{meta=[]}) ->
    Sections;
apply_meta(Sections, #ps{meta=Meta}) ->
    [{'$meta', lists:reverse(Meta)}|Sections].

incr_lnum(#ps{lnum=N}=PS) -> PS#ps{lnum=N + 1}.
