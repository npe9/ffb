package com.fumbbl.ffb.server.db.insert;

import java.util.ArrayList;
import java.util.List;

import com.fumbbl.ffb.server.db.IDbUpdateParameterList;

/**
 *
 * @author Kalimar
 */
public class DbUserSettingsInsertParameterList implements IDbUpdateParameterList {

	private List<DbUserSettingsInsertParameter> fParameters;

	public DbUserSettingsInsertParameterList() {
		fParameters = new ArrayList<>();
	}

	public DbUserSettingsInsertParameter[] getParameters() {
		return fParameters.toArray(new DbUserSettingsInsertParameter[fParameters.size()]);
	}

	public void addParameter(DbUserSettingsInsertParameter pParameter) {
		fParameters.add(pParameter);
	}

	public void addParameter(String pCoach, String pSettingName, String pSettingValue) {
		addParameter(new DbUserSettingsInsertParameter(pCoach, pSettingName, pSettingValue));
	}

}