package com.balancedbytes.games.ffb.net.commands;

import com.balancedbytes.games.ffb.json.IJsonOption;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.net.NetCommand;
import com.balancedbytes.games.ffb.net.NetCommandId;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;




/**
 * 
 * @author Kalimar
 */
public abstract class ClientCommand extends NetCommand {

  private Byte fEntropy;
  
  public void setEntropy(byte entropy) {
    fEntropy = entropy;
  }
  
  public boolean hasEntropy() {
    return (fEntropy != null);
  }
  
  public byte getEntropy() {
    return fEntropy;
  }
  
  // JSON serialization

  public JsonObject toJsonValue() {
    JsonObject jsonObject = new JsonObject();
    IJsonOption.NET_COMMAND_ID.addTo(jsonObject, getId());
    if (hasEntropy()) {
      IJsonOption.ENTROPY.addTo(jsonObject, getEntropy());
    }
    return jsonObject;
  }
  
  public ClientCommand initFrom(JsonValue jsonValue) {
    JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
    UtilNetCommand.validateCommandId(this, (NetCommandId) IJsonOption.NET_COMMAND_ID.getFrom(jsonObject));
    if (IJsonOption.ENTROPY.isDefinedIn(jsonObject)) {
      setEntropy((byte) IJsonOption.ENTROPY.getFrom(jsonObject));
    }
    return this;
  }
    
}