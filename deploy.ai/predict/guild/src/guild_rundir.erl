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

-module(guild_rundir).

-export([path_for_project_section/3, guild_dir/1, guild_file/2, meta_dir/1,
         init/1, write_attrs/2]).

%% ===================================================================
%% Path for project section
%% ===================================================================

path_for_project_section(Section, Project, Time) ->
    RunRoot = guild_project_util:runroot(Section, Project),
    Name = rundir_name(Time, Section),
    filename:join(RunRoot, Name).

rundir_name(Time, Section) ->
    guild_util:format_dir_timestamp(Time) ++ rundir_suffix(Section).

rundir_suffix(Section) ->
    case guild_model:name_for_project_section(Section) of
        {ok, Name} -> "-" ++ safe_path(Name);
        error -> ""
    end.

safe_path(Suffix) ->
    re:replace(Suffix, "/", "_", [global, {return, list}]).

%% ===================================================================
%% Subdirs and files
%% ===================================================================

guild_dir(RunDir) ->
    filename:join(RunDir, "guild.d").

guild_file(RunDir, Name) ->
    filename:join(guild_dir(RunDir), Name).

meta_dir(RunDir) ->
    filename:join(guild_dir(RunDir), "meta").

%% ===================================================================
%% Init
%% ===================================================================

init(RunDir) ->
    init_rundir_skel(RunDir).

init_rundir_skel(RunDir) ->
    ok = filelib:ensure_dir(meta_dir(RunDir) ++ "/").

write_attrs(RunDir, Attrs) ->
    lists:foreach(
      fun({K, V}) -> write_attr(meta_dir(RunDir), K, V) end,
      Attrs).

write_attr(Dir, Key, Val) ->
    Path = filename:join(Dir, Key),
    case file:write_file(Path, val_to_string(Val)) of
        ok -> ok;
        {error, enoent} -> error(enoent)
    end.

val_to_string(undefined)                       -> "";
val_to_string(A) when is_atom(A)               -> atom_to_list(A);
val_to_string(S) when is_list(S); is_binary(S) -> S;
val_to_string(I) when is_integer(I)            -> integer_to_list(I);
val_to_string(F) when is_float(F)              -> float_to_list(F).
