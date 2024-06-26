package com.fumbbl.ffb.inducement.bb2020;

import com.fumbbl.ffb.INamedObject;
import com.fumbbl.ffb.inducement.InducementDuration;
import com.fumbbl.ffb.mechanics.StatsMechanic;
import com.fumbbl.ffb.model.skill.SkillClassWithValue;
import com.fumbbl.ffb.modifiers.PlayerStatKey;
import com.fumbbl.ffb.modifiers.TemporaryEnhancements;
import com.fumbbl.ffb.modifiers.TemporaryStatDecrementer;
import com.fumbbl.ffb.modifiers.TemporaryStatIncrementer;
import com.fumbbl.ffb.modifiers.TemporaryStatModifier;
import com.fumbbl.ffb.skill.Pro;
import com.fumbbl.ffb.skill.bb2020.Loner;
import com.fumbbl.ffb.skill.bb2020.MightyBlow;
import com.fumbbl.ffb.skill.bb2020.Stab;

import java.util.HashSet;

public enum Prayer implements INamedObject {
	TREACHEROUS_TRAPDOOR("Treacherous Trapdoor",
		"Trapdoors appear. On a roll of 1 a player stepping on them falls through them",
		InducementDuration.UNTIL_END_OF_HALF, true),
	FRIENDS_WITH_THE_REF("Friends with the Ref", "Argue the call succeeds on 5+"),
	STILETTO("Stiletto",
		"One random player available to play during this drive without Loner gains Stab", false, true) {
		@Override
		public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
			return new TemporaryEnhancements().withSkills(new HashSet<SkillClassWithValue>() {{
				add(new SkillClassWithValue(Stab.class));
			}});
		}

		@Override
		public String eventMessage() {
			return " gains Stab";
		}
	},
	IRON_MAN("Iron Man",
		"One chosen player available to play during this drive without Loner improves AV by 1 (Max 11+)",
		InducementDuration.UNTIL_END_OF_GAME, false, true) {
		@Override
		public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
			return new TemporaryEnhancements().withModifiers(new HashSet<TemporaryStatModifier>() {{
				add(new TemporaryStatIncrementer(PlayerStatKey.AV, mechanic));
			}});
		}

		@Override
		public String eventMessage() {
			return " gains 1 AV";
		}
	},
	KNUCKLE_DUSTERS("Knuckle Dusters",
		"One chosen player available to play during this drive without Loner gains Mighty Blow (+1)", false, true) {
		@Override
		public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
			return new TemporaryEnhancements().withSkills(new HashSet<SkillClassWithValue>() {{
				add(new SkillClassWithValue(MightyBlow.class));
			}});
		}

		@Override
		public String eventMessage() {
			return " gains Mighty Blow (+1)";
		}
	},
	BAD_HABITS("Bad Habits",
		"D3 random opponent players available to play during this drive without Loner gain Loner (2+)", false, true) {
		@Override
		public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
			return new TemporaryEnhancements().withSkills(new HashSet<SkillClassWithValue>() {{
				add(new SkillClassWithValue(Loner.class, "2"));
			}});
		}

		@Override
		public String eventMessage() {
			return " gains Loner (2+)";
		}
	},
	GREASY_CLEATS("Greasy Cleats",
		"One random opponent player available to play during this drive has his MA reduced by 1", false, true) {
		@Override
		public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
			return new TemporaryEnhancements().withModifiers(new HashSet<TemporaryStatModifier>() {{
				add(new TemporaryStatDecrementer(PlayerStatKey.MA, mechanic));
			}});
		}

		@Override
		public String eventMessage() {
			return " loses 1 MA";
		}
	},
	BLESSED_STATUE_OF_NUFFLE("Blessed Statue of Nuffle",
		"One chosen player available to play during this drive without Loner gains Pro", InducementDuration.UNTIL_END_OF_GAME, false, true) {
		@Override
		public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
			return new TemporaryEnhancements().withSkills(new HashSet<SkillClassWithValue>() {{
				add(new SkillClassWithValue(Pro.class));
			}});
		}

		@Override
		public String eventMessage() {
			return " gains Pro";
		}
	},
	MOLES_UNDER_THE_PITCH("Moles under the Pitch",
		"Rushes have a -1 modifier (-2 if both coaches rolled this result)"),
	PERFECT_PASSING("Perfect Passing",
		"Completions generate 2 instead of 1 spp", InducementDuration.UNTIL_END_OF_GAME),
	FAN_INTERACTION("Fan Interaction",
		"Casualties caused by crowd pushes generate 2 spp"),
	NECESSARY_VIOLENCE("Necessary Violence",
		"Casualties generate 3 instead of 2 spp"),
	FOULING_FRENZY("Fouling Frenzy",
		"Casualties caused by fouls generate 2 spp"),
	THROW_A_ROCK("Throw a Rock",
		"If an opposing player should stall they get hit by a rock on a 5+ and are knocked down immediately"),
	UNDER_SCRUTINY("Under Scrutiny",
		"Fouls by opposing players are always spotted",
		InducementDuration.UNTIL_END_OF_HALF),
	INTENSIVE_TRAINING("Intensive Training",
		"One random player available to play during this drive without Loner gains a chosen Primary skill",
		InducementDuration.UNTIL_END_OF_GAME, false, true);
	private final String name, description;
	private final boolean affectsBothTeams, changingPlayer;
	private final InducementDuration duration;

	Prayer(String name, String description) {
		this(name, description, InducementDuration.UNTIL_END_OF_DRIVE);
	}

	Prayer(String name, String description, InducementDuration duration) {
		this(name, description, duration, false, false);
	}

	Prayer(String name, String description, boolean affectsBothTeams, boolean changingPlayer) {
		this(name, description, InducementDuration.UNTIL_END_OF_DRIVE, affectsBothTeams, changingPlayer);
	}

	Prayer(String name, String description, InducementDuration duration, boolean affectsBothTeams) {
		this(name, description, duration, affectsBothTeams, false);
	}

	Prayer(String name, String description, InducementDuration duration, boolean affectsBothTeams, boolean changingPlayer) {
		this.name = name;
		this.description = description;
		this.affectsBothTeams = affectsBothTeams;
		this.duration = duration;
		this.changingPlayer = changingPlayer;
	}

	@Override
	public String getName() {
		return name;
	}

	public boolean affectsBothTeams() {
		return affectsBothTeams;
	}

	public String getDescription() {
		return description;
	}

	public InducementDuration getDuration() {
		return duration;
	}

	public TemporaryEnhancements enhancements(StatsMechanic mechanic) {
		return new TemporaryEnhancements();
	}

	public String eventMessage() {
		return "";
	}

	public boolean isChangingPlayer() {
		return changingPlayer;
	}
}
