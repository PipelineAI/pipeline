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

-module(port_io).

-behavior(gen_server).

-export([start_link/1]).

-export([init/1, handle_info/2, handle_cast/2, handle_call/3,
         terminate/2, code_change/3]).

-record(state, {port, buf, buflen, readers}).

%% ===================================================================
%% Start / init
%% ===================================================================

start_link(Exe) ->
    gen_server:start_link(?MODULE, [Exe], []).

init([Exe]) ->
    Port = erlang:open_port({spawn_executable, Exe}, [binary, exit_status]),
    {ok, #state{port=Port, buf=[], buflen=0, readers=[]}}.

handle_info({io_request, From, Ref, Req}, State) ->
    handle_io_request(Req, From, Ref, State);
handle_info({Port, {data, Data}}, #state{port=Port}=State) ->
    handle_data(Data, State);
handle_info({Port, {exit_status, 0}}, #state{port=Port}=State) ->
    {stop, normal, State};
handle_info({Port, {exit_status, N}}, #state{port=Port}=State) ->
    {stop, {exit_status, N}, State}.

%% ===================================================================
%% IO request
%% ===================================================================

handle_io_request({put_chars, latin1, Chars}, From, Ref, State) ->
    send_to_port(Chars, State),
    send_io_reply(From, Ref, ok),
    {noreply, State};
handle_io_request({get_chars, _Prompt, Len}, From, Ref, State) ->
    Next = apply_readers(add_reader(Len, From, Ref, State)),
    {noreply, Next}.

send_to_port(Data, #state{port=Port}) ->
    erlang:port_command(Port, Data).

send_io_reply(From, Ref, Reply) ->
    erlang:send(From, {io_reply, Ref, Reply}).

add_reader(Len, From, Ref, #state{readers=Readers}=S) ->
    Reader = {Len, From, Ref},
    S#state{readers=Readers++[Reader]}.

apply_readers(State) ->
    case pop_available_reader(State) of
        {Reader, NextState} ->
            apply_readers(apply_reader(Reader, NextState));
        false ->
            State
    end.

pop_available_reader(#state{readers=[]}) -> false;
pop_available_reader(#state{readers=[Reader|Rest]}=State) ->
    case data_available(Reader, State) of
        true -> {Reader, State#state{readers=Rest}};
        false -> false
    end.

data_available({Len, _, _}, #state{buflen=Available}) ->
    Len =< Available.

apply_reader({Len, From, Ref}, #state{buf=Buf}=State) ->
    {Chars, NextBuf} = get_chars(Len, Buf),
    send_io_reply(From, Ref, {ok, Chars}),
    set_buffer(NextBuf, State).

get_chars(Len, [B1|RestBuf]) when Len =< byte_size(B1) ->
    {Chars, RestChars} = split_binary(B1, Len),
    {Chars, get_chars_finalize_buf(RestChars, RestBuf)};
get_chars(Len, [B1, B2|RestBuf]) ->
    get_chars(Len, [<<B1/binary, B2/binary>>|RestBuf]).

get_chars_finalize_buf(<<>>, Buf) -> Buf;
get_chars_finalize_buf(Chars, Buf) -> [Chars|Buf].

set_buffer(Buf, S) ->
    S#state{buf=Buf, buflen=iolist_size(Buf)}.

%% ===================================================================
%% Handle data
%% ===================================================================

handle_data(Data, State) ->
    Next = apply_readers(buffer(Data, State)),
    {noreply, Next}.

buffer(Data, #state{buf=Buf, buflen=Len}=S) ->
    S#state{buf=Buf++[Data], buflen=Len+byte_size(Data)}.

%% ===================================================================
%% gen_server boilerplate
%% ===================================================================

handle_cast(_Msg, State) ->
    {noreply, State}.

handle_call(_Msg, _From, State) ->
    {noreply, State}.

terminate(_Reason, _State) ->
    ok.

code_change(_OldVsn, State, _Extra) ->
    {ok, State}.
