-module(guild_evaldir).

-export([path_for_run/2, init/1]).

%% ===================================================================
%% Path for run
%% ===================================================================

path_for_run(Run, Time) ->
    filename:join(guild_run:dir(Run), evaldir_name(Time)).

evaldir_name(Time) ->
    "eval-" ++ guild_util:format_dir_timestamp(Time).

%% ===================================================================
%% Init
%% ===================================================================

init(EvalDir) ->
    ok = filelib:ensure_dir(EvalDir ++ "/").
