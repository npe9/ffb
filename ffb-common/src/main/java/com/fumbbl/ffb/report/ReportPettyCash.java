package com.fumbbl.ffb.report;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;

/**
 * @author Kalimar
 */
@RulesCollection(RulesCollection.Rules.COMMON)
public class ReportPettyCash extends NoDiceReport {

	private String fTeamId;
	private int fGold;

	public ReportPettyCash() {
		super();
	}

	public ReportPettyCash(String pTeamId, int pGold) {
		fTeamId = pTeamId;
		fGold = pGold;
	}

	public ReportId getId() {
		return ReportId.PETTY_CASH;
	}

	public String getTeamId() {
		return fTeamId;
	}

	public int getGold() {
		return fGold;
	}

	// transformation

	public IReport transform(IFactorySource source) {
		return new ReportPettyCash(getTeamId(), getGold());
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.REPORT_ID.addTo(jsonObject, getId());
		IJsonOption.TEAM_ID.addTo(jsonObject, fTeamId);
		IJsonOption.GOLD.addTo(jsonObject, fGold);
		return jsonObject;
	}

	public ReportPettyCash initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilReport.validateReportId(this, (ReportId) IJsonOption.REPORT_ID.getFrom(source, jsonObject));
		fTeamId = IJsonOption.TEAM_ID.getFrom(source, jsonObject);
		fGold = IJsonOption.GOLD.getFrom(source, jsonObject);
		return this;
	}

}
