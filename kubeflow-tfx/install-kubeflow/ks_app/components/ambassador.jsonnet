local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components.ambassador;

local ambassador = import "kubeflow/common/ambassador.libsonnet";
local instance = ambassador.new(env, params);
instance.list(instance.all)
