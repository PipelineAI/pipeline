local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components.metacontroller;

local metacontroller = import "kubeflow/metacontroller/metacontroller.libsonnet";
local instance = metacontroller.new(env, params);
instance.list(instance.all)
