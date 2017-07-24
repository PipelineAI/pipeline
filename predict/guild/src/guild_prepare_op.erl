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

-module(guild_prepare_op).

-behavior(guild_op).

-export([from_project_spec/3]).

-export([cmd_preview/1, init/1, cmd/1, opdir/1, meta/1, tasks/1]).

-record(op, {section, project, cmd}).

-record(state, {op}).

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
        cmd={CmdArgs, CmdEnv}}}.

%% ===================================================================
%% Init (process state)
%% ===================================================================

init(Op) ->
    {ok, #state{op=Op}}.

%% ===================================================================
%% Cmd preview
%% ===================================================================

cmd_preview(#op{cmd=Cmd}) -> Cmd.

%% ===================================================================
%% Op dir
%% ===================================================================

opdir(State) -> {ignore, State}.

%% ===================================================================
%% Cmd
%% ===================================================================

cmd(#state{op=#op{cmd={Args, Env}, project=Project}}=State) ->
    ResolvedArgs = guild_util:resolve_args(Args, Env),
    Cwd = guild_project:dir(Project),
    {ok, ResolvedArgs, Env, Cwd, State}.

%% ===================================================================
%% Meta
%% ===================================================================

meta(State) -> {ignore, State}.

%% ===================================================================
%% Tasks
%% ===================================================================

tasks(State) -> {ignore, State}.
