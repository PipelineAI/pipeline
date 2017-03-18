package com.turn.tpmml.evaluator;

import com.turn.tpmml.Attribute;
import com.turn.tpmml.Characteristic;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Scorecard;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.ScoreCardModelManager;
import com.turn.tpmml.manager.ScoreCardPMMLResult;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScorecardEvaluator extends ScoreCardModelManager implements Evaluator {


	private static final long serialVersionUID = 1L;

	public ScorecardEvaluator(PMML pmml) {
		super(pmml);
	}

	public ScorecardEvaluator(PMML pmml, Scorecard scorecard) {
		super(pmml, scorecard);
	}

	public Object prepare(FieldName name, Object value) throws EvaluationException {
		try {
			return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
	}

	public ScorecardEvaluator(ScoreCardModelManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
	}

	// Evaluate the parameters on the score card.
	public IPMMLResult evaluate(Map<FieldName, ?> parameters) throws EvaluationException {
		Double score = 0.0;
		EvaluationContext context = new ModelManagerEvaluationContext(this, parameters);
		TreeMap<Double, String> diffToReasonCode = new TreeMap<Double, String>();
		List<Characteristic> cl = scorecard.getCharacteristics().getCharacteristics();
		for (Characteristic c : cl) {
			List<Attribute> al = c.getAttributes();
			for (Attribute a : al) {
				// Evaluate the predicate.
				Boolean predicateValue = PredicateUtil.evaluate(a.getPredicate(), context);
				// If it is valid, and the value is true, update the score.
				if (predicateValue != null && predicateValue.booleanValue()) {
					score += a.getPartialScore();

					double diff = 0;
					switch (reasonCodeAlgorithm) {
					case POINTS_BELOW:
						diff = c.getBaselineScore() - a.getPartialScore();
						break;
					case POINTS_ABOVE:
						diff = a.getPartialScore() - c.getBaselineScore();
						break;
					default:
						// We should never be there.
						assert false;
						break;
					}

					if (a.getReasonCode() != null && !a.getReasonCode().isEmpty()) {
						diffToReasonCode.put(diff, a.getReasonCode());
					} else {
						diffToReasonCode.put(diff, c.getReasonCode());
					}
					break;
					// FIXME: Add a missing value strategy.
				}
			}
		}

		lastReasonCode = diffToReasonCode.lastEntry().getValue();

		ScoreCardPMMLResult res = new ScoreCardPMMLResult();
		try {
			res.put(getOutputField(this).getName(), score);
			res.setLastReasonCode(lastReasonCode);
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		return res;
	}

	public String getResultExplanation() {
		return lastReasonCode;
	}

}
