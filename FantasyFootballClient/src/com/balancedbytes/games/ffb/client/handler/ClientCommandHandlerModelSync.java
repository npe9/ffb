package com.balancedbytes.games.ffb.client.handler;

import com.balancedbytes.games.ffb.FieldCoordinate;
import com.balancedbytes.games.ffb.TurnMode;
import com.balancedbytes.games.ffb.client.ClientData;
import com.balancedbytes.games.ffb.client.FantasyFootballClient;
import com.balancedbytes.games.ffb.client.UserInterface;
import com.balancedbytes.games.ffb.client.animation.AnimationSequenceFactory;
import com.balancedbytes.games.ffb.client.animation.IAnimationListener;
import com.balancedbytes.games.ffb.client.animation.IAnimationSequence;
import com.balancedbytes.games.ffb.client.layer.FieldLayer;
import com.balancedbytes.games.ffb.client.util.UtilThrowTeamMate;
import com.balancedbytes.games.ffb.client.util.UtilTimeout;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Animation;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.change.old.IModelChange;
import com.balancedbytes.games.ffb.model.change.old.ModelChangeGameAttribute;
import com.balancedbytes.games.ffb.model.change.old.ModelChangeListOld;
import com.balancedbytes.games.ffb.model.change.old.ModelChangeTurnData;
import com.balancedbytes.games.ffb.net.NetCommand;
import com.balancedbytes.games.ffb.net.NetCommandId;
import com.balancedbytes.games.ffb.net.commands.ServerCommandModelSync;
import com.balancedbytes.games.ffb.report.IReport;
import com.balancedbytes.games.ffb.report.ReportBlockChoice;
import com.balancedbytes.games.ffb.report.ReportList;

/**
 * 
 * @author Kalimar
 */
public class ClientCommandHandlerModelSync extends ClientCommandHandler implements IAnimationListener {
  
  private ServerCommandModelSync fSyncCommand;
  private ClientCommandHandlerMode fMode;
  
  private FieldCoordinate fBallCoordinate;
  private FieldCoordinate fBombCoordinate;
  private FieldCoordinate fThrownPlayerCoordinate;
  
  private boolean fUpdateActingPlayer;
  private boolean fUpdateTurnNr;
  private boolean fUpdateTurnMode;
  private boolean fUpdateTimeout;
  private boolean fUpdateInducements;
  private boolean fClearSelectedPlayer;

  protected ClientCommandHandlerModelSync(FantasyFootballClient pClient) {
    super(pClient);
  }
  
  public NetCommandId getId() {
    return NetCommandId.SERVER_MODEL_SYNC;
  }

  public boolean handleNetCommand(NetCommand pNetCommand, ClientCommandHandlerMode pMode) {
    
    fSyncCommand = (ServerCommandModelSync) pNetCommand;
    fMode = pMode;

    Game game = getClient().getGame();
  
    if ((fMode == ClientCommandHandlerMode.QUEUING) || (fMode == ClientCommandHandlerMode.PLAYING)) {
      game.setGameTime(fSyncCommand.getGameTime());
      game.setTurnTime(fSyncCommand.getTurnTime());
    }

    if (fMode == ClientCommandHandlerMode.QUEUING) {
      return true;
    }
    
    ModelChangeListOld modelChangeList = fSyncCommand.getModelChanges();
    modelChangeList.applyTo(game);
    
    UserInterface userInterface = getClient().getUserInterface();

    if (pMode != ClientCommandHandlerMode.REPLAYING) {
      userInterface.getLog().markCommandBegin(fSyncCommand.getCommandNr());
      userInterface.getStatusReport().report(fSyncCommand.getReportList());
      userInterface.getLog().markCommandEnd(fSyncCommand.getCommandNr());
    }
  
    findUpdates(fSyncCommand.getModelChanges());
    
    handleExtraEffects(fSyncCommand.getReportList());

    Animation animation = fSyncCommand.getAnimation();
    boolean waitForAnimation = ((animation != null) && ((fMode == ClientCommandHandlerMode.PLAYING) || ((fMode == ClientCommandHandlerMode.REPLAYING) && getClient().getReplayer().isReplayingSingleSpeedForward())));

    // prepare for animation by hiding ball, bomb or thrown player
    
    if (waitForAnimation) {
    	switch (animation.getAnimationType()) {
      	case THROW_BOMB:
      	case HAIL_MARY_BOMB:
          game.getFieldModel().setRangeRuler(null);
      		fBombCoordinate = game.getFieldModel().getBombCoordinate();
      		game.getFieldModel().setBombCoordinate(null);
      		break;
      	case PASS:
      	case KICK:
      	case HAIL_MARY_PASS:
          game.getFieldModel().setRangeRuler(null);
      		fBallCoordinate = game.getFieldModel().getBallCoordinate();
      		game.getFieldModel().setBallCoordinate(null);
      		break;
      	case THROW_TEAM_MATE:
          game.getFieldModel().setRangeRuler(null);
          Player thrownPlayer = game.getPlayerById(animation.getThrownPlayerId());
          fThrownPlayerCoordinate = game.getFieldModel().getPlayerCoordinate(thrownPlayer);
          game.getFieldModel().remove(thrownPlayer);
          break;
        default:
        	break;
    	}
    }
    
    updateUserinterface();

    if (waitForAnimation) {
    	startAnimation(animation);
    } else {
      playSound(fSyncCommand.getSound(), fMode, true);
    }
      
    return !waitForAnimation;

  }
  
  public void animationFinished() {
    
    Game game = getClient().getGame();
    UserInterface userInterface = getClient().getUserInterface();

    Animation animation = fSyncCommand.getAnimation();
  	switch (animation.getAnimationType()) {
    	case THROW_BOMB:
    	case HAIL_MARY_BOMB:
        game.getFieldModel().setBombCoordinate(fBombCoordinate);
    		break;
    	case PASS:
    	case KICK:
    	case HAIL_MARY_PASS:
        game.getFieldModel().setBallCoordinate(fBallCoordinate);
    		break;
    	case THROW_TEAM_MATE:
        Player thrownPlayer = game.getPlayerById(animation.getThrownPlayerId());
        game.getFieldModel().setPlayerCoordinate(thrownPlayer, fThrownPlayerCoordinate);
        break;
      default:
      	break;
  	}
    
    userInterface.getFieldComponent().refresh();
    playSound(fSyncCommand.getSound(), fMode, true);

    getClient().getCommandHandlerFactory().updateClientState(fSyncCommand, fMode);
    
    if (fMode == ClientCommandHandlerMode.REPLAYING) {
    	getClient().getReplayer().resume();
    }
    
  }
  
  private void findUpdates(ModelChangeListOld pModelChangeList) {
    
    if (pModelChangeList != null) {

      fUpdateTurnNr = false;
      fUpdateTurnMode = false;
      fUpdateActingPlayer = false;
      fUpdateTimeout = false;
      fUpdateInducements = false;
      fClearSelectedPlayer = false;

      for (IModelChange modelChange : pModelChangeList.getChanges()) {
        switch (modelChange.getId()) {
          case ACTING_PLAYER_CHANGE:
            fUpdateActingPlayer = true;
            break;
          case TURN_DATA_CHANGE:
            ModelChangeTurnData turnDataChange = (ModelChangeTurnData) modelChange;
            switch (turnDataChange.getChange()) {
              case SET_TURN_NR:
                fUpdateTurnNr = true;
                break;
              case ADD_INDUCEMENT:
              case REMOVE_INDUCEMENT:
                fUpdateInducements = true;
                break;
              default:
              	break;
            }
            break;
          case GAME_ATTRIBUTE_CHANGE:
            ModelChangeGameAttribute gameAttributeChange = (ModelChangeGameAttribute) modelChange;
            switch (gameAttributeChange.getChange()) {
              case SET_TIMEOUT_POSSIBLE:
                fUpdateTimeout = true;
                break;
              case SET_DEFENDER_ID:
                fClearSelectedPlayer = (gameAttributeChange.getValue() != null);
                break;
              case SET_TURN_MODE:
              	fUpdateTurnMode = true;
              	break;
              default:
              	break;
            }
            break;
          default:
          	break;
        }
      }
      
    }
    
  }
  
  private void updateUserinterface() {

    ClientData clientData = getClient().getClientData();
    UserInterface userInterface = getClient().getUserInterface();
    Game game = getClient().getGame();
    ActingPlayer actingPlayer = game.getActingPlayer();

    if (fUpdateTimeout && (fMode == ClientCommandHandlerMode.PLAYING)) {
      UtilTimeout.showTimeoutStatus(getClient());    
    }
    
    if (fUpdateActingPlayer)  {
      clientData.setActingPlayerUpdated(true);
      userInterface.getFieldComponent().getLayerUnderPlayers().clearMovePath();
    	FieldCoordinate playerCoordinate = game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer());
    	if ((playerCoordinate != null) && !playerCoordinate.isBoxCoordinate()) {
    		userInterface.getFieldComponent().getLayerPlayers().updateBallAndPlayers(playerCoordinate, true);
    	}
    }
    
    if (fClearSelectedPlayer) {
      clientData.setSelectedPlayer(null);
    }

    if (fUpdateTurnNr || (fUpdateTurnMode && (TurnMode.KICKOFF != game.getTurnMode()))) {
      clientData.clear();
    }
        
    if (fUpdateInducements) {
      userInterface.getGameMenuBar().updateInducements();
    }
    
    if (fMode == ClientCommandHandlerMode.PLAYING) {
      UtilThrowTeamMate.updateThrownPlayer(getClient());
      refreshFieldComponent();
      updateDialog();
      refreshSideBars();
      refreshGameMenuBar();
    }
    
  }
  
  private void handleExtraEffects(ReportList pReportList) {
    ClientData clientData = getClient().getClientData();
    for (IReport report : pReportList.getReports()) {
      switch (report.getId()) {
        case BLOCK_CHOICE:
          ReportBlockChoice reportBlockChoice = (ReportBlockChoice) report;
          clientData.setBlockDiceResult(reportBlockChoice.getNrOfDice(), reportBlockChoice.getBlockRoll(), reportBlockChoice.getDiceIndex());
          break;
        default:
        	break;
      }
    }
  }
  
  private void startAnimation(Animation pAnimation) {
  	IAnimationSequence animationSequence = AnimationSequenceFactory.getInstance().getAnimationSequence(getClient(), pAnimation);
  	if (animationSequence != null) {
  		if (fMode == ClientCommandHandlerMode.REPLAYING) {
  			getClient().getReplayer().pause();
  		}
    	FieldLayer fieldLayerRangeRuler = getClient().getUserInterface().getFieldComponent().getLayerRangeRuler();
    	animationSequence.play(fieldLayerRangeRuler, this);
  	}
  }
  
}
