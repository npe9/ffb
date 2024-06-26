package com.fumbbl.ffb.model;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.PlayerState;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.IJsonSerializable;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.change.ModelChange;
import com.fumbbl.ffb.model.change.ModelChangeId;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kalimar
 */
public class TeamResult implements IJsonSerializable {

	private int fScore;
	private int fFame;
	private int fSpectators;
	private int fWinnings;
	private int fFanFactorModifier;
	private int fSpirallingExpenses;

	private int fBadlyHurtSuffered;
	private int fSeriousInjurySuffered;
	private int fRipSuffered;

	private boolean fConceded;
	private int fRaisedDead;
	private int fPettyCashTransferred;
	private int fPettyCashUsed;
	private int fTeamValue;
	private int pettyCashFromTvDiff;
	private int treasurySpentOnInducements;
	private int fanFactor;
	private int dedicatedFansModifier;
	private int penaltyScore;

	private final Map<String, PlayerResult> fPlayerResultByPlayerId;

	private final transient GameResult fGameResult;
	private transient Team fTeam;
	private final transient boolean fHomeData;

	public TeamResult(GameResult pGameResult, boolean pHomeData) {
		fGameResult = pGameResult;
		fHomeData = pHomeData;
		fPlayerResultByPlayerId = new HashMap<>();
		penaltyScore = -1;
	}

	public GameResult getGameResult() {
		return fGameResult;
	}

	public boolean isHomeData() {
		return fHomeData;
	}

	public void setTeam(Team pTeam) {
		fTeam = pTeam;
	}

	public Team getTeam() {
		return fTeam;
	}

	public void setConceded(boolean pConceded) {
		if (pConceded == fConceded) {
			return;
		}
		fConceded = pConceded;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_CONCEDED, fConceded);
	}

	public boolean hasConceded() {
		return fConceded;
	}

	public void setRaisedDead(int pRaisedDead) {
		if (pRaisedDead == fRaisedDead) {
			return;
		}
		fRaisedDead = pRaisedDead;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_RAISED_DEAD, fRaisedDead);
	}

	public int getRaisedDead() {
		return fRaisedDead;
	}

	public int getDedicatedFansModifier() {
		return dedicatedFansModifier;
	}

	public void setDedicatedFansModifier(int dedicatedFansModifier) {
		if (this.dedicatedFansModifier == dedicatedFansModifier) {
			return;
		}
		this.dedicatedFansModifier = dedicatedFansModifier;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER, dedicatedFansModifier);
	}

	public int getPenaltyScore() {
		return penaltyScore;
	}

	public void setPenaltyScore(int penaltyScore) {
		if (this.penaltyScore == penaltyScore) {
			return;
		}
		this.penaltyScore = penaltyScore;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_PENALTY_SCORE, penaltyScore);
	}

	public int getFame() {
		return fFame;
	}

	public void setFame(int pFame) {
		if (pFame == fFame) {
			return;
		}
		fFame = pFame;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_FAME, fFame);
	}

	public int getSpectators() {
		return fSpectators;
	}

	public void setSpectators(int pSpectators) {
		if (pSpectators == fSpectators) {
			return;
		}
		fSpectators = pSpectators;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_SPECTATORS, fSpectators);
	}

	public int getWinnings() {
		return fWinnings;
	}

	public void setWinnings(int pWinnings) {
		if (pWinnings == fWinnings) {
			return;
		}
		fWinnings = pWinnings;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_WINNINGS, fWinnings);
	}

	public int getFanFactorModifier() {
		return fFanFactorModifier;
	}

	public void setFanFactorModifier(int pFanFactorModifier) {
		if (pFanFactorModifier == fFanFactorModifier) {
			return;
		}
		fFanFactorModifier = pFanFactorModifier;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR_MODIFIER, fFanFactorModifier);
	}

	public int getScore() {
		return fScore;
	}

	public void setScore(int pScore) {
		if (pScore == fScore) {
			return;
		}
		fScore = pScore;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_SCORE, fScore);
	}

	public void sufferInjury(PlayerState pPlayerState) {
		if (pPlayerState != null) {
			switch (pPlayerState.getBase()) {
				case PlayerState.BADLY_HURT:
					setBadlyHurtSuffered(getBadlyHurtSuffered() + 1);
					break;
				case PlayerState.SERIOUS_INJURY:
					setSeriousInjurySuffered(getSeriousInjurySuffered() + 1);
					break;
				case PlayerState.RIP:
					setRipSuffered(getRipSuffered() + 1);
					break;
				default:
					break;
			}
		}
	}

	public int getBadlyHurtSuffered() {
		return fBadlyHurtSuffered;
	}

	public void setBadlyHurtSuffered(int pBadlyHurtSuffered) {
		if (pBadlyHurtSuffered == fBadlyHurtSuffered) {
			return;
		}
		fBadlyHurtSuffered = pBadlyHurtSuffered;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_BADLY_HURT_SUFFERED, fBadlyHurtSuffered);
	}

	public int getSeriousInjurySuffered() {
		return fSeriousInjurySuffered;
	}

	public void setSeriousInjurySuffered(int pSeriousInjurySuffered) {
		if (pSeriousInjurySuffered == fSeriousInjurySuffered) {
			return;
		}
		fSeriousInjurySuffered = pSeriousInjurySuffered;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED, fSeriousInjurySuffered);
	}

	public int getRipSuffered() {
		return fRipSuffered;
	}

	public void setRipSuffered(int pRipSuffered) {
		if (pRipSuffered == fRipSuffered) {
			return;
		}
		fRipSuffered = pRipSuffered;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_RIP_SUFFERED, fRipSuffered);
	}

	public int getSpirallingExpenses() {
		return fSpirallingExpenses;
	}

	public void setSpirallingExpenses(int pSpirallingExpenses) {
		if (pSpirallingExpenses == fSpirallingExpenses) {
			return;
		}
		fSpirallingExpenses = pSpirallingExpenses;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_SPIRALLING_EXPENSES, fSpirallingExpenses);
	}

	public int getPettyCashTransferred() {
		return fPettyCashTransferred;
	}

	public void setPettyCashTransferred(int pPettyCash) {
		if (pPettyCash == fPettyCashTransferred) {
			return;
		}
		fPettyCashTransferred = pPettyCash;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED, fPettyCashTransferred);
	}

	public int getPettyCashUsed() {
		return fPettyCashUsed;
	}

	public void setPettyCashUsed(int pPettyCash) {
		if (pPettyCash == fPettyCashUsed) {
			return;
		}
		fPettyCashUsed = pPettyCash;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_USED, fPettyCashUsed);
	}

	public int getTeamValue() {
		return fTeamValue;
	}

	public void setTeamValue(int pTeamValue) {
		if (pTeamValue == fTeamValue) {
			return;
		}
		fTeamValue = pTeamValue;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_TEAM_VALUE, fTeamValue);
	}

	public int getPettyCashFromTvDiff() {
		return pettyCashFromTvDiff;
	}

	public void setPettyCashFromTvDiff(int pettyCashFromTvDiff) {
		this.pettyCashFromTvDiff = pettyCashFromTvDiff;
	}

	public int getTreasurySpentOnInducements() {
		return treasurySpentOnInducements;
	}

	public void setTreasurySpentOnInducements(int treasurySpentOnInducements) {
		this.treasurySpentOnInducements = treasurySpentOnInducements;
	}

	public int getFanFactor() {
		return fanFactor;
	}

	public void setFanFactor(int fanFactor) {
		if (this.fanFactor == fanFactor) {
			return;
		}
		this.fanFactor = fanFactor;
		notifyObservers(ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR, fanFactor);
	}

	public int totalCompletions() {
		int completions = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			completions += getPlayerResult(player).getCompletions();
		}
		return completions;
	}

	public int totalInterceptions() {
		int interceptions = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			interceptions += getPlayerResult(player).getInterceptions();
		}
		return interceptions;
	}

	public int totalDeflections() {
		int deflections = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			deflections += getPlayerResult(player).getDeflections();
		}
		return deflections;
	}

	public int totalCasualties() {
		int casualties = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			casualties += getPlayerResult(player).getCasualties();
		}
		return casualties;
	}

	public int totalBlocks() {
		int blocks = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			blocks += getPlayerResult(player).getBlocks();
		}
		return blocks;
	}

	public int totalFouls() {
		int fouls = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			fouls += getPlayerResult(player).getFouls();
		}
		return fouls;
	}

	public int totalRushing() {
		int rushing = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			rushing += getPlayerResult(player).getRushing();
		}
		return rushing;
	}

	public int totalPassing() {
		int passing = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			passing += getPlayerResult(player).getPassing();
		}
		return passing;
	}

	public int totalEarnedSpps() {
		int earnedSpps = 0;
		for (Player<?> player : getTeam().getPlayers()) {
			earnedSpps += getPlayerResult(player).totalEarnedSpps();
		}
		return earnedSpps;
	}

	public PlayerResult getPlayerResult(Player<?> pPlayer) {
		String playerId = (pPlayer != null) ? pPlayer.getId() : null;
		PlayerResult playerResult = fPlayerResultByPlayerId.get(playerId);
		if ((playerResult == null) && getTeam().hasPlayer(pPlayer)) {
			playerResult = new PlayerResult(this, pPlayer);
			fPlayerResultByPlayerId.put(playerResult.getPlayerId(), playerResult);
		}
		return playerResult;
	}

	public void removePlayerResult(Player<?> pPlayer) {
		String playerId = (pPlayer != null) ? pPlayer.getId() : null;
		fPlayerResultByPlayerId.remove(playerId);
	}

	public Game getGame() {
		return getGameResult().getGame();
	}

	public void init(TeamResult pTeamResult) {
		if (pTeamResult != null) {
			fScore = pTeamResult.getScore();
			fFame = pTeamResult.getFame();
			fSpectators = pTeamResult.getSpectators();
			fWinnings = pTeamResult.getWinnings();
			fFanFactorModifier = pTeamResult.getFanFactorModifier();
			fSpirallingExpenses = pTeamResult.getSpirallingExpenses();
			fBadlyHurtSuffered = pTeamResult.getBadlyHurtSuffered();
			fSeriousInjurySuffered = pTeamResult.getSeriousInjurySuffered();
			fRipSuffered = pTeamResult.getRipSuffered();
			fConceded = pTeamResult.hasConceded();
			fRaisedDead = pTeamResult.getRaisedDead();
			fPettyCashTransferred = pTeamResult.getPettyCashTransferred();
			fPettyCashUsed = pTeamResult.getPettyCashUsed();
			fTeamValue = pTeamResult.getTeamValue();
			fanFactor = pTeamResult.getFanFactor();
			pettyCashFromTvDiff = pTeamResult.getPettyCashFromTvDiff();
			treasurySpentOnInducements = pTeamResult.getTreasurySpentOnInducements();
			for (Player<?> player : fTeam.getPlayers()) {
				PlayerResult oldPlayerResult = pTeamResult.getPlayerResult(player);
				PlayerResult newPlayerResult = new PlayerResult(this);
				newPlayerResult.init(oldPlayerResult);
				fPlayerResultByPlayerId.put(player.getId(), newPlayerResult);
			}
		}
	}

	// change tracking

	private void notifyObservers(ModelChangeId pChangeId, Object pValue) {
		if ((getGame() == null) || (pChangeId == null)) {
			return;
		}
		String key = isHomeData() ? ModelChange.HOME : ModelChange.AWAY;
		ModelChange modelChange = new ModelChange(pChangeId, key, pValue);
		getGame().notifyObservers(modelChange);
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.SCORE.addTo(jsonObject, fScore);
		IJsonOption.CONCEDED.addTo(jsonObject, fConceded);
		IJsonOption.RAISED_DEAD.addTo(jsonObject, fRaisedDead);
		IJsonOption.SPECTATORS.addTo(jsonObject, fSpectators);
		IJsonOption.FAME.addTo(jsonObject, fFame);
		IJsonOption.WINNINGS.addTo(jsonObject, fWinnings);
		IJsonOption.FAN_FACTOR_MODIFIER.addTo(jsonObject, fFanFactorModifier);
		IJsonOption.BADLY_HURT_SUFFERED.addTo(jsonObject, fBadlyHurtSuffered);
		IJsonOption.SERIOUS_INJURY_SUFFERED.addTo(jsonObject, fSeriousInjurySuffered);
		IJsonOption.RIP_SUFFERED.addTo(jsonObject, fRipSuffered);
		IJsonOption.SPIRALLING_EXPENSES.addTo(jsonObject, fSpirallingExpenses);
		if (getTeam() != null) {
			JsonArray playerResultArray = new JsonArray();
			for (Player<?> player : getTeam().getPlayers()) {
				playerResultArray.add(getPlayerResult(player).toJsonValue());
			}
			IJsonOption.PLAYER_RESULTS.addTo(jsonObject, playerResultArray);
		}
		IJsonOption.PETTY_CASH_FROM_TV_DIFF.addTo(jsonObject, pettyCashFromTvDiff);
		IJsonOption.PETTY_CASH_TRANSFERRED.addTo(jsonObject, fPettyCashTransferred);
		IJsonOption.PETTY_CASH_USED.addTo(jsonObject, fPettyCashUsed);
		IJsonOption.TEAM_VALUE.addTo(jsonObject, fTeamValue);
		IJsonOption.TREASURY_USED_ON_INDUCEMENTS.addTo(jsonObject, treasurySpentOnInducements);
		IJsonOption.FAN_FACTOR.addTo(jsonObject, fanFactor);
		IJsonOption.DEDICATED_FANS.addTo(jsonObject, dedicatedFansModifier);
		IJsonOption.PENALTY_SCORE.addTo(jsonObject, penaltyScore);
		return jsonObject;
	}

	public TeamResult initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fScore = IJsonOption.SCORE.getFrom(source, jsonObject);
		fConceded = IJsonOption.CONCEDED.getFrom(source, jsonObject);
		fRaisedDead = IJsonOption.RAISED_DEAD.getFrom(source, jsonObject);
		fSpectators = IJsonOption.SPECTATORS.getFrom(source, jsonObject);
		fFame = IJsonOption.FAME.getFrom(source, jsonObject);
		fWinnings = IJsonOption.WINNINGS.getFrom(source, jsonObject);
		fFanFactorModifier = IJsonOption.FAN_FACTOR_MODIFIER.getFrom(source, jsonObject);
		fBadlyHurtSuffered = IJsonOption.BADLY_HURT_SUFFERED.getFrom(source, jsonObject);
		fSeriousInjurySuffered = IJsonOption.SERIOUS_INJURY_SUFFERED.getFrom(source, jsonObject);
		fRipSuffered = IJsonOption.RIP_SUFFERED.getFrom(source, jsonObject);
		fSpirallingExpenses = IJsonOption.SPIRALLING_EXPENSES.getFrom(source, jsonObject);
		fPlayerResultByPlayerId.clear();
		JsonArray playerResultArray = IJsonOption.PLAYER_RESULTS.getFrom(source, jsonObject);
		if (playerResultArray != null) {
			for (int i = 0; i < playerResultArray.size(); i++) {
				PlayerResult playerResult = new PlayerResult(this);
				playerResult.initFrom(source, playerResultArray.get(i));
				fPlayerResultByPlayerId.put(playerResult.getPlayer().getId(), playerResult);
			}
		}
		pettyCashFromTvDiff = IJsonOption.PETTY_CASH_FROM_TV_DIFF.getFrom(source, jsonObject);
		fPettyCashTransferred = IJsonOption.PETTY_CASH_TRANSFERRED.getFrom(source, jsonObject);
		fPettyCashUsed = IJsonOption.PETTY_CASH_USED.getFrom(source, jsonObject);
		fTeamValue = IJsonOption.TEAM_VALUE.getFrom(source, jsonObject);
		treasurySpentOnInducements = IJsonOption.TREASURY_USED_ON_INDUCEMENTS.getFrom(source, jsonObject);
		fanFactor = IJsonOption.FAN_FACTOR.getFrom(source, jsonObject);
		dedicatedFansModifier = IJsonOption.DEDICATED_FANS.getFrom(source, jsonObject);
		penaltyScore = IJsonOption.PENALTY_SCORE.getFrom(source, jsonObject);
		return this;
	}

}
