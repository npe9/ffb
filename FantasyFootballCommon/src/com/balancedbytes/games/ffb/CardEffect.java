package com.balancedbytes.games.ffb;




/**
 * 
 * @author Kalimar
 */
public enum CardEffect implements IEnumWithName {
  
  DISTRACTED("Distracted"),
  ILLEGALLY_SUBSTITUTED("IllegallySubstituted");

  private String fName;
  
  private CardEffect(String pName) {
    fName = pName;
  }
  
  public String getName() {
    return fName;
  }

}
