package com.fumbbl.ffb.json;

import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.factory.IFactorySource;

public class JsonBooleanMapOption extends JsonAbstractOption {
	public JsonBooleanMapOption(String pKey) {
		super(pKey);
	}

	public Map<String, Boolean> getFrom(IFactorySource source, JsonObject jsonObject) {
		Map<String, Boolean> map = new HashMap<>();

		if (isDefinedIn(jsonObject)) {
			JsonValue jsonValue = getValueFrom(jsonObject);
			if (jsonValue instanceof JsonObject) {
				JsonObject wrappedObject = (JsonObject) jsonValue;
				for (String name : wrappedObject.names()) {
					Boolean value = wrappedObject.get(name).asBoolean();
					map.put(name, value);
				}
			}

		}
		return map;
	}

	public void addTo(JsonObject pJsonObject, Map<String, Boolean> map) {
		JsonObject jsonObject = new JsonObject();
		for (Map.Entry<String, Boolean> entry : map.entrySet()) {
			jsonObject.add(entry.getKey(), entry.getValue());
		}

		addValueTo(pJsonObject, jsonObject);
	}
}
