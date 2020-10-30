package com.balancedbytes.games.ffb.server.skillbehaviour;

import com.balancedbytes.games.ffb.ReRollSource;
import com.balancedbytes.games.ffb.ReRolledAction;
import com.balancedbytes.games.ffb.net.commands.ClientCommandUseSkill;
import com.balancedbytes.games.ffb.server.model.SkillBehaviour;
import com.balancedbytes.games.ffb.server.model.StepModifier;
import com.balancedbytes.games.ffb.server.step.StepCommandStatus;
import com.balancedbytes.games.ffb.server.step.action.common.StepCatchScatterThrowIn;
import com.balancedbytes.games.ffb.server.step.action.common.StepCatchScatterThrowIn.StepState;
import com.balancedbytes.games.ffb.skill.Catch;

public class CatchBehaviour extends SkillBehaviour<Catch> {
  public CatchBehaviour() {
    super();
    
    registerModifier(new StepModifier<StepCatchScatterThrowIn, StepCatchScatterThrowIn.StepState>() {

      @Override
      public StepCommandStatus handleCommandHook(StepCatchScatterThrowIn step, StepState state,
          ClientCommandUseSkill useSkillCommand) {
        return null;
      }

      @Override
      public boolean handleExecuteStepHook(StepCatchScatterThrowIn step, StepState state) {
        step.setReRolledAction(ReRolledAction.CATCH);
        step.setReRollSource(ReRollSource.CATCH);

        return true;
      }
      
    });
  }
}