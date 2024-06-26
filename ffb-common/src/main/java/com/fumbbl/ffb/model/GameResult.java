package com.fumbbl.ffb.model;

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
public class GameResult implements IJsonSerializable {

	private TeamResult fTeamResultHome;
	private TeamResult fTeamResultAway;

	private final transient Game fGame;

	public GameResult(Game pGame) {
		this(pGame, null, null);
	}

	private GameResult(Game pGame, TeamResult pTeamResultHome, TeamResult pTeamResultAway) {
		fGame = pGame;
		fTeamResultHome = pTeamResultHome;
		if (fTeamResultHome == null) {
			fTeamResultHome = new TeamResult(this, true);
			fTeamResultHome.setTeam(fGame.getTeamHome());
		}
		fTeamResultAway = pTeamResultAway;
		if (fTeamResultAway == null) {
			fTeamResultAway = new TeamResult(this, false);
			fTeamResultAway.setTeam(fGame.getTeamAway());
		}
	}

	public Game getGame() {
		return fGame;
	}

	public TeamResult getTeamResultHome() {
		return fTeamResultHome;
	}

	public TeamResult getTeamResultAway() {
		return fTeamResultAway;
	}

	public int getScoreHome() {
		return getTeamResultHome().getScore();
	}

	public int getScoreAway() {
		return getTeamResultAway().getScore();
	}

	public GameResult transform() {
		TeamResult transformedTeamResultHome = new TeamResult(this, true);
		transformedTeamResultHome.setTeam(getTeamResultAway().getTeam());
		transformedTeamResultHome.init(getTeamResultAway());
		TeamResult transformedTeamResultAway = new TeamResult(this, false);
		transformedTeamResultAway.setTeam(getTeamResultHome().getTeam());
		transformedTeamResultAway.init(getTeamResultHome());
		return new GameResult(getGame(), transformedTeamResultHome, transformedTeamResultAway);
	}

	public PlayerResult getPlayerResult(Player<?> pPlayer) {
		if (getGame().getTeamHome().hasPlayer(pPlayer)) {
			return getTeamResultHome().getPlayerResult(pPlayer);
		} else {
			return getTeamResultAway().getPlayerResult(pPlayer);
		}
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.TEAM_RESULT_HOME.addTo(jsonObject, fTeamResultHome.toJsonValue());
		IJsonOption.TEAM_RESULT_AWAY.addTo(jsonObject, fTeamResultAway.toJsonValue());
		return jsonObject;
	}

	public GameResult initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fTeamResultHome.initFrom(source, IJsonOption.TEAM_RESULT_HOME.getFrom(source, jsonObject));
		fTeamResultAway.initFrom(source, IJsonOption.TEAM_RESULT_AWAY.getFrom(source, jsonObject));
		return this;
	}

}
