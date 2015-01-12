package com.balancedbytes.games.ffb.report;

import com.balancedbytes.games.ffb.json.IJsonOption;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;



/**
 * 
 * @author Kalimar
 */
public class ReportBiteSpectator implements IReport {
  
  private String fPlayerId;
  
  public ReportBiteSpectator() {
    super();
  }

  public ReportBiteSpectator(String pCatcherId) {
    fPlayerId = pCatcherId;
  }
  
  public ReportId getId() {
    return ReportId.BITE_SPECTATOR;
  }
  
  public String getPlayerId() {
    return fPlayerId;
  }

  // transformation
  
  public IReport transform() {
    return new ReportBiteSpectator(getPlayerId());
  }
  
  // JSON serialization
  
  public JsonObject toJsonValue() {
    JsonObject jsonObject = new JsonObject();
    IJsonOption.REPORT_ID.addTo(jsonObject, getId());
    IJsonOption.PLAYER_ID.addTo(jsonObject, fPlayerId);
    return jsonObject;
  }
  
  public ReportBiteSpectator initFrom(JsonValue pJsonValue) {
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    UtilReport.validateReportId(this, (ReportId) IJsonOption.REPORT_ID.getFrom(jsonObject));
    fPlayerId = IJsonOption.PLAYER_ID.getFrom(jsonObject);
    return this;
  }
    
}
