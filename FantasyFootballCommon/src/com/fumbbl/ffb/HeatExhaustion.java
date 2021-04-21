package com.fumbbl.ffb;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.IJsonSerializable;
import com.fumbbl.ffb.json.UtilJson;

/**
 * 
 * @author Kalimar
 */
public class HeatExhaustion implements IJsonSerializable {

	private String fPlayerId;
	private boolean fExhausted;
	private int fRoll;

	public HeatExhaustion() {
		super();
	}

	public HeatExhaustion(String pPlayerId, boolean pExhausted, int pRoll) {
		fPlayerId = pPlayerId;
		fExhausted = pExhausted;
		fRoll = pRoll;
	}

	public String getPlayerId() {
		return fPlayerId;
	}

	public boolean isExhausted() {
		return fExhausted;
	}

	public int getRoll() {
		return fRoll;
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.PLAYER_ID.addTo(jsonObject, fPlayerId);
		IJsonOption.EXHAUSTED.addTo(jsonObject, fExhausted);
		IJsonOption.ROLL.addTo(jsonObject, fRoll);
		return jsonObject;
	}

	public HeatExhaustion initFrom(IFactorySource game, JsonValue pJsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
		fPlayerId = IJsonOption.PLAYER_ID.getFrom(game, jsonObject);
		fExhausted = IJsonOption.EXHAUSTED.getFrom(game, jsonObject);
		fRoll = IJsonOption.ROLL.getFrom(game, jsonObject);
		return this;
	}

}