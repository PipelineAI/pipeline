local env = std.extVar("__ksonnet/environments");
local params = std.extVar("__ksonnet/params").components.pipeline;

local k = import "k.libsonnet";
local pipelineBase = import "kubeflow/pipeline/pipeline.libsonnet";

// updatedParams includes the namespace from env by default.
local updatedParams = params + env;

local pipeline = pipelineBase {
	params+: updatedParams,
};

std.prune(k.core.v1.list.new(pipeline.parts.all))
