import os

# Configuration file for jupyterhub.

#------------------------------------------------------------------------------
# Configurable configuration
#------------------------------------------------------------------------------

#------------------------------------------------------------------------------
# LoggingConfigurable configuration
#------------------------------------------------------------------------------

# A parent class for Configurables that log.
#
# Subclasses have a log trait, and the default behavior is to get the logger
# from the currently running Application.

#------------------------------------------------------------------------------
# SingletonConfigurable configuration
#------------------------------------------------------------------------------

# A configurable that only allows one instance.
#
# This class is for classes that should only have one instance of itself or
# *any* subclass. To create and retrieve such a class use the
# :meth:`SingletonConfigurable.instance` method.

#------------------------------------------------------------------------------
# Application configuration
#------------------------------------------------------------------------------

# This is an application.

# The date format used by logging formatters for %(asctime)s
# c.Application.log_datefmt = '%Y-%m-%d %H:%M:%S'

# The Logging format template
# c.Application.log_format = '[%(name)s]%(highlevel)s %(message)s'

# Set the log level by value or name.
# c.Application.log_level = 30

#------------------------------------------------------------------------------
# JupyterHub configuration
#------------------------------------------------------------------------------

# An Application for starting a Multi-User Jupyter Notebook server.

# Grant admin users permission to access single-user servers.
#
# Users should be properly informed if this is enabled.
c.JupyterHub.admin_access = True

# DEPRECATED, use Authenticator.admin_users instead.
# c.JupyterHub.admin_users = set()

# Answer yes to any questions (e.g. confirm overwrite)
c.JupyterHub.answer_yes = True

# Dict of token:username to be loaded into the database.
#
# Allows ahead-of-time generation of API tokens for use by services.
# c.JupyterHub.api_tokens = {}

# Class for authenticating users.
#
# This should be a class with the following form:
#
# - constructor takes one kwarg: `config`, the IPython config object.
#
# - is a tornado.gen.coroutine
# - returns username on success, None on failure
# - takes two arguments: (handler, data),
#   where `handler` is the calling web.RequestHandler,
#   and `data` is the POST form data from the login page.
#c.JupyterHub.authenticator_class = 'jupyterhub.auth.PAMAuthenticator'
c.JupyterHub.authenticator_class = 'dummyauthenticator.DummyAuthenticator'
#c.JupyterHub.authenticator_class = 'oauthenticator.GitHubOAuthenticator'
#c.GitHubOAuthenticator.oauth_callback_url = os.environ['OAUTH_CALLBACK_URL']
#c.GitHubOAuthenticator.client_id = os.environ['GITHUB_CLIENT_ID']
#c.GitHubOAuthenticator.client_secret = os.environ['GITHUB_CLIENT_SECRET']

# The base URL of the entire application
c.JupyterHub.base_url = '/'

# Whether to shutdown the proxy when the Hub shuts down.
#
# Disable if you want to be able to teardown the Hub while leaving the proxy
# running.
#
# Only valid if the proxy was starting by the Hub process.
#
# If both this and cleanup_servers are False, sending SIGINT to the Hub will
# only shutdown the Hub, leaving everything else running.
#
# The Hub should be able to resume from database state.
c.JupyterHub.cleanup_proxy = True

# Whether to shutdown single-user servers when the Hub shuts down.
#
# Disable if you want to be able to teardown the Hub while leaving the single-
# user servers running.
#
# If both this and cleanup_proxy are False, sending SIGINT to the Hub will only
# shutdown the Hub, leaving everything else running.
#
# If both this and cleanup_proxy are False, sending SIGINT to the Hub will only
# shutdown the Hub, leaving everything else running.
#
# The Hub should be able to resume from database state.
c.JupyterHub.cleanup_servers = True

# The config file to load
# c.JupyterHub.config_file = '/root/config/jupyter/jupyterhub_config.py'
# Confirm that JupyterHub should be run without SSL. This is **NOT RECOMMENDED**
# unless SSL termination is being handled by another layer.
c.JupyterHub.confirm_no_ssl = True

# Number of days for a login cookie to be valid. Default is two weeks.
# c.JupyterHub.cookie_max_age_days = 14

# The cookie secret to use to encrypt cookies.
#
# Loaded from the JPY_COOKIE_SECRET env variable by default.
# c.JupyterHub.cookie_secret = b''

# File in which to store the cookie secret.
# c.JupyterHub.cookie_secret_file = '/root/pipeline/work/jupyterhub/jupyterhub_cookie_secret'

# The location of jupyterhub data files (e.g. /usr/local/share/jupyter/hub)
# c.JupyterHub.data_files_path = '/root/pipeline/work/jupyterhub'

# Include any kwargs to pass to the database connection. See
# sqlalchemy.create_engine for details.
# c.JupyterHub.db_kwargs = {}
# url for the database. e.g. `sqlite:///jupyterhub.sqlite`
#c.JupyterHub.db_url = 'sqlite:////root/jupyterhub.sqlite'

# log all database transactions. This has A LOT of output
# c.JupyterHub.debug_db = False

# show debug output in configurable-http-proxy
# c.JupyterHub.debug_proxy = False

# Send JupyterHub's logs to this file.
#
# This will *only* include the logs of the Hub itself, not the logs of the proxy
# or any single-user servers.
#c.JupyterHub.extra_log_file = '/root/jupyterhub.log'

# Extra log handlers to set on JupyterHub logger
# c.JupyterHub.extra_log_handlers = []

# Generate default config file
# #c.JupyterHub.generate_config = False

# The ip for this process
c.JupyterHub.hub_ip = '0.0.0.0'

# The port for this process
# c.JupyterHub.hub_port = 3081

# The prefix for the hub server. Must not be '/'
# c.JupyterHub.hub_prefix = '/hub/'

# The public facing ip of the whole application (the proxy)
c.JupyterHub.ip = '0.0.0.0'

# Supply extra arguments that will be passed to Jinja environment.
# c.JupyterHub.jinja_environment_options = {}
# Interval (in seconds) at which to update last-activity timestamps.
# c.JupyterHub.last_activity_interval = 300

# Specify path to a logo image to override the Jupyter logo in the banner.
# c.JupyterHub.logo_file = ''

# File to write PID Useful for daemonizing jupyterhub.
# c.JupyterHub.pid_file = ''

# The public facing port of the proxy
c.JupyterHub.port = 8754

# The ip for the proxy API handlers
c.JupyterHub.proxy_api_ip = '0.0.0.0'

# The port for the proxy API handlers
# c.JupyterHub.proxy_api_port = 0

# The Proxy Auth token.
#
# Loaded from the CONFIGPROXY_AUTH_TOKEN env variable by default.
# c.JupyterHub.proxy_auth_token = ''

# Interval (in seconds) at which to check if the proxy is running.
# c.JupyterHub.proxy_check_interval = 30

# The command to start the http proxy.
#
# Only override if configurable-http-proxy is not on your PATH
# c.JupyterHub.proxy_cmd = ['configurable-http-proxy']

# Purge and reset the database.
# c.JupyterHub.reset_db = False

# The class to use for spawning single-user servers.
#
# Should be a subclass of Spawner.
#c.JupyterHub.spawner_class = 'jupyterhub.spawner.LocalProcessSpawner'
#c.JupyterHub.spawner_class = 'dockerspawner.DockerSpawner'
c.JupyterHub.spawner_class = 'simplespawner.SimpleLocalProcessSpawner'
c.SimpleLocalProcessSpawner.home_path_template = '/root/'

# Spawn user containers from this image
#c.DockerSpawner.container_image = 'jupyter/pyspark-notebook'

# Have the Spawner override the Docker run command
#c.DockerSpawner.extra_create_kwargs.update({
#    'command': '/usr/local/bin/start-singleuser.sh'
#})


# Path to SSL certificate file for the public facing interface of the proxy
#
# Use with ssl_key
# c.JupyterHub.ssl_cert = ''

# Path to SSL key file for the public facing interface of the proxy
#
# Use with ssl_cert
# c.JupyterHub.ssl_key = ''

# Host to send statds metrics to
# c.JupyterHub.statsd_host = ''

# Port on which to send statsd metrics about the hub
# c.JupyterHub.statsd_port = 8125

# Prefix to use for all metrics sent by jupyterhub to statsd
# c.JupyterHub.statsd_prefix = 'jupyterhub'

# Run single-user servers on subdomains of this host.
#
# This should be the full https://hub.domain.tld[:port]
#
# Provides additional cross-site protections for javascript served by single-
# user servers.
#
# Requires <username>.hub.domain.tld to resolve to the same host as
# hub.domain.tld.
#
# In general, this is most easily achieved with wildcard DNS.
#
# When using SSL (i.e. always) this also requires a wildcard SSL certificate.
# c.JupyterHub.subdomain_host = ''

# Paths to search for jinja templates.
# c.JupyterHub.template_paths = []

# Extra settings overrides to pass to the tornado application.
# c.JupyterHub.tornado_settings = {}

#------------------------------------------------------------------------------
# Spawner configuration
#------------------------------------------------------------------------------

# Base class for spawning single-user notebook servers.
#
# Subclass this, and override the following methods:
#
# - load_state - get_state - start - stop - poll

# Extra arguments to be passed to the single-user server
# c.Spawner.args = []
# The command used for starting notebooks.
# c.Spawner.cmd = ['jupyterhub-singleuser']

# Enable debug-logging of the single-user server
c.Spawner.debug = True

# The default URL for the single-user server.
#
# Can be used in conjunction with --notebook-dir=/ to enable  full filesystem
# traversal, while preserving user's homedir as landing page for notebook
#
# `%U` will be expanded to the user's username
c.Spawner.default_url = ''

# Disable per-user configuration of single-user servers.
#
# This prevents any config in users' $HOME directories from having an effect on
# their server.
c.Spawner.disable_user_config = True

# Whitelist of environment variables for the subprocess to inherit
c.Spawner.env_keep = ['PATH', 'PYTHONPATH', 'CONDA_ROOT', 'CONDA_DEFAULT_ENV', 'VIRTUAL_ENV', 'LANG', 'LC_ALL', 'SPARK_HOME', 'PYSPARK_PYTHON', 'SPARK_MASTER', 'PYSPARK_SUBMIT_ARGS', 'SPARK_SUBMIT_ARGS']

# Environment variables to load for the Spawner.
#
# Value could be a string or a callable. If it is a callable, it will be called
# with one parameter, which will be the instance of the spawner in use. It
# should quickly (without doing much blocking operations) return a string that
# will be used as the value for the environment variable.
# c.Spawner.environment = {}

# Timeout (in seconds) before giving up on a spawned HTTP server
#
# Once a server has successfully been spawned, this is the amount of time we
# wait before assuming that the server is unable to accept connections.
# c.Spawner.http_timeout = 30

# The IP address (or hostname) the single-user server should listen on
c.Spawner.ip = '0.0.0.0'

# The notebook directory for the single-user server
#
# `~` will be expanded to the user's home directory `%U` will be expanded to the
# user's username
c.Spawner.notebook_dir = 'notebooks'

# An HTML form for options a user can specify on launching their server. The
# surrounding `<form>` element and the submit button are already provided.
#
# For example:
#     <br>
#     Choose a letter:
#     <select name="letter" multiple="true">
#       <option value="A">The letter A</option>
#       <option value="B">The letter B</option>
#     </select>
# c.Spawner.options_form = ''

# Interval (in seconds) on which to poll the spawner.
# c.Spawner.poll_interval = 30
# Timeout (in seconds) before giving up on the spawner.
#
# This is the timeout for start to return, not the timeout for the server to
# respond. Callers of spawner.start will assume that startup has failed if it
# takes longer than this. start should return when the server process is started
# and its location is known.
# c.Spawner.start_timeout = 60

#------------------------------------------------------------------------------
# LocalProcessSpawner configuration
#------------------------------------------------------------------------------

# A Spawner that just uses Popen to start local processes as users.
#
# Requires users to exist on the local system.
#
# This is the default spawner for JupyterHub.

# Seconds to wait for process to halt after SIGINT before proceeding to SIGTERM
# c.LocalProcessSpawner.INTERRUPT_TIMEOUT = 10

# Seconds to wait for process to halt after SIGKILL before giving up
# c.LocalProcessSpawner.KILL_TIMEOUT = 5

# Seconds to wait for process to halt after SIGTERM before proceeding to SIGKILL
# c.LocalProcessSpawner.TERM_TIMEOUT = 5

#------------------------------------------------------------------------------
# Authenticator configuration
#------------------------------------------------------------------------------

# A class for authentication.
#
# The primary API is one method, `authenticate`, a tornado coroutine for
# authenticating users.

# set of usernames of admin users
#
# If unspecified, only the user that launches the server will be admin.
#c.Authenticator.admin_users = {"root"}

# Dictionary mapping authenticator usernames to JupyterHub users.
#
# Can be used to map OAuth service names to local users, for instance.
#
# Used in normalize_username.
# c.Authenticator.username_map = {}

# Regular expression pattern for validating usernames.
#
# If not defined: allow any username.
# c.Authenticator.username_pattern = ''

# Username whitelist.
#
# Use this to restrict which users can login. If empty, allow any user to
# attempt login.
#c.Authenticator.whitelist = set("")

#------------------------------------------------------------------------------
# LocalAuthenticator configuration
#------------------------------------------------------------------------------

# Base class for Authenticators that work with local Linux/UNIX users
#
# Checks for local users, and can attempt to create them if they exist.

# The command to use for creating users as a list of strings.
#
# For each element in the list, the string USERNAME will be replaced with the
# user's username. The username will also be appended as the final argument.
#
# For Linux, the default value is:
#
#     ['adduser', '-q', '--gecos', '""', '--disabled-password']
#
# To specify a custom home directory, set this to:
#
#     ['adduser', '-q', '--gecos', '""', '--home', '/customhome/USERNAME',
# '--disabled-password']
#
# This will run the command:
#
# adduser -q --gecos "" --home /customhome/river --disabled-password river
#
# when the user 'river' is created.
#c.LocalAuthenticator.add_user_cmd = []

# If a user is added that doesn't exist on the system, should I try to create
# the system user?
c.LocalAuthenticator.create_system_users = False

# Automatically whitelist anyone in this group.
#c.LocalAuthenticator.group_whitelist = set("root")

#------------------------------------------------------------------------------
# PAMAuthenticator configuration
#------------------------------------------------------------------------------

# Authenticate local Linux/UNIX users with PAM

# The encoding to use for PAM
# c.PAMAuthenticator.encoding = 'utf8'

# Whether to open PAM sessions when spawners are started.
#
# This may trigger things like mounting shared filsystems, loading credentials,
# etc. depending on system configuration, but it does not always work.
#
# It can be disabled with::
#
# c.PAMAuthenticator.open_sessions = False

# The PAM service to use for authentication.
# c.PAMAuthenticator.service = 'login'                     
