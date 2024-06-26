package com.fumbbl.ffb.server.util;

import com.fumbbl.ffb.Direction;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.Team;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.util.UtilPlayer;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Kalimar
 */
public class UtilServerCatchScatterThrowIn {

	public static Player<?>[] findDivingCatchers(GameState pGameState, Team pTeam, FieldCoordinate pCoordinate) {
		Set<Player<?>> divingCatchPlayers = new HashSet<>();
		Game game = pGameState.getGame();
		Player<?>[] adjacentPlayers = UtilPlayer.findAdjacentPlayersWithTacklezones(game, pTeam, pCoordinate, false);
		for (Player<?> player : adjacentPlayers) {
			if (player.hasSkillProperty(NamedProperties.canAttemptCatchInAdjacentSquares)) {
				divingCatchPlayers.add(player);
			}
		}
		Player<?>[] playerArray = divingCatchPlayers.toArray(new Player[0]);
		UtilPlayer.sortByPlayerNr(playerArray);
		return playerArray;
	}

	public static FieldCoordinate findScatterCoordinate(FieldCoordinate pStartCoordinate, Direction pScatterDirection,
			int pScatterDistance) {
		if (pStartCoordinate == null) {
			throw new IllegalArgumentException("Parameter startCoordinate must not be null.");
		}
		if (pScatterDirection == null) {
			throw new IllegalArgumentException("Parameter scatterDirection must not be null.");
		}
		switch (pScatterDirection) {
		case NORTH:
			return pStartCoordinate.add(0, -pScatterDistance);
		case NORTHEAST:
			return pStartCoordinate.add(pScatterDistance, -pScatterDistance);
		case EAST:
			return pStartCoordinate.add(pScatterDistance, 0);
		case SOUTHEAST:
			return pStartCoordinate.add(pScatterDistance, pScatterDistance);
		case SOUTH:
			return pStartCoordinate.add(0, pScatterDistance);
		case SOUTHWEST:
			return pStartCoordinate.add(-pScatterDistance, pScatterDistance);
		case WEST:
			return pStartCoordinate.add(-pScatterDistance, 0);
		case NORTHWEST:
			return pStartCoordinate.add(-pScatterDistance, -pScatterDistance);
		default:
			throw new IllegalStateException("Unable to determine scatterCoordinate.");
		}
	}

}
