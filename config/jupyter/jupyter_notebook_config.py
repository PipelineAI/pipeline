# Configuration file for jupyter-notebook.

#------------------------------------------------------------------------------
# Configurable configuration
#------------------------------------------------------------------------------

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
# JupyterApp configuration
#------------------------------------------------------------------------------

# Base class for Jupyter applications

# Answer yes to any prompts.
c.JupyterApp.answer_yes = True 

# Full path of a config file.
# c.JupyterApp.config_file = u''

# Generate default config file.
# c.JupyterApp.generate_config = False

# Specify a config file to load.
# c.JupyterApp.config_file_name = u''

#------------------------------------------------------------------------------
# NotebookApp configuration
#------------------------------------------------------------------------------

# The number of additional ports to try if the specified port is not available.
c.NotebookApp.port_retries = 0

# Extra variables to supply to jinja templates when rendering.
# c.NotebookApp.jinja_template_vars = traitlets.Undefined

# The url for MathJax.js.
# c.NotebookApp.mathjax_url = ''

# Supply extra arguments that will be passed to Jinja environment.
# c.NotebookApp.jinja_environment_options = traitlets.Undefined

# The IP address the notebook server will listen on.
c.NotebookApp.ip = '*' 

# DEPRECATED use base_url
# c.NotebookApp.base_project_url = '/'

# Python modules to load as notebook server extensions. This is an experimental
# API, and may change in future releases.
# c.NotebookApp.server_extensions = traitlets.Undefined
# Note:  These extensions require the ~/.jupyter path to exist otherwise, errors will occur on startup
c.NotebookApp.server_extensions=['ipyparallel.nbextension']

# The random bytes used to secure cookies. By default this is a new random
# number every time you start the Notebook. Set it to a value in a config file
# to enable logins to persist across server sessions.
# 
# Note: Cookie secrets should be kept private, do not share config files with
# cookie_secret stored in plaintext (you can read the value from a file).
# c.NotebookApp.cookie_secret = ''

# The default URL to redirect to from `/`
# c.NotebookApp.default_url = '/tree'

# The port the notebook server will listen on.
c.NotebookApp.port = 8754 

# The kernel spec manager class to use. Should be a subclass of
# `jupyter_client.kernelspec.KernelSpecManager`.
# 
# The Api of KernelSpecManager is provisional and might change without warning
# between this version of IPython and the next stable one.
# c.NotebookApp.kernel_spec_manager_class = <class 'jupyter_client.kernelspec.KernelSpecManager'>

# Set the Access-Control-Allow-Origin header
# 
# Use '*' to allow any origin to access your server.
# 
# Takes precedence over allow_origin_pat.
c.NotebookApp.allow_origin = '*'

# The notebook manager class to use.
# c.NotebookApp.contents_manager_class = <class 'notebook.services.contents.filemanager.FileContentsManager'>

# Use a regular expression for the Access-Control-Allow-Origin header
# 
# Requests from an origin matching the expression will get replies with:
# 
#     Access-Control-Allow-Origin: origin
# 
# where `origin` is the origin of the request.
# 
# Ignored if allow_origin is set.
# c.NotebookApp.allow_origin_pat = ''

# The full path to an SSL/TLS certificate file.
# c.NotebookApp.certfile = u''

# The logout handler class to use.
# c.NotebookApp.logout_handler_class = <class 'notebook.auth.logout.LogoutHandler'>

# The base URL for the notebook server.
# 
# Leading and trailing slashes can be omitted, and will automatically be added.
c.NotebookApp.base_url = '/'

# The session manager class to use.
# c.NotebookApp.session_manager_class = <class 'notebook.services.sessions.sessionmanager.SessionManager'>

# Supply overrides for the tornado.web.Application that the IPython notebook
# uses.
# c.NotebookApp.tornado_settings = traitlets.Undefined

# The directory to use for notebooks and kernels.
c.NotebookApp.notebook_dir = u'/root/pipeline/myapps/jupyter/'

# The kernel manager class to use.
# c.NotebookApp.kernel_manager_class = <class 'notebook.services.kernels.kernelmanager.MappingKernelManager'>

# The file where the cookie secret is stored.
# c.NotebookApp.cookie_secret_file = u''

# Supply SSL options for the tornado HTTPServer. See the tornado docs for
# details.
# c.NotebookApp.ssl_options = traitlets.Undefined

# 
# c.NotebookApp.file_to_run = ''

# DISABLED: use %pylab or %matplotlib in the notebook to enable matplotlib.
# c.NotebookApp.pylab = 'disabled'

# Whether to enable MathJax for typesetting math/TeX
# 
# MathJax is the javascript library IPython uses to render math/LaTeX. It is
# very large, so you may want to disable it if you have a slow internet
# connection, or for offline use of the notebook.
# 
# When disabled, equations etc. will appear as their untransformed TeX source.
# c.NotebookApp.enable_mathjax = True

# Reraise exceptions encountered loading server extensions?
# c.NotebookApp.reraise_server_extension_failures = False

# The base URL for websockets, if it differs from the HTTP server (hint: it
# almost certainly doesn't).
# 
# Should be in the form of an HTTP origin: ws[s]://hostname[:port]
# c.NotebookApp.websocket_url = ''

# Whether to open in a browser after starting. The specific browser used is
# platform dependent and determined by the python standard library `webbrowser`
# module, unless it is overridden using the --browser (NotebookApp.browser)
# configuration option.
c.NotebookApp.open_browser = False 

# Hashed password to use for web authentication.
# 
# To generate, type in a python/IPython shell:
# 
#   from notebook.auth import passwd; passwd()
# 
# The string should be of the form type:salt:hashed-password.
# c.NotebookApp.password = u''

# extra paths to look for Javascript notebook extensions
# c.NotebookApp.extra_nbextensions_path = traitlets.Undefined

# Set the Access-Control-Allow-Credentials: true header
# c.NotebookApp.allow_credentials = False

# Extra paths to search for serving static files.
# 
# This allows adding javascript/css to be available from the notebook server
# machine, or overriding individual files in the IPython
# c.NotebookApp.extra_static_paths = traitlets.Undefined

# The login handler class to use.
# c.NotebookApp.login_handler_class = <class 'notebook.auth.login.LoginHandler'>

# Whether to trust or not X-Scheme/X-Forwarded-Proto and X-Real-Ip/X-Forwarded-
# For headerssent by the upstream reverse proxy. Necessary if the proxy handles
# SSL
# c.NotebookApp.trust_xheaders = False

# Extra paths to search for serving jinja templates.
# 
# Can be used to override templates from notebook.templates.
# c.NotebookApp.extra_template_paths = traitlets.Undefined

# The config manager class to use
# c.NotebookApp.config_manager_class = <class 'notebook.services.config.manager.ConfigManager'>

# The full path to a private key file for usage with SSL/TLS.
# c.NotebookApp.keyfile = u''

# DEPRECATED, use tornado_settings
# c.NotebookApp.webapp_settings = traitlets.Undefined

# Specify what command to use to invoke a web browser when opening the notebook.
# If not specified, the default browser will be determined by the `webbrowser`
# standard library module, which allows setting of the BROWSER environment
# variable to override it.
# c.NotebookApp.browser = u''

#------------------------------------------------------------------------------
# LoggingConfigurable configuration
#------------------------------------------------------------------------------

# A parent class for Configurables that log.
# 
# Subclasses have a log trait, and the default behavior is to get the logger
# from the currently running Application.

#------------------------------------------------------------------------------
# ConnectionFileMixin configuration
#------------------------------------------------------------------------------

# Mixin for configurable classes that work with connection files

# set the stdin (ROUTER) port [default: random]
# c.ConnectionFileMixin.stdin_port = 0

# Set the kernel's IP address [default localhost]. If the IP address is
# something other than localhost, then Consoles on other machines will be able
# to connect to the Kernel, so be careful!
# c.ConnectionFileMixin.ip = u''

# JSON file in which to store connection info [default: kernel-<pid>.json]
# 
# This file will contain the IP, ports, and authentication key needed to connect
# clients to this kernel. By default, this file will be created in the security
# dir of the current profile, but can be specified by absolute path.
# c.ConnectionFileMixin.connection_file = ''

# set the control (ROUTER) port [default: random]
# c.ConnectionFileMixin.control_port = 0

# set the heartbeat port [default: random]
# c.ConnectionFileMixin.hb_port = 0

# set the shell (ROUTER) port [default: random]
# c.ConnectionFileMixin.shell_port = 0

# 
# c.ConnectionFileMixin.transport = 'tcp'

# set the iopub (PUB) port [default: random]
# c.ConnectionFileMixin.iopub_port = 0

#------------------------------------------------------------------------------
# KernelManager configuration
#------------------------------------------------------------------------------

# Manages a single kernel in a subprocess on this host.
# 
# This version starts kernels with Popen.

# DEPRECATED: Use kernel_name instead.
# 
# The Popen Command to launch the kernel. Override this if you have a custom
# kernel. If kernel_cmd is specified in a configuration file, Jupyter does not
# pass any arguments to the kernel, because it cannot make any assumptions about
# the arguments that the kernel understands. In particular, this means that the
# kernel does not receive the option --debug if it given on the Jupyter command
# line.
# c.KernelManager.kernel_cmd = traitlets.Undefined

# Should we autorestart the kernel if it dies.
# c.KernelManager.autorestart = False

#------------------------------------------------------------------------------
# Session configuration
#------------------------------------------------------------------------------

# Object for handling serialization and sending of messages.
# 
# The Session object handles building messages and sending them with ZMQ sockets
# or ZMQStream objects.  Objects can communicate with each other over the
# network via Session objects, and only need to work with the dict-based IPython
# message spec. The Session will handle serialization/deserialization, security,
# and metadata.
# 
# Sessions support configurable serialization via packer/unpacker traits, and
# signing with HMAC digests via the key/keyfile traits.
# 
# Parameters ----------
# 
# debug : bool
#     whether to trigger extra debugging statements
# packer/unpacker : str : 'json', 'pickle' or import_string
#     importstrings for methods to serialize message parts.  If just
#     'json' or 'pickle', predefined JSON and pickle packers will be used.
#     Otherwise, the entire importstring must be used.
# 
#     The functions must accept at least valid JSON input, and output *bytes*.
# 
#     For example, to use msgpack:
#     packer = 'msgpack.packb', unpacker='msgpack.unpackb'
# pack/unpack : callables
#     You can also set the pack/unpack callables for serialization directly.
# session : bytes
#     the ID of this Session object.  The default is to generate a new UUID.
# username : unicode
#     username added to message headers.  The default is to ask the OS.
# key : bytes
#     The key used to initialize an HMAC signature.  If unset, messages
#     will not be signed or checked.
# keyfile : filepath
#     The file containing a key.  If this is set, `key` will be initialized
#     to the contents of the file.

# Username for the Session. Default is your system username.
# c.Session.username = u'username'

# Threshold (in bytes) beyond which a buffer should be sent without copying.
# c.Session.copy_threshold = 65536

# The name of the packer for serializing messages. Should be one of 'json',
# 'pickle', or an import name for a custom callable serializer.
# c.Session.packer = 'json'

# Metadata dictionary, which serves as the default top-level metadata dict for
# each message.
# c.Session.metadata = traitlets.Undefined

# The maximum number of digests to remember.
# 
# The digest history will be culled when it exceeds this value.
# c.Session.digest_history_size = 65536

# The UUID identifying this session.
# c.Session.session = u''

# The digest scheme used to construct the message signatures. Must have the form
# 'hmac-HASH'.
# c.Session.signature_scheme = 'hmac-sha256'

# execution key, for signing messages.
# c.Session.key = ''

# Debug output in the Session
# c.Session.debug = False

# The name of the unpacker for unserializing messages. Only used with custom
# functions for `packer`.
# c.Session.unpacker = 'json'

# path to file containing execution key.
# c.Session.keyfile = ''

# Threshold (in bytes) beyond which an object's buffer should be extracted to
# avoid pickling.
# c.Session.buffer_threshold = 1024

# The maximum number of items for a container to be introspected for custom
# serialization. Containers larger than this are pickled outright.
# c.Session.item_threshold = 64

#------------------------------------------------------------------------------
# MultiKernelManager configuration
#------------------------------------------------------------------------------

# A class for managing multiple kernels.

# The name of the default kernel to start
# c.MultiKernelManager.default_kernel_name = 'python2'

# The kernel manager class.  This is configurable to allow subclassing of the
# KernelManager for customized behavior.
# c.MultiKernelManager.kernel_manager_class = 'jupyter_client.ioloop.IOLoopKernelManager'

#------------------------------------------------------------------------------
# MappingKernelManager configuration
#------------------------------------------------------------------------------

# A KernelManager that handles notebook mapping and HTTP error handling

# 
# c.MappingKernelManager.root_dir = u''

#------------------------------------------------------------------------------
# ContentsManager configuration
#------------------------------------------------------------------------------

# Base class for serving files and directories.
# 
# This serves any text or binary file, as well as directories, with special
# handling for JSON notebook documents.
# 
# Most APIs take a path argument, which is always an API-style unicode path, and
# always refers to a directory.
# 
# - unicode, not url-escaped
# - '/'-separated
# - leading and trailing '/' will be stripped
# - if unspecified, path defaults to '',
#   indicating the root path.

# The base name used when creating untitled files.
# c.ContentsManager.untitled_file = 'untitled'

# Python callable or importstring thereof
# 
# To be called on a contents model prior to save.
# 
# This can be used to process the structure, such as removing notebook outputs
# or other side effects that should not be saved.
# 
# It will be called as (all arguments passed by keyword)::
# 
#     hook(path=path, model=model, contents_manager=self)
# 
# - model: the model to be saved. Includes file contents.
#   Modifying this dict will affect the file that is stored.
# - path: the API path of the save destination
# - contents_manager: this ContentsManager instance
# c.ContentsManager.pre_save_hook = None

# 
# c.ContentsManager.checkpoints_class = <class 'notebook.services.contents.checkpoints.Checkpoints'>

# Glob patterns to hide in file and directory listings.
# c.ContentsManager.hide_globs = traitlets.Undefined

# The base name used when creating untitled notebooks.
# c.ContentsManager.untitled_notebook = 'Untitled'

# The base name used when creating untitled directories.
# c.ContentsManager.untitled_directory = 'Untitled Folder'

# 
# c.ContentsManager.checkpoints = traitlets.Undefined

# 
# c.ContentsManager.checkpoints_kwargs = traitlets.Undefined

#------------------------------------------------------------------------------
# FileContentsManager configuration
#------------------------------------------------------------------------------

# DEPRECATED, use post_save_hook
# c.FileContentsManager.save_script = False

# 
# c.FileContentsManager.root_dir = u''

# Python callable or importstring thereof
# 
# to be called on the path of a file just saved.
# 
# This can be used to process the file on disk, such as converting the notebook
# to a script or HTML via nbconvert.
# 
# It will be called as (all arguments passed by keyword)::
# 
#     hook(os_path=os_path, model=model, contents_manager=instance)
# 
# - path: the filesystem path to the file just written - model: the model
# representing the file - contents_manager: this ContentsManager instance
# c.FileContentsManager.post_save_hook = None

#------------------------------------------------------------------------------
# NotebookNotary configuration
#------------------------------------------------------------------------------

# A class for computing and verifying notebook signatures.

# The number of notebook signatures to cache. When the number of signatures
# exceeds this value, the oldest 25% of signatures will be culled.
# c.NotebookNotary.cache_size = 65535

# The secret key with which notebooks are signed.
# c.NotebookNotary.secret = ''

# The sqlite file in which to store notebook signatures. By default, this will
# be in your Jupyter runtime directory. You can set it to ':memory:' to disable
# sqlite writing to the filesystem.
# c.NotebookNotary.db_file = u''

# The hashing algorithm used to sign notebooks.
# c.NotebookNotary.algorithm = 'sha256'

# The file where the secret key is stored.
# c.NotebookNotary.secret_file = u''

#------------------------------------------------------------------------------
# KernelSpecManager configuration
#------------------------------------------------------------------------------

# Whitelist of allowed kernel names.
# 
# By default, all installed kernels are allowed.
# c.KernelSpecManager.whitelist = traitlets.Undefined
