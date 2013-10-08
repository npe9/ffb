package com.balancedbytes.games.ffb.model.change.old;


import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;

import com.balancedbytes.games.ffb.PlayerAction;
import com.balancedbytes.games.ffb.Skill;
import com.balancedbytes.games.ffb.bytearray.ByteArray;
import com.balancedbytes.games.ffb.bytearray.ByteList;
import com.balancedbytes.games.ffb.model.ActingPlayer;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.xml.UtilXml;

/**
 * 
 * @author Kalimar
 */
public class ModelChangeActingPlayer implements IModelChange {
  
  private static final String _XML_ATTRIBUTE_CHANGE = "change";
  private static final String _XML_ATTRIBUTE_VALUE = "value";

  private CommandActingPlayerChange fChange;
  private Object fValue;
  
  protected ModelChangeActingPlayer() {
    super();
  }
  
  public ModelChangeActingPlayer(CommandActingPlayerChange pChange, Object pValue) {
    if (pChange == null) {
      throw new IllegalArgumentException("Parameter change must not be null.");
    }
    fChange = pChange;
    getChange().getAttributeType().checkValueType(pValue);
    fValue = pValue;
  }
  
  public ModelChangeIdOld getId() {
    return ModelChangeIdOld.ACTING_PLAYER_CHANGE;
  }
  
  public CommandActingPlayerChange getChange() {
    return fChange;
  }
  
  public Object getValue() {
    return fValue;
  }
  
  public void applyTo(Game pGame) {
    ActingPlayer actingPlayer = pGame.getActingPlayer();
    boolean trackingChanges = pGame.isTrackingChanges();
    pGame.setTrackingChanges(false);
    switch (getChange()) {
      case SET_PLAYER_ID:
        actingPlayer.setPlayerId((String) getValue());
        break;
      case SET_STRENGTH:
        actingPlayer.setStrength((Byte) getValue());
        break;
      case SET_CURRENT_MOVE:
        actingPlayer.setCurrentMove((Byte) getValue());
        break;
      case SET_GOING_FOR_IT:
        actingPlayer.setGoingForIt((Boolean) getValue());
        break;
      case SET_DODGING:
        actingPlayer.setDodging((Boolean) getValue());
        break;
      case SET_LEAPING:
        actingPlayer.setLeaping((Boolean) getValue());
        break;
      case SET_STANDING_UP:
        actingPlayer.setStandingUp((Boolean) getValue());
        break;
      case SET_SUFFERING_BLOOD_LUST:
        actingPlayer.setSufferingBloodLust((Boolean) getValue());
        break;
      case SET_SUFFERING_ANIMOSITY:
        actingPlayer.setSufferingAnimosity((Boolean) getValue());
        break;
      case SET_HAS_BLOCKED:
        actingPlayer.setHasBlocked((Boolean) getValue());
        break;
      case SET_HAS_FOULED:
        actingPlayer.setHasFouled((Boolean) getValue());
        break;
      case SET_HAS_PASSED:
        actingPlayer.setHasPassed((Boolean) getValue());
        break;
      case SET_HAS_MOVED:
        actingPlayer.setHasMoved((Boolean) getValue());
        break;
      case SET_HAS_FED:
        actingPlayer.setHasFed((Boolean) getValue());
        break;
      case SET_PLAYER_ACTION:
        actingPlayer.setPlayerAction((PlayerAction) getValue());
        break;
      case MARK_SKILL_USED:
        actingPlayer.markSkillUsed((Skill) getValue());
        break;
      default:
        throw new IllegalStateException("Unhandled change " + getChange() + ".");
    }
    pGame.setTrackingChanges(trackingChanges);
  }
 
  // transformation
  
  public IModelChange transform() {
    return new ModelChangeActingPlayer(getChange(), getValue());
  }
  
  // XML serialization
  
  public void addToXml(TransformerHandler pHandler) {
    
    AttributesImpl attributes = new AttributesImpl();
    UtilXml.addAttribute(attributes, XML_ATTRIBUTE_ID, getId().getName());
    if (getChange() != null) {
      UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_CHANGE, getChange().getName());
      getChange().getAttributeType().addXmlAttribute(attributes, _XML_ATTRIBUTE_VALUE, getValue());
    }
    
    UtilXml.addEmptyElement(pHandler, XML_TAG);
    
  }
  
  public String toXml(boolean pIndent) {
    return UtilXml.toXml(this, pIndent);
  }
  
  // ByteArray serialization
  
  public int getByteArraySerializationVersion() {
    return 1;
  }
  
  public void addTo(ByteList pByteList) {
    pByteList.addByte((byte) getId().getId());
    pByteList.addSmallInt(getByteArraySerializationVersion());
    pByteList.addByte((byte) getChange().getId());
    getChange().getAttributeType().addTo(pByteList, getValue());
  }
  
  public int initFrom(ByteArray pByteArray) {
    ModelChangeIdOld changeId = ModelChangeIdOld.fromId(pByteArray.getByte());
    if (getId() != changeId) {
      throw new IllegalStateException("Wrong change id. Expected " + getId().getName() + " received " + ((changeId != null) ? changeId.getName() : "null"));
    }
    int byteArraySerializationVersion = pByteArray.getSmallInt();
    fChange = CommandActingPlayerChange.fromId(pByteArray.getByte());
    if (getChange() == null) {
      throw new IllegalStateException("Attribute change must not be null.");
    }
    fValue = getChange().getAttributeType().initFrom(pByteArray);
    return byteArraySerializationVersion;
  }
  
}