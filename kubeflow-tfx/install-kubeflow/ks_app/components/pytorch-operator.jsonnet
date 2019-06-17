local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components["pytorch-operator"];

local k = import "k.libsonnet";
local operator = import "kubeflow/pytorch-job/pytorch-operator.libsonnet";

k.core.v1.list.new(operator.all(params, env))
