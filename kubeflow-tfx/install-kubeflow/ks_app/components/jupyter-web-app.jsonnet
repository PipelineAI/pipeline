local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components["jupyter-web-app"];

local jupyter_ui = import "kubeflow/jupyter/jupyter-web-app.libsonnet";

local instance = jupyter_ui.new(env, params);
instance.list(instance.all)
