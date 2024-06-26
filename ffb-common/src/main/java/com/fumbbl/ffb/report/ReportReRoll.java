package com.fumbbl.ffb.report;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.ReRollSource;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.stats.DicePoolStat;
import com.fumbbl.ffb.stats.DieBase;
import com.fumbbl.ffb.stats.DieStat;
import com.fumbbl.ffb.stats.TeamMapping;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.COMMON)
public class ReportReRoll implements IReport {

	private String fPlayerId;
	private ReRollSource fReRollSource;
	private boolean fSuccessful;
	private int fRoll;

	public ReportReRoll() {
		super();
	}

	public ReportReRoll(String pPlayerId, ReRollSource pReRollSource, boolean pSuccessful, int pRoll) {
		fPlayerId = pPlayerId;
		fReRollSource = pReRollSource;
		fSuccessful = pSuccessful;
		fRoll = pRoll;
	}

	public ReportId getId() {
		return ReportId.RE_ROLL;
	}

	public String getPlayerId() {
		return fPlayerId;
	}

	public ReRollSource getReRollSource() {
		return fReRollSource;
	}

	public boolean isSuccessful() {
		return fSuccessful;
	}

	public int getRoll() {
		return fRoll;
	}

	// transformation

	public IReport transform(IFactorySource source) {
		return new ReportReRoll(getPlayerId(), getReRollSource(), isSuccessful(), getRoll());
	}

	@Override
	public void addStats(Game game, List<DieStat<?>> diceStats) {
		if (fRoll > 0) {
			diceStats.add(new DicePoolStat(DieBase.D6, TeamMapping.TEAM_FOR_PLAYER, fPlayerId, Collections.singletonList(fRoll)));
		}
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.REPORT_ID.addTo(jsonObject, getId());
		IJsonOption.PLAYER_ID.addTo(jsonObject, fPlayerId);
		IJsonOption.RE_ROLL_SOURCE.addTo(jsonObject, fReRollSource);
		IJsonOption.SUCCESSFUL.addTo(jsonObject, fSuccessful);
		IJsonOption.ROLL.addTo(jsonObject, fRoll);
		return jsonObject;
	}

	public ReportReRoll initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilReport.validateReportId(this, (ReportId) IJsonOption.REPORT_ID.getFrom(source, jsonObject));
		fPlayerId = IJsonOption.PLAYER_ID.getFrom(source, jsonObject);
		fReRollSource = (ReRollSource) IJsonOption.RE_ROLL_SOURCE.getFrom(source, jsonObject);
		fSuccessful = IJsonOption.SUCCESSFUL.getFrom(source, jsonObject);
		fRoll = IJsonOption.ROLL.getFrom(source, jsonObject);
		return this;
	}

}
