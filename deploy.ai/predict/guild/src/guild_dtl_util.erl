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

-module(guild_dtl_util).

-export([compile_template/3]).

compile_template(File, Module, UserOpts) ->
    AllOpts =
        [return,
         {out_dir, false},
         {compiler_options, [nowarn_unused_vars]}
         |UserOpts],
    handle_compile(erlydtl:compile_file(File, Module, AllOpts), File).

handle_compile({ok, _, WarningsList}, File) ->
    lists:foreach(
      fun({_, Warnings}) -> print_errors(File, Warnings) end,
      WarningsList);
handle_compile({error, Errors, []}, File) ->
    print_errors(File, Errors),
    error({template_compile, File}).

print_errors(File, Errors) ->
    lists:foreach(fun(E) -> print_error(File, E) end, Errors).

print_error(_, {_, sys_core_fold, useless_building}) ->
    ok;
print_error(File, {_, [{Where, erlydtl_parser, Err}]}) ->
    FileRef = format_file_ref(File, Where),
    Msg = erlydtl_parser:format_error(Err),
    guild_log:warn("~s: ~s~n", [FileRef, Msg]);
print_error(File, {_, [{Where, erlydtl_scanner, Err}]}) ->
    FileRef = format_file_ref(File, Where),
    Msg = erlydtl_scanner:format_error(Err),
    guild_log:warn("~s: ~s~n", [FileRef, Msg]);
print_error(File, {_, [{Where, erlydtl_compiler, Err}]}) ->
    FileRef = format_file_ref(File, Where),
    Msg = format_compiler_error(Err),
    guild_log:warn("~s: ~s~n", [FileRef, Msg]);
print_error(File, {Where, erlydtl_beam_compiler, Err}) ->
    FileRef = format_file_ref(File, Where),
    Msg = format_compiler_error(Err),
    guild_log:warn("~s: ~s~n", [FileRef, Msg]).

format_file_ref(File, none) ->
    File;
format_file_ref(File, {Line, Col}) when is_integer(Line), is_integer(Col) ->
    io_lib:format("~s:~b:~b", [File, Line, Col]);
format_file_ref(File, Line) when is_integer(Line) ->
    io_lib:format("~s:~b", [File, Line]).

format_compiler_error({unknown_filter, Filter, _Arity}) ->
    io_lib:format("unknown filter '~s'", [Filter]);
format_compiler_error({read_file, _, enoent}) ->
    "file does not exist";
format_compiler_error(Err) ->
    erlydtl_compiler:format_error(Err).
