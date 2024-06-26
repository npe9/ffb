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
public class DialogBlockRollParameter implements IDialogParameter {

	private String fChoosingTeamId;
	private int fNrOfDice;
	private int[] fBlockRoll;
	private boolean fTeamReRollOption;
	private boolean fProReRollOption;

	public DialogBlockRollParameter() {
		super();
	}

	public DialogBlockRollParameter(String pChoosingTeamId, int pNrOfDice, int[] pBlockRoll, boolean pTeamReRollOption,
			boolean pProReRollOption) {
		fChoosingTeamId = pChoosingTeamId;
		fNrOfDice = pNrOfDice;
		fBlockRoll = pBlockRoll;
		fTeamReRollOption = pTeamReRollOption;
		fProReRollOption = pProReRollOption;
	}

	public DialogId getId() {
		return DialogId.BLOCK_ROLL;
	}

	public String getChoosingTeamId() {
		return fChoosingTeamId;
	}

	public int getNrOfDice() {
		return fNrOfDice;
	}

	public int[] getBlockRoll() {
		return fBlockRoll;
	}

	public boolean hasTeamReRollOption() {
		return fTeamReRollOption;
	}

	public boolean hasProReRollOption() {
		return fProReRollOption;
	}

	// transformation

	public IDialogParameter transform() {
		return new DialogBlockRollParameter(getChoosingTeamId(), getNrOfDice(), getBlockRoll(), hasTeamReRollOption(),
				hasProReRollOption());
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.DIALOG_ID.addTo(jsonObject, getId());
		IJsonOption.CHOOSING_TEAM_ID.addTo(jsonObject, fChoosingTeamId);
		IJsonOption.NR_OF_DICE.addTo(jsonObject, fNrOfDice);
		IJsonOption.BLOCK_ROLL.addTo(jsonObject, fBlockRoll);
		IJsonOption.TEAM_RE_ROLL_OPTION.addTo(jsonObject, fTeamReRollOption);
		IJsonOption.PRO_RE_ROLL_OPTION.addTo(jsonObject, fProReRollOption);
		return jsonObject;
	}

	public DialogBlockRollParameter initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilDialogParameter.validateDialogId(this, (DialogId) IJsonOption.DIALOG_ID.getFrom(source, jsonObject));
		fChoosingTeamId = IJsonOption.CHOOSING_TEAM_ID.getFrom(source, jsonObject);
		fNrOfDice = IJsonOption.NR_OF_DICE.getFrom(source, jsonObject);
		fBlockRoll = IJsonOption.BLOCK_ROLL.getFrom(source, jsonObject);
		fTeamReRollOption = IJsonOption.TEAM_RE_ROLL_OPTION.getFrom(source, jsonObject);
		fProReRollOption = IJsonOption.PRO_RE_ROLL_OPTION.getFrom(source, jsonObject);
		return this;
	}

}
