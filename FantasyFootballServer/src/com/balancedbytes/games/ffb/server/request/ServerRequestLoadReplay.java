package com.balancedbytes.games.ffb.server.request;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import com.balancedbytes.games.ffb.FantasyFootballException;
import com.balancedbytes.games.ffb.server.FantasyFootballServer;
import com.balancedbytes.games.ffb.server.GameCacheMode;
import com.balancedbytes.games.ffb.server.GameState;
import com.balancedbytes.games.ffb.server.IServerProperty;
import com.balancedbytes.games.ffb.server.net.ReceivedCommand;
import com.balancedbytes.games.ffb.server.net.commands.InternalServerCommandReplayLoaded;
import com.balancedbytes.games.ffb.server.util.UtilHttpClient;
import com.balancedbytes.games.ffb.util.StringTool;
import com.eclipsesource.json.JsonValue;


/**
 * 
 * @author Kalimar
 */
public class ServerRequestLoadReplay extends ServerRequest {

  private long fGameId;
  private int fReplayToCommandNr;
  private Session fSession;

  public ServerRequestLoadReplay(long pGameId, int pReplayToCommandNr, Session pSession) {
    fGameId = pGameId;
    fReplayToCommandNr = pReplayToCommandNr;
    fSession = pSession;
  }

  public long getGameId() {
    return fGameId;
  }
  
  public Session getSession() {
    return fSession;
  }
  
  public int getReplayToCommandNr() {
    return fReplayToCommandNr;
  }
  
  @Override
  public void process(ServerRequestProcessor pRequestProcessor) {
    FantasyFootballServer server = pRequestProcessor.getServer();
    GameState gameState = null;
    try {
      String loadUrl = StringTool.bind(server.getProperty(IServerProperty.BACKUP_URL_LOAD), getGameId());
      String response = UtilHttpClient.fetchPage(loadUrl);
      if (StringTool.isProvided(response)) {
        JsonValue jsonValue = JsonValue.readFrom(response);
        if ((jsonValue != null) && !jsonValue.isNull()) {
          gameState = new GameState(server);
          gameState.initFrom(jsonValue);
        }
      }
    } catch (IOException ioe) {
      throw new FantasyFootballException(ioe);
    }
    if (gameState != null) {
      server.getGameCache().add(gameState, GameCacheMode.REPLAY_GAME);
      InternalServerCommandReplayLoaded replayLoadedCommand = new InternalServerCommandReplayLoaded(getGameId(), getReplayToCommandNr());
      server.getCommunication().handleCommand(new ReceivedCommand(replayLoadedCommand, getSession()));
    }
  }
  
}