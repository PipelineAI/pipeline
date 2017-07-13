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

-module(guild_delete_eval_cmd).

-export([parser/0, main/2]).

%% ===================================================================
%% Parser
%% ===================================================================

parser() ->
    cli:parser(
      "guild delete-eval",
      "[OPTION]... EVALDIR",
      "Remove an evaluation by deleting EVALDIR.\n"
      "\n"
      "Use 'guild list-evals' to list evaluations that can be used for "
      "EVALDIR.\n",
      [],
      [{pos_args, 1}]).

%% ===================================================================
%% Main
%% ===================================================================

main(_Opts, [EvalDir]) ->
    maybe_remove_evaldir(guild_eval:is_eval(EvalDir), EvalDir).

maybe_remove_evaldir(true, Dir) ->
    remove_dir(Dir);
maybe_remove_evaldir(false, Dir) ->
    print_invalid_evaldir(Dir).

remove_dir(Dir) ->
    guild_cli:out("Deleting ~s~n", [Dir]),
    "" = os:cmd(["rm -rf ", quote_dir(Dir)]),
    ok.

quote_dir(Dir) ->
    Escaped = re:replace(Dir, "\"", "\\\\\"", [global, {return, list}]),
    "\"" ++ Escaped ++ "\"".

print_invalid_evaldir(Dir) ->
    guild_cli:cli_error(
      io_lib:format(
        "'~s' is not a Guild evaluation directory~n"
        "Try 'guild list-evals' for a list.",
        [Dir])).
