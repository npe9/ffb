package com.fumbbl.ffb.client.dialog;

import com.fumbbl.ffb.ClientMode;
import com.fumbbl.ffb.StatusType;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.dialog.DialogId;

/**
 * 
 * @author Kalimar
 */
public class DialogStartGameHandler extends DialogHandler {

	public DialogStartGameHandler(FantasyFootballClient pClient) {
		super(pClient);
	}

	public void showDialog() {
		if (ClientMode.PLAYER == getClient().getMode()) {
			if (getClient().getGame().getScheduled() != null) {
				getClient().getCommunication().sendStartGame();
			} else {
				setDialog(new DialogStartGame(getClient()));
				getDialog().showDialog(this);
			}
		}
	}

	public void dialogClosed(IDialog pDialog) {
		hideDialog();
		if (testDialogHasId(pDialog, DialogId.START_GAME)) {
			DialogStartGame gameStartDialog = (DialogStartGame) pDialog;
			if (gameStartDialog.isChoiceYes()) {
				getClient().getCommunication().sendStartGame();
				showStatus("Start game", "Waiting for coach to start the game.", StatusType.WAITING);
			} else {
				getClient().exitClient();
			}
		}
	}

}
