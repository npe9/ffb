package com.fumbbl.ffb.client.report.bb2016;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.client.TextStyle;
import com.fumbbl.ffb.client.report.ReportMessageBase;
import com.fumbbl.ffb.client.report.ReportMessageType;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.bb2016.ReportKickoffRiot;

@ReportMessageType(ReportId.KICKOFF_RIOT)
@RulesCollection(Rules.BB2016)
public class KickoffRiotMessage extends ReportMessageBase<ReportKickoffRiot> {

    @Override
    protected void render(ReportKickoffRiot report) {
  		StringBuilder status = new StringBuilder();
  		if (report.getRoll() > 0) {
  			status.append("Riot Roll [ ").append(report.getRoll()).append(" ]");
  		} else {
  			status.append("Riot in Turn ")
  				.append(game.isHomePlaying() ? game.getTurnDataAway().getTurnNr() : game.getTurnDataHome().getTurnNr());
  		}
  		println(getIndent(), TextStyle.ROLL, status.toString());
  		if (report.getTurnModifier() < 0) {
  			println(getIndent() + 1, "The referee adjusts the clock back after the riot is over.");
  			status = new StringBuilder();
  			status.append("Turn Counter is moved ").append(Math.abs(report.getTurnModifier()));
  			status.append((report.getTurnModifier() == -1) ? " step" : " steps").append(" backward.");
  			println(getIndent() + 1, status.toString());
  		} else {
  			println(getIndent() + 1, "The referee does not stop the clock during the riot.");
  			status = new StringBuilder();
  			status.append("Turn Counter is moved ").append(Math.abs(report.getTurnModifier()));
  			status.append((report.getTurnModifier() == -1) ? " step" : " steps").append(" forward.");
  			println(getIndent() + 1, status.toString());
  		}
    }
}
