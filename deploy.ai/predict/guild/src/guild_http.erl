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

-module(guild_http).

-export([start_link/3]).

-export([init_mime_types/0, ok_html/1, ok_html/2, ok_text/1,
         ok_json/1, ok_no_content/0, error_html/1, redirect/1,
         redirect/2, not_found/0, not_found/1, bad_request/0,
         bad_request/1, internal_error/0, internal_error/1,
         validate_params/2, validate_params/3]).

%% ===================================================================
%% Start
%% ===================================================================

start_link(Port, App, Opts) ->
    psycho_server:start_link(Port, App, Opts).

%% ===================================================================
%% Init mime types
%% ===================================================================

init_mime_types() ->
    Types = psycho_mime:load_types(mime_types_source()),
    psycho_mime:init(Types).

mime_types_source() ->
    filename:join(guild_util:priv_dir(psycho), "mime.types").

%% ===================================================================
%% Response helpers
%% ===================================================================

ok_html(Page) ->
    ok_html(Page, []).

ok_html(Page, ExtraHeaders) ->
    {{200, "OK"}, [{"Content-Type", "text/html"}|ExtraHeaders], Page}.

ok_text(Page) ->
    {{200, "OK"}, [{"Content-Type", "text/plain"}], Page}.

ok_json(JSON) ->
    {{200, "OK"}, [{"Content-Type", "application/json"}], JSON}.

ok_no_content() ->
    {{204, "No Content"}, [], []}.

error_html(Page) ->
    {{500, "Internal Error"}, [{"Content-Type", "text/html"}], Page}.

redirect(Location) ->
    redirect(Location, []).

redirect(Location, Headers) ->
    {{302, "See Other"}, [{"Location", Location}|Headers]}.

not_found() ->
    {{404, "Not Found"}, [{"Content-Type", "text/plain"}], "Not found\n"}.

not_found(Page) ->
    {{404, "Not Found"}, [{"Content-Type", "text/html"}], Page}.

bad_request() ->
    bad_request("Bad request\n").

bad_request(Msg) ->
    {{400, "Bad request"}, [{"Content-Type", "text/plain"}], Msg}.

internal_error() ->
    internal_error("Internal error\n").

internal_error(Msg) ->
    {{500, "Internal Error"}, [{"Content-Type", "text/plain"}], Msg}.

%% ===================================================================
%% Param validation helpers
%% ===================================================================

validate_params(Params, Schema) ->
    validate_params(Params, Schema, fun validate_error_throw_bad_request/1).

validate_error_throw_bad_request(Err) ->
    throw(guild_http:bad_request(psycho_util:format_validate_error(Err))).

validate_params(Params, Schema, HandleError) ->
    case psycho_util:validate(Params, Schema) of
        {ok, Validated} -> Validated;
        {error, Err} -> HandleError(Err)
    end.
