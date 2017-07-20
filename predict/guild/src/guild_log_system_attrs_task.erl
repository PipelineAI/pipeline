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
%%
%% guild_log_system_attrs_task
%%
%% Writes system attributes to the rundb for the operation.

-module(guild_log_system_attrs_task).

-behavior(e2_task).

-export([start_link/1]).

-export([init/1, handle_task/1]).

-record(state, {rundir}).

start_link(Op) ->
    e2_task:start_link(?MODULE, [Op], []).

init([Op]) ->
    guild_proc:reg(optask, self()),
    RunDir = guild_op:opdir(Op),
    {ok, #state{rundir=RunDir}}.

handle_task(#state{rundir=RunDir}) ->
    Attrs = sys_attrs() ++ gpu_attrs() ++ tensorflow_attrs(),
    guild_run_db:log_attrs(RunDir, Attrs),
    {stop, normal}.

sys_attrs() ->
    lists:foldl(fun apply_sys_attrs/2, [], guild_sys:system_attrs()).

apply_sys_attrs(Attrs, Acc) ->
    Sorted =
        lists:sort(
          [{atom_to_list(Name), Val}
           || {Name, Val} <- maps:to_list(Attrs)]),
    Sorted ++ Acc.

gpu_attrs() ->
    lists:foldl(fun apply_gpu_attrs/2, [], guild_sys:gpu_attrs()).

apply_gpu_attrs(Attrs, Acc) ->
    #{index:=Index,
      name:=Name,
      memory:=Memory} = Attrs,
    Key = fun(X) -> "gpu" ++ Index ++ "_" ++ X end,
    [{Key("name"), Name},
     {Key("memory"), Memory}
     |Acc].

tensorflow_attrs() ->
    %% TODO: We're cheating here and hard-coding a TF function but
    %% this needs to come from the associated model via an appropriate
    %% lookup.
    Bin = guild_app:priv_bin("tensorflow-attrs"),
    case exec:run(Bin, [stdout, stderr, sync]) of
        {ok, Result} ->
            parse_tensorflow_attrs(proplists:get_value(stdout, Result));
        {error, Result} ->
            guild_log:internal(
              "ERROR reading TensorFlow stats:~n~p~n", [Result]),
            []
    end.

parse_tensorflow_attrs(undefined) ->
    [];
parse_tensorflow_attrs(Out) ->
    [parse_attr(Line) || Line <- split_lines(Out)].

split_lines(Out) -> re:split(Out, "\n", [trim]).

parse_attr(Line) ->
    [Key, Val] = re:split(Line, "=", [{parts, 2}]),
    {Key, Val}.
