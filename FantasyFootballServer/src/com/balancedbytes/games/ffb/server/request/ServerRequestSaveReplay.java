package com.balancedbytes.games.ffb.server.request;

import com.balancedbytes.games.ffb.server.FantasyFootballServer;
import com.balancedbytes.games.ffb.server.GameCache;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.admin.UtilBackup;


/**
 * 
 * @author Kalimar
 */
public class ServerRequestSaveReplay extends ServerRequest {

  private long fGameId;

  public ServerRequestSaveReplay(long gameId) {
    fGameId = gameId;
  }

  public long getGameId() {
    return fGameId;
  }
  
  @Override
  public void process(ServerRequestProcessor pRequestProcessor) {
    FantasyFootballServer server = pRequestProcessor.getServer();
    GameCache gameCache = server.getGameCache();
    GameState gameState = gameCache.getGameStateById(getGameId());
    if (gameState == null) {
      gameState = gameCache.queryFromDb(getGameId());
    }
    if (gameState == null) {
      // game already backed up - nothing to be done
      return;
    }
    boolean backupOk = UtilBackup.save(gameState);
    if (backupOk) {
      // request replay to see if backup has been successful, queue delete command
      server.getRequestProcessor().add(new ServerRequestLoadReplay(gameState.getId(), 0, null, ServerRequestLoadReplay.DELETE_GAME));
    }
  }
    
}
