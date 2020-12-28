package com.balancedbytes.games.ffb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Hashtable;

import com.balancedbytes.games.ffb.model.Skill;
import com.balancedbytes.games.ffb.model.SkillConstants;

/**
 * 
 * @author Kalimar
 */
public class SkillFactory implements INamedObjectFactory {
	private Hashtable<String, Skill> skills;
	private Hashtable<Class<? extends Skill>, Skill> skillMap;

	public SkillFactory() {
		skills = new Hashtable<>();
		skillMap = new Hashtable<>();

		try {
			Field[] fields = SkillConstants.class.getFields();
			for (Field field : fields) {
				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers) && Skill.class.isAssignableFrom(field.getType())) {
					addSkill((Skill) field.get(null));
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		for (Skill skill: skills.values()) {
			skill.postConstruct();
		}
	}

	public Collection<Skill> getSkills() {
		return skills.values();
	}

	private void addSkill(Skill skill) {
		skills.put(skill.getName().toLowerCase(), skill);
		skillMap.put(skill.getClass(), skill);
	}

	public Skill forName(String name) {
		name = name.toLowerCase();
		if (skills.containsKey(name)) {
			return skills.get(name);
		}

		if ("Ball & Chain".equalsIgnoreCase(name) || "Ball &amp; Chain".equalsIgnoreCase(name)) {
			return skills.get("ball and chain");
		}
		return null;
	}

	public Skill forClass(Class<? extends Skill> c) {
		return skillMap.get(c);
	}
}
