package com.fumbbl.ffb.factory;

import com.fumbbl.ffb.Direction;
import com.fumbbl.ffb.FactoryType;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.util.ArrayTool;

/**
 * 
 * @author Kalimar
 */
@FactoryType(FactoryType.Factory.DIRECTION)
@RulesCollection(Rules.COMMON)
public class DirectionFactory implements INamedObjectFactory {

	public Direction forName(String pName) {
		for (Direction direction : Direction.values()) {
			if (direction.getName().equalsIgnoreCase(pName)) {
				return direction;
			}
		}
		return null;
	}

	public Direction forRoll(int pRoll) {
		switch (pRoll) {
		case 1:
			return Direction.NORTH;
		case 2:
			return Direction.NORTHEAST;
		case 3:
			return Direction.EAST;
		case 4:
			return Direction.SOUTHEAST;
		case 5:
			return Direction.SOUTH;
		case 6:
			return Direction.SOUTHWEST;
		case 7:
			return Direction.WEST;
		case 8:
			return Direction.NORTHWEST;
		default:
			return null;
		}
	}

	public Direction[] transform(Direction[] pDirections) {
		Direction[] transformedDirections = new Direction[0];
		if (ArrayTool.isProvided(pDirections)) {
			transformedDirections = new Direction[pDirections.length];
			for (int i = 0; i < transformedDirections.length; i++) {
				transformedDirections[i] = pDirections[i].transform();
			}
		}
		return transformedDirections;
	}

	@Override
	public void initialize(Game game) {
		// TODO Auto-generated method stub
		
	}

}
