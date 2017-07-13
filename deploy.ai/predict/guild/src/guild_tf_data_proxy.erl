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

-module(guild_tf_data_proxy).

-export([data/2]).

-record(resp, {sock, status, len, headers, body}).

data(Port, Path) ->
    Opts = [binary, {packet, http_bin}, {active, false}],
    {ok, Sock} = gen_tcp:connect("localhost", Port, Opts),
    Req = request(Path),
    ok = gen_tcp:send(Sock, Req),
    headers(#resp{sock=Sock, headers=[], body=[]}).

request(Path) ->
    [<<"GET /data/">>, Path, <<" HTTP/1.0\r\n\r\n">>].

headers(#resp{sock=Sock}=Resp) ->
    handle_header(gen_tcp:recv(Sock, 0), Resp).

handle_header({ok, {http_response, _, Code, Reason}}, R) ->
    headers(R#resp{status={Code, Reason}});
handle_header({ok, {http_header, _, Name, _, Val}}, R) ->
    headers(maybe_len(Name, Val, header(Name, Val, R)));
handle_header({ok, http_eoh}, R) ->
    body(R);
handle_header({error, Err}, R) ->
    handle_error(Err, R).

header(Name, Val, #resp{headers=Hs}=R) ->
    R#resp{headers=[{ensure_string(Name), Val}|Hs]}.

ensure_string(A) when is_atom(A) -> atom_to_list(A);
ensure_string(B) when is_binary(B) -> binary_to_list(B);
ensure_string(L) when is_list(L) -> L.

maybe_len('Content-Length', Val, R) ->
    R#resp{len=binary_to_integer(Val)};
maybe_len(_, _, R) -> R.

handle_error(Err, #resp{sock=Sock}) ->
    gen_tcp:close(Sock),
    {error, Err}.

body(#resp{sock=Sock, len=Len}=Resp) when Len /= undefined ->
    ok = inet:setopts(Sock, [{packet, raw}]),
    handle_body(gen_tcp:recv(Sock, Len), Resp).

handle_body({ok, Bin}, #resp{len=Len, body=Body}=R) ->
    body(R#resp{len=Len-size(Bin), body=[Bin|Body]});
handle_body({error, closed}, #resp{len=0}=R) ->
    #resp{
       status=Status,
       headers=Headers,
       body=Body} = R,
    {ok, {Status, Headers, lists:reverse(Body)}};
handle_body({error, Err}, R) ->
    handle_error(Err, R).
