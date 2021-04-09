package com.balancedbytes.games.ffb.client;

/**
 * 
 * @author Kalimar
 */
public enum ActionKey {

	PLAYER_MOVE_NORTH(IClientProperty.KEY_PLAYER_MOVE_NORTH),
	PLAYER_MOVE_NORTHEAST(IClientProperty.KEY_PLAYER_MOVE_NORTHEAST),
	PLAYER_MOVE_EAST(IClientProperty.KEY_PLAYER_MOVE_EAST),
	PLAYER_MOVE_SOUTHEAST(IClientProperty.KEY_PLAYER_MOVE_SOUTHEAST),
	PLAYER_MOVE_SOUTH(IClientProperty.KEY_PLAYER_MOVE_SOUTH),
	PLAYER_MOVE_SOUTHWEST(IClientProperty.KEY_PLAYER_MOVE_SOUTHWEST),
	PLAYER_MOVE_WEST(IClientProperty.KEY_PLAYER_MOVE_WEST),
	PLAYER_MOVE_NORTHWEST(IClientProperty.KEY_PLAYER_MOVE_NORTHWEST),

	PLAYER_SELECT(IClientProperty.KEY_PLAYER_SELECT), PLAYER_CYCLE_RIGHT(IClientProperty.KEY_PLAYER_CYCLE_RIGHT),
	PLAYER_CYCLE_LEFT(IClientProperty.KEY_PLAYER_CYCLE_LEFT),

	PLAYER_ACTION_BLOCK(IClientProperty.KEY_PLAYER_ACTION_BLOCK),
	PLAYER_ACTION_BLITZ(IClientProperty.KEY_PLAYER_ACTION_BLITZ),
	PLAYER_ACTION_FOUL(IClientProperty.KEY_PLAYER_ACTION_FOUL),
	PLAYER_ACTION_MOVE(IClientProperty.KEY_PLAYER_ACTION_MOVE),
	PLAYER_ACTION_STAND_UP(IClientProperty.KEY_PLAYER_ACTION_STAND_UP),
	PLAYER_ACTION_HAND_OVER(IClientProperty.KEY_PLAYER_ACTION_HAND_OVER),
	PLAYER_ACTION_PASS(IClientProperty.KEY_PLAYER_ACTION_PASS),
	PLAYER_ACTION_JUMP(IClientProperty.KEY_PLAYER_ACTION_JUMP),
	PLAYER_ACTION_END_MOVE(IClientProperty.KEY_PLAYER_ACTION_END_MOVE),
	PLAYER_ACTION_STAB(IClientProperty.KEY_PLAYER_ACTION_STAB),
	PLAYER_ACTION_CHAINSAW(IClientProperty.KEY_PLAYER_ACTION_CHAINSAW),
	PLAYER_ACTION_GAZE(IClientProperty.KEY_PLAYER_ACTION_GAZE),
	PLAYER_ACTION_RANGE_GRID(IClientProperty.KEY_PLAYER_ACTION_RANGE_GRID),
	PLAYER_ACTION_HAIL_MARY_PASS(IClientProperty.KEY_PLAYER_ACTION_HAIL_MARY_PASS),
	PLAYER_ACTION_MULTIPLE_BLOCK(IClientProperty.KEY_PLAYER_ACTION_MULTIPLE_BLOCK),

	TOOLBAR_TURN_END(IClientProperty.KEY_TOOLBAR_TURN_END),
	TOOLBAR_ILLEGAL_PROCEDURE(IClientProperty.KEY_TOOLBAR_ILLEGAL_PROCEDURE),

	MENU_SETUP_LOAD(IClientProperty.KEY_MENU_SETUP_LOAD), MENU_SETUP_SAVE(IClientProperty.KEY_MENU_SETUP_SAVE),
	MENU_REPLAY(IClientProperty.KEY_MENU_REPLAY);

	private String fPropertyName;

	private ActionKey(String pPropertyName) {
		fPropertyName = pPropertyName;
	}

	public String getPropertyName() {
		return fPropertyName;
	}

}
