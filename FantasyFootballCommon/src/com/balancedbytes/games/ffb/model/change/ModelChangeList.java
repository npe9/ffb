package com.balancedbytes.games.ffb.model.change;

import java.util.ArrayList;
import java.util.List;

import com.balancedbytes.games.ffb.json.IJsonOption;
import com.balancedbytes.games.ffb.json.IJsonSerializable;
import com.balancedbytes.games.ffb.json.UtilJson;
import com.balancedbytes.games.ffb.model.Game;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


/**
 * 
 * @author Kalimar
 */
public class ModelChangeList implements IJsonSerializable {
  
  private List<ModelChange> fChanges;
  
  public ModelChangeList() {
  	this(16);
  }

  public ModelChangeList(int pInitialCapacity) {
  	fChanges = new ArrayList<ModelChange>(pInitialCapacity);
  }

  public void add(ModelChange pChange) {
    fChanges.add(pChange);
  }
  
  public void add(ModelChangeList pChangeList) {
    if (pChangeList != null) {
      for (ModelChange change : pChangeList.getChanges()) {
        add(change);
      }
    }
  }
  
  public ModelChange[] getChanges() {
    return fChanges.toArray(new ModelChange[fChanges.size()]);
  }
  
  public void clear() {
    fChanges.clear();
  }
  
  public int size() {
    return fChanges.size();
  }
  
  public void applyTo(Game pGame) {
    ModelChangeProcessor processor = new ModelChangeProcessor();
    for (ModelChange change : getChanges()) {
    	processor.apply(pGame, change);
    }
  }
  
  // transformation
  
  public ModelChangeList transform() {
    ModelChangeList transformedList = new ModelChangeList(size());
    ModelChangeProcessor processor = new ModelChangeProcessor();
    for (ModelChange change : getChanges()) {
    	transformedList.add(processor.transform(change));
    }
    return transformedList;
  }
  
  // JSON serialization
  
  public JsonObject toJsonValue() {
    JsonObject jsonObject = new JsonObject();
    JsonArray modelChanges = new JsonArray();
    for (ModelChange change : fChanges) {
      modelChanges.add(change.toJsonValue());
    }
    IJsonOption.MODEL_CHANGES.addTo(jsonObject, modelChanges);
    return jsonObject;
  }
  
  public ModelChangeList initFrom(JsonValue pJsonValue) {
    JsonObject jsonObject = UtilJson.toJsonObject(pJsonValue);
    JsonArray modelChanges = IJsonOption.MODEL_CHANGES.getFrom(jsonObject);
    clear();
    for (int i = 0; i < modelChanges.size(); i++) {
      add(new ModelChange().initFrom(modelChanges.get(i)));
    }
    return this;
  }
     
}