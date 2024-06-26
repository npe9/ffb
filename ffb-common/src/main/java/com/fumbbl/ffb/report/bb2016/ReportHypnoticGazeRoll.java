package com.fumbbl.ffb.report.bb2016;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.modifiers.RollModifier;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.ReportSkillRoll;

@RulesCollection(RulesCollection.Rules.BB2016)
public class ReportHypnoticGazeRoll extends ReportSkillRoll {

	public ReportHypnoticGazeRoll() {
	}

	public ReportHypnoticGazeRoll(String pPlayerId, boolean pSuccessful, int pRoll, int pMinimumRoll,
	                              boolean pReRolled, RollModifier<?>[] pRollModifiers) {
		super(pPlayerId, pSuccessful, pRoll, pMinimumRoll, pReRolled, pRollModifiers);
	}

	@Override
	public ReportId getId() {
		return ReportId.HYPNOTIC_GAZE_ROLL;
	}

	@Override
	public ReportHypnoticGazeRoll transform(IFactorySource source) {
		return new ReportHypnoticGazeRoll(getPlayerId(), isSuccessful(), getRoll(), getMinimumRoll(), isReRolled(),
			getRollModifiers());
	}
}
