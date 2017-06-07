package com.turn.tpmml.manager;

/**
 * We store the node on which the evaluation has ended. It can be null if
 * there was no child and the missing node strategy was to return the last
 * decision.
 *
 * @author tbadie
 *
 */
public class TreePMMLResult extends PMMLResult {
	private String nodeId = null;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
}
