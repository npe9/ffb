package com.fumbbl.ffb.client.report;

import com.fumbbl.ffb.FactoryType.Factory;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.client.TextStyle;
import com.fumbbl.ffb.mechanics.AgilityMechanic;
import com.fumbbl.ffb.mechanics.Mechanic;
import com.fumbbl.ffb.mechanics.Wording;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.ReportInterceptionRoll;

@ReportMessageType(ReportId.INTERCEPTION_ROLL)
@RulesCollection(Rules.COMMON)
public class InterceptionRollMessage extends ReportMessageBase<ReportInterceptionRoll> {

    @Override
    protected void render(ReportInterceptionRoll report) {
  		StringBuilder status = new StringBuilder();
  		StringBuilder neededRoll = null;
  		Player<?> player = game.getPlayerById(report.getPlayerId());
			Wording wording = ((AgilityMechanic) game.getFactory(Factory.MECHANIC).forName(Mechanic.Type.AGILITY.name())).interceptionWording(report.isIgnoreAgility());
  		if (!report.isReRolled()) {
  			print(getIndent(), true, player);
  			if (report.isBomb()) {
  				println(getIndent(), TextStyle.BOLD, " tries to " + wording.getVerb() + " the bomb:");
  			} else {
  				println(getIndent(), TextStyle.BOLD, " tries to " + wording.getVerb() + " the ball:");
  			}
  		}
  		status.append(wording.getNoun()).append(" Roll [ ").append(report.getRoll()).append(" ]");
  		println(getIndent() + 1, TextStyle.ROLL, status.toString());
  		print(getIndent() + 2, false, player);
  		if (report.isSuccessful()) {
  			if (report.isBomb()) {
  				println(getIndent() + 2, " " + wording.getInflection() + " the bomb.");
  			} else {
  				println(getIndent() + 2, " " + wording.getInflection() + " the ball.");
  			}
  			if (!report.isReRolled()) {
  				neededRoll = new StringBuilder().append("Succeeded on a roll of ").append(report.getMinimumRoll()).append("+");
  			}
  		} else {
  			if (report.isBomb()) {
  				println(getIndent() + 2, " fails to " + wording.getVerb() + " the bomb.");
  			} else {
  				println(getIndent() + 2, " fails to " + wording.getVerb() + " the ball.");
  			}
  			if (!report.isReRolled()) {
  				neededRoll = new StringBuilder().append("Roll a ").append(report.getMinimumRoll()).append("+ to succeed");
  			}
  		}
  		if (neededRoll != null) {
				if (!report.isIgnoreAgility()) {
					AgilityMechanic mechanic = (AgilityMechanic) game.getRules().getFactory(Factory.MECHANIC).forName(Mechanic.Type.AGILITY.name());
					neededRoll.append(mechanic.formatInterceptionResult(report, player));
				}
				println(getIndent() + 2, TextStyle.NEEDED_ROLL, neededRoll.toString());
			}
    }
}
