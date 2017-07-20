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

-module(guild_tensorflow_port).

-behavior(e2_service).

-export([start_link/0, test_protocol/0, read_image/2, load_model/1,
         load_project_model/2, run_model/2, run_model/3,
         run_project_model/3, run_project_model/4,
         project_model_info/2, project_model_stats/2]).

-export([init/1, handle_msg/3]).

-record(state, {port}).

%% ===================================================================
%% Start / init
%% ===================================================================

start_link() ->
    e2_service:start_link(?MODULE, [], [registered]).

init([]) ->
    {ok, Port} = port_io:start_link(port_exe()),
    {ok, #state{port=Port}}.

port_exe() ->
    guild_app:priv_bin("tensorflow-port").

%% ===================================================================
%% API
%% ===================================================================

test_protocol() ->
    e2_service:call(?MODULE, {call, test_protocol}).

read_image(RunDir, Index) ->
    e2_service:call(?MODULE, {call, {read_image, RunDir, Index}}).

load_model(ModelPath) ->
    e2_service:call(?MODULE, {call, {load_model, ModelPath}}).

load_project_model(Project, Run) ->
    load_model(project_model_path(Project, Run)).

run_model(ModelPath, Request) ->
    run_model(ModelPath, Request, []).

run_model(ModelPath, Request, Opts) ->
    Call = run_model_call(ModelPath, Request, Opts),
    e2_service:call(?MODULE, {call, Call}).

run_model_call(ModelPath, Request, Opts) ->
    case proplists:get_bool(with_stats, Opts) of
        true  -> {run_model_with_stats, ModelPath, Request};
        false -> {run_model, ModelPath, Request}
    end.

run_project_model(Project, Run, Request) ->
    run_project_model(Project, Run, Request, []).

run_project_model(Project, Run, Request, Opts) ->
    ModelPath = project_model_path(Project, Run),
    run_model(ModelPath, Request, Opts).

project_model_path(_Project, Run) ->
    filename:join(guild_run:dir(Run), "model/export").

project_model_info(Project, Run) ->
    ModelPath = project_model_path(Project, Run),
    e2_service:call(?MODULE, {call, {model_info, ModelPath}}).

project_model_stats(Project, Run) ->
    ModelPath = project_model_path(Project, Run),
    e2_service:call(?MODULE, {call, {model_stats, ModelPath}}).

%% ===================================================================
%% Message dispatch
%% ===================================================================

handle_msg({call, Call}, From, State) ->
    handle_port_call(Call, From, State);
handle_msg({Port, {exit_status, Status}}, noreply, #state{port=Port}) ->
    {stop, stop_reason(Status)}.

stop_reason(0) -> normal;
stop_reason(N) -> {exit_status, N}.

%% ===================================================================
%% Port interface
%% ===================================================================

handle_port_call(Call, _From, State) ->
    Ref = send_port_request(Call, State),
    Resp = recv_port_response(Ref, Call, State),
    {reply, Resp, State}.

send_port_request(Call, State) ->
    Ref = request_ref(),
    {Cmd, Args} = encode_request(Call),
    port_write(encode_uint(Ref), State),
    port_write(encode_ushort(Cmd), State),
    port_write(encode_uint(length(Args)), State),
    port_write(encode_args(Args), State),
    Ref.

encode_args(Args) ->
    [encode_string(Arg) || Arg <- Args].

-define(max_ref, 4294967296).

request_ref() -> rand:uniform(?max_ref).

port_write(Data, #state{port=Port}) ->
    ok = file:write(Port, Data).

recv_port_response(Ref, Call, State) ->
    Ref = decode_uint(port_read(4, State)),
    Status = decode_ushort(port_read(2, State)),
    PartsLen = decode_uint(port_read(4, State)),
    Parts = recv_response_parts(PartsLen, State),
    decode_response(Call, Status, Parts).

recv_response_parts(N, State) ->
    recv_response_parts_acc(N, State, []).

recv_response_parts_acc(0, _State, Acc) ->
    lists:reverse(Acc);
recv_response_parts_acc(N, State, Acc) ->
    Len = decode_ulong(port_read(8, State)),
    Part = port_read(Len, State),
    recv_response_parts_acc(N - 1, State, [Part|Acc]).

port_read(Len, #state{port=Port}) ->
    {ok, Data} = file:read(Port, Len),
    Data.

%% ===================================================================
%% Data encoders / decoders
%% ===================================================================

encode_ushort(I) -> <<I:2/integer-unit:8>>.

encode_uint(I) -> <<I:4/integer-unit:8>>.

encode_ulong(I) -> <<I:8/integer-unit:8>>.

encode_string(S) -> [encode_ulong(iolist_size(S)), S].

decode_ushort(Bin) -> <<I:2/integer-unit:8>> = Bin, I.

decode_uint(Bin) -> <<I:4/integer-unit:8>> = Bin, I.

decode_ulong(Bin) -> <<I:8/integer-unit:8>> = Bin, I.

%% ===================================================================
%% Request encoders
%% ===================================================================

%% Keep in sync with priv/bin/tensorflow-port2

-define(CMD_TEST_PROTOCOL,        0).
-define(CMD_READ_IMAGE,           1).
-define(CMD_LOAD_MODEL,           2).
-define(CMD_RUN_MODEL,            3).
-define(CMD_MODEL_INFO,           4).
-define(CMD_MODEL_STATS,          5).
-define(CMD_RUN_MODEL_WITH_STATS, 6).

encode_request({read_image, Dir, Index}) ->
    {?CMD_READ_IMAGE, [Dir, integer_to_list(Index)]};
encode_request({load_model, ModelPath}) ->
    {?CMD_LOAD_MODEL, [ModelPath]};
encode_request({run_model, ModelPath, Request}) ->
    {?CMD_RUN_MODEL, [ModelPath, Request]};
encode_request({run_model_with_stats, ModelPath, Request}) ->
    {?CMD_RUN_MODEL_WITH_STATS, [ModelPath, Request]};
encode_request({model_info, ModelPath}) ->
    {?CMD_MODEL_INFO, [ModelPath]};
encode_request({model_stats, ModelPath}) ->
    {?CMD_MODEL_STATS, [ModelPath]};
encode_request(test_protocol) ->
    {?CMD_TEST_PROTOCOL, ["e pluribus unum"]}.

%% ===================================================================
%% Response decoders
%% ===================================================================

-define(STATUS_OK,    0).
-define(STATUS_ERROR, 1).

decode_response({read_image, _, _}, ?STATUS_OK, Parts) ->
    {ok, decode_image(Parts)};
decode_response({load_model, _}, ?STATUS_OK, []) ->
    ok;
decode_response({run_model, _, _}, ?STATUS_OK, [Resp]) ->
    {ok, Resp};
decode_response({run_model_with_stats, _, _}, ?STATUS_OK, [Resp]) ->
    {ok, Resp};
decode_response({model_info, _}, ?STATUS_OK, [Resp]) ->
    {ok, Resp};
decode_response({model_stats, _}, ?STATUS_OK, [Resp]) ->
    {ok, Resp};
decode_response(test_protocol, ?STATUS_OK, Parts) ->
    ok = check_test_response(Parts);
decode_response(_Call, ?STATUS_ERROR, [Err]) ->
    {error, Err}.

check_test_response([<<"e pluribus unum">>]) -> ok;
check_test_response(Other) -> {error, Other}.

decode_image([File, Tag, DimEnc, Type, Bytes]) ->
    Dim = decode_image_dim(DimEnc),
    #{file => File,
      tag => Tag,
      dim => Dim,
      type => Type,
      bytes => Bytes}.

decode_image_dim(Enc) ->
    [H, W, D] = re:split(Enc, " ", []),
    {binary_to_integer(H), binary_to_integer(W), binary_to_integer(D)}.
