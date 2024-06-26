package com.fumbbl.ffb.option;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;

import com.eclipsesource.json.JsonObject;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.util.StringTool;
import com.fumbbl.ffb.xml.UtilXml;

/**
 * 
 * @author Kalimar
 */
public abstract class GameOptionAbstract implements IGameOption {

	private GameOptionId fId;

	public GameOptionAbstract(GameOptionId pId) {
		fId = pId;
	}

	@Override
	public GameOptionId getId() {
		return fId;
	}

	protected void setId(GameOptionId pId) {
		fId = pId;
	}

	@Override
	public boolean isChanged() {
		return !StringTool.print(getDefaultAsString()).equals(getValueAsString());
	}

	protected abstract String getDefaultAsString();

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.GAME_OPTION_ID.addTo(jsonObject, getId());
		IJsonOption.GAME_OPTION_VALUE.addTo(jsonObject, getValueAsString());
		return jsonObject;
	}

	// XML serialization

	public void addToXml(TransformerHandler pHandler) {
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, XML_ATTRIBUTE_NAME, (getId() != null) ? getId().getName() : null);
		UtilXml.addAttribute(attributes, XML_ATTRIBUTE_VALUE, getValueAsString());
		UtilXml.startElement(pHandler, XML_TAG, attributes);
		UtilXml.endElement(pHandler, XML_TAG);
	}

	public String toXml(boolean pIndent) {
		return UtilXml.toXml(this, pIndent);
	}

}
