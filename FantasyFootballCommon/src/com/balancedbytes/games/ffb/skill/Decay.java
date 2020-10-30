package com.balancedbytes.games.ffb.skill;

import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.model.Skill;

/**
* Staying on the pitch is difficult when your rotting body is barely held
* together. When this player suffers a Casualty result on the Injury table,
* roll twice on the Casualty table (see page 25) and apply both results. The
* player will only ever miss one future match as a result of his injuries,
* even if he suffers two results with this effect.
*/
public class Decay extends Skill {

  public Decay() {
    super("Decay", SkillCategory.EXTRAORDINARY);
  }

}