package com.fumbbl.ffb.marking;

import com.fumbbl.ffb.InjuryAttribute;
import com.fumbbl.ffb.SeriousInjury;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.factory.SkillFactory;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.Position;
import com.fumbbl.ffb.model.skill.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@MockitoSettings(strictness = Strictness.LENIENT)
class MarkerGeneratorTest {

	private static final String BLOCK_MARKING = "B";
	private static final String BLODGE_MARKING = "X";
	private static final String DODGE_MARKING = "D";
	private static final String BLACKLE_MARKING = "Y";
	private static final String WRECKLE_MARKING = "Q";
	private static final String MA_MARKING = "Ma";
	private static final String AG_MARKING = "Ag";
	private static final String TACKLE_MARKING = "T";
	private static final String WRESTLE_MARKING = "W";
	public static final String OTHER_MARKING = "O";
	private static final String SNEAKY_GIT = "sneaky git";
	private static final String BLOCK = "block";
	private static final String DODGE = "dodge";
	private static final String TACKLE = "tackle";
	private static final String WRESTLE = "wrestle";
	private static final String UNKNOWN = "unknown";
	private static final String SEPARATOR = ", ";

	private final MarkerGenerator generator = new MarkerGenerator();
	private AutoMarkingConfig config;
	private AutoMarkingRecord.Builder builder;
	private MockSkillFactory skillFactory;
	private Set<AutoMarkingRecord> markings;
	@Mock
	private Player<Position> player;
	@Mock
	private Position position;

	@BeforeEach
	public void setUp() {
		skillFactory = new MockSkillFactory();
		config = new AutoMarkingConfig();
		builder = new AutoMarkingRecord.Builder(skillFactory);
		markings = config.getMarkings();

		List<Skill> gainedSkills = new ArrayList<Skill>() {{
			add(skillFactory.forName(BLOCK));
			add(skillFactory.forName(DODGE));
		}};
		given(player.getSkillsIncludingTemporaryOnesWithDuplicates()).willReturn(gainedSkills);

		Skill[] baseSkills = {skillFactory.forName(WRESTLE), skillFactory.forName(TACKLE)};
		given(player.getPosition()).willReturn(position);
		given(position.getSkills()).willReturn(baseSkills);

		given(player.getLastingInjuries()).willReturn(new SeriousInjury[]{
			com.fumbbl.ffb.bb2020.SeriousInjury.SMASHED_KNEE,
			com.fumbbl.ffb.bb2020.SeriousInjury.SMASHED_KNEE,
			com.fumbbl.ffb.bb2020.SeriousInjury.NECK_INJURY
		});
	}

	@Test
	public void generate() {
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void generateWithSeparator() {
		config.setSeparator(SEPARATOR);
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).withApplyRepeatedly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING + SEPARATOR + DODGE_MARKING + SEPARATOR + MA_MARKING + SEPARATOR + MA_MARKING, marking);
	}

	@Test
	public void ignoreUnknownSkills() {
		Skill[] gainedSkills = {skillFactory.forName(BLOCK), skillFactory.forName(DODGE), null};
		given(player.getSkills()).willReturn(gainedSkills);

		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withSkill(UNKNOWN).withMarking(OTHER_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void generateNoMarking() {
		markings.add(builder.withSkill(SNEAKY_GIT).withMarking(BLOCK_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertTrue(marking.isEmpty());
	}

	@Test
	public void generateOnlyForPresentSkills() {
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withSkill(SNEAKY_GIT).withMarking(DODGE_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void generateMarkingsForOverlappingConfigs() {
		markings.add(builder.withSkill(BLOCK).withSkill(DODGE).withMarking(BLODGE_MARKING).build());
		markings.add(builder.withSkill(BLOCK).withSkill(TACKLE).withMarking(BLACKLE_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLODGE_MARKING + BLACKLE_MARKING, marking);
	}

	@Test
	public void ignoreCombinedConfigsForGainedSkillsWithOnlyPartialMatch() {
		markings.add(builder.withSkill(BLOCK).withSkill(DODGE).withMarking(BLODGE_MARKING).build());
		markings.add(builder.withSkill(BLOCK).withSkill(TACKLE).withMarking(BLACKLE_MARKING).withGainedOnly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLODGE_MARKING, marking);
	}

	@Test
	public void ignoreSubsets() {
		markings.add(builder.withSkill(BLOCK).withSkill(DODGE).withMarking(BLODGE_MARKING).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLODGE_MARKING, marking);
	}

	@Test
	public void ignoreSubsetUnlessApplyToMakesDifference() {
		markings.add(builder.withSkill(BLOCK).withSkill(DODGE).withMarking(BLODGE_MARKING).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withApplyTo(ApplyTo.OPPONENT).build());
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).withApplyTo(ApplyTo.BOTH).build());

		String marking = generator.generate(player, config, false);

		assertEquals(BLOCK_MARKING + DODGE_MARKING, marking);
	}

	@Test
	public void ignoreSubsetUnlessGainedOnlyMakesDifference() {
		markings.add(builder.withSkill(WRESTLE).withSkill(TACKLE).withMarking(WRECKLE_MARKING).withGainedOnly(true).build());
		markings.add(builder.withSkill(WRESTLE).withMarking(WRESTLE_MARKING).build());

		String marking = generator.generate(player, config, false);

		assertEquals(WRESTLE_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigs() {
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING + DODGE_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithOpponent() {
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).build());

		String marking = generator.generate(player, config, false);

		assertEquals(BLOCK_MARKING + DODGE_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithMatchingApplyTo() {
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withApplyTo(ApplyTo.OPPONENT).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithOpponentAndMatchingApplyTo() {
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withApplyTo(ApplyTo.OPPONENT).build());

		String marking = generator.generate(player, config, false);

		assertEquals(DODGE_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithGainedOnly() {
		markings.add(builder.withSkill(WRESTLE).withMarking(WRESTLE_MARKING).withGainedOnly(true).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withGainedOnly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(DODGE_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithOpponentAndGainedOnly() {
		markings.add(builder.withSkill(WRESTLE).withMarking(WRESTLE_MARKING).withGainedOnly(true).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withGainedOnly(true).build());

		String marking = generator.generate(player, config, false);

		assertEquals(DODGE_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithMatchingGainedAndApplyTo() {
		markings.add(builder.withSkill(WRESTLE).withMarking(WRESTLE_MARKING).withGainedOnly(true).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withApplyTo(ApplyTo.OPPONENT).build());
		markings.add(builder.withSkill(TACKLE).withMarking(TACKLE_MARKING).withGainedOnly(true).withApplyTo(ApplyTo.OPPONENT).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void generateForAllMatchingConfigsWithOpponentAndMatchingGainedAndApplyTo() {
		markings.add(builder.withSkill(WRESTLE).withMarking(WRESTLE_MARKING).withGainedOnly(true).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).withApplyTo(ApplyTo.OWN).build());
		markings.add(builder.withSkill(DODGE).withMarking(DODGE_MARKING).withApplyTo(ApplyTo.OPPONENT).build());
		markings.add(builder.withSkill(TACKLE).withMarking(TACKLE_MARKING).withGainedOnly(true).withApplyTo(ApplyTo.OPPONENT).build());

		String marking = generator.generate(player, config, false);

		assertEquals(DODGE_MARKING, marking);
	}

	@Test
	public void generateForSingleInjuryMarkings() {
		markings.add(builder.withInjury(InjuryAttribute.AG).withMarking(AG_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(AG_MARKING + MA_MARKING, marking);
	}

	@Test
	public void ignoreGainedOnlyOnInjuryMarkings() {
		markings.add(builder.withInjury(InjuryAttribute.AG).withMarking(AG_MARKING).withGainedOnly(true).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).withGainedOnly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(AG_MARKING + MA_MARKING, marking);
	}

	@Test
	public void generateForMultiInjuryMarkings() {
		markings.add(builder.withInjury(InjuryAttribute.AG).withInjury(InjuryAttribute.AG).withMarking(AG_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void generateForSingleInjuryMarkingsOnlyForOwnPlayer() {
		markings.add(builder.withInjury(InjuryAttribute.AG).withApplyTo(ApplyTo.OWN).withMarking(AG_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withApplyTo(ApplyTo.OPPONENT).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(AG_MARKING, marking);
	}

	@Test
	public void generateForSingleInjuryMarkingsOnlyForOpponent() {
		markings.add(builder.withInjury(InjuryAttribute.AG).withApplyTo(ApplyTo.OWN).withMarking(AG_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withApplyTo(ApplyTo.OPPONENT).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, false);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void generateForCombinedSkillAndInjuryMarkings() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.AV).withSkill(DODGE).withMarking(DODGE_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.AG).withSkill(SNEAKY_GIT).withMarking(AG_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void ignoreInjuryOnlyMarkingsIfTheyAreASubset() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withSkill(BLOCK).withMarking(BLOCK_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void ignoreCombinedSkillAndInjuryMarkingsIfGainedOnlyDoesNotMatch() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withSkill(WRESTLE).withGainedOnly(true).withMarking(WRESTLE_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void generateSingleMarkingForMultiStatIncreases() {
		List<Skill> increases = new ArrayList<Skill>() {{
			add(skillFactory.forName("+AG"));
			add(skillFactory.forName("+AG"));
		}};
		given(player.getSkillsIncludingTemporaryOnesWithDuplicates()).willReturn(increases);
		given(player.getLastingInjuries()).willReturn(new SeriousInjury[0]);
		markings.add(builder.withSkill("+AG").withMarking(AG_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(AG_MARKING, marking);
	}

	@Test
	public void generateMarkingMatchingForMultiStatIncreases() {
		List<Skill> increases = new ArrayList<Skill>() {{
			add(skillFactory.forName("+AG"));
			add(skillFactory.forName("+AG"));
		}};
		given(player.getSkillsIncludingTemporaryOnesWithDuplicates()).willReturn(increases);
		given(player.getLastingInjuries()).willReturn(new SeriousInjury[0]);
		markings.add(builder.withSkill("+AG").withSkill("+AG").withMarking(AG_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(AG_MARKING, marking);
	}

	@Test
	public void ignoreMatchingForMultiStatIncreasesIfOnlyOneIsPresent() {
		List<Skill> increases = new ArrayList<Skill>() {{
			add(skillFactory.forName("+AG"));
		}};
		given(player.getSkillsIncludingTemporaryOnesWithDuplicates()).willReturn(increases);
		given(player.getLastingInjuries()).willReturn(new SeriousInjury[0]);
		markings.add(builder.withSkill("+AG").withSkill("+AG").withMarking(AG_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertTrue(marking.isEmpty());
	}

	@Test
	public void generateOnlyForNetStatIncreases() {
		Skill[] increases = new Skill[]{skillFactory.forName("+AG", SkillCategory.STAT_INCREASE), skillFactory.forName("+AG", SkillCategory.STAT_INCREASE)};
		given(player.getSkills()).willReturn(increases);
		markings.add(builder.withSkill("+AG").withMarking(AG_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.AG).withMarking(AG_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(AG_MARKING, marking);
	}

	@Test
	public void generateOnlyForNetInjuries() {
		Skill[] increases = new Skill[]{skillFactory.forName("+MA", SkillCategory.STAT_INCREASE)};
		given(player.getSkills()).willReturn(increases);
		markings.add(builder.withSkill("+MA").withMarking(MA_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void generateSingleMarkingForMultiInjuries() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void generateMarkingMatchingForMultiInjuries() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void ignoreMatchingForMultiInjuriesIfOnlyOneIsPresent() {
		markings.add(builder.withInjury(InjuryAttribute.AG).withInjury(InjuryAttribute.AG).withMarking(MA_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertTrue(marking.isEmpty());
	}

	@Test
	public void sortInjuriesLastAndAlphabeticallyOtherwise() {
		markings.add(builder.withSkill(BLOCK).withSkill(DODGE).withMarking(BLODGE_MARKING).build());
		markings.add(builder.withSkill(WRESTLE).withMarking(WRESTLE_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.AG).withSkill(TACKLE).withMarking(OTHER_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(OTHER_MARKING + WRESTLE_MARKING + BLODGE_MARKING + MA_MARKING, marking);
	}

	@Test
	public void ignoreIdenticalMarkingWithGainedOnly() {
		markings.add(builder.withSkill(BLOCK).withGainedOnly(true).withMarking(OTHER_MARKING).build());
		markings.add(builder.withSkill(BLOCK).withMarking(BLOCK_MARKING).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	@Test
	public void ignoreIdenticalMarkingWithNoRepetition() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(OTHER_MARKING).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).withApplyRepeatedly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING + MA_MARKING, marking);
	}

	@Test
	public void generateRepeatedMarking() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).withApplyRepeatedly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING + MA_MARKING, marking);
	}

	@Test
	public void generateMultiInjuryMarkingOverRepeated() {
		markings.add(builder.withInjury(InjuryAttribute.MA).withMarking(OTHER_MARKING).withApplyRepeatedly(true).build());
		markings.add(builder.withInjury(InjuryAttribute.MA).withInjury(InjuryAttribute.MA).withMarking(MA_MARKING).withApplyRepeatedly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(MA_MARKING, marking);
	}

	@Test
	public void generateRepeatedMarkingOnlyOnceIfNotCompletelyApplicable() {
		markings.add(builder.withSkill(BLOCK).withInjury(InjuryAttribute.MA).withMarking(BLOCK_MARKING).withApplyRepeatedly(true).build());

		String marking = generator.generate(player, config, true);

		assertEquals(BLOCK_MARKING, marking);
	}

	private static class MockSkillFactory extends SkillFactory {

		private final Map<String, Skill> skills = new HashMap<>();

		@Override
		public Skill forName(String name) {
			return forName(name, null);
		}

		public Skill forName(String name, SkillCategory skillCategory) {
			if (UNKNOWN.equalsIgnoreCase(name)) {
				return null;
			}
			name = name.toLowerCase();
			if (!skills.containsKey(name) || (skillCategory != null && skills.get(name).getCategory() != skillCategory)) {
				Skill skill = mock(Skill.class);
				given(skill.getName()).willReturn(name);
				if (skillCategory != null) {
					given(skill.getCategory()).willReturn(skillCategory);
				}
				skills.put(name, skill);
			}

			return skills.get(name);
		}
	}
}