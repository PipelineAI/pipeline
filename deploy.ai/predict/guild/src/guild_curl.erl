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

-module(guild_curl).

-export([get/1, get/2, post/1, post/2]).

get(URL) ->
    get(URL, []).

get(URL, Opts) ->
    exec_cmd(curl_cmd("GET", URL, Opts)).

post(URL) ->
    post(URL, []).

post(URL, Opts) ->
    exec_cmd(curl_cmd("POST", URL, Opts)).

curl_cmd(Method, URL, Opts) ->
    BaseArgs = ["/usr/bin/curl", "-sfS", "-X", Method],
    DataArgs = curl_data_args(Opts),
    HeaderArgs = curl_header_args(Opts),
    OutputArgs = curl_output_args(Opts),
    lists:concat([BaseArgs, DataArgs, HeaderArgs, OutputArgs, [URL]]).

curl_data_args(Opts) ->
    case proplists:get_value(data, Opts) of
        undefined -> [];
        Data -> lists:foldl(fun curl_data_args_acc/2, [], Data)
    end.

curl_data_args_acc({Name, Val}, Acc) ->
    ["-d", Name ++ "=" ++ Val|Acc].

curl_header_args(Opts) ->
    case proplists:get_value(headers, Opts) of
        undefined -> [];
        Headers -> lists:foldl(fun curl_headers_args_acc/2, [], Headers)
    end.

curl_headers_args_acc({Name, Val}, Acc) ->
    ["-H", Name ++ ": " ++ Val|Acc].

curl_output_args(Opts) ->
    case proplists:get_value(file, Opts) of
        undefined -> [];
        File -> ["-o", File]
    end.

exec_cmd(Cmd) ->
    handle_exec(exec:run(Cmd, [sync, stdout, stderr])).

handle_exec({ok, []}) ->
    {ok, <<>>};
handle_exec({ok, [{stdout, Out}]}) ->
    {ok, Out};
handle_exec({error, [{exit_status, Status}, {stderr, Err}]}) ->
    {error, {Status bsr 8, Err}}.
