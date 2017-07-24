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

-module(guild_model).

-export([name_for_project_section/1, name_for_project_section/2,
         find_model_for_name/2, project_section_id/1]).

name_for_project_section({["model", Name|_], _}) ->
    {ok, Name};
name_for_project_section(Section) ->
    case guild_project:section_attr(Section, "train") of
        {ok, TrainAttr} -> {ok, name_for_attr(TrainAttr)};
        error -> error
    end.

name_for_project_section(Section, Default) ->
    case name_for_project_section(Section) of
        {ok, Name} -> Name;
        error -> Default
    end.

name_for_attr(Attr) ->
    hd(re:split(Attr, "\\s", [{return, list}])).

find_model_for_name(Project, Name) ->
    guild_util:find_apply(
      [fun model_for_name/2,
       fun model_for_train_cmd/2],
      [Project, Name]).

model_for_name(Project, Name) ->
    case guild_project:sections(Project, ["model", Name]) of
        [Model|_] -> {ok, Model};
        [] -> error
    end.

model_for_train_cmd([{["model"|_], _}=Model|Rest], TrainCmd) ->
    case guild_project:section_attr(Model, "train") of
        {ok, TrainCmd} -> {ok, Model};
        _ -> model_for_train_cmd(Rest, TrainCmd)
    end;
model_for_train_cmd([_|Rest], TrainCmd) ->
    model_for_train_cmd(Rest, TrainCmd);
model_for_train_cmd([], _TrainCmd) ->
    error.

project_section_id({["model", Name|_], _}) -> Name;
project_section_id(_) -> undefined.
