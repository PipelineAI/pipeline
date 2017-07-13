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

-module(guild_train_op).

-behavior(guild_op).

-export([from_project_spec/3]).

-export([cmd_preview/1, init/1, cmd/1, opdir/1, meta/1, tasks/1]).

-record(op, {section, project, flags, cmd}).

-record(state, {op, started, rundir}).

-define(default_stats_task_repeat, 10000).

%% ===================================================================
%% Init (static)
%% ===================================================================

from_project_spec(Spec, Section, Project) ->
    Flags = guild_project_util:flags(Section, Project),
    CmdArgs = guild_op_support:python_cmd(Spec, Flags),
    CmdEnv = guild_op_support:static_env(),
    {?MODULE,
     #op{
        section=Section,
        project=Project,
        flags=Flags,
        cmd={CmdArgs, CmdEnv}}}.

%% ===================================================================
%% Init (process state)
%% ===================================================================

init(Op) ->
    {ok, #state{op=Op, started=guild_run:timestamp()}}.

%% ===================================================================
%% Cmd preview
%% ===================================================================

cmd_preview(#op{cmd=Cmd}) -> Cmd.

%% ===================================================================
%% Op dir
%% ===================================================================

opdir(#state{op=Op, started=Started}=State) ->
    RunDir = rundir(Op, Started),
    {ok, RunDir, State#state{rundir=RunDir}}.

rundir(#op{section=Section, project=Project}, Started) ->
    guild_rundir:path_for_project_section(Section, Project, Started).

%% ===================================================================
%% Cmd
%% ===================================================================

cmd(#state{op=#op{cmd={Args, BaseEnv}, project=Project}}=State) ->
    Env = run_env(State) ++ BaseEnv,
    ResolvedArgs = guild_util:resolve_args(Args, Env),
    Cwd = guild_project:dir(Project),
    {ok, ResolvedArgs, Env, Cwd, State}.

run_env(#state{rundir=RunDir}) -> [{"RUNDIR", RunDir}].

%% ===================================================================
%% Meta
%% ===================================================================

meta(State) ->
    {ok, run_attrs(State), State}.

run_attrs(#state{op=#op{section=Section, cmd={CmdArgs, Env}},
                 started=Started}) ->
    [section_name_attr(Section),
     {started, Started},
     {cmd, format_cmd_attr(CmdArgs)},
     {env, format_env_attr(Env)}].

section_name_attr({[Type], _}) ->
    {Type, ""};
section_name_attr({[Type, Name|_], _}) ->
    {Type, Name}.

format_cmd_attr(Cmd) ->
    guild_util:format_cmd_args(Cmd).

format_env_attr(Env) ->
    [[Name, "=", Val, "\n"] || {Name, Val} <- Env].

%% ===================================================================
%% Tasks
%% ===================================================================

tasks(#state{op=#op{flags=Flags}}=State) ->
    Tasks =
        train_tasks(State)
        ++ guild_op_support:default_collector_tasks(Flags),
    {ok, Tasks, State}.

train_tasks(#state{op=#op{section=Section, project=Project, flags=Flags}}) ->
        [{guild_log_flags_task, start_link, [Flags]},
         {guild_log_system_attrs_task, start_link, []},
         {guild_snapshot_project_task, start_link, [Section, Project]}].
