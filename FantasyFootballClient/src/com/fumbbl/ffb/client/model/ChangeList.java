package com.fumbbl.ffb.client.model;

import java.util.ArrayList;
import java.util.List;

public class ChangeList {

    private final List<VersionChangeList> versions = new ArrayList<>();

    public static final ChangeList INSTANCE = new ChangeList();

    public ChangeList() {

        versions.add(new VersionChangeList("2.3.0")
            .addImprovement("Added \"What's new?\" dialog")
            .addBugfix("Prevent throwing throw/kick player that was injured too severe by Animal Savagery")
            .addBugfix("Prevent apothecary usage on zapped players")
            .addBugfix("Do not use Chainsaw modifier when player throws regular block")
            .addBugfix("Prevent overflow in 2016 petty cash dialog")
            .addBugfix("Preserve labels in replay mode when playing/moving backwards")
            .addBugfix("Potential fix for missing stat upgrades")
            .addBugfix("Allow Chainsaw players to continue blitz move after performing regular block")
            .addBugfix("Prone/stunned players hit by B&C failing a rush suffer av roll")
            .addBugfix("Rooted players can uproot themselves with Diving Tackle")
            .addBugfix("Do not use Claws or Mighty Blow when attacker is knocked down as well")
            .addBugfix("Roll for Foul Appearance on Frenzy blocks during blitz actions as well")
			.addBugfix("BB2016: Do not roll for Foul Appearance on Frenzy blocks")


        );

    }

    public List<VersionChangeList> getVersions() {
        return versions;
    }

    public String fingerPrint() {
        return String.valueOf(versions.get(0).hashCode());
    }
}
