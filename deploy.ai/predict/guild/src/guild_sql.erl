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

-module(guild_sql).

-export([exec_insert/3, exec_delete/3, exec_select/2, exec_select/3,
         sql_arg_placeholders/1, sql_arg_vals/2]).

%% ===================================================================
%% Exec insert
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

%% ===================================================================
%% Exec delete
%% ===================================================================

exec_delete(Db, SQL, Args) ->
    case sqlite3:sql_exec_timeout(Db, SQL, Args, infinity) of
        ok -> ok
    end.

%% ===================================================================
%% Exec select
%% ===================================================================

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

%% ===================================================================
%% SQL arg placeholders
%% ===================================================================

sql_arg_placeholders([Val]) ->
    arg_placeholder_group(Val);
sql_arg_placeholders(Vals) ->
    Groups = [arg_placeholder_group(Val) || Val <- Vals],
    guild_util:list_join(Groups, ",").

arg_placeholder_group({_, _}) -> "(?,?)";
arg_placeholder_group({_, _, _}) -> "(?,?,?)";
arg_placeholder_group({_, _, _, _}) -> "(?,?,?,?)".

%% ===================================================================
%% SQL arg vals
%% ===================================================================

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
