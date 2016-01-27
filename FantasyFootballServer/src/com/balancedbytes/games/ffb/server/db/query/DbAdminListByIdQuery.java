package com.balancedbytes.games.ffb.server.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.balancedbytes.games.ffb.FantasyFootballException;
import com.balancedbytes.games.ffb.GameStatusFactory;
import com.balancedbytes.games.ffb.server.FantasyFootballServer;
import com.balancedbytes.games.ffb.server.ServerMode;
import com.balancedbytes.games.ffb.server.admin.AdminList;
import com.balancedbytes.games.ffb.server.admin.AdminListEntry;
import com.balancedbytes.games.ffb.server.db.DbStatement;
import com.balancedbytes.games.ffb.server.db.DbStatementId;
import com.balancedbytes.games.ffb.server.db.IDbTableGamesInfo;

/**
 * 
 * @author Kalimar
 */
public class DbAdminListByIdQuery extends DbStatement {
  
  private PreparedStatement fStatement;
  
  public DbAdminListByIdQuery(FantasyFootballServer pServer) {
    super(pServer);
  }
  
  public DbStatementId getId() {
    return DbStatementId.ADMIN_LIST_BY_ID_QUERY;
  }
  
  public void prepare(Connection pConnection) {
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT ")
      .append(IDbTableGamesInfo.COLUMN_ID).append(",")
      .append(IDbTableGamesInfo.COLUMN_STARTED).append(",")
      .append(IDbTableGamesInfo.COLUMN_FINISHED).append(",")
      .append(IDbTableGamesInfo.COLUMN_LAST_UPDATED).append(",")
      .append(IDbTableGamesInfo.COLUMN_COACH_HOME).append(",")
    	.append(IDbTableGamesInfo.COLUMN_TEAM_HOME_ID).append(",")
    	.append(IDbTableGamesInfo.COLUMN_TEAM_HOME_NAME).append(",")
    	.append(IDbTableGamesInfo.COLUMN_COACH_AWAY).append(",")
    	.append(IDbTableGamesInfo.COLUMN_TEAM_AWAY_ID).append(",")
    	.append(IDbTableGamesInfo.COLUMN_TEAM_AWAY_NAME).append(",")
      .append(IDbTableGamesInfo.COLUMN_HALF).append(",")
      .append(IDbTableGamesInfo.COLUMN_TURN).append(",")
      .append(IDbTableGamesInfo.COLUMN_STATUS)
      .append(" FROM ").append(IDbTableGamesInfo.TABLE_NAME)
      .append(" WHERE ").append(IDbTableGamesInfo.COLUMN_ID).append("=?");
      if (getServer().getMode() == ServerMode.FUMBBL) {
        sql.append(" AND ").append(IDbTableGamesInfo.COLUMN_TESTING).append("=false");
      }
      fStatement = pConnection.prepareStatement(sql.toString());
    } catch (SQLException sqlE) {
      throw new FantasyFootballException(sqlE);
    }
  }
  
  public void execute(AdminList pAdminList, long pGameId) {
    if (pAdminList == null) {
    	return;
    }
    try {
      fStatement.setLong(1, pGameId);
      ResultSet resultSet = fStatement.executeQuery();
      while (resultSet.next()) {
        int col = 1;
        AdminListEntry entry = new AdminListEntry();
        entry.setGameId(resultSet.getLong(col++));
        Timestamp started = resultSet.getTimestamp(col++);
        if (started != null) {
          entry.setStarted(new Date(started.getTime()));
        }
        Timestamp finished = resultSet.getTimestamp(col++);
        if (finished != null) {
          entry.setFinished(new Date(finished.getTime()));
        }
        Timestamp lastUpdated = resultSet.getTimestamp(col++);
        if (lastUpdated != null) {
          entry.setLastUpdated(new Date(lastUpdated.getTime()));
        }
        entry.setTeamHomeCoach(resultSet.getString(col++));
        entry.setTeamHomeId(resultSet.getString(col++));
        entry.setTeamHomeName(resultSet.getString(col++));
        entry.setTeamAwayCoach(resultSet.getString(col++));
        entry.setTeamAwayId(resultSet.getString(col++));
        entry.setTeamAwayName(resultSet.getString(col++));
        entry.setHalf(resultSet.getInt(col++));
        entry.setTurn(resultSet.getInt(col++));
        entry.setStatus(new GameStatusFactory().forTypeString(resultSet.getString(col++)));
        pAdminList.add(entry);
      }
      resultSet.close();
    } catch (SQLException pSqlE) {
      throw new FantasyFootballException(pSqlE);
    }
  }
    
}