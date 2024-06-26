package com.fumbbl.ffb;

public enum BlockResult implements INamedObject {

	SKULL("SKULL"), BOTH_DOWN("BOTH DOWN"), PUSHBACK("PUSHBACK"), POW_PUSHBACK("POW/PUSH"), POW("POW");

	private String fName;

	private BlockResult(String pName) {
		fName = pName;
	}

	public String getName() {
		return fName;
	}

}
