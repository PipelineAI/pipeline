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

-module(guild_package_cmd).

-export([parser/0, main/2]).

parser() ->
    cli:parser(
      "guild package",
      "[OPTION]...",
      "Creates a Guild package.",
      package_opts() ++ guild_cmd_support:project_options([flag_support]),
      [{pos_args, 0}]).

package_opts() ->
    [{srcdir, "--srcdir",
      "Use DIR as $srcdir; implies --skip-sources",
      [{metavar, "DIR"}]},
     {skip_sources, "--skip-sources",
      "Don't download, verify, or extract sources", [flag]},
     {skip_package, "--skip-package",
      "Don't modify the current package directory", [flag]},
     {skip_archive, "--skip-archive",
      "Don't create package archive", [flag]},
     {clean, "-c, --clean",
      "Removes sources before building", [flag]},
     {debug, "--debug", "enable debugging", [hidden, flag]}].

%% ===================================================================
%% Main
%% ===================================================================

main(Opts, _Args) ->
    Project = guild_cmd_support:project_from_opts("GuildPkg", Opts),
    guild_app:init_support([exec]),
    Bin = guild_app:priv_bin("guild-package"),
    Args = [Bin, guild_project:dir(Project)],
    Env = package_env(Project, Opts),
    guild_cmd_support:exec_run(Args, [{env, Env}]).

package_env(Project, Opts) ->
    project_env(Project) ++ cmd_env(Opts).

project_env(Project) ->
    Attrs = guild_project:section_attrs(Project, ["package"]),
    [{"package_" ++ Name, Val} || {Name, Val} <- Attrs].

cmd_env(Opts) ->
    guild_cmd_support:env_from_opts(
      [{srcdir, "ALT_SRCDIR"},
       {skip_sources, "SKIP_SOURCES", "1"},
       {skip_package, "SKIP_PACKAGE", "1"},
       {skip_archive, "SKIP_ARCHIVE", "1"},
       {clean, "CLEAN", "1"},
       {debug, "DEBUG", "1"}],
      Opts).
