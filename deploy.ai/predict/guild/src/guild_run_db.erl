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

-module(guild_run_db).

-behavior(e2_service).

-export([start_link/0, open/1, open/2, log_series_values/2,
         log_flags/2, log_attrs/2, log_output/2, series/2,
         series_keys/1, flags/1, attrs/1, output/1, close/1]).

-export([init/1, handle_msg/3, terminate/2]).

-record(state, {dbs}).

-define(run_db_name, "run.db").

-define(default_series_encoding, 1).
-define(compressed_series_encoding, 2).

-define(NULL_ENC, <<255,255,255,255,255,255,255,255>>).

%% ===================================================================
%% Start / init
%% ===================================================================

start_link() ->
    e2_service:start_link(?MODULE, [], [registered]).

init([]) ->
    {ok, #state{dbs=dict:new()}}.

%% ===================================================================
%% API
%% ===================================================================

open(RunDir) ->
    open(RunDir, []).

open(RunDir, Options) ->
    e2_service:call(?MODULE, {open, RunDir, Options}).

log_series_values(RunDir, Vals) ->
    e2_service:call(?MODULE, {op, RunDir, {log_series_vals, Vals}}).

log_flags(RunDir, Flags) ->
    e2_service:call(?MODULE, {op, RunDir, {log_flags, Flags}}).

log_attrs(RunDir, Attrs) ->
    e2_service:call(?MODULE, {op, RunDir, {log_attrs, Attrs}}).

log_output(RunDir, Output) ->
    e2_service:call(?MODULE, {op, RunDir, {log_output, Output}}).

series(RunDir, Pattern) ->
    e2_service:call(?MODULE, {op, RunDir, {series, Pattern}}).

series_keys(RunDir) ->
    e2_service:call(?MODULE, {op, RunDir, series_keys}).

flags(RunDir) ->
    e2_service:call(?MODULE, {op, RunDir, flags}).

attrs(RunDir) ->
    e2_service:call(?MODULE, {op, RunDir, attrs}).

output(RunDir) ->
    e2_service:call(?MODULE, {op, RunDir, output}).

close(RunDir) ->
    e2_service:call(?MODULE, {close, RunDir}).

%% ===================================================================
%% Dispatch
%% ===================================================================

handle_msg({open, RunDir, Options}, _From, State) ->
    handle_open(RunDir, Options, State);
handle_msg({close, RunDir}, _From, State) ->
    handle_close(RunDir, State);
handle_msg({op, RunDir, Op}, _From, State) ->
    handle_db_op(RunDir, Op, State);
handle_msg({'EXIT', MaybeDb, normal}, noreply, State) ->
    handle_db_exit(MaybeDb, State).

handle_db_op(RunDir, Op, State) ->
    case find_db(RunDir, State) of
        {ok, Db} -> {reply, db_op(Db, Op), State};
        error -> {reply, {error, {nodb, RunDir}}, State}
    end.

find_db(RunDir, #state{dbs=Dbs}) ->
    dict:find(RunDir, Dbs).

%% ===================================================================
% Open
%% ===================================================================

handle_open(RunDir, Options, State) ->
    {Reply, Next} = ensure_opened(RunDir, Options, State),
    {reply, Reply, Next}.

ensure_opened(RunDir, Options, State) ->
    case is_db_open(RunDir, State) of
        true -> {ok, State};
        false -> try_open_db(RunDir, Options, State)
    end.

is_db_open(RunDir, #state{dbs=Dbs}) ->
    dict:is_key(RunDir, Dbs).

try_open_db(RunDir, Opts, State) ->
    Path = db_path(RunDir),
    Exists = filelib:is_file(Path),
    Create = proplists:get_bool(create_if_missing, Opts),
    maybe_open_db(Exists, Create, Path, RunDir, State).

db_path(RunDir) ->
    guild_rundir:guild_file(RunDir, ?run_db_name).

maybe_open_db(_Exists=true, _Create, Path, RunDir, State) ->
    open_db(Path, [], RunDir, State);
maybe_open_db(_Exists=false, _Create=true, Path, RunDir, State) ->
    open_db(Path, [init_schema], RunDir, State);
maybe_open_db(_Exsts=false, _Create=false, _Path, _RunDir, State) ->
    {{error, missing}, State}.

open_db(Path, Opts, RunDir, State) ->
    ok = filelib:ensure_dir(Path),
    Db = sqlite3_open_db(Path),
    maybe_init_schema(proplists:get_bool(init_schema, Opts), Db),
    {ok, add_db(RunDir, Db, State)}.

sqlite3_open_db(File) ->
    {ok, Db} = sqlite3:open(anonymous, [{file, File}]),
    Db.

maybe_init_schema(true, Db) ->
    SQL =
        "create table if not exists attr ("
        "    name text primary key,"
        "    val text);"
        "create table if not exists flag ("
        "    name text primary key,"
        "    val);"
        "create table if not exists series ("
        "    key_hash integer,"
        "    time integer,"
        "    encoding integer,"
        "    data,"
        "    primary key (key_hash, time));"
        "create table if not exists series_key ("
        "    key text primary key,"
        "    hash integer);"
        "create table if not exists output ("
        "    time integer,"
        "    stream integer,"
        "    val text);",
    [ok, ok, ok, ok, ok]
        = sqlite3:sql_exec_script_timeout(Db, SQL, infinity);
maybe_init_schema(false, _Db) ->
    ok.

add_db(RunDir, Db, #state{dbs=Dbs}=S) ->
    S#state{dbs=dict:store(RunDir, Db, Dbs)}.

%% ===================================================================
%% DB ops
%% ===================================================================

db_op(Db, {log_series_vals, Vals}) -> log_series_values_(Db, Vals);
db_op(Db, {log_flags, Flags})      -> log_flags_(Db, Flags);
db_op(Db, {log_attrs, Attrs})      -> log_attrs_(Db, Attrs);
db_op(Db, {log_output, Output})    -> log_output_(Db, Output);
db_op(Db, {series, Pattern})       -> series_(Db, Pattern);
db_op(Db, series_keys)             -> series_keys_(Db);
db_op(Db, flags)                   -> flags_(Db);
db_op(Db, attrs)                   -> attrs_(Db);
db_op(Db, output)                  -> output_(Db).

%% -------------------------------------------------------------------
%% Log series vals
%% -------------------------------------------------------------------

log_series_values_(_Db, []) -> ok;
log_series_values_(Db, Vals) ->
    case update_series_key_hashes(Db, Vals) of
        ok -> insert_series_values(Db, Vals);
        {error, Error} -> {error, Error}
    end.

update_series_key_hashes(Db, SeriesVals) ->
    Vals = [{Key, hash_key(Key)} || {Key, _} <- SeriesVals],
    SQL = ["insert or ignore into series_key values ",
           sql_arg_placeholders(Vals)],
    exec_insert(Db, SQL, sql_arg_vals(Vals, [str, int])).

hash_key(Key) -> erlang:crc32(Key).

insert_series_values(Db, RawVals) ->
    maybe_insert_series_values(Db, encode_series_values(RawVals)).

encode_series_values(Vals) ->
    lists:foldl(fun encode_series_values_acc/2, [], Vals).

encode_series_values_acc({Key, [[T0, _, _]|_]=Vals}, Acc) ->
    {Encoding, Data} = encode_values(Vals),
    [{hash_key(Key), T0, Encoding, Data}|Acc];
encode_series_values_acc({_Key, []}, Acc) ->
    Acc.

encode_values(Vals) ->
    {?default_series_encoding, default_series_encode(Vals)}.

default_series_encode(Vals) ->
    [encode_tsv(Time, Step, Val) || [Time, Step, Val] <- Vals].

encode_tsv(Time, Step, Val) ->
    <<Time:64, Step:64, (encode_series_value(Val)):8/binary>>.

encode_series_value(null) -> ?NULL_ENC;
encode_series_value(Val) -> <<Val/float>>.

maybe_insert_series_values(_Db, []) -> ok;
maybe_insert_series_values(Db, Vals) ->
    SQL = ["insert or ignore into series values ",
           sql_arg_placeholders(Vals)],
    exec_insert(Db, SQL, sql_arg_vals(Vals, [int, int, int, blob])).

%% -------------------------------------------------------------------
%% Log flags
%% -------------------------------------------------------------------

log_flags_(_Db, []) -> ok;
log_flags_(Db, Flags) ->
    SQL = ["insert or replace into flag values ", sql_arg_placeholders(Flags)],
    exec_insert(Db, SQL, sql_arg_vals(Flags, [str, str])).

%% -------------------------------------------------------------------
%% Log attrs
%% -------------------------------------------------------------------

log_attrs_(_Db, []) -> ok;
log_attrs_(Db, Attrs) ->
    SQL = ["insert or replace into attr values ",
           sql_arg_placeholders(Attrs)],
    exec_insert(Db, SQL, sql_arg_vals(Attrs, [str, str])).

%% -------------------------------------------------------------------
%% Log output
%% -------------------------------------------------------------------

log_output_(Db, Output) ->
    lists:foreach(fun(Line) -> log_output_line(Db, Line) end, Output).

log_output_line(Db, {Time, Stream, Val}) ->
    SQL = "insert into output values (?, ?, ?)",
    exec_insert(Db, SQL, [Time, atom_to_list(Stream), iolist_to_binary(Val)]).

%% -------------------------------------------------------------------
%% Flags
%% -------------------------------------------------------------------

flags_(Db) ->
    SQL = "select name, val from flag order by name",
    case exec_select(Db, SQL) of
        {ok, {_, Rows}} -> {ok, Rows};
        {error, Error} -> {error, Error}
    end.

%% -------------------------------------------------------------------
%% Attrs
%% -------------------------------------------------------------------

attrs_(Db) ->
    SQL = "select name, val from attr order by name",
    case exec_select(Db, SQL) of
        {ok, {_, Rows}} -> {ok, Rows};
        {error, Error} -> {error, Error}
    end.

%% -------------------------------------------------------------------
%% Series
%% -------------------------------------------------------------------

series_(Db, Pattern) ->
    series_for_hashes(series_key_hashes_for_pattern(Db, Pattern), Db).

series_key_hashes_for_pattern(Db, Pattern) ->
    case series_key_hashes(Db) of
        {ok, Hashes} -> {ok, filter_key_hashes(Hashes, Pattern)};
        {error, Error} -> {error, Error}
    end.

series_key_hashes(Db) ->
    SQL = "select key, hash from series_key",
    case exec_select(Db, SQL) of
        {ok, {_, Rows}} -> {ok, Rows};
        {error, Error} -> {error, Error}
    end.

filter_key_hashes(Hashes, Pattern) ->
    case re:compile(whole_value_pattern(Pattern)) of
        {ok, Re} -> filter_key_hashes_re(Hashes, Re);
        {error, _} -> []
    end.

whole_value_pattern(Pattern) ->
    [$^, Pattern, $$].

filter_key_hashes_re(Hashes, Re) ->
    lists:filter(fun({Key, _Hash}) -> match_re(Key, Re) end, Hashes).

match_re(Str, Re) ->
    re:run(Str, Re, [{capture, none}]) == match.

series_for_hashes({ok, []}, _Db) ->
    {ok, []};
series_for_hashes({ok, Hashes}, Db) ->
    SQL =
        io_lib:format(
          "select key_hash, time, encoding, data from series "
          "where key_hash in (~s) order by key_hash, time",
          [key_hash_list(Hashes)]),
    case exec_select(Db, SQL) of
        {ok, {_, Rows}} -> {ok, group_series(Rows, Hashes)};
        {error, Error} -> {error, Error}
    end;
series_for_hashes({error, Error}, _Db) ->
    {error, Error}.

key_hash_list(Hashes) ->
    guild_util:list_join([integer_to_list(Hash) || {_Key, Hash} <- Hashes], ",").

group_series(Rows, Hashes) ->
    replace_hashes_with_keys(group_series(Rows, undefined, [], []), Hashes).

group_series([{Key, _T0, Encoding, {blob, Data}}|Rest], Key, Vals0, Acc) ->
    Vals = apply_decoded_series_vals(Encoding, Data, Vals0),
    group_series(Rest, Key, Vals, Acc);
group_series([{NextKey, _, _, _}|_]=Series, Key, Vals, Acc) ->
    group_series(Series, NextKey, [], finalize_series_group(Key, Vals, Acc));
group_series([], Key, Vals, Acc) ->
    lists:reverse(finalize_series_group(Key, Vals, Acc)).

apply_decoded_series_vals(_Encoding, <<>>, Vals) ->
    Vals;
apply_decoded_series_vals(Encoding, Data, Vals) ->
    {Decoded, Rest} = decode_series_vals(Encoding, Data),
    apply_decoded_series_vals(Encoding, Rest, [Decoded|Vals]).

decode_series_vals(?default_series_encoding, Data) -> decode_tsv(Data).

decode_tsv(<<Time:64, Steps:64, Val:8/binary, Rest/binary>>) ->
    {[Time, Steps, decode_series_value(Val)], Rest}.

decode_series_value(?NULL_ENC) -> null;
decode_series_value(<<F/float>>) -> F.

finalize_series_group(undefined, [], Acc) -> Acc;
finalize_series_group(Key, Vals, Acc) ->
    [{Key, lists:reverse(Vals)}|Acc].

replace_hashes_with_keys(Series, Hashes) ->
    Keys = dict:from_list([{Hash, Key} || {Key, Hash} <- Hashes]),
    [{dict:fetch(Hash, Keys), Vals} || {Hash, Vals} <- Series].

%% -------------------------------------------------------------------
%% Series keys
%% -------------------------------------------------------------------

series_keys_(Db) ->
    SQL = "select key from series_key",
    case exec_select(Db, SQL) of
        {ok, {_, Rows}} -> {ok, [Name || {Name} <- Rows]};
        {error, Error} -> {error, Error}
    end.

%% -------------------------------------------------------------------
%% Output
%% -------------------------------------------------------------------

output_(Db) ->
    SQL = "select time, stream, val from output order by time",
    case exec_select(Db, SQL) of
        {ok, {_, Rows}} -> {ok, output_from_rows(Rows)};
        {error, Error} -> {error, Error}
    end.

output_from_rows(Rows) ->
    [{Time, binary_to_stream(Stream), Val}
     || {Time, Stream, Val} <- Rows].

binary_to_stream(<<"stdout">>) -> stdout;
binary_to_stream(<<"stderr">>) -> stderr;
binary_to_stream(Other)        -> Other.

%% ===================================================================
%% Close
%% ===================================================================

handle_close(RunDir, State) ->
    Next = try_close_db(RunDir, State),
    {reply, ok, Next}.

try_close_db(RunDir, State) ->
    case find_db(RunDir, State) of
        {ok, Db} ->
            close_db(Db),
            remove_db(RunDir, State);
        error ->
            State
    end.

close_db(Db) ->
    ok = sqlite3:close(Db).

remove_db(RunDir, #state{dbs=Dbs}=S) ->
    S#state{dbs=dict:erase(RunDir, Dbs)}.

%% ===================================================================
%% Db exit
%% ===================================================================

handle_db_exit(Db, State) ->
    {noreply, remove_db_value(Db, State)}.

remove_db_value(Db, #state{dbs=Dbs}=S) ->
    F = fun(_, Val) -> Val /= Db end,
    S#state{dbs=dict:filter(F, Dbs)}.

%% ===================================================================
%% Shutdown
%% ===================================================================

terminate(_Reason, State) ->
    close_all(State).

close_all(#state{dbs=Dbs}) ->
    dict:map(fun(_, Db) -> close_db(Db) end, Dbs).

%% ===================================================================
%% Helpers
%% ===================================================================

exec_insert(Db, SQL, Args) ->
    try_insert(Db, SQL, Args, 5).

try_insert(_Db, _SQL, _Args, 0) ->
    {error, locked};
try_insert(Db, SQL, Args, N) ->
    case sqlite3:sql_exec_timeout(Db, SQL, Args, infinity) of
        {rowid, _} -> ok;
        {error, 5, _LockedMsg} ->
            try_insert_again(Db, SQL, Args, N);
        {error, 8, _ReadOnlyDbMsg} ->
            {error, read_only}
    end.

try_insert_again(Db, SQL, Args, N) ->
    random_wait(),
    try_insert(Db, SQL, Args, N - 1).

exec_select(Db, SQL) ->
    exec_select(Db, SQL, []).

exec_select(Db, SQL, Args) ->
    try_select(Db, SQL, Args, 5).

try_select(_Db, _SQL, _Args, 0) ->
    {error, locked};
try_select(Db, SQL, Args, N) ->
    case sqlite3:sql_exec_timeout(Db, SQL, Args, infinity) of
        [{columns, Cols}, {rows, Rows}] ->
            {ok, {Cols, Rows}};
        {error, 5, _LockedMsg} ->
            try_select_again(Db, SQL, Args, N);
        %% Curious error interface coming from sqlite3
        [{columns, _}, {rows, _}, {error, 5, _LockedMsg}] ->
            try_select_again(Db, SQL, Args, N)
    end.

try_select_again(Db, SQL, Args, N) ->
    random_wait(),
    try_select(Db, SQL, Args, N - 1).

random_wait() ->
    WaitMs = erlang:phash2(erlang:system_time(), 250),
    timer:sleep(WaitMs).

sql_arg_placeholders([Val]) ->
    arg_placeholder_group(Val);
sql_arg_placeholders(Vals) ->
    Groups = [arg_placeholder_group(Val) || Val <- Vals],
    guild_util:list_join(Groups, ",").

arg_placeholder_group({_, _}) -> "(?,?)";
arg_placeholder_group({_, _, _}) -> "(?,?,?)";
arg_placeholder_group({_, _, _, _}) -> "(?,?,?,?)".

sql_arg_vals(Vals, Types) ->
    sql_arg_vals(Vals, Types, []).

sql_arg_vals([Val|Rest], Types, Acc) ->
    sql_arg_vals(Rest, Types, apply_sql_val_acc(Val, Types, Acc));
sql_arg_vals([], _Types, Acc) ->
    lists:reverse(Acc).

apply_sql_val_acc({X1, X2}, [T1, T2], Acc) ->
    [cvt(X2, T2), cvt(X1, T1)|Acc];
apply_sql_val_acc({X1, X2, X3}, [T1, T2, T3], Acc) ->
    [cvt(X3, T3), cvt(X2, T2), cvt(X1, T1)|Acc];
apply_sql_val_acc({X1, X2, X3, X4}, [T1, T2, T3, T4], Acc) ->
    [cvt(X4, T4), cvt(X3, T3), cvt(X2, T2), cvt(X1, T1)|Acc].

cvt(S, str)   -> iolist_to_binary(S);
cvt(I, int)   -> I;
cvt(F, float) -> F;
cvt(B, blob)  -> {blob, iolist_to_binary(B)}.
