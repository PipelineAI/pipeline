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

-module(guild_operation).

-behavior(e2_task).

-export([new/5, cmd_info/1, start_link/2, rundir/1, ospid/1, stop/2]).

-export([init/1, handle_task/1, handle_msg/3]).

-record(op, {rundir_spec, section, project, cmd_args, cmd_extra_env,
             app_support, tasks, stream_handler_inits}).

-record(state, {op, started, cmd, rundir, exec_pid, exec_ospid,
                stream_handlers, stdout_buf, stderr_buf}).

%% ===================================================================
%% New
%% ===================================================================

new(RunDirSpec, Section, Project, CmdArgs, Opts) ->
    #op{
       rundir_spec=RunDirSpec,
       section=Section,
       project=Project,
       cmd_args=CmdArgs,
       cmd_extra_env=opt_list(env, Opts),
       app_support=opt_list(app_support, Opts),
       tasks=op_tasks(RunDirSpec, Opts),
       stream_handler_inits=opt_list(stream_handlers, Opts)}.

opt_list(Name, Opts) ->
    proplists:get_value(Name, Opts, []).

op_tasks({new, _}, Opts) ->
    new_run_core_tasks() ++ opt_list(tasks, Opts);
op_tasks(_, Opts) ->
    opt_list(tasks, Opts).

new_run_core_tasks() ->
    [{guild_run_status_task, start_link, []},
     {guild_run_db_task, start_link, []}].

%% ===================================================================
%% Cmd info
%% ===================================================================

cmd_info(#op{cmd_args=Args, cmd_extra_env=ExtraEnv}) ->
    Env = ExtraEnv ++ system_env(),
    #{args => Args, env => Env}.

%% ===================================================================
%% Start / init
%% ===================================================================

start_link(Name, Op) ->
    e2_task:start_link(?MODULE, Op, [{registered, Name}]).

init(Op) ->
    guild_proc:reg(operation, self()),
    process_flag(trap_exit, true),
    {ok, init_state(Op)}.

init_state(Op) ->
    #state{
       op=Op,
       stdout_buf=guild_util:new_input_buffer(),
       stderr_buf=guild_util:new_input_buffer()}.

%% ===================================================================
%% API
%% ===================================================================

rundir(Op) ->
    gproc:get_value({p, l, rundir}, Op).

ospid(Op) ->
    gproc:get_value({p, l, ospid}, Op).

stop(Op, Timeout) ->
    e2_task:call(Op, {stop, Timeout}).

%% ===================================================================
%% Task impl
%% ===================================================================

handle_task(State) ->
    Next = guild_util:fold_apply(op_steps(State), State),
    {wait_for_msg, Next}.

op_steps(#state{op=#op{rundir_spec={new, _}}}) ->
    [fun init_app_support/1,
     fun started_timestamp/1,
     fun init_rundir/1,
     fun init_cmd/1,
     fun init_run_meta/1,
     fun snapshot_project/1,
     fun init_errors_log/1,
     fun init_stream_handlers/1,
     fun start_exec/1,
     fun start_tasks/1];
op_steps(#state{op=#op{rundir_spec={overlay, _}}}) ->
    [fun init_app_support/1,
     fun set_rundir/1,
     fun init_cmd/1,
     fun init_stream_handlers/1,
     fun start_exec/1,
     fun start_tasks/1];
op_steps(#state{op=#op{rundir_spec=none}}) ->
    [fun init_app_support/1,
     fun init_cmd/1,
     fun init_stream_handlers/1,
     fun start_exec/1,
     fun start_tasks/1].

init_app_support(#state{op=#op{app_support=AppSupport}}=State) ->
    guild_app:init_support([exec|AppSupport]),
    State.

started_timestamp(S) ->
    S#state{started=guild_run:timestamp()}.

%% ===================================================================
%% Cmd
%% ===================================================================

init_cmd(#state{op=#op{cmd_args=Args}}=State) ->
    Env = cmd_env(State),
    ResolvedArgs = guild_util:resolve_args(Args, Env),
    State#state{cmd={ResolvedArgs, Env}}.

cmd_env(State) ->
    cmd_core_env(State) ++ cmd_extra_env(State).

cmd_core_env(State) ->
    system_env() ++ run_env(State).

system_env() ->
    [{"PKGHOME", guild_app:pkg_dir()}].

run_env(#state{rundir=undefined}) -> [];
run_env(#state{rundir=RunDir}) -> [{"RUNDIR", RunDir}].

cmd_extra_env(#state{op=#op{cmd_extra_env=Extra}}) ->
    Extra.

%% ===================================================================
%% Rundir
%% ===================================================================

set_rundir(#state{op=#op{rundir_spec={overlay, RunDir}}}=S) ->
    S#state{rundir=RunDir}.

init_rundir(#state{op=#op{rundir_spec={new, Opts}}}=State) ->
    RunDir = rundir_path(Opts, State),
    guild_rundir:init(RunDir),
    gproc:add_local_property(rundir, RunDir),
    State#state{rundir=RunDir}.

rundir_path(Opts, State) ->
    RunRoot = runroot(State),
    Name = rundir_name(Opts, State),
    filename:join(RunRoot, Name).

runroot(#state{op=#op{section=Section, project=Project}}) ->
    guild_project_util:runroot(Section, Project).

rundir_name(Opts, #state{started=Started}) ->
    case proplists:get_value(suffix, Opts) of
        undefined -> format_started(Started);
        Suffix -> format_started(Started) ++ format_suffix(Suffix)
    end.

format_started(RunStarted) ->
    Now = guild_run:timestamp_to_now(RunStarted),
    {{Y, M, D}, {H, Mn, S}} = calendar:now_to_universal_time(Now),
    io_lib:format("~b~2..0b~2..0bT~2..0b~2..0b~2..0bZ", [Y, M, D, H, Mn, S]).

format_suffix(Suffix) ->
    re:replace(Suffix, "/", "_", [global, {return, list}]).

%% ===================================================================
%% Run meta
%% ===================================================================

init_run_meta(#state{rundir=RunDir}=State) ->
    guild_rundir:write_attrs(RunDir, run_attrs(State)),
    State.

run_attrs(#state{op=#op{section=Section}, started=Started, cmd={Cmd, Env}}) ->
    [section_name_attr(Section),
     {started, Started},
     {cmd, format_cmd_attr(Cmd)},
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
%% Snapshot project
%% ===================================================================

snapshot_project(#state{rundir=RunDir,
                        op=#op{section=Section, project=Project}}=State) ->
    Bin = guild_app:priv_bin("snapshot-project"),
    ProjectDir = guild_project:dir(Project),
    GuildDir = guild_rundir:guild_dir(RunDir),
    Sources = section_sources(Section, Project),
    guild_exec:run_quiet([Bin, ProjectDir, GuildDir, Sources]),
    State.

section_sources({SectionPath, _}, Project) ->
    Attrs =
        guild_project:section_attr_union(
          Project, [SectionPath, ["project"]]),
    proplists:get_value("sources", Attrs, "").

%% ===================================================================
%% Errors log
%% ===================================================================

init_errors_log(#state{rundir=RunDir}=State) ->
    Path = guild_rundir:guild_file(RunDir, "errors.log"),
    error_logger:logfile({open, Path}),
    State.

%% ===================================================================
%% Init stream handlers
%% ===================================================================

init_stream_handlers(#state{op=#op{stream_handler_inits=Inits}}=S) ->
    Handlers = [HandlerInit(self()) || HandlerInit <- Inits],
    S#state{stream_handlers=Handlers}.

%% ===================================================================
%% Start exec
%% ===================================================================

start_exec(#state{cmd={Cmd, Env}}=State) ->
    WorkingDir = cmd_working_dir(State),
    Opts =
        [{env, Env},
         {cd, WorkingDir},
         stdout, stderr],
    {ok, Pid, OSPid} = guild_exec:run_link(Cmd, Opts),
    gproc:add_local_property(ospid, OSPid),
    State#state{exec_pid=Pid, exec_ospid=OSPid}.

cmd_working_dir(#state{op=#op{project=Project}}) ->
    guild_project:dir(Project).

%% ===================================================================
%% Tasks
%% ===================================================================

start_tasks(#state{op=#op{tasks=Tasks}}=State) ->
    lists:foreach(fun start_task/1, Tasks),
    State.

start_task(TaskSpec) ->
    {ok, _} = guild_optask_sup:start_task(TaskSpec, self()).

%% ===================================================================
%% Messages
%% ===================================================================

handle_msg({Stream, OSPid, Bin}, _From, #state{exec_ospid=OSPid}=State) ->
    handle_input(Stream, Bin, State);
handle_msg({'EXIT', Pid, Reason}, _From, #state{exec_pid=Pid}) ->
    handle_exec_exit(Reason);
handle_msg({stop, Timeout}, _From, State) ->
    handle_stop(Timeout, State).

handle_input(Stream, Bin, State) ->
    {Lines, Next} = stream_input(Stream, Bin, State),
    handle_stream_lines(Stream, Lines, State),
    {noreply, Next}.

stream_input(stdout, Bin, #state{stdout_buf=Buf}=S) ->
    {Lines, NextBuf} = guild_util:input(Buf, Bin),
    {Lines, S#state{stdout_buf=NextBuf}};
stream_input(stderr, Bin, #state{stderr_buf=Buf}=S) ->
    {Lines, NextBuf} = guild_util:input(Buf, Bin),
    {Lines, S#state{stderr_buf=NextBuf}}.

handle_stream_lines(Stream, Lines, #state{stream_handlers=Handlers}) ->
    dispatch_to_stream_handlers({Stream, Lines}, Handlers).

dispatch_to_stream_handlers(Msg, Handlers) ->
    lists:foreach(fun(H) -> call_stream_handler(H, Msg) end, Handlers).

call_stream_handler(F, Msg) when is_function(F) -> F(Msg);
call_stream_handler({M, F, A}, Msg) -> M:F([Msg|A]).

handle_exec_exit(Reason) ->
    {stop, Reason}.

handle_stop(Timeout, #state{exec_pid=Pid}=State) ->
    exec:stop_and_wait(Pid, Timeout),
    {stop, normal, ok, State}.
