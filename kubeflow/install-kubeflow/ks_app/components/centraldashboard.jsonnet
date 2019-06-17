local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components.centraldashboard;

local centraldashboard = import "kubeflow/common/centraldashboard.libsonnet";
local instance = centraldashboard.new(env, params);
instance.list(instance.all)
