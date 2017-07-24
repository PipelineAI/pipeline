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

-module(guild_cmd_support).

-export([project_options/0, project_options/1, project_from_opts/1,
         project_from_opts/2, project_dir_from_opts/1,
         project_error_msg/2, project_error_msg/3, project_dir_desc/1,
         project_dir_opt/1, latest_rundir/1, rundir_from_args/3,
         validate_rundir/1, run_for_args/2, model_section_for_name/2,
         model_section_for_args/2,
         model_or_resource_section_for_args/2, run_db_for_args/2,
         port_opt/2, exec_operation/2, exec_op/2, preview_op_cmd/1,
         exec_run/2, env_from_opts/2, cli_out_spaces/1]).

-define(github_repo_url, "https://github.com/guildai/guild").

%% ===================================================================
%% Project support
%% ===================================================================

project_options() ->
    project_options([]).

project_options(Opts) ->
    more_project_options(Opts) ++ base_project_options().

base_project_options() ->
    [{project_dir, "-P, --project",
      "project directory (default is current directory)",
      [{metavar, "DIR"}]}].

more_project_options(UserOpts) ->
    Order = [latest_run, flag_support],
    more_project_options_acc(Order, UserOpts, []).

more_project_options_acc([Name|Rest], UserOpts, Acc) ->
    more_project_options_acc(
      Rest, UserOpts,
      maybe_apply_project_options(Name, UserOpts, Acc));
more_project_options_acc([], _, Acc) ->
    lists:reverse(Acc).

maybe_apply_project_options(Name, Opts, Acc) ->
    case proplists:get_bool(Name, Opts) of
        true -> lists:reverse(project_options_(Name)) ++ Acc;
        false -> Acc
    end.

project_options_(flag_support) -> flag_options();
project_options_(latest_run)   -> latest_run_options().

flag_options() ->
    [{profile, "-p, --profile",
      "use alternate flags profile",
      [{metavar, "NAME"}]},
     {flags, "-F, --flag",
      "set a project flag; may be used multiple times",
      [{metavar, "NAME[=VAL]"}]}].

latest_run_options() ->
    [{latest, "-L, --latest-run",
      "use the most recent run",
      [flag]}].

project_from_opts(Opts) ->
    project_from_opts("Guild", Opts).

project_from_opts(Name, Opts) ->
    Project = try_project_from_dir(Name, project_dir_from_opts(Opts)),
    ProfileFlags = profile_flags(Opts, Project),
    CmdlineFlags = cmdline_flags(Opts),
    apply_flags_to_project(
      [{cmdline, CmdlineFlags},
       {profile, ProfileFlags}],
      Project).

project_dir_from_opts(Opts) ->
    proplists:get_value(project_dir, Opts, ".").

try_project_from_dir(Name, Dir) ->
    case guild_project:from_file(filename:join(Dir, Name)) of
        {ok, Project} -> Project;
        {error, Err}  -> guild_cli:cli_error(project_error_msg(Err, Name, Dir))
    end.

project_error_msg(Error, Dir) ->
    project_error_msg(Error, "Guild", Dir).

project_error_msg({missing_project_file, _}, Name, Dir) ->
    missing_project_file_msg(Name, Dir);
project_error_msg({syntax, LNum}, Name, Dir) ->
    syntax_error_msg(LNum, Name, Dir);
project_error_msg({directive, LNum}, Name, Dir) ->
    bad_directive_error_msg(LNum, Name, Dir).

missing_project_file_msg(Name, Dir) ->
    io_lib:format(
      "~s does not contain a ~s file~n"
      "Try 'guild init~s' to initialize a project or change directories.",
      [project_dir_desc(Dir), Name, project_dir_opt(Dir)]).

syntax_error_msg(LNum, Name, Dir) ->
    Path = filename:join(Dir, Name),
    io_lib:format(
      "~s contains an error on line ~b~n"
      "~s:~b: syntax error~n"
      "Try editing the file or submit an issue at " ?github_repo_url,
      [Path, LNum, Path, LNum]).

bad_directive_error_msg(LNum, Name, Dir) ->
    Path = filename:join(Dir, Name),
    io_lib:format(
      "~s contains an error on line ~b~n"
      "~s:~b: unknown directive~n"
      "Try editing the file or submit an issue at " ?github_repo_url,
      [Path, LNum, Path, LNum]).

profile_flags(Opts, Project) ->
    case proplists:get_value(profile, Opts) of
        undefined -> [];
        Profile -> profile_flags_(Profile, Project)
    end.

profile_flags_(Profile, Project) ->
    case guild_project:section(Project, ["flags", Profile]) of
        {ok, Section} -> guild_project:section_attrs(Section);
        error -> bad_profile_error(Profile)
    end.

bad_profile_error(Profile) ->
    guild_cli:cli_error(
      io_lib:format(
        "flags '~s' not defined for this project",
        [Profile])).

cmdline_flags(Opts) ->
    [split_flag_opt(Val) || {_, Val} <- proplists:lookup_all(flags, Opts)].

split_flag_opt(Flag) ->
    case re:split(Flag, "=", [{return, list}, {parts, 2}]) of
        [Name, Val] -> {Name, Val};
        [Name]      -> {Name, "true"}
    end.

apply_flags_to_project(FlagGroups, Project) ->
    lists:foldl(fun apply_flag_group_to_project/2, Project, FlagGroups).

apply_flag_group_to_project({Group, Flags}, Project) ->
    Grouped = [{Group, Key, Val} || {Key, Val} <- Flags],
    lists:foldl(fun apply_flag_to_project/2, Project, Grouped).

apply_flag_to_project({Group, Key, Val}, Project) ->
    guild_project:set_attr(Project, ["flags", Group], Key, Val).

project_dir_desc(".") -> "This directory";
project_dir_desc(Dir) -> io_lib:format("Directory '~s'", [Dir]).

project_dir_opt(".") -> "";
project_dir_opt(Dir) -> io_lib:format(" --project-dir ~s", [escape_path(Dir)]).

escape_path(Path) -> re:replace(Path, " ", "\\\\ ", [global]).

%% ===================================================================
%% Latest rundir
%% ===================================================================

latest_rundir(Project) ->
    case guild_run:runs_for_project(Project) of
        [] -> no_runs_error();
        [Latest|_] -> guild_run:dir(Latest)
    end.

no_runs_error() ->
    guild_cli:cli_error("There are no runs for this project").

%% ===================================================================
%% Rundir from args
%% ===================================================================

rundir_from_args([RunDir], Opts, _Project) ->
    assert_not_latest_flag(Opts),
    RunDir;
rundir_from_args([], Opts, Project) ->
    assert_latest_flag(Opts),
    latest_rundir(Project).

assert_not_latest_flag(Opts) ->
    case proplists:get_bool(latest, Opts) of
        false -> ok;
        true ->
            guild_cli:cli_error(
              "--latest-run cannot be used with RUNDIR")
    end.

assert_latest_flag(Opts) ->
    case proplists:get_bool(latest, Opts) of
        true -> ok;
        false ->
            guild_cli:cli_error(
              "either RUNDIR or --latest-run is required")
    end.

%% ===================================================================
%% Validate rundir
%% ===================================================================

validate_rundir(Dir) ->
    case guild_run:is_run(Dir) of
        true -> ok;
        false ->
            guild_cli:cli_error(
              "~s does not appear to be a valid run",
              [Dir])
    end.

%% ===================================================================
%% Run for args
%% ===================================================================

run_for_args(Args, Opts) ->
    Project = project_from_opts(Opts),
    RunDir = rundir_from_args(Args, Opts, Project),
    Run = run_for_rundir(RunDir),
    Model = model_for_run(Run, Project),
    {Run, Project, Model}.

run_for_rundir(RunDir) ->
    case guild_run:run_for_rundir(RunDir) of
        {ok, Run} -> Run;
        error -> bad_rundir_error(RunDir)
    end.

bad_rundir_error(RunDir) ->
    guild_cli:cli_error(io_lib:format("cannot a find run at ~s", [RunDir])).

model_for_run(Run, Project) ->
    guild_cmd_support:model_section_for_name(run_model_name(Run), Project).

run_model_name(Run) ->
    case guild_run:attr(Run, "model") of
        {ok, <<>>} -> undefined;
        {ok, Name} -> binary_to_list(Name);
        error      -> undefined
    end.

%% ===================================================================
%% Model section for name
%% ===================================================================

model_section_for_name(undefined, Project) ->
    default_model(Project);
model_section_for_name(Name, Project) ->
    named_model(Name, Project).

default_model(Project) ->
    {ok, Model} =
        guild_util:find_apply(
          [fun nameless_model/1,
           fun first_model/1,
           fun(_) -> no_models_error() end],
          [Project]),
    Model.

nameless_model(Project) ->
    guild_project:section(Project, ["model"]).

first_model(Project) ->
    case guild_project:sections(Project, ["model"]) of
        [M|_] -> {ok, M};
        [] -> error
    end.

no_models_error() ->
    guild_cli:cli_error("project does not define any models").

named_model(Name, Project) ->
    case guild_model:find_model_for_name(Project, Name) of
        {ok, Model} -> Model;
        error -> bad_model_error(Name)
    end.

bad_model_error(Name) ->
    guild_cli:cli_error(
      io_lib:format("model '~s' is not defined for this project", [Name])).

%% ===================================================================
%% Model section for args
%% ===================================================================

model_section_for_args([], Project) ->
    model_section_for_name(undefined, Project);
model_section_for_args([Name], Project) ->
    model_section_for_name(Name, Project).

%% ===================================================================
%% Model or resource section for args
%% ===================================================================

model_or_resource_section_for_args([], Project) ->
    model_section_for_name(undefined, Project);
model_or_resource_section_for_args([Name], Project) ->
    {ok, Result} =
        guild_util:find_apply(
          [fun model_for_name/2,
           fun resource_for_name/2,
           fun(_, _) -> bad_model_or_resource_error(Name) end],
          [Project, Name]),
    Result.

model_for_name(Project, Name) ->
    guild_model:find_model_for_name(Project, Name).

resource_for_name(Project, Name) ->
    guild_project:section(Project, ["resource", Name]).

bad_model_or_resource_error(Name) ->
    guild_cli:cli_error(
      io_lib:format(
        "model/resource '~s' is not defined for this project",
        [Name])).

%% ===================================================================
%% Run DB for args
%% ===================================================================

run_db_for_args(Opts, Args) ->
    Project = project_from_opts(Opts),
    RunDir = rundir_from_args(Args, Opts, Project),
    case guild_run_db:open(RunDir) of
        ok -> RunDir;
        {error, missing} -> missing_db_error(RunDir)
    end.

missing_db_error(RunDir) ->
    guild_cli:cli_error(
      io_lib:format("~s does not contain run data", [RunDir])).

%% ===================================================================
%% Server port opt
%% ===================================================================

port_opt(Opts, Default) ->
    validate_port(
      cli_opt:int_val(port, Opts, Default, "invalid value for --port")).

validate_port(P) when P > 0, P < 65535 -> P;
validate_port(_) -> throw({error, "invalid value for --port"}).

%% ===================================================================
%% Exec operation
%% ===================================================================

exec_operation(Name, Op) ->
    {ok, Pid} = guild_operation_sup:start_op(Name, Op),
    wait_for_op_result(Pid).

%% ===================================================================
%% Exec op
%% ===================================================================

exec_op(Name, Op) ->
    {ok, Pid} = guild_op_sup:start_op(Name, Op),
    wait_for_op_result(Pid).

wait_for_op_result(Pid) ->
    guild_proc:reg(Pid),
    OpExit = guild_proc:wait_for({proc, Pid}),
    guild_proc:wait_for({scope, optask}),
    op_result_for_exit(OpExit).

op_result_for_exit({_, normal}) ->
    ok;
op_result_for_exit({_, {exit_status, Status}}) ->
    op_result_for_status(exec:status(Status));
op_result_for_exit({_, Err}) ->
    unexpected_op_error(Err).

op_result_for_status({status, N})          -> {ok, N};
op_result_for_status({signal, Sig, _Core}) -> signal_exit_error(Sig) .

unexpected_op_error(Err) ->
    {error, io_lib:format("unexpected error: ~p", [Err])}.

signal_exit_error(Signal) ->
    {error, io_lib:format("operation interrupted with ~p", [Signal])}.

%% ===================================================================
%% Preview op cmd
%% ===================================================================

preview_op_cmd(Op) ->
    {Args, Env} = guild_op:cmd_preview(Op),
    ResolvedArgs = guild_util:resolve_args(Args, Env),
    print_cmd(ResolvedArgs),
    print_env(Env).

print_cmd(Args) ->
    guild_cli:out("Command:~n~n"),
    print_cmd_args(Args),
    guild_cli:out("~n").

print_cmd_args([First|Rest]) ->
    guild_cli:out("  ~s", [First]),
    print_rest_cmd_args(Rest).

print_rest_cmd_args(["-"++_=Opt, Next|Rest]) ->
    guild_cli:out(" \\~n    ~s", [Opt]),
    case Next of
        "-"++_ ->
            print_rest_cmd_args([Next|Rest]);
        OptVal ->
            guild_cli:out(" ~s", [maybe_quote(OptVal)]),
            print_rest_cmd_args(Rest)
    end;
print_rest_cmd_args([Arg|Rest]) ->
    guild_cli:out(" \\~n    ~s", [Arg]),
    print_rest_cmd_args(Rest);
print_rest_cmd_args([]) ->
    guild_cli:out("~n").

maybe_quote(Opt) ->
    case re:run(Opt, " ", [{capture, none}]) of
        match   -> ["\"", Opt, "\""];
        nomatch -> Opt
    end.

print_env(undefined) -> ok;
print_env(Env) ->
    guild_cli:out("Environment:~n~n"),
    lists:foreach(
      fun({Name, Val}) -> guild_cli:out("  ~s=~s~n", [Name, Val]) end,
      lists:sort(Env)),
    guild_cli:out("~n").

%% ===================================================================
%% Exec run
%% ===================================================================

exec_run(Args, Opts) ->
    exec_run_result(guild_exec:run(Args, Opts)).

exec_run_result({ok, []}) ->
    ok;
exec_run_result({error, [{exit_status, Status}]}) ->
    {error, exec:status(Status)}.

%% ===================================================================
%% Env from opts
%% ===================================================================

env_from_opts(Rules, Opts) ->
    {_, Env} = lists:foldl(fun apply_cmd_env/2, {Opts, []}, Rules),
    Env.

apply_cmd_env({Flag, Name}, {Opts, Env}) ->
    {Opts, maybe_opt_val(Flag, Name, Opts, Env)};
apply_cmd_env({Flag, Name, Val}, {Opts, Env}) ->
    {Opts, maybe_flag_val(Flag, Name, Val, Opts, Env)}.

maybe_opt_val(Flag, Name, Opts, Env) ->
    case proplists:get_value(Flag, Opts) of
        undefined -> Env;
        Val -> [{Name, Val}|Env]
    end.

maybe_flag_val(Flag, Name, Val, Opts, Env) ->
    case proplists:get_bool(Flag, Opts) of
        true -> [{Name, Val}|Env];
        false -> Env
    end.

%% ===================================================================
%% CLI output spaces
%% ===================================================================

cli_out_spaces(N) when N > 0 ->
    guild_cli:out(" "),
    cli_out_spaces(N - 1);
cli_out_spaces(_) ->
    ok.
