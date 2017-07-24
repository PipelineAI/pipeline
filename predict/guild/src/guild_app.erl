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

-module(guild_app).

-export([init/0, init_support/1, start_child/1]).

-export([priv_dir/0, priv_dir/1, priv_bin/1, tmp_dir/0, test_dir/0,
         user_dir/0, user_dir/1, pkg_dir/0, set_env/2, get_env/1,
         get_env/2]).

-define(app, guild).

-define(sup_child(M), {M, [{shutdown, infinity}]}).
-define(core_child(M, T), {M, [{shutdown, T}]}).

%% ===================================================================
%% Init app
%% ===================================================================

init() ->
    {ok, supervisors() ++ core_services()}.

supervisors() ->
    [?sup_child(guild_operation_sup),
     ?sup_child(guild_op_sup),
     ?sup_child(guild_optask_sup),
     ?sup_child(guild_http_sup),
     ?sup_child(guild_view_sup),
     ?sup_child(guild_db_sup)].

core_services() ->
    [?core_child(guild_globals,  brutal_kill),
     ?core_child(guild_proc,     100),
     ?core_child(guild_sys,      100),
     ?core_child(guild_run_db,   1000)].

%% ===================================================================
%% Init support
%% ===================================================================

init_support(Multiple) when is_list(Multiple) ->
    lists:foreach(fun init_support/1, Multiple);
init_support(exec) ->
    validate_started(guild_exec:init());
init_support(json) ->
    validate_started(guild_json:init());
init_support({app_child, Child}) ->
    validate_started(start_child(Child)).

validate_started(ok) -> ok;
validate_started({ok, _}) -> ok;
validate_started({error, {already_started, _}}) -> ok;
validate_started(Other) -> error(Other).

%% ===================================================================
%% Start child (add to root supervsor)
%% ===================================================================

start_child(Child) ->
    e2_supervisor:start_child(guild_app, Child).

%% ===================================================================
%% Misc app functions
%% ===================================================================

priv_dir() ->
    guild_util:priv_dir(?app).

priv_dir(Subdir) ->
    filename:join(priv_dir(), Subdir).

priv_bin(Name) ->
    filename:join(priv_dir("bin"), Name).

test_dir() ->
    filename:join(priv_dir(), "test").

tmp_dir() -> "/tmp".

user_dir() ->
    filename:join(os:getenv("HOME"), ".guild").

pkg_dir() ->
    user_dir("pkg").

user_dir(Subdir) ->
    filename:join(user_dir(), Subdir).

set_env(Key, Val) ->
    application:set_env(?app, Key, Val).

get_env(Key) ->
    application:get_env(?app, Key).

get_env(Key, Default) ->
    case get_env(Key) of
        {ok, Val} -> Val;
        undefined -> Default
    end.
