local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components["notebook-controller"];

local notebooks = import "kubeflow/jupyter/notebook_controller.libsonnet";
local instance = notebooks.new(env, params);
instance.list(instance.all)
