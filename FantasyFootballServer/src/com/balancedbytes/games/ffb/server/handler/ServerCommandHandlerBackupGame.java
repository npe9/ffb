package com.balancedbytes.games.ffb.server.handler;

import com.balancedbytes.games.ffb.net.NetCommandId;
import com.balancedbytes.games.ffb.server.FantasyFootballServer;
import com.balancedbytes.games.ffb.server.GameCache;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerLogLevel;
import com.balancedbytes.games.ffb.server.admin.UtilBackup;
import com.balancedbytes.games.ffb.server.net.ReceivedCommand;
import com.balancedbytes.games.ffb.server.net.commands.InternalServerCommandBackupGame;

/**
 * 
 * @author Kalimar
 */
public class ServerCommandHandlerBackupGame extends ServerCommandHandler {

  protected ServerCommandHandlerBackupGame(FantasyFootballServer pServer) {
    super(pServer);
  }
  
  public NetCommandId getId() {
    return NetCommandId.INTERNAL_SERVER_BACKUP_GAME;
  }

  public void handleCommand(ReceivedCommand pReceivedCommand) {
    InternalServerCommandBackupGame backupGameCommand = (InternalServerCommandBackupGame) pReceivedCommand.getCommand();
    GameState gameState = loadGameStateById(backupGameCommand.getGameId());
    if (gameState == null) {
      getServer().getDebugLog().log(IServerLogLevel.WARN, backupGameCommand.getGameId(), "Unable to backup game - game not found.");
      return;
    }
    boolean backupOk = UtilBackup.save(gameState);
    if (backupOk) {
      gameState.setSwappedOut(true);
      gameState.getServer().getGameCache().queueDbUpdate(gameState, false);
    }
  }
  
  private GameState loadGameStateById(long pGameId) {
    GameCache gameCache = getServer().getGameCache();
    GameState gameState = gameCache.getGameStateById(pGameId);
    if (gameState == null) {
      gameState = gameCache.queryFromDb(pGameId);
    }
    return gameState;
  }
  
}