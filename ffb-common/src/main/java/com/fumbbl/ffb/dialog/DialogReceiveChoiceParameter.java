package com.fumbbl.ffb.dialog;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.IDialogParameter;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;

/**
 * 
 * @author Kalimar
 */
public class DialogReceiveChoiceParameter implements IDialogParameter {

	private String fChoosingTeamId;

	public DialogReceiveChoiceParameter() {
		super();
	}

	public DialogReceiveChoiceParameter(String pChoosingTeamId) {
		fChoosingTeamId = pChoosingTeamId;
	}

	public DialogId getId() {
		return DialogId.RECEIVE_CHOICE;
	}

	public String getChoosingTeamId() {
		return fChoosingTeamId;
	}

	// transformation

	public IDialogParameter transform() {
		return new DialogReceiveChoiceParameter(getChoosingTeamId());
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.DIALOG_ID.addTo(jsonObject, getId());
		IJsonOption.CHOOSING_TEAM_ID.addTo(jsonObject, fChoosingTeamId);
		return jsonObject;
	}

	public DialogReceiveChoiceParameter initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilDialogParameter.validateDialogId(this, (DialogId) IJsonOption.DIALOG_ID.getFrom(source, jsonObject));
		fChoosingTeamId = IJsonOption.CHOOSING_TEAM_ID.getFrom(source, jsonObject);
		return this;
	}

}
