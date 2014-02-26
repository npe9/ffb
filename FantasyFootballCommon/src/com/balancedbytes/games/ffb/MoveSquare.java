package com.balancedbytes.games.ffb;

import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.bytearray.IByteArrayReadable;
import com.balancedbytes.games.ffb.json.IJsonOption;
import com.balancedbytes.games.ffb.json.IJsonSerializable;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.util.ArrayTool;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


/**
 * 
 * @author Kalimar
 */
public final class MoveSquare implements IByteArrayReadable, IJsonSerializable {

  private FieldCoordinate fCoordinate;
  private int fMinimumRollDodge;
  private int fMinimumRollGoForIt;
  
  public MoveSquare() {
    super();
  }
  
  public MoveSquare(FieldCoordinate pCoordinate, int pMinimumRollDodge, int pMinimumRollGoForIt) {
    if (pCoordinate == null) {
      throw new IllegalArgumentException("Parameter coordinate must not be null.");
    }
    fCoordinate = pCoordinate;
    fMinimumRollDodge = pMinimumRollDodge;
    fMinimumRollGoForIt = pMinimumRollGoForIt;
  }
  
  public FieldCoordinate getCoordinate() {
    return fCoordinate;
  }
  
  public int getMinimumRollDodge() {
    return fMinimumRollDodge;
  }
  
  public boolean isDodging() {
    return (getMinimumRollDodge() > 0);
  }
  
  public int getMinimumRollGoForIt() {
    return fMinimumRollGoForIt;
  }
  
  public boolean isGoingForIt() {
    return (getMinimumRollGoForIt() > 0);
  }
  
  public int hashCode() {
    return getCoordinate().hashCode();
  }
  
  public boolean equals(Object pObj) {
    return ((pObj instanceof MoveSquare) && getCoordinate().equals(((MoveSquare) pObj).getCoordinate()));
  }
  
  // transformation
  
  public MoveSquare transform() {
    return new MoveSquare(getCoordinate().transform(), getMinimumRollDodge(), getMinimumRollGoForIt());
  }
  
  public static MoveSquare transform(MoveSquare pMoveSquare) {
    return (pMoveSquare != null) ? pMoveSquare.transform() : null;
  }
  
  public static MoveSquare[] transform(MoveSquare[] pMoveSquares) {
    MoveSquare[] transformedMoveSquares = new MoveSquare[0];
    if (ArrayTool.isProvided(pMoveSquares)) {
      transformedMoveSquares = new MoveSquare[pMoveSquares.length];
      for (int i = 0; i < transformedMoveSquares.length; i++) {
        transformedMoveSquares[i] = transform(pMoveSquares[i]);
      }
    }
    return transformedMoveSquares;
  }
  
  // ByteArray serialization
  
  public int getByteArraySerializationVersion() {
    return 1;
  }
  
  public void addTo(ByteList pByteList) {
    pByteList.addSmallInt(getByteArraySerializationVersion());
    pByteList.addFieldCoordinate(getCoordinate());
    pByteList.addByte((byte) getMinimumRollDodge());
    pByteList.addByte((byte) getMinimumRollGoForIt());
  }
  
  public int initFrom(ByteArray pByteArray) {
    int byteArraySerializationVersion = pByteArray.getSmallInt();
    fCoordinate = pByteArray.getFieldCoordinate();
    fMinimumRollDodge = pByteArray.getByte();
    fMinimumRollGoForIt = pByteArray.getByte();
    return byteArraySerializationVersion;
  }
  
  // JSON serialization
  
  public JsonObject toJsonValue() {
    JsonObject jsonObject = new JsonObject();
    IJsonOption.COORDINATE.addTo(jsonObject, fCoordinate);
    IJsonOption.MINIMUM_ROLL_DODGE.addTo(jsonObject, fMinimumRollDodge);
    IJsonOption.MINIMUM_ROLL_GFI.addTo(jsonObject, fMinimumRollGoForIt);
    return jsonObject;
  }
  
  public MoveSquare initFrom(JsonValue pJsonValue) {
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    fCoordinate = IJsonOption.COORDINATE.getFrom(jsonObject);
    fMinimumRollDodge = IJsonOption.MINIMUM_ROLL_DODGE.getFrom(jsonObject);
    fMinimumRollGoForIt = IJsonOption.MINIMUM_ROLL_GFI.getFrom(jsonObject);
    return this;
  }
  
}
