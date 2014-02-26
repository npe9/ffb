package com.balancedbytes.games.ffb;

import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.bytearray.IByteArrayReadable;
import com.balancedbytes.games.ffb.json.IJsonOption;
import com.balancedbytes.games.ffb.json.IJsonSerializable;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;




/**
 * 
 * @author Kalimar
 */
public class PlayerMarker implements IByteArrayReadable, IJsonSerializable {
  
  private String fPlayerId;
  private String fHomeText;
  private String fAwayText;
  
  public PlayerMarker() {
    super();
  }
  
  public PlayerMarker(String pPlayerId) {
    fPlayerId = pPlayerId;
  }

  public String getPlayerId() {
    return fPlayerId;
  }  
  
  public void setHomeText(String pHomeText) {
    fHomeText = pHomeText;
  }
  
  public String getHomeText() {
    return fHomeText;
  }
  
  public void setAwayText(String pAwayText) {
    fAwayText = pAwayText;
  }
  
  public String getAwayText() {
    return fAwayText;
  }
  
  public int hashCode() {
    return getPlayerId().hashCode();
  }
  
  public boolean equals(Object pObj) {
    return ((pObj instanceof PlayerMarker) && getPlayerId().equals(((PlayerMarker) pObj).getPlayerId()));
  }
 
  // Transformation
  
  public PlayerMarker transform() {
    PlayerMarker transformedMarker = new PlayerMarker(getPlayerId());
    transformedMarker.setAwayText(getHomeText());
    transformedMarker.setHomeText(getAwayText());
    return transformedMarker;
  }
  
  public static PlayerMarker transform(PlayerMarker pFieldMarker) {
    return (pFieldMarker != null) ? pFieldMarker.transform() : null;
  }
    
  // ByteArray serialization
  
  public int getByteArraySerializationVersion() {
    return 1;
  }
  
  public void addTo(ByteList pByteList) {
    pByteList.addSmallInt(getByteArraySerializationVersion());
    pByteList.addString(getPlayerId());
    pByteList.addString(getHomeText());
    pByteList.addString(getAwayText());
  }
    
  public int initFrom(ByteArray pByteArray) {
    int byteArraySerializationVersion = pByteArray.getSmallInt();
    fPlayerId = pByteArray.getString();
    fHomeText = pByteArray.getString();
    fAwayText = pByteArray.getString();
    return byteArraySerializationVersion;
  }
  
  // JSON serialization
  
  public JsonObject toJsonValue() {
    JsonObject jsonObject = new JsonObject();
    IJsonOption.PLAYER_ID.addTo(jsonObject, fPlayerId);
    IJsonOption.HOME_TEXT.addTo(jsonObject, fHomeText);
    IJsonOption.AWAY_TEXT.addTo(jsonObject, fAwayText);
    return jsonObject;
  }
  
  public PlayerMarker initFrom(JsonValue pJsonValue) {
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fPlayerId = IJsonOption.PLAYER_ID.getFrom(jsonObject);
    fHomeText = IJsonOption.HOME_TEXT.getFrom(jsonObject);
    fAwayText = IJsonOption.AWAY_TEXT.getFrom(jsonObject);
    return this;
  }
  
}
