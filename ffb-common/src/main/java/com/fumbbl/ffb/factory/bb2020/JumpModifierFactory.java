package com.fumbbl.ffb.factory.bb2020;

import com.fumbbl.ffb.FactoryType;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.Team;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.modifiers.JumpContext;
import com.fumbbl.ffb.modifiers.JumpModifier;
import com.fumbbl.ffb.modifiers.JumpModifierCollection;
import com.fumbbl.ffb.modifiers.ModifierType;
import com.fumbbl.ffb.modifiers.RollModifier;
import com.fumbbl.ffb.util.Scanner;
import com.fumbbl.ffb.util.UtilCards;
import com.fumbbl.ffb.util.UtilPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Kalimar
 */
@FactoryType(FactoryType.Factory.JUMP_MODIFIER)
@RulesCollection(Rules.BB2020)
public class JumpModifierFactory extends com.fumbbl.ffb.factory.JumpModifierFactory {

	private JumpModifierCollection jumpModifierCollection;

	public JumpModifier forName(String name) {
		return Stream.concat(
				jumpModifierCollection.getModifiers().stream(),
				modifierAggregator.getJumpModifiers().stream())
				.filter(modifier -> modifier.getName().equals(name))
				.findFirst()
				.orElse(null);	}


	@Override
	protected Scanner<JumpModifierCollection> getScanner() {
		return new Scanner<>(JumpModifierCollection.class);
	}

	@Override
	protected JumpModifierCollection getModifierCollection() {
		return jumpModifierCollection;
	}

	@Override
	protected void setModifierCollection(JumpModifierCollection modifierCollection) {
		this.jumpModifierCollection = modifierCollection;
	}

	@Override
	protected Collection<JumpModifier> getModifier(Skill skill) {
		return skill.getJumpModifiers();
	}

	@Override
	protected Optional<JumpModifier> checkClass(RollModifier<?> modifier) {
		return modifier instanceof JumpModifier ? Optional.of((JumpModifier) modifier) : Optional.empty();
	}

	@Override
	protected boolean isAffectedByDisturbingPresence(JumpContext context) {
		return false;
	}

	@Override
	protected boolean isAffectedByTackleZones(JumpContext context) {
		return false;
	}

	@Override
	public Set<JumpModifier> findModifiers(JumpContext context) {
		Set<JumpModifier> modifiers = new HashSet<>();
		if (!context.getPlayer().hasSkillProperty(NamedProperties.ignoreTacklezonesWhenJumping)) {
			Optional<JumpModifier> tacklezoneModifier = getTacklezoneModifier(context);
			tacklezoneModifier.ifPresent(modifiers::add);
		}

		if (!UtilCards.hasSkillToCancelProperty(context.getPlayer(), NamedProperties.makesJumpingHarder)) {

			prehensileTailModifier(findNumberOfPrehensileTails(context.getGame(), context.getFrom()))
				.ifPresent(modifiers::add);
		}

		modifiers.addAll(super.findModifiers(context));

		int sum = modifiers.stream().mapToInt(JumpModifier::getModifier).sum();
		context.setAccumulatedModifiers(sum + context.getAccumulatedModifiers());
		for (Skill skill : context.getPlayer().getSkills()) {
			skill.getJumpModifiers().stream()
				.filter(modifier -> modifier.getType() == ModifierType.DEPENDS_ON_SUM_OF_OTHERS
					&& modifier.appliesToContext(skill, context))
				.forEach(modifiers::add);
		}
		return modifiers;
	}

	private int findNumberOfPrehensileTails(Game pGame, FieldCoordinate pCoordinateFrom) {
		ActingPlayer actingPlayer = pGame.getActingPlayer();
		Team otherTeam = UtilPlayer.findOtherTeam(pGame, actingPlayer.getPlayer());
		int nrOfPrehensileTails = 0;
		Player<?>[] opponents = UtilPlayer.findAdjacentPlayersWithTacklezones(pGame, otherTeam, pCoordinateFrom, true);
		for (Player<?> opponent : opponents) {
			if (UtilCards.hasSkillWithProperty(opponent, NamedProperties.makesJumpingHarder)) {
				nrOfPrehensileTails++;
			}
		}
		return nrOfPrehensileTails;
	}

	private Optional<JumpModifier> prehensileTailModifier(int number) {
		return jumpModifierCollection.getModifiers(ModifierType.PREHENSILE_TAIL).stream()
			.filter(modifier -> modifier.getMultiplier() == number)
			.findFirst();
	}

	@Override
	protected int numberOfTacklezones(JumpContext context) {
		Team otherTeam = UtilPlayer.findOtherTeam(context.getGame(), context.getPlayer());

		int fromZones = UtilPlayer.findAdjacentPlayersWithTacklezones(context.getGame(), otherTeam, context.getFrom(), false).length;
		int toZones = UtilPlayer.findAdjacentPlayersWithTacklezones(context.getGame(), otherTeam, context.getTo(), false).length;

		return Math.max(fromZones, toZones);
	}
}
