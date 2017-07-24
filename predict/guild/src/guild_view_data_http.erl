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

-module(guild_view_data_http).

-export([create_app/1, app/4]).

-define(default_max_epochs, 400).

%% ===================================================================
%% App
%% ===================================================================

create_app(View) ->
    ViewSettings = guild_view:settings(View),
    psycho_util:dispatch_app(
      {?MODULE, app},
      [method, parsed_path, View, ViewSettings]).

%% ===================================================================
%% Dispatch
%% ===================================================================

app("GET", {"/data/runs", _, _}, View, _) ->
    handle_runs(View);
app("GET", {"/data/series/" ++ Path, _, Params}, View, _) ->
    handle_series(View, Path, Params);
app("GET", {"/data/flags", _, Params}, View, _) ->
    handle_flags(View, Params);
app("GET", {"/data/attrs", _, Params}, View, _) ->
    handle_attrs(View, Params);
app("GET", {"/data/artifacts", _, Params}, View, _) ->
    handle_artifacts(View, Params);
app("GET", {"/data/output", _, Params}, View, _) ->
    handle_output(View, Params);
app("GET", {"/data/compare", _, Params}, View, _) ->
    handle_compare(View, Params);
app("GET", {"/data/sources", _, _}, View, _) ->
    handle_sources(View);
app("GET", {"/data/settings", _, _}, View, _) ->
    handle_settings(View);
app("GET", {"/data/project", _, _}, View, _) ->
    handle_project(View);
app("GET", {"/data/tf/" ++ Path, Qs, _}, _View, Settings) ->
    handle_tf_data(Path, Qs, Settings);
app(_, _, _, _) ->
    guild_http:bad_request().

%% ===================================================================
%% Runs
%% ===================================================================

handle_runs(View) ->
    Runs = guild_view:formatted_runs(View),
    guild_http:ok_json(guild_json:encode(Runs)).

%% ===================================================================
%% Series
%% ===================================================================

handle_series(View, Path, Params) ->
    Run = run_for_params(Params, View),
    Pattern = http_uri:decode(Path),
    Max = max_epochs_for_params(Params),
    Series = guild_data_reader:series(Run, Pattern, Max),
    guild_http:ok_json(guild_json:encode(Series)).

max_epochs_for_params(Params) ->
    Schema = [{"max_epochs", [{any, [integer, "all"]}]}],
    Error = fun max_epochs_validate_error/1,
    case guild_http:validate_params(Params, Schema, Error) of
        []           -> ?default_max_epochs;
        [{_, "all"}] -> all;
        [{_, Max}]   -> Max
    end.

max_epochs_validate_error(_) ->
    throw(
      guild_http:bad_request(
        "max_epochs must be a valid integer or 'all'")).

%% ===================================================================
%% Flags
%% ===================================================================

handle_flags(View, Params) ->
    Run = run_for_params(Params, View),
    Flags = guild_data_reader:flags(Run),
    guild_http:ok_json(guild_json:encode(Flags)).

%% ===================================================================
%% Attrs
%% ===================================================================

handle_attrs(View, Params) ->
    Run = run_for_params(Params, View),
    Attrs = guild_data_reader:attrs(Run),
    guild_http:ok_json(guild_json:encode(Attrs)).

%% ===================================================================
%% Artifacts
%% ===================================================================

handle_artifacts(View, Params) ->
    _Run = run_for_params(Params, View),
    Artifacts = [#{name => <<"foo">>}, #{name => <<"bar">>}],
    guild_http:ok_json(guild_json:encode(Artifacts)).

%% ===================================================================
%% Output
%% ===================================================================

handle_output(View, Params) ->
    Run = run_for_params(Params, View),
    Output = guild_data_reader:output(Run),
    guild_http:ok_json(guild_json:encode(Output)).

%% ===================================================================
%% Compare
%% ===================================================================

handle_compare(View, Params) ->
    Sources = sources_for_params(Params),
    Runs = runs_for_params(Params, View),
    Compare = (catch guild_data_reader:compare(Runs, Sources)),
    handle_compare_result(Compare).

sources_for_params(Params) ->
    Param = proplists:get_value("sources", Params, ""),
    Split = string:tokens(Param, ","),
    lists:usort(Split).

runs_for_params(Params, View) ->
    runs_for_ids(run_ids_for_params(Params), View).

run_ids_for_params(Params) ->
    case proplists:get_value("runs", Params) of
        undefined -> undefined;
        Param -> parse_run_ids(Param)
    end.

parse_run_ids(Str) ->
    Tokens = string:tokens(Str, ","),
    [I || {I, []} <- lists:map(fun string:to_integer/1, Tokens)].

runs_for_ids([], _View) -> [];
runs_for_ids(undefined, View) ->
    guild_view:all_runs(View);
runs_for_ids(Ids, View) ->
    Filter = fun(Run) -> lists:member(guild_run:id(Run), Ids) end,
    lists:filter(Filter, guild_view:all_runs(View)).

handle_compare_result({'EXIT', Err}) ->
    handle_compare_error(Err);
handle_compare_result(Compare) ->
    guild_http:ok_json(guild_json:encode(Compare)).

handle_compare_error({{run_source, Source}, _}) ->
    guild_http:bad_request(["invalid source: ", Source]);
handle_compare_error(Other) ->
    error_logger:error_report({compare_error, Other}),
    guild_http:internal_error().

%% ===================================================================
%% Sources
%% ===================================================================

handle_sources(View) ->
    Sources = all_runs_sources(View),
    guild_http:ok_json(guild_json:encode(Sources)).

all_runs_sources(View) ->
    Runs = guild_view:all_runs(View),
    Keys = guild_data_reader:series_keys(Runs),
    sources_for_series_keys(Keys).

sources_for_series_keys(Keys) ->
    [<<"flags">>, <<"attrs">>|format_series_keys(Keys)].

format_series_keys(Keys) ->
    Sorted = lists:sort(Keys),
    [<<"series/", Key/binary>> || Key <- Sorted].

%% ===================================================================
%% Settings
%% ===================================================================

handle_settings(View) ->
    Settings = guild_view:settings(View),
    guild_http:ok_json(guild_json:encode(Settings)).

%% ===================================================================
%% Project
%% ===================================================================

handle_project(View) ->
    Project = guild_view:project(View),
    guild_http:ok_json(guild_json:encode(Project)).

%% ===================================================================
%% TensorFlow data
%% ===================================================================

handle_tf_data(Path, Qs, #{tensorboard:=#{port:=Port}})
  when is_integer(Port) ->
    handle_tf_data_(Path, Qs, Port);
handle_tf_data(_Path, _Qs, _Settings) ->
    guild_http:internal_error("TensorBoard not running").

handle_tf_data_(Path, Qs, Port) ->
    FullPath = [Path, "?", Qs],
    handle_tf_data_result(guild_tf_data_proxy:data(Port, FullPath)).

handle_tf_data_result({ok, {Status, Headers, Body}}) ->
    {Status, Headers, Body};
handle_tf_data_result({error, Err}) ->
    guild_log:internal("Error reading from tf proxy: ~p~n", [Err]),
    guild_http:internal_error().

%% ===================================================================
%% Shared
%% ===================================================================

run_for_params(Params, View) ->
    guild_view_http:run_for_params(Params, View).
