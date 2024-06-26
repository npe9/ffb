package com.fumbbl.ffb.net.commands;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.SoundId;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.Animation;
import com.fumbbl.ffb.model.change.ModelChangeList;
import com.fumbbl.ffb.net.NetCommandId;
import com.fumbbl.ffb.report.ReportList;

/**
 * 
 * @author Kalimar
 */
public class ServerCommandModelSync extends ServerCommand {

	private ModelChangeList fModelChanges;
	private ReportList fReportList;
	private Animation fAnimation;
	private SoundId fSound;
	private long fGameTime;
	private long fTurnTime;

	public ServerCommandModelSync() {
		fModelChanges = new ModelChangeList();
		fReportList = new ReportList();
	}

	public ServerCommandModelSync(ModelChangeList pModelChanges, ReportList pReportList, Animation pAnimation,
			SoundId pSound, long pGameTime, long pTurnTime) {
		this();
		fModelChanges.add(pModelChanges);
		fReportList.add(pReportList);
		fAnimation = pAnimation;
		fSound = pSound;
		fGameTime = pGameTime;
		fTurnTime = pTurnTime;
	}

	public NetCommandId getId() {
		return NetCommandId.SERVER_MODEL_SYNC;
	}

	public ModelChangeList getModelChanges() {
		return fModelChanges;
	}

	public ReportList getReportList() {
		return fReportList;
	}

	public Animation getAnimation() {
		return fAnimation;
	}

	public SoundId getSound() {
		return fSound;
	}

	public long getGameTime() {
		return fGameTime;
	}

	public long getTurnTime() {
		return fTurnTime;
	}

	// transformation

	public ServerCommandModelSync transform(IFactorySource source) {
		Animation transformedAnimation = (getAnimation() != null) ? getAnimation().transform() : null;
		ServerCommandModelSync transformedCommand = new ServerCommandModelSync(getModelChanges().transform(),
				getReportList().transform(source), transformedAnimation, getSound(), getGameTime(), getTurnTime());
		transformedCommand.setCommandNr(getCommandNr());
		return transformedCommand;
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.NET_COMMAND_ID.addTo(jsonObject, getId());
		IJsonOption.COMMAND_NR.addTo(jsonObject, getCommandNr());
		if (fModelChanges != null) {
			IJsonOption.MODEL_CHANGE_LIST.addTo(jsonObject, fModelChanges.toJsonValue());
		}
		if (fReportList != null) {
			IJsonOption.REPORT_LIST.addTo(jsonObject, fReportList.toJsonValue());
		}
		if (fAnimation != null) {
			IJsonOption.ANIMATION.addTo(jsonObject, fAnimation.toJsonValue());
		}
		IJsonOption.SOUND.addTo(jsonObject, fSound);
		IJsonOption.GAME_TIME.addTo(jsonObject, fGameTime);
		IJsonOption.TURN_TIME.addTo(jsonObject, fTurnTime);
		return jsonObject;
	}

	public ServerCommandModelSync initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilNetCommand.validateCommandId(this, (NetCommandId) IJsonOption.NET_COMMAND_ID.getFrom(source, jsonObject));
		setCommandNr(IJsonOption.COMMAND_NR.getFrom(source, jsonObject));
		JsonObject modelChangeListObject = IJsonOption.MODEL_CHANGE_LIST.getFrom(source, jsonObject);
		fModelChanges = new ModelChangeList();
		if (modelChangeListObject != null) {
			fModelChanges.initFrom(source, modelChangeListObject);
		}
		fReportList = new ReportList();
		JsonObject reportListObject = IJsonOption.REPORT_LIST.getFrom(source, jsonObject);
		if (reportListObject != null) {
			fReportList.initFrom(source, reportListObject);
		}
		fAnimation = null;
		JsonObject animationObject = IJsonOption.ANIMATION.getFrom(source, jsonObject);
		if (animationObject != null) {
			fAnimation = new Animation().initFrom(source, animationObject);
		}
		fSound = (SoundId) IJsonOption.SOUND.getFrom(source, jsonObject);
		fGameTime = IJsonOption.GAME_TIME.getFrom(source, jsonObject);
		fTurnTime = IJsonOption.TURN_TIME.getFrom(source, jsonObject);
		return this;
	}

}
