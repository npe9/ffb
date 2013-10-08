package com.balancedbytes.games.ffb;

/**
 * 
 * @author Kalimar
 */
public class ConcedeGameStatusFactory implements IEnumWithIdFactory, IEnumWithNameFactory {
  
  public ConcedeGameStatus forName(String pName) {
    for (ConcedeGameStatus status : ConcedeGameStatus.values()) {
      if (status.getName().equalsIgnoreCase(pName)) {
        return status;
      }
    }
    return null;
  }

  public ConcedeGameStatus forId(int pId) {
    for (ConcedeGameStatus status : ConcedeGameStatus.values()) {
      if (status.getId() == pId) {
        return status;
      }
    }
    return null;
  }

}