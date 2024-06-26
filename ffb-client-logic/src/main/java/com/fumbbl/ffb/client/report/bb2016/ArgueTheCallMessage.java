package com.fumbbl.ffb.client.report.bb2016;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.client.TextStyle;
import com.fumbbl.ffb.client.report.ReportMessageBase;
import com.fumbbl.ffb.client.report.ReportMessageType;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.bb2016.ReportArgueTheCallRoll;

@ReportMessageType(ReportId.ARGUE_THE_CALL)
@RulesCollection(Rules.BB2016)
public class ArgueTheCallMessage extends ReportMessageBase<ReportArgueTheCallRoll> {

    @Override
    protected void render(ReportArgueTheCallRoll report) {
  		Player<?> player = game.getPlayerById(report.getPlayerId());
  		StringBuilder status = new StringBuilder();
  		status.append("Argue the Call Roll [ ").append(report.getRoll()).append(" ]");
  		println(getIndent(), TextStyle.ROLL, status.toString());
  		if (report.isSuccessful()) {
			  print(getIndent() + 1, TextStyle.NONE, "The ref refrains from banning ");
			  print(getIndent() + 1, false, player);
			  status = new StringBuilder();
			  status.append(" and ").append(player.getPlayerGender().getNominative()).append(" is sent to the reserve instead.");
			  println(getIndent() + 1, TextStyle.NONE, status.toString());
		  } else {
  			print(getIndent() + 1, TextStyle.NONE, "The ref bans ");
  			print(getIndent() + 1, false, player);
  			println(getIndent() + 1, TextStyle.NONE, " from the game.");
  		}
  		if (report.isCoachBanned()) {
  			print(getIndent() + 1, TextStyle.NONE, "Coach ");
  			if (game.getTeamHome().hasPlayer(player)) {
  				print(getIndent() + 1, TextStyle.HOME, game.getTeamHome().getCoach());
  			} else {
  				print(getIndent() + 1, TextStyle.AWAY, game.getTeamAway().getCoach());
  			}
  			println(getIndent() + 1, TextStyle.NONE, " is also banned from the game.");
  		}
		}

}
