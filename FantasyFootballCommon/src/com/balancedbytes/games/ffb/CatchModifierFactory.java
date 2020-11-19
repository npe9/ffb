package com.balancedbytes.games.ffb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.balancedbytes.games.ffb.CatchModifiers.CatchContext;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.model.modifier.NamedProperties;
import com.balancedbytes.games.ffb.util.UtilCards;
import com.balancedbytes.games.ffb.util.UtilDisturbingPresence;
import com.balancedbytes.games.ffb.util.UtilPlayer;

/**
 * 
 * @author Kalimar
 */
public class CatchModifierFactory implements IRollModifierFactory {
  
  public CatchModifier forName(String pName) {
	 return CatchModifiers.values().get(pName.toLowerCase());
  }
  

  public Set<CatchModifier> findCatchModifiers(Game pGame, Player<?> pPlayer, CatchScatterThrowInMode pCatchMode) {
    Set<CatchModifier> catchModifiers = new HashSet<CatchModifier>();
    
    
    CatchContext context = new CatchContext(pPlayer, pCatchMode);
    catchModifiers.addAll(UtilCards.getCatchModifiers(pPlayer, context));
    
    if ((CatchScatterThrowInMode.CATCH_ACCURATE_PASS == pCatchMode) || (CatchScatterThrowInMode.CATCH_ACCURATE_BOMB == pCatchMode)) {
      catchModifiers.add(CatchModifiers.ACCURATE);
    }
    
    if ((CatchScatterThrowInMode.CATCH_ACCURATE_PASS_EMPTY_SQUARE == pCatchMode || CatchScatterThrowInMode.CATCH_ACCURATE_BOMB_EMPTY_SQUARE == pCatchMode)
      && UtilCards.hasSkillWithProperty(pPlayer, NamedProperties.addBonusForAccuratePass)) {
      catchModifiers.add(CatchModifiers.ACCURATE);
    }
    
    if (CatchScatterThrowInMode.CATCH_HAND_OFF == pCatchMode) {
      catchModifiers.add(CatchModifiers.HAND_OFF);
    }
    if (Weather.POURING_RAIN == pGame.getFieldModel().getWeather()) {
      catchModifiers.add(CatchModifiers.POURING_RAIN);
    }
    if (!UtilCards.hasSkillWithProperty(pPlayer, NamedProperties.ignoreTacklezonesWhenCatching)) {
      CatchModifier tacklezoneModifier = getTacklezoneModifier(pGame, pPlayer);
      if (tacklezoneModifier != null) {
        catchModifiers.add(tacklezoneModifier);
      }
    }
    CatchModifier disturbingPresenceModifier = getDisturbingPresenceModifier(pGame, pPlayer);
    if (disturbingPresenceModifier != null) {
      catchModifiers.add(disturbingPresenceModifier);
    }
    return catchModifiers;
  }
  
  public CatchModifier[] toArray(Set<CatchModifier> pCatchModifierSet) {
    if (pCatchModifierSet != null) {
      CatchModifier[] catchModifierArray = pCatchModifierSet.toArray(new CatchModifier[pCatchModifierSet.size()]);
      Arrays.sort(
        catchModifierArray,
        new Comparator<CatchModifier>() {
          public int compare(CatchModifier pO1, CatchModifier pO2) {
            return pO1.getName().compareTo(pO2.getName());
          }
        }
      );
      return catchModifierArray;
    } else {
      return new CatchModifier[0];
    }
  }
  
  private CatchModifier getTacklezoneModifier(Game pGame, Player<?> pPlayer) {
	  int tacklezones = UtilPlayer.findTacklezones(pGame, pPlayer);
	  if (tacklezones > 0) {
		  for (Map.Entry<String, CatchModifier> entry : CatchModifiers.values().entrySet()) {
			  CatchModifier modifier = entry.getValue();
			  if (modifier.isTacklezoneModifier() && (modifier.getModifier() == tacklezones)) {
				  return modifier;
			  }
		  }
	  }
	  return null;
  }

  private CatchModifier getDisturbingPresenceModifier(Game pGame, Player<?> pPlayer) {
	  int disturbingPresences = UtilDisturbingPresence.findOpposingDisturbingPresences(pGame, pPlayer);
	  if (disturbingPresences > 0) {
		  for (Map.Entry<String, CatchModifier> entry : CatchModifiers.values().entrySet()) {
			  CatchModifier modifier = entry.getValue();
			  if (modifier.isDisturbingPresenceModifier() && (modifier.getModifier() == disturbingPresences)) {
				  return modifier;
			  }
		  }
	  }
	  return null;
  }

}
