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

-module(guild_util).

-export([fold_apply/2, foreach_apply/2, find_apply/1, find_apply/2,
         find_apply/3, find_apply2/2, try_apply/3, maybe_default/2,
         priv_dir/1, new_input_buffer/0, input/2, finalize_input/1,
         find_exe/1, split_cmd/1, split_keyvals/1, list_join/2,
         reduce_to/2, normalize_series/3, os_pid_exists/1,
         format_cmd_args/1, make_tmp_dir/0, random_name/0,
         delete_tmp_dir/1, resolve_args/2, resolve_keyvals/2,
         consult_string/1, format_exec_error/1, latest_mtime/1,
         free_port/0, format_dir_timestamp/1]).

-include_lib("kernel/include/file.hrl").

%% ===================================================================
%% Common programming patterns support
%% ===================================================================

fold_apply(Funs, Acc0) ->
    lists:foldl(fun(F, Acc) -> F(Acc) end, Acc0, Funs).

foreach_apply(Funs, X) ->
    lists:foreach(fun(F) -> F(X) end, Funs).

find_apply(Funs) -> find_apply(Funs, []).

find_apply([F|Rest], Args) ->
    case apply(F, Args) of
        {ok, Val} -> {ok, Val};
        error -> find_apply(Rest, Args)
    end;
find_apply([], _Args) ->
    error.

find_apply(Funs, Args, Default) ->
    case find_apply(Funs, Args) of
        {ok, Val} -> Val;
        error -> Default
    end.

find_apply2([F|Rest], Args) ->
    case apply(F, Args) of
        {stop, Val} -> Val;
        continue -> find_apply2(Rest, Args)
    end;
find_apply2([], _Args) ->
    error(not_found).

try_apply([F|Rest], Args, Default) ->
    try
        apply(F, Args)
    catch
        _:_ -> try_apply(Rest, Args, Default)
    end;
try_apply([], _Args, Default) ->
    Default.

maybe_default({ok, Val}, _Default) -> Val;
maybe_default(error, Default) -> Default.

%% ===================================================================
%% Priv dir support
%% ===================================================================

priv_dir(App) ->
    priv_dir(
      [fun default_priv_dir/1,
       fun script_relative_priv_dir/1],
      App).

priv_dir([Fun|Rest], App) ->
    case Fun(App) of
        {ok, Dir} -> Dir;
        error -> priv_dir(Rest, App)
    end;
priv_dir([], App) ->
    error({cannot_find_priv_dir, App}).

default_priv_dir(App) ->
    valid_priv_dir(code:priv_dir(App)).

script_relative_priv_dir(App) ->
    Priv = code:priv_dir(App),
    Script = script_path_from_priv(Priv),
    case filelib:is_file(Script) of
        true -> valid_priv_dir(priv_dir_from_script(Script, App));
        false -> error
    end.

script_path_from_priv(Priv) ->
    ensure_resolved(filename:dirname(filename:dirname(Priv))).

ensure_resolved(Src) ->
    case file:read_link_all(Src) of
        {ok, Target} -> filename:absname(Target, filename:dirname(Src));
        {error, _} -> Src
    end.

priv_dir_from_script(Script, App) ->
    filename:join(
      filename:dirname(Script),
      "../lib/" ++ atom_to_list(App) ++ "/priv").

valid_priv_dir(Dir) ->
    case filelib:is_dir(Dir) of
        true -> {ok, Dir};
        false -> error
    end.

%% ===================================================================
%% Input buffer
%% ===================================================================

new_input_buffer() -> {[], undefined}.

input(Buf, Bin) ->
    Now = input_timestamp(),
    handle_input_lines(split_lines(Bin), Now, Buf).

input_timestamp() ->
    erlang:system_time(micro_seconds).

split_lines(Bin) ->
    re:split(Bin, "\r\n|\n|\r|\032").

handle_input_lines([<<>>], _Now, Buf) ->
    finalize_buffer_lines(Buf);
handle_input_lines([Bin], Now, Buf) ->
    finalize_buffer_lines(buffer_input(Bin, Now, Buf));
handle_input_lines([Bin|Rest], Now, Buf) ->
    NextBuf = finalize_buffered_input(buffer_input(Bin, Now, Buf)),
    handle_input_lines(Rest, Now, NextBuf).

finalize_buffer_lines({Lines, Working}) ->
    {lists:reverse(Lines), {[], Working}}.

buffer_input(Bin, Now, {Lines, undefined}) ->
    {Lines, {Now, [Bin]}};
buffer_input(Bin, _Now, {Lines, {Time, Parts}}) ->
    {Lines, {Time, [Bin|Parts]}}.

finalize_buffered_input({Lines, {Time, Parts}}) ->
    {[{Time, lists:reverse(Parts)}|Lines], undefined};
finalize_buffered_input({Lines, undefined}) ->
    {Lines, undefined}.

finalize_input(Buf) ->
    {Lines, undefined} = finalize_buffered_input(Buf),
    lists:reverse(Lines).

%% ===================================================================
%% Find exe
%% ===================================================================

find_exe(Name) ->
    case os:find_executable(Name) of
        false -> error({find_exe, Name});
        Exe -> Exe
    end.

%% ===================================================================
%% Split cmd
%% ===================================================================

split_cmd(Spec) ->
    %% Basic support for quoted args / no support for escaped quotes
    Pattern = "\"([^\"]+)\"|([^\\s\"]+)",
    case re:run(Spec, Pattern, [global, {capture, all_but_first, list}]) of
        {match, Match} -> cmd_parts_for_match(Match);
        nomatch -> []
    end.

cmd_parts_for_match(Match) ->
    Part = fun(["", P]) -> P; ([P]) -> P end,
    [Part(X) || X <- Match].

%% ===================================================================
%% Split keyvals
%% ===================================================================

split_keyvals(Spec) ->
    %% Basic support for quoted vals / no support for escaped quotes

    %Pattern = "(.+?)=(.+?)(?:\\s+|$)",

    Pattern = "(.+?)=(?:\"([^\"]+)\"|([^\\s\"]+))(?:\\s+|$)",

    case re:run(Spec, Pattern, [global, {capture, all_but_first, list}]) of
        {match, Match} -> keyvals_for_match(Match);
        nomatch -> []
    end.

keyvals_for_match(Match) ->
     KV = fun([Key, "", Val]) -> {Key, Val}; ([Key, Val]) -> {Key, Val} end,
     [KV(X) || X <- Match].

%% ===================================================================
%% List join
%% ===================================================================

list_join([], _Internal) -> [];
list_join(L, Internal) ->
    list_join_acc(L, Internal, []).

list_join_acc([Last], _, Acc) -> lists:reverse([Last|Acc]);
list_join_acc([X|Rest], I, Acc) -> list_join_acc(Rest, I, [I, X|Acc]).

%% ===================================================================
%% Reduce to count
%% ===================================================================

reduce_to(L, Max) when length(L) > Max, Max > 0 ->
    KeepEvery = length(L) div Max + 1,
    %% Reverse and start at 0 to ensure we have the last
    reduce_to(lists:reverse(L), 0, KeepEvery, Max, 0, []);
reduce_to(L, _Max) ->
    L.

reduce_to([X|Rest], N, KeepEvery, Max, AccLen, Acc) when AccLen < Max ->
    case N rem KeepEvery of
        0 -> reduce_to(Rest, N + 1, KeepEvery, Max, AccLen + 1, [X|Acc]);
        _ -> reduce_to(Rest, N + 1, KeepEvery, Max, AccLen, Acc)
    end;
reduce_to(_, _, _, _, _, Acc) ->
    Acc.

%% ===================================================================
%% Normalize series
%% ===================================================================

normalize_series(S, N, Consolidate) when N > 0 ->
    Reversed = lists:reverse(S),
    [[X0, _]|_] = S,
    [[X1, _]|_] = Reversed,
    TotalDistance = X1 - X0,
    EpochSize = TotalDistance div N + 1,
    normalize_acc(Reversed, X1, EpochSize, Consolidate, []).

normalize_acc([[X, Val]|Rest], X1, ES, C, Acc) ->
    Epoch = X1 - (X1 - X) div ES * ES,
    normalize_acc(Rest, X1, ES, C, maybe_add(Epoch, Val, C, Acc));
normalize_acc([], _, _, _, Acc) ->
    Acc.

maybe_add(Epoch, XVal, Cons, [[Epoch, CurVal]|AccRest]) ->
    [[Epoch, consolidate(Cons, XVal, CurVal)]|AccRest];
maybe_add(Epoch, XVal, _Cons, Acc) ->
    [[Epoch, XVal]|Acc].

consolidate(max ,  X,  Cur) when X > Cur -> X;
consolidate(min ,  X,  Cur) when X < Cur -> X;
consolidate(first, X, _Cur)              -> X;
consolidate(_,    _X,  Cur)              -> Cur.

%% ===================================================================
%% OS pid exists
%% ===================================================================

os_pid_exists(Pid) when is_integer(Pid) ->
    os_pid_exists(integer_to_list(Pid));
os_pid_exists(Pid) ->
    case exec:run(["/bin/ps", "-p", Pid], [sync]) of
        {ok, []} -> true;
        {error, _} -> false
    end.

%% ===================================================================
%% Format command args
%% ===================================================================

format_cmd_args(Args) ->
    string:join([maybe_quote_arg(Arg) || Arg <- Args], " ").

maybe_quote_arg(Arg) ->
    case lists:member($\s, Arg) of
        true -> ["\"", Arg, "\""];
        false -> Arg
    end.

%% ===================================================================
%% Temp dir support
%% ===================================================================

make_tmp_dir() ->
    Path = random_tmp_path(),
    case filelib:ensure_dir(Path ++ "/") of
        ok -> {ok, Path};
        {error, Err} -> {error, Err}
    end.

random_tmp_path() ->
    filename:join(guild_app:tmp_dir(), "guild-" ++ random_name()).

%% ===================================================================
%% Random name
%% ===================================================================

random_name() ->
    Rand = crypto:strong_rand_bytes(6),
    Encoded = base64:encode(Rand),
    re:replace(Encoded, "(/|\\+|=)", "0", [global, {return, list}]).

delete_tmp_dir(Dir) when is_list(Dir) ->
    safeguard_delete_dir(Dir),
    "" = os:cmd("rm -rf '" ++ Dir ++ "'"),
    ok.

safeguard_delete_dir(Dir) ->
    foreach_apply(
      [fun check_dir_abs/1,
       fun check_dir_min_depth/1],
      Dir).

check_dir_abs(Dir) ->
    case filename:pathtype(Dir) of
        absolute -> ok;
        BadType -> error({refuse_delete_dir, Dir, BadType})
    end.

-define(delete_dir_min_depth, 3).

check_dir_min_depth(Dir) ->
    case dir_depth(Dir) >= ?delete_dir_min_depth of
        true -> ok;
        false -> error({refuse_delete_dir, Dir, min_depth})
    end.

dir_depth(Dir) -> length(filename:split(Dir)).

%% ===================================================================
%% Resolve args
%% ===================================================================

resolve_args(Args, Env) ->
    [resolve_arg_env_refs(Arg, Env) || Arg <- Args].

resolve_arg_env_refs(Arg, Env) ->
    Resolve = fun({Name, Val}, In) -> replace_env_refs(In, Name, Val) end,
    lists:foldl(Resolve, Arg, Env).

replace_env_refs(In, Name, Val) ->
    re:replace(In, "\\$" ++ Name, Val, [{return, list}, global]).

%% ===================================================================
%% Resolve key vals
%% ===================================================================

resolve_keyvals(KeyVals, Env) ->
    [{Key, resolve_arg_env_refs(Val, Env)} || {Key, Val} <- KeyVals].

%% ===================================================================
%% Consult string
%% ===================================================================

consult_string(S) ->
    case erl_scan:string(S) of
        {ok, Tokens, _} -> consult_tokens(Tokens);
        {error, Err} -> {error, Err}
    end.

consult_tokens(Tokens) ->
    parse_tokens(split_tokens_by_dot(Tokens)).

split_tokens_by_dot(Tokens) ->
    split_tokens_by_dot_acc(Tokens, [], []).

split_tokens_by_dot_acc([{dot, N}|Rest], Cur, Acc) ->
    Tokens = lists:reverse([{dot, N}|Cur]),
    split_tokens_by_dot_acc(Rest, [], [Tokens|Acc]);
split_tokens_by_dot_acc([Token|Rest], Cur, Acc) ->
    split_tokens_by_dot_acc(Rest, [Token|Cur], Acc);
split_tokens_by_dot_acc([], [], Acc) ->
    lists:reverse(Acc);
split_tokens_by_dot_acc([], Cur, Acc) ->
    lists:reverse([lists:reverse(Cur)|Acc]).

parse_tokens(Tokens) ->
    parse_tokens_acc(Tokens, []).

parse_tokens_acc([Tokens|Rest], Acc) ->
    case erl_parse:parse_term(Tokens) of
        {ok, Term} -> parse_tokens_acc(Rest, [Term|Acc]);
        {error, Err} -> {error, Err}
    end;
parse_tokens_acc([], Acc) ->
    {ok, lists:reverse(Acc)}.

%% ===================================================================
%% Format exec error
%% ===================================================================

format_exec_error(Err) ->
    {status, Status} = exec:status(proplists:get_value(exit_status, Err)),
    StdOut = strip(proplists:get_value(stdout, Err, [])),
    StdErr = strip(proplists:get_value(stderr, Err, [])),
    case {StdOut, StdErr} of
        {[], _} -> io_lib:format("~s (~b)", [StdErr, Status]);
        {_, []} -> io_lib:format("~s (~b)", [StdOut, Status]);
        {_, _}  -> io_lib:format("~s ~s (~b)", [StdOut, StdErr, Status])
    end.

strip(S0) ->
    {match, [S]} = re:run(S0, "^\s*(.*?)\s*$", [{capture, [1], list}, dotall]),
    S.

%% ===================================================================
%% Lastest mtime
%% ===================================================================

latest_mtime(FilePatterns) ->
    case mtimes(match_files(FilePatterns)) of
        [] -> undefined;
        Files -> lists:max(Files)
    end.

match_files(Patterns) ->
    lists:foldl(fun match_file_acc/2, [], Patterns).

match_file_acc(Pattern, Acc) ->
    Matches = filelib:wildcard(Pattern),
    lists:foldl(fun(F, AccIn) -> [F|AccIn] end, Acc, Matches).

mtimes(Files) ->
    lists:foldl(fun mtime_acc/2, [], Files).

mtime_acc(File, Acc) ->
    case file:read_file_info(File, [{time, posix}]) of
        {ok,  #file_info{mtime=Time}} -> [Time|Acc];
        {error, _} -> Acc
    end.

%% ===================================================================
%% Free port
%% ===================================================================

free_port() ->
    %% See https://en.wikipedia.org/wiki/Ephemeral_port
    Min = 49152,
    Max = 65535,
    RandSeed = rand:seed(exs64),
    Attempts = 0,
    rand_free_port(Min, Max, RandSeed, Attempts).

rand_free_port(Min, Max, Seed, Attempts) ->
    maybe_too_many_attempts(Attempts),
    {RandX, NextSeed} = rand:uniform_s(Max - Min, Seed),
    Port = Min + RandX,
    case gen_tcp:connect("localhost", Port, [], 100) of
        {ok, Sock} ->
            gen_tcp:close(Sock),
            rand_free_port(Min, Max, NextSeed, Attempts + 1);
        {error, timeout} ->
            rand_free_port(Min, Max, NextSeed, Attempts + 1);
        {error, econnrefused} ->
            Port
    end.

-define(max_free_port_attempts, 100).

maybe_too_many_attempts(N) when N >= ?max_free_port_attempts ->
    error(too_many_free_port_attempts);
maybe_too_many_attempts(_) ->
    ok.

%% ===================================================================
%% Format dir timetamp
%% ===================================================================

format_dir_timestamp(Time) ->
    Now = guild_run:timestamp_to_now(Time),
    {{Y, M, D}, {H, Mn, S}} = calendar:now_to_universal_time(Now),
    io_lib:format("~b~2..0b~2..0bT~2..0b~2..0b~2..0bZ", [Y, M, D, H, Mn, S]).
