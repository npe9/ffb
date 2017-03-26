package com.balancedbytes.games.ffb;

/**
 * 
 * @author Kalimar
 */
public class TeamStatusFactory implements INamedObjectFactory {

  // TODO: this method should no longer be necessary
  public TeamStatus forId(int pId) {
    switch (pId) {
      case 0:
        return TeamStatus.NEW;
      case 1:
        return TeamStatus.ACTIVE;
      case 2:
        return TeamStatus.PENDING_APPROVAL;
      case 3:
        return TeamStatus.BLOCKED;
      case 4:
        return TeamStatus.RETIRED;
      case 5:
        return TeamStatus.WAITING_FOR_OPPONENT;
      case 6:
        return TeamStatus.SKILL_ROLLS_PENDING;
      default:
        return null;
    }
  }
    
  public TeamStatus forName(String pName) {
    for (TeamStatus status : TeamStatus.values()) {
      if (status.getName().equalsIgnoreCase(pName)) {
        return status;
      }
    }
    return null;
  }

}
