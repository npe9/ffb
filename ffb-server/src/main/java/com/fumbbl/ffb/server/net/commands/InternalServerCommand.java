package com.fumbbl.ffb.server.net.commands;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.FactoryType.FactoryContext;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.net.NetCommand;
import com.fumbbl.ffb.net.NetCommandId;
import com.fumbbl.ffb.net.commands.UtilNetCommand;

/**
 * 
 * @author Kalimar
 */
public abstract class InternalServerCommand extends NetCommand {

	protected static final String XML_ATTRIBUTE_GAME_ID = "gameId";

	private long fGameId;

	public InternalServerCommand() {
		this(0L);
	}

	public InternalServerCommand(long pGameId) {
		setGameId(pGameId);
	}

	public boolean isInternal() {
		return true;
	}

	public long getGameId() {
		return fGameId;
	}

	protected void setGameId(long pGameId) {
		fGameId = pGameId;
	}

	public FactoryContext getContext() {
		return FactoryContext.APPLICATION;
	}
	
	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.NET_COMMAND_ID.addTo(jsonObject, getId());
		if (fGameId > 0) {
			IJsonOption.GAME_ID.addTo(jsonObject, fGameId);
		}
		return jsonObject;
	}

	public InternalServerCommand initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilNetCommand.validateCommandId(this, (NetCommandId) IJsonOption.NET_COMMAND_ID.getFrom(source, jsonObject));
		fGameId = 0L;
		if (IJsonOption.GAME_ID.isDefinedIn(jsonObject)) {
			fGameId = IJsonOption.GAME_ID.getFrom(source, jsonObject);
		}
		return this;
	}

}
