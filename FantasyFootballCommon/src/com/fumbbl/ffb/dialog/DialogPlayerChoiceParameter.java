package com.fumbbl.ffb.dialog;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.IDialogParameter;
import com.fumbbl.ffb.PlayerChoiceMode;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.util.ArrayTool;
import com.fumbbl.ffb.util.StringTool;

/**
 *
 * @author Kalimar
 */
public class DialogPlayerChoiceParameter implements IDialogParameter {

	private String fTeamId;
	private PlayerChoiceMode fPlayerChoiceMode;
	private List<String> fPlayerIds;
	private List<String> fDescriptions;
	private int fMaxSelects;

	public DialogPlayerChoiceParameter() {
		fPlayerIds = new ArrayList<>();
		fDescriptions = new ArrayList<>();
	}

	public DialogPlayerChoiceParameter(String pTeamId, PlayerChoiceMode pPlayerChoiceMode, Player<?>[] pPlayers,
			String[] pDescriptions, int pMaxSelects) {
		this(pTeamId, pPlayerChoiceMode, findPlayerIds(pPlayers), pDescriptions, pMaxSelects);
	}

	public DialogPlayerChoiceParameter(String pTeamId, PlayerChoiceMode pPlayerChoiceMode, String[] pPlayerIds,
			String[] pDescriptions, int pMaxSelects) {
		this();
		fTeamId = pTeamId;
		fPlayerChoiceMode = pPlayerChoiceMode;
		fMaxSelects = pMaxSelects;
		addDescriptions(pDescriptions);
		addPlayerIds(pPlayerIds);
	}

	public DialogId getId() {
		return DialogId.PLAYER_CHOICE;
	}

	public String getTeamId() {
		return fTeamId;
	}

	public int getMaxSelects() {
		return fMaxSelects;
	}

	public PlayerChoiceMode getPlayerChoiceMode() {
		return fPlayerChoiceMode;
	}

	public String[] getPlayerIds() {
		return fPlayerIds.toArray(new String[fPlayerIds.size()]);
	}

	public void addPlayerId(String pPlayerId) {
		if (StringTool.isProvided(pPlayerId)) {
			fPlayerIds.add(pPlayerId);
		}
	}

	private void addPlayerIds(String[] pPlayerIds) {
		if (ArrayTool.isProvided(pPlayerIds)) {
			for (int i = 0; i < pPlayerIds.length; i++) {
				addPlayerId(pPlayerIds[i]);
			}
		}
	}

	public String[] getDescriptions() {
		return fDescriptions.toArray(new String[fDescriptions.size()]);
	}

	public void addDescription(String pDescription) {
		if (StringTool.isProvided(pDescription)) {
			fDescriptions.add(pDescription);
		}
	}

	private void addDescriptions(String[] pDescriptions) {
		if (ArrayTool.isProvided(pDescriptions)) {
			for (int i = 0; i < pDescriptions.length; i++) {
				addDescription(pDescriptions[i]);
			}
		}
	}

	private static String[] findPlayerIds(Player<?>[] pPlayers) {
		if (ArrayTool.isProvided(pPlayers)) {
			String[] playerIds = new String[pPlayers.length];
			for (int i = 0; i < playerIds.length; i++) {
				playerIds[i] = pPlayers[i].getId();
			}
			return playerIds;
		} else {
			return new String[0];
		}
	}

	// transformation

	public IDialogParameter transform() {
		return new DialogPlayerChoiceParameter(getTeamId(), getPlayerChoiceMode(), getPlayerIds(), getDescriptions(),
				getMaxSelects());
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.DIALOG_ID.addTo(jsonObject, getId());
		IJsonOption.TEAM_ID.addTo(jsonObject, fTeamId);
		IJsonOption.PLAYER_CHOICE_MODE.addTo(jsonObject, fPlayerChoiceMode);
		IJsonOption.MAX_SELECTS.addTo(jsonObject, fMaxSelects);
		IJsonOption.PLAYER_IDS.addTo(jsonObject, fPlayerIds);
		IJsonOption.DESCRIPTIONS.addTo(jsonObject, fDescriptions);
		return jsonObject;
	}

	public DialogPlayerChoiceParameter initFrom(IFactorySource game, JsonValue pJsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
		UtilDialogParameter.validateDialogId(this, (DialogId) IJsonOption.DIALOG_ID.getFrom(game, jsonObject));
		fTeamId = IJsonOption.TEAM_ID.getFrom(game, jsonObject);
		fPlayerChoiceMode = (PlayerChoiceMode) IJsonOption.PLAYER_CHOICE_MODE.getFrom(game, jsonObject);
		fMaxSelects = IJsonOption.MAX_SELECTS.getFrom(game, jsonObject);
		addPlayerIds(IJsonOption.PLAYER_IDS.getFrom(game, jsonObject));
		addDescriptions(IJsonOption.DESCRIPTIONS.getFrom(game, jsonObject));
		return this;
	}

}