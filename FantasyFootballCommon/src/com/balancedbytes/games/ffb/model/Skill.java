package com.balancedbytes.games.ffb.model;

import com.balancedbytes.games.ffb.ArmorModifier;
import com.balancedbytes.games.ffb.modifiers.CatchModifier;
import com.balancedbytes.games.ffb.DodgeModifier;
import com.balancedbytes.games.ffb.INamedObject;
import com.balancedbytes.games.ffb.InjuryModifier;
import com.balancedbytes.games.ffb.LeapModifier;
import com.balancedbytes.games.ffb.PassModifier;
import com.balancedbytes.games.ffb.PickupModifier;
import com.balancedbytes.games.ffb.ReRollSource;
import com.balancedbytes.games.ffb.ReRolledAction;
import com.balancedbytes.games.ffb.SkillCategory;
import com.balancedbytes.games.ffb.model.modifier.CancelSkillProperty;
import com.balancedbytes.games.ffb.modifiers.CatchModifierKey;
import com.balancedbytes.games.ffb.modifiers.InterceptionModifierKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public abstract class Skill implements INamedObject {

	private String name;
	private SkillCategory category;
	private List<PlayerModifier> playerModifiers = new ArrayList<>();
	private List<PassModifier> passModifiers = new ArrayList<>();
	private List<PickupModifier> pickupModifiers = new ArrayList<>();
	private List<DodgeModifier> dodgeModifiers = new ArrayList<>();
	private List<LeapModifier> leapModifiers = new ArrayList<>();
	private List<InterceptionModifierKey> interceptionModifiers = new ArrayList<>();
	private List<InjuryModifier> injuryModifiers = new ArrayList<>();
	private List<ArmorModifier> armorModifiers = new ArrayList<>();
	private List<CatchModifierKey> catchModifiers = new ArrayList<>();
	private ISkillBehaviour<? extends Skill> behaviour;
	private List<ISkillProperty> skillProperties = new ArrayList<>();
	private Hashtable<ReRolledAction, ReRollSource> rerollSources = new Hashtable<>();

	public Skill(String name, SkillCategory category) {
		this.name = name;
		this.category = category;
	}

	public void postConstruct() {}

	@Override
	public String getName() {
		return name;
	}

	public SkillCategory getCategory() {
		return category;
	}

	public boolean equals(Object other) {
		return name != null && other instanceof Skill && name.equals(((Skill) other).name);
	}

	public static Comparator<Skill> getComparator() {
		return Comparator.comparing(Skill::getName);
	}

	protected void registerModifier(LeapModifier modifier) {
		leapModifiers.add(modifier);
	}

	protected void registerModifier(PassModifier modifier) {
		passModifiers.add(modifier);
	}

	protected void registerModifier(PickupModifier modifier) {
		pickupModifiers.add(modifier);
	}

	protected void registerModifier(DodgeModifier modifier) {
		dodgeModifiers.add(modifier);
	}

	protected void registerModifier(PlayerModifier modifier) {
		playerModifiers.add(modifier);
	}

	protected void registerModifierKey(InterceptionModifierKey modifier) {
		interceptionModifiers.add(modifier);
	}

	protected void registerModifier(ArmorModifier modifier) {
		armorModifiers.add(modifier);
	}

	protected void registerModifier(InjuryModifier modifier) {
		injuryModifiers.add(modifier);
	}

	protected void registerModifierKey(CatchModifierKey modifier) {
		catchModifiers.add(modifier);
	}

	protected void registerProperty(ISkillProperty property) {
		skillProperties.add(property);
	}

	protected void registerRerollSource(ReRolledAction action, ReRollSource source) {
		rerollSources.put(action, source);
	}

	public ISkillBehaviour<? extends Skill> getSkillBehaviour() {
		return behaviour;
	}

	public List<PlayerModifier> getPlayerModifiers() {
		return playerModifiers;
	}

	public List<PassModifier> getPassModifiers() {
		return passModifiers;
	}

	public List<PickupModifier> getPickupModifiers() {
		return pickupModifiers;
	}

	public List<DodgeModifier> getDodgeModifiers() {
		return dodgeModifiers;
	}

	public List<LeapModifier> getLeapModifiers() {
		return leapModifiers;
	}

	public List<InterceptionModifierKey> getInterceptionModifiers() {
		return interceptionModifiers;
	}

	public List<CatchModifierKey> getCatchModifiers() {
		return catchModifiers;
	}

	public List<ArmorModifier> getArmorModifiers() {
		return armorModifiers;
	}

	public List<InjuryModifier> getInjuryModifiers() {
		return injuryModifiers;
	}

	public void setBehaviour(ISkillBehaviour<? extends Skill> behaviour) {
		this.behaviour = behaviour;
	}

	public int getCost(Player<?> player) {
		Position position = player.getPosition();
		if (position.hasSkill(this)) {
			return 0;
		}
		if (position.isDoubleCategory(category)) {
			return 30000;
		} else {
			return 20000;
		}
	}

	public String[] getSkillUseDescription() {
		return null;
	}

	public boolean canCancel(Skill otherSkill) {
		for (ISkillProperty skillProperty : skillProperties) {
			if (skillProperty instanceof CancelSkillProperty
					&& ((CancelSkillProperty) skillProperty).cancelsSkill(otherSkill)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasSkillProperty(ISkillProperty property) {
		for (ISkillProperty skillProperty : skillProperties) {
			if (property.matches(skillProperty)) {
				return true;
			}
		}
		return false;
	}

	public String getConfusionMessage() {
		return "is confused";
	}

	public ReRollSource getRerollSource(ReRolledAction action) {
		if (rerollSources.containsKey(action)) {
			return rerollSources.get(action);
		}
		return null;
	}
}
