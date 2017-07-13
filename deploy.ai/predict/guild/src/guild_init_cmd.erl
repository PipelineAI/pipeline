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

-module(guild_init_cmd).

-export([parser/0, main/2]).

-behavior(erlydtl_library).

-export([version/0, inventory/1, warn_if_empty/2, required/2,
         latest_package/1, latest_package_checkpoint/1]).

-record(template, {pkg, src, project}).

parser() ->
    cli:parser(
      "guild init",
      "[OPTION]... [DIR] [VAR=VALUE]...",
      "Initialize a Guild project.\n"
      "\n"
      "By default Guild creates an annotated project file in DIR (defaults "
      "to current directory). Specify a template to create a full configured "
      "project from a source package. TEMPLATE may be the name of an "
      "installed source package or a directory containing a 'Guild.in' project "
      "template.\n"
      "\n"
      "When specifying a template, specify variables using VAR=VALUE "
      "arguments. To list variables available for a template, use "
      "--print-vars. Some variables are required and are indicated as such "
      "in the list. If a required variable is not specified, the command "
      "will fail with an error message.\n"
      "\n"
      "You may alternatively initialize a Guild package using the --package "
      "option. This will generate a skeleton GuildPkg file and packager "
      "script that you must edit manually to define your package."
      "",
      init_opts(),
      [{pos_args, {0, any}}]).

init_opts() ->
    [{template, "--template", "propject template", [{metavar, "TEMPLATE"}]},
     {package, "--package",
      "initialize a Guild package (exlusive of --template)", [flag]},
     {force, "--force",
      "initialize project even when DIR is non-empty (WARNING this will "
      "overwite existing files)", [flag]},
     {ignore_vars, "--ignore-vars",
      "initialize project even when required vars are not provided or "
      "contain invalid package references", [flag]},
     {print_vars, "--print-vars", "print variables used by TEMPLATE", [flag]}].

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, Args) ->
    handle_main(project_type(Opts), Opts, Args).

project_type(Opts) ->
    case proplists:get_bool(package, Opts) of
        true -> package;
        false -> project
    end.

handle_main(project, Opts, Args) -> project_init(Opts, Args);
handle_main(package, Opts, Args) -> package_init(Opts, Args).

%% ===================================================================
%% Project init
%% ===================================================================

project_init(Opts, Args) ->
    Template = resolve_template(Opts),
    case print_vars_flag(Opts) of
        true -> print_vars(Template);
        false -> init_project(Template, Args, Opts)
    end.

resolve_template(Opts) ->
    case proplists:get_value(template, Opts) of
        undefined -> annotated_project_template();
        Pkg -> resolve_package_template(Pkg)
    end.

annotated_project_template() ->
    #template{project=annotated_project()}.

annotated_project() ->
    Path = filename:join(guild_app:priv_dir("projects"), "annotated"),
    {ok, Project} = guild_project:from_file(Path),
    Project.

resolve_package_template(Val) ->
    Locations =
        [fun find_project_template/1,
         fun find_installed_package_template/1],
    case guild_util:find_apply(Locations, [Val]) of
        {ok, Template} -> Template;
        error -> bad_template_error(Val)
    end.

find_project_template(Dir) ->
    case filelib:is_dir(Dir) of
        true ->
            Pkg = filename:basename(Dir),
            {ok, package_template_from_path(Dir, Pkg)};
        false ->
            error
    end.

package_template_from_path(PkgDir, Pkg) ->
    TemplatePath = filename:join(PkgDir, "Guild.in"),
    case guild_project:from_file(TemplatePath) of
        {ok, Project} ->
            #template{pkg=Pkg, src=PkgDir, project=Project};
        {error, {missing_project_file, _}} ->
            missing_guild_in_error(PkgDir)
    end.

missing_guild_in_error(PkgDir) ->
    guild_cli:cli_error(
      io_lib:format(
        "~s is not a source package (missing Guild.in)",
        [PkgDir])).

find_installed_package_template(Pkg) ->
    case guild_package_util:latest_package_path(Pkg) of
        {ok, Path} ->
            {ok, package_template_from_path(Path, Pkg)};
        error ->
            error
    end.

bad_template_error(Val) ->
    guild_cli:cli_error(
      io_lib:format(
        "'~s' does not appear to be a project template~n"
        "Try 'guild init --help' for more information.",
        [Val])).

print_vars_flag(Opts) ->
    proplists:get_bool(print_vars, Opts).

%% ===================================================================
%% Print vars
%% ===================================================================

print_vars(Template) ->
    lists:foreach(fun print_var/1, var_defs(Template)).

var_defs(#template{project=Project}) ->
    [vardef(Var) || Var <- guild_project:meta(Project, "var")].

vardef(["var", Name]) ->
    #{name => Name, help => undefined, opts => []};
vardef(["var", Name, Help]) ->
    #{name => Name, help => Help, opts => []};
vardef(["var", Name, Help|Opts]) ->
    #{name => Name, help => Help, opts => Opts}.

-define(help_inset, 20).

print_var(#{name:=Name, help:=Help}) ->
    print_var_name(Name),
    print_var_help(Help, ?help_inset - length(Name) + 1),
    guild_cli:out("\n").

print_var_name(Name) ->
    guild_cli:out(Name).

print_var_help(undefined, _Indent) -> ok;
print_var_help(Help, Indent) ->
    guild_cmd_support:cli_out_spaces(Indent),
    guild_cli:out(Help).

%% ===================================================================
%% Init project
%% ===================================================================

init_project(Template, Args, Opts) ->
    ProjectDir = project_dir_from_args(Args),
    assert_project_dir_empty(ProjectDir, Opts),
    Vars = validated_template_vars(Args, ProjectDir, Template, Opts),
    GuildBin = render_guild_file(Template, Vars, Opts),
    maybe_copy_template_src(Template, ProjectDir),
    write_guild_file(GuildBin, ProjectDir),
    maybe_ignored_errors_msg().

project_dir_from_args([Arg|Rest]) ->
    case lists:member($=, Arg) of
        true -> project_dir_from_args(Rest);
        false -> Arg
    end;
project_dir_from_args([]) ->
    filename:absname("").

assert_project_dir_empty(Dir, Opts) ->
    case file:list_dir(Dir) of
        {error, enoent} -> ok;
        {ok, []} -> ok;
        {ok, _} ->
            maybe_project_dir_not_empty_error(Dir, Opts)
    end.

maybe_project_dir_not_empty_error(Dir, Opts) ->
    case proplists:get_bool(force, Opts) of
        true -> ok;
        false -> project_dir_not_empty_error(Dir)
    end.

project_dir_not_empty_error(Dir) ->
    guild_cli:cli_error(
      io_lib:format(
        "'~s' is not an empty directory\n"
        "You may use 'guild init --force' to bypass this check.",
        [Dir])).

validated_template_vars(Args, ProjectDir, Template, Opts) ->
    BaseVars = base_vars(ProjectDir),
    UserVars = vars_from_args(Args),
    validate_user_vars(UserVars, Template, Opts),
    UserAndDefaultVars = apply_default_vars(UserVars, Template, BaseVars),
    UserAndDefaultVars ++ BaseVars.

base_vars(ProjectDir) ->
    [{project_dir, dir_basename(ProjectDir)}].

dir_basename(".") -> dir_basename("");
dir_basename(Dir) -> filename:basename(filename:absname(Dir)).

vars_from_args(Args) ->
    lists:foldl(fun acc_vars/2, [], Args).

acc_vars(Arg, Acc) ->
    case re:split(Arg, "=", [{parts, 2}, {return, list}]) of
        [Name, Val] -> [{Name, Val}|Acc];
        [_] -> Acc
    end.

validate_user_vars(Vars, Template, Opts) ->
    case proplists:get_bool(ignore_vars, Opts) of
        true -> ok;
        false -> validate_user_vars(Vars, Template)
    end.

validate_user_vars(Vars, Template) ->
    VarDefs = var_defs(Template),
    Validate = var_validate_fun(Vars, Template),
    lists:foreach(Validate, VarDefs).

var_validate_fun(Vars, Template) ->
    fun(Def) -> check_required_var(Def, Vars, Template) end.

check_required_var(#{name:=Name, opts:=Opts}, Vars, Template) ->
    case is_var_required(Opts) andalso is_var_missing(Name, Vars) of
        true -> missing_required_var_error(Name, Template);
        false -> ok
    end.

is_var_required(Opts) ->
    lists:member("required", Opts).

is_var_missing(Name, Vars) ->
    proplists:get_value(Name, Vars, undefined) == undefined.

missing_required_var_error(Name, #template{pkg=Pkg}) ->
    guild_cli:cli_error(
      io_lib:format(
        "project template for ~s requires '~s' variable~n"
        "Try 'guild init --template=~s --print-vars' for help or "
        "'guild init --ignore-vars' to bypass this check.",
        [Pkg, Name, Pkg])).

apply_default_vars(UserVars, Template, BaseVars) ->
    VarDefs = var_defs(Template),
    ApplyMissing = fun(Def, Vars) -> apply_missing_var(Vars, Def, BaseVars) end,
    lists:foldl(ApplyMissing, UserVars, VarDefs).

apply_missing_var(Vars, #{name:=Name}=Def, BaseVars) ->
    case is_var_missing(Name, Vars) of
        true -> maybe_apply_default(Vars, Def, BaseVars);
        false -> Vars
    end.

maybe_apply_default(Vars, #{name:=Name, opts:=Opts}, BaseVars) ->
    case var_default(Opts) of
        undefined -> Vars;
        Default -> [{Name, render_default(Default, BaseVars)}|Vars]
    end.

var_default(["default="++Default|_]) -> Default;
var_default([_|Rest]) -> var_default(Rest);
var_default([]) -> undefined.

render_default(Val, Vars) ->
    Mod = guild_init_var_template,
    Opts = [return, {out_dir, false}],
    case erlydtl:compile_template(Val, Mod, Opts) of
        {ok, _, []} ->
            handle_default_render(Mod:render(Vars), Val);
        {error, Err} ->
            handle_default_compile_error(Err, Val)
    end.

handle_default_compile_error(Err, Orig) ->
    guild_cli:warn("~p", [Err]),
    Orig.

handle_default_render({ok, Rendered}, _) ->
    Rendered;
handle_default_render({error, Err}, Orig) ->
    guild_cli:warn("~p~n", [Err]),
    Orig.

render_guild_file(#template{project=Project}, Vars, Opts) ->
    ProjectSrc = guild_project:project_file(Project),
    Mod = compile_guild_file(ProjectSrc),
    render_guild_file_(Mod, Vars, Opts).

compile_guild_file(ProjectSrc) ->
    Mod = guild_init_project_template,
    Opts = [{default_libraries, [?MODULE]}],
    try guild_dtl_util:compile_template(ProjectSrc, Mod, Opts) of
        ok -> Mod
    catch
        error:{template_compile, _} -> bad_template_error()
    end.

bad_template_error() ->
    guild_cli:cli_error(
      "unable to initialize project - template contains errors").

render_guild_file_(Mod, Vars, Opts) ->
    guild_globals:put(guild_init_cmd_opts, Opts),
    handle_render_guild_file(Mod:render(Vars)).

handle_render_guild_file({ok, Bin}) ->
    guild_project_util:strip_meta(Bin);
handle_render_guild_file({error, Msg}) ->
    guild_cli:cli_error(Msg).

maybe_copy_template_src(#template{src=undefined}, _Dest) -> ok;
maybe_copy_template_src(#template{src=Src}, Dest) ->
    guild_app:init_support([exec]),
    Bin = guild_app:priv_bin("guild-init-project"),
    Args = [Bin, Src, Dest],
    guild_cmd_support:exec_run(Args, []).

write_guild_file(Bin, Dir) ->
    Path = filename:join(Dir, "Guild"),
    ok = filelib:ensure_dir(Path),
    ok = file:write_file(Path, Bin).

maybe_ignored_errors_msg() ->
    case guild_globals:get(guild_init_cmd_var_errors_ignored) of
        {ok, true} -> ignored_errors_msg();
        _ -> ok
    end.

ignored_errors_msg() ->
    guild_cli:warn(
      "WARNING: one or more errors were ignored because "
      "'--ignore-vars' was used\n").

%% ===================================================================
%% Template support
%% ===================================================================

version() -> 1.

inventory(filters) ->
    [warn_if_empty,
     required,
     latest_package,
     latest_package_checkpoint];
inventory(tags) ->
    [].

warn_if_empty(Val, Msg) when Val == ""; Val == undefined ->
    guild_cli:warn("WARNING: ~s~n", [Msg]),
    Val;
warn_if_empty(Val, _Msg) ->
    Val.

required(Val, Msg) when Val == ""; Val == undefined ->
    throw(Msg);
required(Val, _Msg) ->
    Val.

latest_package(undefined) ->
    "";
latest_package(Val) ->
    case guild_package_util:latest_package_path(Val) of
        {ok, Path} ->
            filename:basename(Path);
        {error, package} ->
            maybe_missing_package_error(Val)
    end.

maybe_missing_package_error(Name) ->
    case ignore_vars_global_option() of
        true ->
            warn_missing_package(Name),
            "";
        false ->
            throw(missing_package_msg(Name))
    end.

ignore_vars_global_option() ->
    {ok, Opts} = guild_globals:get(guild_init_cmd_opts),
    proplists:get_bool(ignore_vars, Opts).

warn_missing_package(Name) ->
    guild_cli:warn(
      io_lib:format(
        "WARNING: there are no installed packages matching '~s'\n",
        [Name])),
    guild_globals:put(guild_init_cmd_var_errors_ignored, true).

missing_package_msg(Name) ->
    io_lib:format(
      "there are no installed packages matching '~s'\n"
      "Use '--ignore-vars' to bypass this warning.",
      [Name]).

latest_package_checkpoint(undefined) -> "";
latest_package_checkpoint(Val) ->
    case guild_package_util:latest_package_checkpoint_path(Val) of
        {ok, Path} ->
            format_package_checkpoint(Path);
        {error, package} ->
            maybe_missing_package_error(Val);
        {error, {checkpoint, Path}} ->
            maybe_missing_checkpoint_error(Val, Path)
    end.

format_package_checkpoint(Path) ->
    Name = filename:basename(Path),
    Pkg = filename:basename(filename:dirname(Path)),
    filename:join(Pkg, Name).

maybe_missing_checkpoint_error(Val, Path) ->
    case ignore_vars_global_option() of
        true ->
            warn_missing_checkpoint(Val, Path),
            "";
        false ->
            throw(missing_checkpoint_msg(Val, Path))
    end.

warn_missing_checkpoint(Pkg, Path) ->
    guild_cli:warn(
      io_lib:format(
        "WARNING: there are no checkpoints available for ~s (using ~s)",
        [Pkg, Path])).

missing_checkpoint_msg(Pkg, Path) ->
    io_lib:format(
      "there are no checkpoints available for ~s (using ~s)",
      [Pkg, Path]).

%% ===================================================================
%% Package init
%% ===================================================================

package_init(_Opts, Args) ->
    ProjectDir = project_dir_from_args(Args),
    copy_package_files(ProjectDir).

copy_package_files(Dest) ->
    SrcDir = guild_app:priv_dir("projects"),
    guild_app:init_support([exec]),
    Bin = guild_app:priv_bin("guild-init-package"),
    Args = [Bin, SrcDir, Dest],
    guild_cmd_support:exec_run(Args, []).
