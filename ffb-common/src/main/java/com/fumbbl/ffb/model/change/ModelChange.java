package com.fumbbl.ffb.model.change;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.IJsonSerializable;
import com.fumbbl.ffb.json.UtilJson;

/**
 * 
 * @author Kalimar
 */
public class ModelChange implements IJsonSerializable {

	public static final String HOME = "home";
	public static final String AWAY = "away";

	private ModelChangeId fChangeId;
	private String fKey;
	private Object fValue;

	public ModelChange() {
		super();
	}

	public ModelChange(ModelChangeId pChangeId, String pKey, Object pValue) {
		setChangeId(pChangeId);
		setKey(pKey);
		setValue(pValue);
	}

	public ModelChangeId getChangeId() {
		return fChangeId;
	}

	public void setChangeId(ModelChangeId pChangeId) {
		fChangeId = pChangeId;
	}

	public String getKey() {
		return fKey;
	}

	public void setKey(String pKey) {
		fKey = pKey;
	}

	public Object getValue() {
		return fValue;
	}

	public void setValue(Object pValue) {
		fValue = pValue;
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.MODEL_CHANGE_ID.addTo(jsonObject, fChangeId);
		IJsonOption.MODEL_CHANGE_KEY.addTo(jsonObject, fKey);
		IJsonOption.MODEL_CHANGE_VALUE.addTo(jsonObject, fChangeId.toJsonValue(fValue));
		return jsonObject;
	}

	public ModelChange initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fChangeId = (ModelChangeId) IJsonOption.MODEL_CHANGE_ID.getFrom(source, jsonObject);
		fKey = IJsonOption.MODEL_CHANGE_KEY.getFrom(source, jsonObject);
		fValue = fChangeId.fromJsonValue(source, IJsonOption.MODEL_CHANGE_VALUE.getFrom(source, jsonObject));
		return this;
	}

}
