package com.turn.tpmml.manager;

/**
 * The score card needs to store the characteristic that made
 * the decision. This is the lastReasonCode.
 *
 * @author tbadie
 *
 */
public class ScoreCardPMMLResult extends PMMLResult {
	private String lastReasonCode = null;

	public String getLastReasonCode() {
		return lastReasonCode;
	}

	public void setLastReasonCode(String lastReasonCode) {
		this.lastReasonCode = lastReasonCode;
	}
}
