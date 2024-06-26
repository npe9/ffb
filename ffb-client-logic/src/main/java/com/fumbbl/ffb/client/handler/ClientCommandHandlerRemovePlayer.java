package com.fumbbl.ffb.client.handler;

import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.GameResult;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.net.NetCommand;
import com.fumbbl.ffb.net.NetCommandId;
import com.fumbbl.ffb.net.commands.ServerCommandRemovePlayer;

public class ClientCommandHandlerRemovePlayer extends ClientCommandHandler {

	protected ClientCommandHandlerRemovePlayer(FantasyFootballClient pClient) {
		super(pClient);
	}

	public NetCommandId getId() {
		return NetCommandId.SERVER_REMOVE_PLAYER;
	}

	public boolean handleNetCommand(NetCommand pNetCommand, ClientCommandHandlerMode pMode) {

		ServerCommandRemovePlayer removePlayerCommand = (ServerCommandRemovePlayer) pNetCommand;

		Game game = getClient().getGame();
		GameResult gameResult = game.getGameResult();

		Player<?> player = game.getPlayerById(removePlayerCommand.getPlayerId());
		game.getFieldModel().remove(player);
		game.getFieldModel().setPlayerState(player, null);
		if (game.getTeamHome().hasPlayer(player)) {
			game.getTeamHome().removePlayer(player);
			gameResult.getTeamResultHome().removePlayerResult(player);
		}
		if (game.getTeamAway().hasPlayer(player)) {
			game.getTeamAway().removePlayer(player);
			gameResult.getTeamResultAway().removePlayerResult(player);
		}

		if (pMode == ClientCommandHandlerMode.PLAYING) {
			refreshGameMenuBar();
			refreshFieldComponent();
			refreshSideBars();
		}

		return true;

	}

}
