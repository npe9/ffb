package com.balancedbytes.games.ffb.client.net;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import com.balancedbytes.games.ffb.client.FantasyFootballClient;
import com.balancedbytes.games.ffb.client.GameTitle;
import com.balancedbytes.games.ffb.client.IClientProperty;
import com.balancedbytes.games.ffb.client.ui.GameTitleUpdateTask;
import com.balancedbytes.games.ffb.json.LZString;
import com.balancedbytes.games.ffb.net.NetCommand;
import com.balancedbytes.games.ffb.net.NetCommandFactory;
import com.balancedbytes.games.ffb.net.NetCommandId;
import com.balancedbytes.games.ffb.net.commands.ServerCommandPong;
import com.balancedbytes.games.ffb.util.StringTool;
import com.eclipsesource.json.JsonValue;

/**
 * 
 * @author Kalimar
 */
@ClientEndpoint
public class CommandEndpoint {

  private FantasyFootballClient fClient;
  private NetCommandFactory fNetCommandFactory;
  private boolean fCommandCompression;
  private Session fSession;

  private final CountDownLatch fCloseLatch;

  public CommandEndpoint(FantasyFootballClient pClient) {
    fClient = pClient;
    fNetCommandFactory = new NetCommandFactory();
    fCloseLatch = new CountDownLatch(1);
    String commandCompressionProperty = null;
    if (fClient != null) {
      commandCompressionProperty = fClient.getProperty(IClientProperty.CLIENT_COMMAND_COMPRESSION);
    }
    fCommandCompression = false;
    if (StringTool.isProvided(commandCompressionProperty)) {
      fCommandCompression = Boolean.parseBoolean(commandCompressionProperty);
    }
  }

  @OnOpen
  public void onOpen(Session session, EndpointConfig endpointConfig) {
    fSession = session;
  }

  @OnMessage
  public void onMessage(String pTextMessage) {

    if (!StringTool.isProvided(pTextMessage) || !isOpen()) {
      return;
    }

    JsonValue jsonValue = JsonValue.readFrom(
      fCommandCompression ? LZString.decompressFromUTF16(pTextMessage) : pTextMessage
    );

    handleNetCommand(fNetCommandFactory.forJsonValue(jsonValue));

  }
  
  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    fClient.getUserInterface().getStatusReport().reportSocketClosed();
    fCloseLatch.countDown();
  }

  public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
    return fCloseLatch.await(duration, unit);
  }

  public boolean send(NetCommand pCommand) throws IOException {

    if ((pCommand == null) || !isOpen()) {
      return false;
    }

    JsonValue jsonValue = pCommand.toJsonValue();
    if (jsonValue == null) {
      return false;
    }

    String textMessage = jsonValue.toString();
    if (fCommandCompression) {
      textMessage = LZString.compressToUTF16(textMessage);
    }

    if (!StringTool.isProvided(textMessage)) {
      return false;
    }

    fSession.getAsyncRemote().sendText(textMessage);
    return true;

  }

  public boolean isOpen() {
    return ((fSession != null) && fSession.isOpen());
  }
  
  private void handleNetCommand(NetCommand netCommand) {
   if (netCommand == null) {
      return;
    } else if (NetCommandId.SERVER_PONG == netCommand.getId()) {
      ServerCommandPong pongCommand = (ServerCommandPong) netCommand;
      if (pongCommand.getTimestamp() > 0) {
        long received = System.currentTimeMillis();
        fClient.getClientPingTask().setLastPingReceived(received);
        GameTitle gameTitle = new GameTitle();
        gameTitle.setPingTime(received - pongCommand.getTimestamp());
        fClient.getUserInterface().invokeLater(
          new GameTitleUpdateTask(fClient, gameTitle)
        );
      }
    } else {
      fClient.getCommunication().handleCommand(netCommand);
    }
  }

}