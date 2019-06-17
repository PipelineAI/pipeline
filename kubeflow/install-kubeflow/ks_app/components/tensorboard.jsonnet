local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components.tensorboard;

local tensorboard = import "kubeflow/tensorboard/tensorboard.libsonnet";
local instance = tensorboard.new(env, params);
instance.list(instance.all)
