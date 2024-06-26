package com.fumbbl.ffb.client.dialog;

import com.fumbbl.ffb.ClientMode;
import com.fumbbl.ffb.ReRolledActions;
import com.fumbbl.ffb.SoundId;
import com.fumbbl.ffb.StatusType;
import com.fumbbl.ffb.client.ClientData;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.client.UserInterface;
import com.fumbbl.ffb.client.net.ClientCommunication;
import com.fumbbl.ffb.dialog.DialogBlockRollParameter;
import com.fumbbl.ffb.dialog.DialogId;
import com.fumbbl.ffb.model.BlockRoll;
import com.fumbbl.ffb.model.Game;

import java.util.Collections;

/**
 * 
 * @author Kalimar
 */
public class DialogBlockRollHandler extends DialogHandler {

	private DialogBlockRollParameter fDialogParameter;

	public DialogBlockRollHandler(FantasyFootballClient pClient) {
		super(pClient);
	}

	public void showDialog() {

		Game game = getClient().getGame();
		ClientData clientData = getClient().getClientData();
		UserInterface userInterface = getClient().getUserInterface();
		fDialogParameter = (DialogBlockRollParameter) game.getDialogParameter();

		if (fDialogParameter != null) {

			if ((ClientMode.PLAYER == getClient().getMode())
					&& game.getTeamHome().getId().equals(fDialogParameter.getChoosingTeamId())) {

				clientData.clearBlockDiceResult();
				setDialog(new DialogBlockRoll(getClient(), fDialogParameter));
				getDialog().showDialog(this);
				if (!game.isHomePlaying()) {
					playSound(SoundId.QUESTION);
				}

			} else {
				BlockRoll blockRoll = new BlockRoll();
				blockRoll.setBlockRoll(fDialogParameter.getBlockRoll());
				blockRoll.setNrOfDice(Math.abs(fDialogParameter.getNrOfDice()));
				blockRoll.setOwnChoice(fDialogParameter.getNrOfDice() >= 0);
				clientData.setBlockDiceResult(Collections.singletonList(blockRoll));
				if ((fDialogParameter.getNrOfDice() < 0) && game.isHomePlaying()) {
					showStatus("Block Roll", "Waiting for coach to choose Block Dice.", StatusType.WAITING);
				}

			}

			userInterface.refreshSideBars();

		}

	}

	public void dialogClosed(IDialog pDialog) {
		hideDialog();
		Game game = getClient().getGame();
		ClientData clientData = getClient().getClientData();
		if (testDialogHasId(pDialog, DialogId.BLOCK_ROLL)) {
			UserInterface userInterface = getClient().getUserInterface();
			DialogBlockRoll blockRollDialog = (DialogBlockRoll) pDialog;
			ClientCommunication communication = getClient().getCommunication();
			if (game.getTeamHome().getId().equals(fDialogParameter.getChoosingTeamId())) {
				BlockRoll blockRoll = new BlockRoll();
				blockRoll.setBlockRoll(fDialogParameter.getBlockRoll());
				blockRoll.setNrOfDice(Math.abs(fDialogParameter.getNrOfDice()));
				blockRoll.setOwnChoice(fDialogParameter.getNrOfDice() >= 0);
				blockRoll.setSelectedIndex(blockRollDialog.getDiceIndex());
				clientData.setBlockDiceResult(Collections.singletonList(blockRoll));
				if (blockRoll.needsSelection()) {
					communication.sendUseReRoll(ReRolledActions.BLOCK, blockRollDialog.getReRollSource());
				} else {
					communication.sendBlockChoice(blockRollDialog.getDiceIndex());
				}
				userInterface.refreshSideBars();
			}
		}
	}

}
