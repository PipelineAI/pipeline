-module(guild_op_support).

-export([static_env/0, python_cmd/2, op_stream_handlers/1,
         required_missing/2, default_collector_tasks/1]).

-define(default_stats_task_repeat, 10000).

%% ===================================================================
%% Static env
%% ===================================================================

static_env() ->
    [{"PKGHOME", guild_app:pkg_dir()},
     {"GPU_COUNT", gpu_count_env()}].

gpu_count_env() ->
    integer_to_list(length(guild_sys:gpu_attrs())).

%% ===================================================================
%% Op stream handlers
%% ===================================================================

python_cmd(CmdSpec, Flags) ->
    Python = guild_util:find_exe("python"),
    [First|Args] = guild_util:split_cmd(CmdSpec),
    Script = resolved_script_path(First),
    [Python, "-u", Script] ++ Args ++ flag_args(Flags).

resolved_script_path(Val) ->
    Checks =
        [fun explicit_path/1,
         fun path_missing_py_ext/1,
         fun unmodified_path/1],
    guild_util:find_apply2(Checks, [Val]).

explicit_path(Val) ->
    case filelib:is_regular(Val) of
        true -> {stop, Val};
        false -> continue
    end.

path_missing_py_ext(Val) -> explicit_path(Val ++ ".py").

unmodified_path(Val) -> {stop, Val}.

flag_args(Flags) ->
    lists:concat(
      [["--" ++ Name, Val]
       || {Name, Val} <- Flags, is_flag_arg(Name)]).

is_flag_arg("description") -> false;
is_flag_arg(_)             -> true.

%% ===================================================================
%% Op stream handlers
%% ===================================================================

op_stream_handlers(Specs) ->
    [op_stream_handler(Spec) || Spec <- Specs].

op_stream_handler(console) ->
    fun(_Op) -> fun log_output_to_console/1 end;
op_stream_handler(run_db_output) ->
    fun(Op) -> run_db_output_handler(Op) end.

log_output_to_console({stdout, Lines}) ->
    lists:foreach(fun({_, L}) -> println(user, L) end, Lines);
log_output_to_console({stderr, Lines}) ->
    lists:foreach(fun({_, L}) -> println(standard_error, L) end, Lines).

println(Device, Bin) ->
    io:format(Device, "~s~n", [Bin]).

run_db_output_handler(Op) ->
    RunDir = guild_op:opdir(Op),
    fun(Out) -> log_output_to_db(RunDir, Out) end.

log_output_to_db(RunDir, {Stream, Lines}) ->
    Output = format_output_for_db(Stream, Lines),
    handle_log_to_db_result(guild_run_db:log_output(RunDir, Output)).

format_output_for_db(Stream, Lines) ->
    [{Time, Stream, Line} || {Time, Line} <- Lines, filter_line_for_db(Line)].

filter_line_for_db([<<"\e", _/binary>>|_]) -> false;
filter_line_for_db(_) -> true.

handle_log_to_db_result(ok) -> ok;
handle_log_to_db_result({error, Err}) ->
    guild_log:internal("Error writing output to db: ~p~n", [Err]).

%% ===================================================================
%% Required missing
%% ===================================================================

required_missing(Section, Project) ->
    required_missing_(
      guild_project:section_attr(Section, "train_requires"),
      Project).

required_missing_({ok, Required}, Project) ->
    case first_missing(required_paths(Required, Project)) of
        undefined -> false;
        Path -> {true, Path}
    end;
required_missing_(error, _Project) ->
    false.

required_paths(Required, Project) ->
    Dir = guild_project:dir(Project),
    [filename:absname(Path, Dir) || Path <- split_required(Required)].

split_required(Required) ->
    re:split(Required, "\s*,\s*", [{return, list}]).

first_missing([Path|Rest]) ->
    case filelib:is_file(Path) of
        true -> first_missing(Rest);
        false -> Path
    end;
first_missing([]) ->
    undefined.

%% ===================================================================
%% Default collector tasks
%% ===================================================================

default_collector_tasks(Flags) ->
    Repeat = stats_interval_opt(Flags),
    [collector("tensorflow-collector", Repeat),
     collector("op-stats", Repeat),
     collector("sys-stats", Repeat),
     collector("gpu-stats", Repeat)].

stats_interval_opt(Flags) ->
    case proplists:get_value("stats_interval", Flags) of
        undefined -> ?default_stats_task_repeat;
        I -> list_to_integer(I) * 1000
    end.

collector(Script, Repeat) ->
    {guild_collector_task, start_link, [Script, [{repeat, Repeat}]]}.
