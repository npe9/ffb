package com.balancedbytes.games.ffb;

/**
 * 
 * @author Kalimar
 */
public class SoundIdFactory implements IEnumWithNameFactory {
  
  public SoundId forName(String pName) {
    for (SoundId sound : SoundId.values()) {
      if (sound.getName().equalsIgnoreCase(pName)) {
        return sound;
      }
    }
    return null;
  }

}