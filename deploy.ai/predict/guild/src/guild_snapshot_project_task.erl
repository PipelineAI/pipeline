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

-module(guild_snapshot_project_task).

-behavior(e2_task).

-export([start_link/3]).

-export([init/1, handle_task/1]).

-record(state, {rundir, section, project}).

%% ===================================================================
%% Start / init
%% ===================================================================

start_link(Op, Section, Project) ->
    e2_task:start_link(?MODULE, [Op, Section, Project], []).

init([Op, Section, Project]) ->
    guild_proc:reg(optask, self()),
    RunDir = guild_op:opdir(Op),
    {ok, #state{rundir=RunDir, section=Section, project=Project}}.

%% ===================================================================
%% Task impl
%% ===================================================================

handle_task(#state{rundir=RunDir, section=Section, project=Project}) ->
    Bin = guild_app:priv_bin("snapshot-project"),
    ProjectDir = guild_project:dir(Project),
    GuildDir = guild_rundir:guild_dir(RunDir),
    Sources = section_sources(Section, Project),
    guild_exec:run_quiet([Bin, ProjectDir, GuildDir, Sources]),
    {stop, normal}.

section_sources({SectionPath, _}, Project) ->
    Attrs =
        guild_project:section_attr_union(
          Project, [SectionPath, ["project"]]),
    proplists:get_value("sources", Attrs, "").
