package com.fumbbl.ffb.server.step.generator.bb2016;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerLogLevel;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.generator.Sequence;

@RulesCollection(RulesCollection.Rules.BB2016)
public class StartGame extends com.fumbbl.ffb.server.step.generator.StartGame {

	@Override
	public void pushSequence(SequenceParams params) {
		GameState gameState = params.getGameState();
		gameState.getServer().getDebugLog().log(IServerLogLevel.DEBUG, gameState.getId(),
			"push startGameSequence onto stack");

		Sequence sequence = new Sequence(gameState);

		sequence.add(StepId.INIT_START_GAME);
		sequence.add(StepId.WEATHER);
		sequence.add(StepId.PETTY_CASH);
		sequence.add(StepId.BUY_CARDS);
		sequence.add(StepId.BUY_INDUCEMENTS);
		// inserts inducement sequence at this point
		sequence.add(StepId.SPECTATORS);
		// continues with kickoffSequence after that

		gameState.getStepStack().push(sequence.getSequence());

	}
}
