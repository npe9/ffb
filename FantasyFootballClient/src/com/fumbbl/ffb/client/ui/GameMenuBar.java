package com.fumbbl.ffb.client.ui;

import com.fumbbl.ffb.ClientMode;
import com.fumbbl.ffb.ClientStateId;
import com.fumbbl.ffb.ConcedeGameStatus;
import com.fumbbl.ffb.FantasyFootballException;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.IIconProperty;
import com.fumbbl.ffb.PlayerType;
import com.fumbbl.ffb.TurnMode;
import com.fumbbl.ffb.client.ActionKey;
import com.fumbbl.ffb.client.ClientData;
import com.fumbbl.ffb.client.ClientReplayer;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.client.IClientProperty;
import com.fumbbl.ffb.client.IClientPropertyValue;
import com.fumbbl.ffb.client.PlayerIconFactory;
import com.fumbbl.ffb.client.UserInterface;
import com.fumbbl.ffb.client.dialog.DialogAbout;
import com.fumbbl.ffb.client.dialog.DialogChatCommands;
import com.fumbbl.ffb.client.dialog.DialogGameStatistics;
import com.fumbbl.ffb.client.dialog.DialogKeyBindings;
import com.fumbbl.ffb.client.dialog.DialogSoundVolume;
import com.fumbbl.ffb.client.dialog.IDialog;
import com.fumbbl.ffb.client.dialog.IDialogCloseListener;
import com.fumbbl.ffb.dialog.DialogId;
import com.fumbbl.ffb.inducement.Card;
import com.fumbbl.ffb.inducement.CardType;
import com.fumbbl.ffb.inducement.Inducement;
import com.fumbbl.ffb.inducement.Usage;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.InducementSet;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.model.PlayerResult;
import com.fumbbl.ffb.model.Team;
import com.fumbbl.ffb.option.GameOptionId;
import com.fumbbl.ffb.option.IGameOption;
import com.fumbbl.ffb.util.ArrayTool;
import com.fumbbl.ffb.util.StringTool;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Kalimar
 */
public class GameMenuBar extends JMenuBar implements ActionListener, IDialogCloseListener {

	private static final String[] _SAVED_USER_SETTINGS = { IClientProperty.SETTING_SOUND_MODE,
			IClientProperty.SETTING_SOUND_VOLUME, IClientProperty.SETTING_ICONS, IClientProperty.SETTING_CHATLOG,
			IClientProperty.SETTING_AUTOMOVE, IClientProperty.SETTING_PITCH_CUSTOMIZATION,
			IClientProperty.SETTING_PITCH_MARKINGS, IClientProperty.SETTING_TEAM_LOGOS, IClientProperty.SETTING_PITCH_WEATHER,
			IClientProperty.SETTING_RANGEGRID };

	private static final String _REPLAY_MODE_ON = "Replay Mode";
	private static final String _REPLAY_MODE_OFF = "Spectator Mode";

	private final FantasyFootballClient fClient;

	private final JMenuItem fGameReplayMenuItem;
	private final JMenuItem fGameConcessionMenuItem;
	private final JMenuItem fGameStatisticsMenuItem;

	private final JMenuItem fLoadSetupMenuItem;
	private final JMenuItem fSaveSetupMenuItem;

	private final JMenuItem fRestoreDefaultsMenuItem;

	private final JMenuItem fSoundVolumeItem;
	private final JRadioButtonMenuItem fSoundOnMenuItem;
	private final JRadioButtonMenuItem fSoundMuteSpectatorsMenuItem;
	private final JRadioButtonMenuItem fSoundOffMenuItem;

	private final JRadioButtonMenuItem fIconsAbstract;
	private final JRadioButtonMenuItem fIconsRosterOpponent;
	private final JRadioButtonMenuItem fIconsRosterBoth;
	private final JRadioButtonMenuItem fIconsTeam;

	private final JMenu fAutomoveMenu;
	private final JRadioButtonMenuItem fAutomoveOnMenuItem;
	private final JRadioButtonMenuItem fAutomoveOffMenuItem;

	private final JMenu fPitchMenu;

	private final JMenu fPitchCustomizationMenu;
	private final JRadioButtonMenuItem fCustomPitchMenuItem;
	private final JRadioButtonMenuItem fDefaultPitchMenuItem;
	private final JRadioButtonMenuItem fBasicPitchMenuItem;

	private final JMenu fPitchMarkingsMenu;
	private final JRadioButtonMenuItem fPitchMarkingsOnMenuItem;
	private final JRadioButtonMenuItem fPitchMarkingsOffMenuItem;

	private final JMenu fTeamLogoMenu;
	private final JRadioButtonMenuItem fTeamLogoBothMenuItem;
	private final JRadioButtonMenuItem fTeamLogoOwnMenuItem;
	private final JRadioButtonMenuItem fTeamLogoNoneMenuItem;

	private final JRadioButtonMenuItem fPitchWeatherOnMenuItem;
	private final JRadioButtonMenuItem fPitchWeatherOffMenuItem;

	private final JMenu fRangeGridMenu;
	private final JRadioButtonMenuItem fRangeGridAlwaysOnMenuItem;
	private final JRadioButtonMenuItem fRangeGridToggleMenuItem;

	private final JMenu fMissingPlayersMenu;

	private final JMenu fInducementsMenu;
	private JMenu fInducementsHomeMenu;
	private JMenu fInducementsAwayMenu;

	private final JMenu fActiveCardsMenu;
	private JMenu fActiveCardsHomeMenu;
	private JMenu fActiveCardsAwayMenu;

	private final JMenu fGameOptionsMenu;

	private final JMenu fHelpMenu;
	private final JMenuItem fAboutMenuItem;
	private final JMenuItem fChatCommandsMenuItem;
	private final JMenuItem fKeyBindingsMenuItem;

	private IDialog fDialogShown;

	private int fCurrentInducementTotalHome;
	private int fCurrentUsedCardsHome;
	private int fCurrentInducementTotalAway;
	private int fCurrentUsedCardsAway;

	private Card[] fCurrentActiveCardsHome;
	private Card[] fCurrentActiveCardsAway;

	private class MenuPlayerMouseListener extends MouseAdapter {

		private final Player<?> fPlayer;

		public MenuPlayerMouseListener(Player<?> pPlayer) {
			fPlayer = pPlayer;
		}

		public void mouseEntered(MouseEvent pMouseEvent) {
			ClientData clientData = getClient().getClientData();
			// do not interfere with dragging (MNG player reappears on pitch bug)
			if ((clientData.getSelectedPlayer() != fPlayer) && (clientData.getDragStartPosition() == null)) {
				clientData.setSelectedPlayer(fPlayer);
				getClient().getUserInterface().refreshSideBars();
			}
		}

	}

	public GameMenuBar(FantasyFootballClient pClient) {

		setFont(new Font("Sans Serif", Font.PLAIN, 12));

		fClient = pClient;

		JMenu fGameMenu = new JMenu("Game");
		fGameMenu.setMnemonic(KeyEvent.VK_G);
		add(fGameMenu);

		fGameReplayMenuItem = new JMenuItem(_REPLAY_MODE_ON, KeyEvent.VK_R);
		String keyMenuReplay = getClient().getProperty(IClientProperty.KEY_MENU_REPLAY);
		if (StringTool.isProvided(keyMenuReplay)) {
			fGameReplayMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyMenuReplay));
		}
		fGameReplayMenuItem.addActionListener(this);
		fGameMenu.add(fGameReplayMenuItem);

		fGameConcessionMenuItem = new JMenuItem("Concede Game", KeyEvent.VK_C);
		fGameConcessionMenuItem.addActionListener(this);
		fGameConcessionMenuItem.setEnabled(false);
		fGameMenu.add(fGameConcessionMenuItem);

		fGameStatisticsMenuItem = new JMenuItem("Game Statistics", KeyEvent.VK_S);
		fGameStatisticsMenuItem.addActionListener(this);
		fGameStatisticsMenuItem.setEnabled(false);
		fGameMenu.add(fGameStatisticsMenuItem);

		JMenu fTeamSetupMenu = new JMenu("Team Setup");
		fTeamSetupMenu.setMnemonic(KeyEvent.VK_T);
		add(fTeamSetupMenu);

		fLoadSetupMenuItem = new JMenuItem("Load Setup", KeyEvent.VK_L);
		String menuSetupLoad = getClient().getProperty(IClientProperty.KEY_MENU_SETUP_LOAD);
		if (StringTool.isProvided(menuSetupLoad)) {
			fLoadSetupMenuItem.setAccelerator(KeyStroke.getKeyStroke(menuSetupLoad));
		}
		fLoadSetupMenuItem.addActionListener(this);
		fTeamSetupMenu.add(fLoadSetupMenuItem);

		fSaveSetupMenuItem = new JMenuItem("Save Setup", KeyEvent.VK_S);
		String menuSetupSave = getClient().getProperty(IClientProperty.KEY_MENU_SETUP_SAVE);
		if (StringTool.isProvided(menuSetupSave)) {
			fSaveSetupMenuItem.setAccelerator(KeyStroke.getKeyStroke(menuSetupSave));
		}
		fSaveSetupMenuItem.addActionListener(this);
		fTeamSetupMenu.add(fSaveSetupMenuItem);

		JMenu fUserSettingsMenu = new JMenu("User Settings");
		fUserSettingsMenu.setMnemonic(KeyEvent.VK_U);
		add(fUserSettingsMenu);

		JMenu fSoundMenu = new JMenu("Sound");
		fSoundMenu.setMnemonic(KeyEvent.VK_S);
		fUserSettingsMenu.add(fSoundMenu);

		fSoundVolumeItem = new JMenuItem("Sound Volume");
		fSoundVolumeItem.setMnemonic(KeyEvent.VK_V);
		fSoundVolumeItem.addActionListener(this);
		fSoundMenu.add(fSoundVolumeItem);

		fSoundMenu.addSeparator();

		ButtonGroup soundGroup = new ButtonGroup();

		fSoundOnMenuItem = new JRadioButtonMenuItem("Sound on");
		fSoundOnMenuItem.addActionListener(this);
		soundGroup.add(fSoundOnMenuItem);
		fSoundMenu.add(fSoundOnMenuItem);

		fSoundMuteSpectatorsMenuItem = new JRadioButtonMenuItem("Mute spectators");
		fSoundMuteSpectatorsMenuItem.addActionListener(this);
		soundGroup.add(fSoundMuteSpectatorsMenuItem);
		fSoundMenu.add(fSoundMuteSpectatorsMenuItem);

		fSoundOffMenuItem = new JRadioButtonMenuItem("Sound off");
		fSoundOffMenuItem.addActionListener(this);
		soundGroup.add(fSoundOffMenuItem);
		fSoundMenu.add(fSoundOffMenuItem);

		JMenu fIconsMenu = new JMenu("Icons");
		fIconsMenu.setMnemonic(KeyEvent.VK_I);
		fUserSettingsMenu.add(fIconsMenu);

		ButtonGroup iconsGroup = new ButtonGroup();

		fIconsTeam = new JRadioButtonMenuItem("Team icons");
		fIconsTeam.setMnemonic(KeyEvent.VK_T);
		fIconsTeam.addActionListener(this);
		iconsGroup.add(fIconsTeam);
		fIconsMenu.add(fIconsTeam);

		fIconsRosterOpponent = new JRadioButtonMenuItem("Roster icons (Opponent)");
		fIconsRosterOpponent.setMnemonic(KeyEvent.VK_O);
		fIconsRosterOpponent.addActionListener(this);
		iconsGroup.add(fIconsRosterOpponent);
		fIconsMenu.add(fIconsRosterOpponent);

		fIconsRosterBoth = new JRadioButtonMenuItem("Roster icons (Both)");
		fIconsRosterBoth.setMnemonic(KeyEvent.VK_B);
		fIconsRosterBoth.addActionListener(this);
		iconsGroup.add(fIconsRosterBoth);
		fIconsMenu.add(fIconsRosterBoth);

		fIconsAbstract = new JRadioButtonMenuItem("Abstract icons");
		fIconsAbstract.setMnemonic(KeyEvent.VK_A);
		fIconsAbstract.addActionListener(this);
		iconsGroup.add(fIconsAbstract);
		fIconsMenu.add(fIconsAbstract);

		fAutomoveMenu = new JMenu("Automove");
		fAutomoveMenu.setMnemonic(KeyEvent.VK_A);
		fUserSettingsMenu.add(fAutomoveMenu);

		ButtonGroup automoveGroup = new ButtonGroup();

		fAutomoveOnMenuItem = new JRadioButtonMenuItem("Automove on");
		fAutomoveOnMenuItem.addActionListener(this);
		automoveGroup.add(fAutomoveOnMenuItem);
		fAutomoveMenu.add(fAutomoveOnMenuItem);

		fAutomoveOffMenuItem = new JRadioButtonMenuItem("Automove off");
		fAutomoveOffMenuItem.addActionListener(this);
		automoveGroup.add(fAutomoveOffMenuItem);
		fAutomoveMenu.add(fAutomoveOffMenuItem);

		fPitchMenu = new JMenu("Pitch");
		fPitchMenu.setMnemonic(KeyEvent.VK_P);
		fUserSettingsMenu.add(fPitchMenu);

		fPitchCustomizationMenu = new JMenu("Pitch Customization");
		fPitchCustomizationMenu.setMnemonic(KeyEvent.VK_C);
		fPitchMenu.add(fPitchCustomizationMenu);

		ButtonGroup pitchCustomGroup = new ButtonGroup();

		fCustomPitchMenuItem = new JRadioButtonMenuItem("Use Custom Pitch");
		fCustomPitchMenuItem.addActionListener(this);
		pitchCustomGroup.add(fCustomPitchMenuItem);
		fPitchCustomizationMenu.add(fCustomPitchMenuItem);

		fDefaultPitchMenuItem = new JRadioButtonMenuItem("Use Default Pitch");
		fDefaultPitchMenuItem.addActionListener(this);
		pitchCustomGroup.add(fDefaultPitchMenuItem);
		fPitchCustomizationMenu.add(fDefaultPitchMenuItem);

		fBasicPitchMenuItem = new JRadioButtonMenuItem("Use Basic Pitch");
		fBasicPitchMenuItem.addActionListener(this);
		pitchCustomGroup.add(fBasicPitchMenuItem);
		fPitchCustomizationMenu.add(fBasicPitchMenuItem);

		fPitchMarkingsMenu = new JMenu("Pitch Markings");
		fPitchMarkingsMenu.setMnemonic(KeyEvent.VK_M);
		fPitchMenu.add(fPitchMarkingsMenu);

		ButtonGroup tdDistanceGroup = new ButtonGroup();

		fPitchMarkingsOnMenuItem = new JRadioButtonMenuItem("Pitch Markings on");
		fPitchMarkingsOnMenuItem.addActionListener(this);
		tdDistanceGroup.add(fPitchMarkingsOnMenuItem);
		fPitchMarkingsMenu.add(fPitchMarkingsOnMenuItem);

		fPitchMarkingsOffMenuItem = new JRadioButtonMenuItem("Pitch Markings off");
		fPitchMarkingsOffMenuItem.addActionListener(this);
		tdDistanceGroup.add(fPitchMarkingsOffMenuItem);
		fPitchMarkingsMenu.add(fPitchMarkingsOffMenuItem);

		fTeamLogoMenu = new JMenu("Team Logo");
		fTeamLogoMenu.setMnemonic(KeyEvent.VK_T);
		fPitchMenu.add(fTeamLogoMenu);

		ButtonGroup teamLogoGroup = new ButtonGroup();

		fTeamLogoBothMenuItem = new JRadioButtonMenuItem("Show both Team-Logos");
		fTeamLogoBothMenuItem.addActionListener(this);
		teamLogoGroup.add(fTeamLogoBothMenuItem);
		fTeamLogoMenu.add(fTeamLogoBothMenuItem);

		fTeamLogoOwnMenuItem = new JRadioButtonMenuItem("Show my Team-Logo only");
		fTeamLogoOwnMenuItem.addActionListener(this);
		teamLogoGroup.add(fTeamLogoOwnMenuItem);
		fTeamLogoMenu.add(fTeamLogoOwnMenuItem);

		fTeamLogoNoneMenuItem = new JRadioButtonMenuItem("Show no Team-Logos");
		fTeamLogoNoneMenuItem.addActionListener(this);
		teamLogoGroup.add(fTeamLogoNoneMenuItem);
		fTeamLogoMenu.add(fTeamLogoNoneMenuItem);

		JMenu fPitchWeatherMenu = new JMenu("Pitch Weather");
		fPitchWeatherMenu.setMnemonic(KeyEvent.VK_W);
		fPitchMenu.add(fPitchWeatherMenu);

		ButtonGroup pitchWeatherGroup = new ButtonGroup();

		fPitchWeatherOnMenuItem = new JRadioButtonMenuItem("Change pitch with weather");
		fPitchWeatherOnMenuItem.addActionListener(this);
		pitchWeatherGroup.add(fPitchWeatherOnMenuItem);
		fPitchWeatherMenu.add(fPitchWeatherOnMenuItem);

		fPitchWeatherOffMenuItem = new JRadioButtonMenuItem("Always show nice weather pitch");
		fPitchWeatherOffMenuItem.addActionListener(this);
		pitchWeatherGroup.add(fPitchWeatherOffMenuItem);
		fPitchWeatherMenu.add(fPitchWeatherOffMenuItem);

		fRangeGridMenu = new JMenu("Range Grid");
		fRangeGridMenu.setMnemonic(KeyEvent.VK_R);
		fUserSettingsMenu.add(fRangeGridMenu);

		ButtonGroup rangeGridGroup = new ButtonGroup();

		fRangeGridAlwaysOnMenuItem = new JRadioButtonMenuItem("Range Grid always on");
		fRangeGridAlwaysOnMenuItem.addActionListener(this);
		rangeGridGroup.add(fRangeGridAlwaysOnMenuItem);
		fRangeGridMenu.add(fRangeGridAlwaysOnMenuItem);

		fRangeGridToggleMenuItem = new JRadioButtonMenuItem("Range Grid toggle");
		fRangeGridToggleMenuItem.addActionListener(this);
		rangeGridGroup.add(fRangeGridToggleMenuItem);
		fRangeGridMenu.add(fRangeGridToggleMenuItem);

		fUserSettingsMenu.addSeparator();

		fRestoreDefaultsMenuItem = new JMenuItem("Restore Defaults");
		fRestoreDefaultsMenuItem.addActionListener(this);
		fRestoreDefaultsMenuItem.setEnabled(false);
		fUserSettingsMenu.add(fRestoreDefaultsMenuItem);

		fMissingPlayersMenu = new JMenu("Missing Players");
		fMissingPlayersMenu.setMnemonic(KeyEvent.VK_M);
		fMissingPlayersMenu.setEnabled(false);
		add(fMissingPlayersMenu);

		fInducementsMenu = new JMenu("Inducements");
		fInducementsMenu.setMnemonic(KeyEvent.VK_I);
		fInducementsMenu.setEnabled(false);
		add(fInducementsMenu);

		fActiveCardsMenu = new JMenu("Active Cards");
		fActiveCardsMenu.setMnemonic(KeyEvent.VK_C);
		fActiveCardsMenu.setEnabled(false);
		add(fActiveCardsMenu);

		fGameOptionsMenu = new JMenu("Game Options");
		fGameOptionsMenu.setMnemonic(KeyEvent.VK_O);
		fGameOptionsMenu.setEnabled(false);
		add(fGameOptionsMenu);

		fHelpMenu = new JMenu("Help");
		fHelpMenu.setMnemonic(KeyEvent.VK_H);
		add(fHelpMenu);

		fAboutMenuItem = new JMenuItem("About", KeyEvent.VK_A);
		fAboutMenuItem.addActionListener(this);
		fHelpMenu.add(fAboutMenuItem);

		fChatCommandsMenuItem = new JMenuItem("Chat Commands", KeyEvent.VK_C);
		fChatCommandsMenuItem.addActionListener(this);
		fHelpMenu.add(fChatCommandsMenuItem);

		fKeyBindingsMenuItem = new JMenuItem("Key Bindings", KeyEvent.VK_K);
		fKeyBindingsMenuItem.addActionListener(this);
		fHelpMenu.add(fKeyBindingsMenuItem);

		refresh();

	}

	public void init() {
		fCurrentInducementTotalHome = -1;
		fCurrentUsedCardsHome = 0;
		fCurrentInducementTotalAway = -1;
		fCurrentUsedCardsAway = 0;
		fCurrentActiveCardsHome = null;
		fCurrentActiveCardsAway = null;
		refresh();
	}

	public void refresh() {

		Game game = getClient().getGame();

		String soundSetting = getClient().getProperty(IClientProperty.SETTING_SOUND_MODE);
		fSoundOnMenuItem.setSelected(true);
		fSoundMuteSpectatorsMenuItem.setSelected(IClientPropertyValue.SETTING_SOUND_MUTE_SPECTATORS.equals(soundSetting));
		fSoundOffMenuItem.setSelected(IClientPropertyValue.SETTING_SOUND_OFF.equals(soundSetting));

		String iconsSetting = getClient().getProperty(IClientProperty.SETTING_ICONS);
		fIconsTeam.setSelected(true);
		fIconsRosterOpponent.setSelected(IClientPropertyValue.SETTING_ICONS_ROSTER_OPPONENT.equals(iconsSetting));
		fIconsRosterBoth.setSelected(IClientPropertyValue.SETTING_ICONS_ROSTER_BOTH.equals(iconsSetting));
		fIconsAbstract.setSelected(IClientPropertyValue.SETTING_ICONS_ABSTRACT.equals(iconsSetting));

		String automoveSetting = getClient().getProperty(IClientProperty.SETTING_AUTOMOVE);
		fAutomoveOnMenuItem.setSelected(true);
		fAutomoveOffMenuItem.setSelected(IClientPropertyValue.SETTING_AUTOMOVE_OFF.equals(automoveSetting));

		String pitchCustomizationSetting = getClient().getProperty(IClientProperty.SETTING_PITCH_CUSTOMIZATION);
		fCustomPitchMenuItem.setSelected(true);
		fDefaultPitchMenuItem.setSelected(IClientPropertyValue.SETTING_PITCH_DEFAULT.equals(pitchCustomizationSetting));
		fBasicPitchMenuItem.setSelected(IClientPropertyValue.SETTING_PITCH_BASIC.equals(pitchCustomizationSetting));

		String pitchMarkingsSetting = getClient().getProperty(IClientProperty.SETTING_PITCH_MARKINGS);
		fPitchMarkingsOffMenuItem.setSelected(true);
		fPitchMarkingsOnMenuItem.setSelected(IClientPropertyValue.SETTING_PITCH_MARKINGS_ON.equals(pitchMarkingsSetting));

		String teamLogosSetting = getClient().getProperty(IClientProperty.SETTING_TEAM_LOGOS);
		fTeamLogoBothMenuItem.setSelected(true);
		fTeamLogoOwnMenuItem.setSelected(IClientPropertyValue.SETTING_TEAM_LOGOS_OWN.equals(teamLogosSetting));
		fTeamLogoNoneMenuItem.setSelected(IClientPropertyValue.SETTING_TEAM_LOGOS_NONE.equals(teamLogosSetting));

		String pitchWeatherSetting = getClient().getProperty(IClientProperty.SETTING_PITCH_WEATHER);
		fPitchWeatherOnMenuItem.setSelected(true);
		fPitchWeatherOffMenuItem.setSelected(IClientPropertyValue.SETTING_PITCH_WEATHER_OFF.equals(pitchWeatherSetting));

		String rangeGridSetting = getClient().getProperty(IClientProperty.SETTING_RANGEGRID);
		fRangeGridToggleMenuItem.setSelected(true);
		fRangeGridAlwaysOnMenuItem.setSelected(IClientPropertyValue.SETTING_RANGEGRID_ALWAYS_ON.equals(rangeGridSetting));

		boolean gameStarted = ((game != null) && (game.getStarted() != null));
		fGameStatisticsMenuItem.setEnabled(gameStarted);
		fGameConcessionMenuItem.setEnabled(gameStarted && game.isHomePlaying()
				&& (ClientMode.PLAYER == getClient().getMode()) && game.isConcessionPossible());

		fGameReplayMenuItem.setEnabled(ClientMode.SPECTATOR == getClient().getMode());

		updateMissingPlayers();
		updateInducements();
		updateActiveCards();
		updateGameOptions();

	}

	public FantasyFootballClient getClient() {
		return fClient;
	}

	public void actionPerformed(ActionEvent e) {
		ClientReplayer replayer = getClient().getReplayer();
		JMenuItem source = (JMenuItem) (e.getSource());
		if (source == fLoadSetupMenuItem) {
			getClient().getClientState().actionKeyPressed(ActionKey.MENU_SETUP_LOAD);
		}
		if (source == fSaveSetupMenuItem) {
			getClient().getClientState().actionKeyPressed(ActionKey.MENU_SETUP_SAVE);
		}
		if (source == fSoundVolumeItem) {
			showDialog(new DialogSoundVolume(getClient()));
		}
		if (source == fSoundOffMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_SOUND_MODE, IClientPropertyValue.SETTING_SOUND_OFF);
			saveUserSettings(false);
		}
		if (source == fSoundMuteSpectatorsMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_SOUND_MODE, IClientPropertyValue.SETTING_SOUND_MUTE_SPECTATORS);
			saveUserSettings(false);
		}
		if (source == fSoundOnMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_SOUND_MODE, IClientPropertyValue.SETTING_SOUND_ON);
			saveUserSettings(false);
		}
		if (source == fIconsTeam) {
			getClient().setProperty(IClientProperty.SETTING_ICONS, IClientPropertyValue.SETTING_ICONS_TEAM);
			saveUserSettings(true);
		}
		if (source == fIconsRosterOpponent) {
			getClient().setProperty(IClientProperty.SETTING_ICONS, IClientPropertyValue.SETTING_ICONS_ROSTER_OPPONENT);
			saveUserSettings(true);
		}
		if (source == fIconsRosterBoth) {
			getClient().setProperty(IClientProperty.SETTING_ICONS, IClientPropertyValue.SETTING_ICONS_ROSTER_BOTH);
			saveUserSettings(true);
		}
		if (source == fIconsAbstract) {
			getClient().setProperty(IClientProperty.SETTING_ICONS, IClientPropertyValue.SETTING_ICONS_ABSTRACT);
			saveUserSettings(true);
		}
		if (source == fAboutMenuItem) {
			showDialog(new DialogAbout(getClient()));
		}
		if (source == fChatCommandsMenuItem) {
			showDialog(new DialogChatCommands(getClient()));
		}
		if (source == fKeyBindingsMenuItem) {
			showDialog(new DialogKeyBindings(getClient()));
		}
		if (source == fGameStatisticsMenuItem) {
			showDialog(new DialogGameStatistics(getClient()));
		}
		if (source == fAutomoveOffMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_AUTOMOVE, IClientPropertyValue.SETTING_AUTOMOVE_OFF);
			saveUserSettings(false);
		}
		if (source == fAutomoveOnMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_AUTOMOVE, IClientPropertyValue.SETTING_AUTOMOVE_ON);
			saveUserSettings(false);
		}
		if (source == fCustomPitchMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_CUSTOMIZATION, IClientPropertyValue.SETTING_PITCH_CUSTOM);
			saveUserSettings(true);
		}
		if (source == fDefaultPitchMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_CUSTOMIZATION, IClientPropertyValue.SETTING_PITCH_DEFAULT);
			saveUserSettings(true);
		}
		if (source == fBasicPitchMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_CUSTOMIZATION, IClientPropertyValue.SETTING_PITCH_BASIC);
			saveUserSettings(true);
		}
		if (source == fPitchMarkingsOffMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_MARKINGS, IClientPropertyValue.SETTING_PITCH_MARKINGS_OFF);
			saveUserSettings(true);
		}
		if (source == fPitchMarkingsOnMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_MARKINGS, IClientPropertyValue.SETTING_PITCH_MARKINGS_ON);
			saveUserSettings(true);
		}
		if (source == fTeamLogoBothMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_TEAM_LOGOS, IClientPropertyValue.SETTING_TEAM_LOGOS_BOTH);
			saveUserSettings(true);
		}
		if (source == fTeamLogoOwnMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_TEAM_LOGOS, IClientPropertyValue.SETTING_TEAM_LOGOS_OWN);
			saveUserSettings(true);
		}
		if (source == fTeamLogoNoneMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_TEAM_LOGOS, IClientPropertyValue.SETTING_TEAM_LOGOS_NONE);
			saveUserSettings(true);
		}
		if (source == fCustomPitchMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_CUSTOMIZATION, IClientPropertyValue.SETTING_PITCH_CUSTOM);
			saveUserSettings(true);
		}
		if (source == fPitchWeatherOnMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_WEATHER, IClientPropertyValue.SETTING_PITCH_WEATHER_ON);
			saveUserSettings(true);
		}
		if (source == fPitchWeatherOffMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_PITCH_WEATHER, IClientPropertyValue.SETTING_PITCH_WEATHER_OFF);
			saveUserSettings(true);
		}
		if (source == fRangeGridAlwaysOnMenuItem) {
			getClient().setProperty(IClientProperty.SETTING_RANGEGRID, IClientPropertyValue.SETTING_RANGEGRID_ALWAYS_ON);
			saveUserSettings(false);
		}
		if (source == fRestoreDefaultsMenuItem) {
			try {
				getClient().loadProperties();
			} catch (IOException pIoE) {
				throw new FantasyFootballException(pIoE);
			}
			refresh();
			saveUserSettings(true);
		}
		if (source == fGameReplayMenuItem) {
			fGameReplayMenuItem.setText(replayer.isReplaying() ? _REPLAY_MODE_ON : _REPLAY_MODE_OFF);
			getClient().getClientState().actionKeyPressed(ActionKey.MENU_REPLAY);
		}
		if (source == fGameConcessionMenuItem) {
			getClient().getCommunication().sendConcedeGame(ConcedeGameStatus.REQUESTED);
		}
	}

	public void changeState(ClientStateId pStateId) {
		Game game = getClient().getGame();
		switch (pStateId) {
		case SETUP:
			boolean setupEnabled = (game.getTurnMode() != TurnMode.QUICK_SNAP);
			fLoadSetupMenuItem.setEnabled(setupEnabled);
			fSaveSetupMenuItem.setEnabled(setupEnabled);
			fRestoreDefaultsMenuItem.setEnabled(true);
			break;
		default:
			fLoadSetupMenuItem.setEnabled(false);
			fSaveSetupMenuItem.setEnabled(false);
			fSoundOnMenuItem.setEnabled(true);
			fSoundMuteSpectatorsMenuItem.setEnabled(true);
			fSoundOffMenuItem.setEnabled(true);
			fRestoreDefaultsMenuItem.setEnabled(true);
			break;
		}
	}

	private void saveUserSettings(boolean pUserinterfaceInit) {
		String[] settingValues = new String[_SAVED_USER_SETTINGS.length];
		for (int i = 0; i < _SAVED_USER_SETTINGS.length; i++) {
			settingValues[i] = getClient().getProperty(_SAVED_USER_SETTINGS[i]);
		}
		getClient().getCommunication().sendUserSettings(_SAVED_USER_SETTINGS, settingValues);
		getClient().getClientState().refreshSettings();
		if (pUserinterfaceInit) {
			getClient().getUserInterface().init(getClient().getGame().getOptions());
		}
	}

	public void dialogClosed(IDialog pDialog) {
		pDialog.hideDialog();
		if (pDialog.getId() == DialogId.SOUND_VOLUME) {
			DialogSoundVolume volumeDialog = (DialogSoundVolume) pDialog;
			getClient().setProperty(IClientProperty.SETTING_SOUND_VOLUME, Integer.toString(volumeDialog.getVolume()));
			saveUserSettings(true);
		}
		fDialogShown = null;
	}

	public void showDialog(IDialog pDialog) {
		if (fDialogShown != null) {
			fDialogShown.hideDialog();
		}
		fDialogShown = pDialog;
		fDialogShown.showDialog(this);
	}

	public void updateGameOptions() {
		fGameOptionsMenu.removeAll();
		IGameOption[] gameOptions = getClient().getGame().getOptions().getOptions();
		Arrays.sort(gameOptions, new Comparator<IGameOption>() {
			public int compare(IGameOption pO1, IGameOption pO2) {
				return pO1.getId().getName().compareTo(pO2.getId().getName());
			}
		});
		int optionsAdded = 0;
		if (getClient().getGame().isTesting()) {
			JMenuItem optionItem = new JMenuItem(
					"* Game is in TEST mode. No results will be uploaded. See help for available test commands.");
			fGameOptionsMenu.add(optionItem);
			optionsAdded++;
		}
		for (IGameOption option : gameOptions) {
			if (option.isChanged() && (option.getId() != GameOptionId.TEST_MODE)
					&& StringTool.isProvided(option.getDisplayMessage())) {
				StringBuilder optionText = new StringBuilder();
				optionText.append("* ").append(option.getDisplayMessage());
				JMenuItem optionItem = new JMenuItem(optionText.toString());
				fGameOptionsMenu.add(optionItem);
				optionsAdded++;
			}
		}
		if (optionsAdded > 0) {
			StringBuilder menuText = new StringBuilder().append(optionsAdded);
			if (optionsAdded > 1) {
				menuText.append(" Game Options");
			} else {
				menuText.append(" Game Option");
			}
			fGameOptionsMenu.setText(menuText.toString());
			fGameOptionsMenu.setEnabled(true);
		} else {
			fGameOptionsMenu.setText("No Game Options");
			fGameOptionsMenu.setEnabled(false);
		}
	}

	public void updateInducements() {

		boolean refreshNecessary = false;
		Game game = getClient().getGame();

		InducementSet inducementSetHome = game.getTurnDataHome().getInducementSet();
		int totalInducementHome = inducementSetHome.totalInducements();
		if ((fCurrentInducementTotalHome < 0) || (fCurrentInducementTotalHome != totalInducementHome)) {
			fCurrentInducementTotalHome = totalInducementHome;
			refreshNecessary = true;
		}
		int usedCardsHome = inducementSetHome.getDeactivatedCards().length + inducementSetHome.getActiveCards().length;
		if (usedCardsHome != fCurrentUsedCardsHome) {
			fCurrentUsedCardsHome = usedCardsHome;
			refreshNecessary = true;
		}

		InducementSet inducementSetAway = game.getTurnDataAway().getInducementSet();
		int totalInducementAway = inducementSetAway.totalInducements();
		if ((fCurrentInducementTotalAway < 0) || (fCurrentInducementTotalAway != totalInducementAway)) {
			fCurrentInducementTotalAway = totalInducementAway;
			refreshNecessary = true;
		}
		int usedCardsAway = inducementSetAway.getDeactivatedCards().length + inducementSetAway.getActiveCards().length;
		if (usedCardsAway != fCurrentUsedCardsAway) {
			fCurrentUsedCardsAway = usedCardsAway;
			refreshNecessary = true;
		}

		if (refreshNecessary) {

			fInducementsHomeMenu = null;
			fInducementsAwayMenu = null;
			fInducementsMenu.removeAll();

			if (fCurrentInducementTotalHome > 0) {
				StringBuilder menuText = new StringBuilder().append(totalInducementHome).append(" Home Team");
				fInducementsHomeMenu = new JMenu(menuText.toString());
				fInducementsHomeMenu.setForeground(Color.RED);
				fInducementsHomeMenu.setMnemonic(KeyEvent.VK_H);
				fInducementsMenu.add(fInducementsHomeMenu);
				addInducements(fInducementsHomeMenu, inducementSetHome);
			}

			if (fCurrentInducementTotalAway > 0) {
				StringBuilder menuText = new StringBuilder().append(totalInducementAway).append(" Away Team");
				fInducementsAwayMenu = new JMenu(menuText.toString());
				fInducementsAwayMenu.setForeground(Color.BLUE);
				fInducementsAwayMenu.setMnemonic(KeyEvent.VK_A);
				fInducementsMenu.add(fInducementsAwayMenu);
				addInducements(fInducementsAwayMenu, inducementSetAway);
			}

			if ((fCurrentInducementTotalHome + fCurrentInducementTotalAway) > 0) {
				StringBuilder menuText = new StringBuilder().append(fCurrentInducementTotalHome + fCurrentInducementTotalAway);
				if ((fCurrentInducementTotalHome + fCurrentInducementTotalAway) > 1) {
					menuText.append(" Inducements");
				} else {
					menuText.append(" Inducement");
				}
				fInducementsMenu.setText(menuText.toString());
				fInducementsMenu.setEnabled(true);
			} else {
				fInducementsMenu.setText("No Inducements");
				fInducementsMenu.setEnabled(false);
			}

		}

	}

	public void updateActiveCards() {

		boolean refreshNecessary = false;
		Game game = getClient().getGame();

		Card[] cardsHome = game.getTurnDataHome().getInducementSet().getActiveCards();
		if ((fCurrentActiveCardsHome == null) || (cardsHome.length != fCurrentActiveCardsHome.length)) {
			fCurrentActiveCardsHome = cardsHome;
			refreshNecessary = true;
		}

		Card[] cardsAway = game.getTurnDataAway().getInducementSet().getActiveCards();
		if ((fCurrentActiveCardsAway == null) || (cardsAway.length != fCurrentActiveCardsAway.length)) {
			fCurrentActiveCardsAway = cardsAway;
			refreshNecessary = true;
		}

		if (refreshNecessary) {

			fActiveCardsMenu.removeAll();

			if (ArrayTool.isProvided(fCurrentActiveCardsHome)) {
				StringBuilder menuText = new StringBuilder().append(fCurrentActiveCardsHome.length).append(" Home Team");
				fActiveCardsHomeMenu = new JMenu(menuText.toString());
				fActiveCardsHomeMenu.setForeground(Color.RED);
				fActiveCardsHomeMenu.setMnemonic(KeyEvent.VK_H);
				fActiveCardsMenu.add(fActiveCardsHomeMenu);
				addActiveCards(fActiveCardsHomeMenu, fCurrentActiveCardsHome);
			}

			if (ArrayTool.isProvided(fCurrentActiveCardsAway)) {
				StringBuilder menuText = new StringBuilder().append(fCurrentActiveCardsAway.length).append(" Away Team");
				fActiveCardsAwayMenu = new JMenu(menuText.toString());
				fActiveCardsAwayMenu.setForeground(Color.BLUE);
				fActiveCardsAwayMenu.setMnemonic(KeyEvent.VK_A);
				fActiveCardsMenu.add(fActiveCardsAwayMenu);
				addActiveCards(fActiveCardsAwayMenu, fCurrentActiveCardsAway);
			}

			int currentActiveCardsHomeLength = ArrayTool.isProvided(fCurrentActiveCardsHome) ? fCurrentActiveCardsHome.length
					: 0;
			int currentActiveCardsAwayLength = ArrayTool.isProvided(fCurrentActiveCardsAway) ? fCurrentActiveCardsAway.length
					: 0;

			if ((currentActiveCardsHomeLength + currentActiveCardsAwayLength) > 0) {
				StringBuilder menuText = new StringBuilder()
						.append(currentActiveCardsHomeLength + currentActiveCardsAwayLength);
				if ((currentActiveCardsHomeLength + currentActiveCardsAwayLength) > 1) {
					menuText.append(" Active Cards");
				} else {
					menuText.append(" Active Card");
				}
				fActiveCardsMenu.setText(menuText.toString());
				fActiveCardsMenu.setEnabled(true);
			} else {
				fActiveCardsMenu.setText("No Active Cards");
				fActiveCardsMenu.setEnabled(false);
			}

		}

	}

	private void addActiveCards(JMenu pCardsMenu, Card[] pCards) {
		Game game = getClient().getGame();
		Arrays.sort(pCards, Card.createComparator());
		Icon cardIcon = new ImageIcon(
				getClient().getUserInterface().getIconCache().getIconByProperty(IIconProperty.SIDEBAR_OVERLAY_PLAYER_CARD));
		for (Card card : pCards) {
			Player<?> player = null;
			if (card.getTarget().isPlayedOnPlayer()) {
				player = game.getFieldModel().findPlayer(card);
			}
			StringBuilder cardText = new StringBuilder();
			cardText.append("<html>");
			cardText.append("<b>").append(card.getName()).append("</b>");
			if (player != null) {
				cardText.append("<br>").append("Played on ").append(player.getName());
			}
			cardText.append("<br>").append(card.getHtmlDescription());
			cardText.append("</html>");
			if (player != null) {
				addPlayerMenuItem(pCardsMenu, player, cardText.toString());
			} else {
				JMenuItem cardMenuItem = new JMenuItem(cardText.toString(), cardIcon);
				pCardsMenu.add(cardMenuItem);
			}
		}
	}

	private void addInducements(JMenu pInducementMenu, InducementSet pInducementSet) {
		Inducement[] inducements = pInducementSet.getInducements();
		Arrays.sort(inducements, Comparator.comparing(pInducement -> pInducement.getType().getName()));
		for (Inducement inducement : inducements) {
			if (!Usage.EXCLUDE_FROM_RESULT.contains(inducement.getType().getUsage())) {
				if (inducement.getValue() > 0) {
					StringBuilder inducementText = new StringBuilder();
					inducementText.append(inducement.getValue()).append(" ");
					if (inducement.getValue() > 1) {
						inducementText.append(inducement.getType().getPlural());
					} else {
						inducementText.append(inducement.getType().getSingular());
					}
					JMenuItem inducementItem = new JMenuItem(inducementText.toString());
					pInducementMenu.add(inducementItem);
				}
			}
		}

		Game game = getClient().getGame();
		Team team = pInducementSet.getTurnData().isHomeData() ? game.getTeamHome() : game.getTeamAway();
		List<Player<?>> starPlayers = new ArrayList<>();
		for (Player<?> player : team.getPlayers()) {
			if (player.getPlayerType() == PlayerType.STAR) {
				starPlayers.add(player);
			}
		}
		if (starPlayers.size() > 0) {
			StringBuilder starPlayerMenuText = new StringBuilder();
			starPlayerMenuText.append(starPlayers.size());
			if (starPlayers.size() == 1) {
				starPlayerMenuText.append(" Star Player");
			} else {
				starPlayerMenuText.append(" Star Players");
			}
			JMenu starPlayerMenu = new JMenu(starPlayerMenuText.toString());
			pInducementMenu.add(starPlayerMenu);
			for (Player<?> player : starPlayers) {
				addPlayerMenuItem(starPlayerMenu, player, player.getName());
			}
		}

		List<Player<?>> mercenaries = new ArrayList<>();
		for (Player<?> player : team.getPlayers()) {
			if (player.getPlayerType() == PlayerType.MERCENARY) {
				mercenaries.add(player);
			}
		}
		if (mercenaries.size() > 0) {
			StringBuilder mercenaryMenuText = new StringBuilder();
			mercenaryMenuText.append(mercenaries.size());
			if (mercenaries.size() == 1) {
				mercenaryMenuText.append(" Mercenary");
			} else {
				mercenaryMenuText.append(" Mercenaries");
			}
			JMenu mercenaryMenu = new JMenu(mercenaryMenuText.toString());
			pInducementMenu.add(mercenaryMenu);
			for (Player<?> player : mercenaries) {
				addPlayerMenuItem(mercenaryMenu, player, player.getName());
			}
		}

		UserInterface userInterface = getClient().getUserInterface();
		Map<CardType, List<Card>> cardMap = buildCardMap(pInducementSet);
		for (CardType type : cardMap.keySet()) {
			List<Card> cardList = cardMap.get(type);
			StringBuilder cardTypeText = new StringBuilder();
			cardTypeText.append(cardList.size()).append(" ");
			if (cardList.size() > 1) {
				cardTypeText.append(type.getInducementNameMultiple());
			} else {
				cardTypeText.append(type.getInducementNameSingle());
			}
			int available = 0;
			for (Card card : cardList) {
				if (pInducementSet.isAvailable(card)) {
					available++;
				}
			}
			cardTypeText.append(" (");
			cardTypeText.append((available > 0) ? available : "None");
			cardTypeText.append(" available)");
			if (pInducementSet.getTurnData().isHomeData() && (getClient().getMode() == ClientMode.PLAYER)) {
				JMenu cardMenu = new JMenu(cardTypeText.toString());
				pInducementMenu.add(cardMenu);
				ImageIcon cardIcon = new ImageIcon(
						userInterface.getIconCache().getIconByProperty(IIconProperty.SIDEBAR_OVERLAY_PLAYER_CARD));
				for (Card card : cardList) {
					if (pInducementSet.isAvailable(card)) {
						StringBuilder cardText = new StringBuilder();
						cardText.append("<html>");
						cardText.append("<b>").append(card.getName()).append("</b>");
						cardText.append("<br>").append(card.getHtmlDescriptionWithPhases());
						cardText.append("</html>");
						JMenuItem cardItem = new JMenuItem(cardText.toString(), cardIcon);
						cardMenu.add(cardItem);
					}
				}
			} else {
				JMenuItem cardItem = new JMenuItem(cardTypeText.toString());
				pInducementMenu.add(cardItem);
			}
		}

	}

	public void updateMissingPlayers() {
		Game game = getClient().getGame();
		fMissingPlayersMenu.removeAll();
		int nrOfEntries = 0;
		for (int i = 0; i < BoxComponent.MAX_BOX_ELEMENTS; i++) {
			Player<?> player = game.getFieldModel().getPlayer(new FieldCoordinate(FieldCoordinate.MNG_HOME_X, i));
			if (player != null) {
				addMissingPlayerMenuItem(player);
				nrOfEntries++;
			} else {
				break;
			}
		}
		for (int i = 0; i < BoxComponent.MAX_BOX_ELEMENTS; i++) {
			Player<?> player = game.getFieldModel().getPlayer(new FieldCoordinate(FieldCoordinate.MNG_AWAY_X, i));
			if (player != null) {
				addMissingPlayerMenuItem(player);
				nrOfEntries++;
			} else {
				break;
			}
		}
		StringBuilder menuText = new StringBuilder();
		if (nrOfEntries > 0) {
			menuText.append(nrOfEntries);
			if (nrOfEntries > 1) {
				menuText.append(" Missing Players");
			} else {
				menuText.append(" Missing Player");
			}
			fMissingPlayersMenu.setEnabled(true);
		} else {
			menuText.append("No Missing Players");
			fMissingPlayersMenu.setEnabled(false);
		}
		fMissingPlayersMenu.setText(menuText.toString());
	}

	private void addMissingPlayerMenuItem(Player<?> pPlayer) {
		if (pPlayer == null) {
			return;
		}
		StringBuilder playerText = new StringBuilder();
		playerText.append("<html>").append(pPlayer.getName());
		if (pPlayer.getRecoveringInjury() != null) {
			playerText.append("<br>").append(pPlayer.getRecoveringInjury().getRecovery());
		} else {
			Game game = getClient().getGame();
			PlayerResult playerResult = game.getGameResult().getPlayerResult(pPlayer);
			if (playerResult.getSendToBoxReason() != null) {
				playerText.append("<br>").append(playerResult.getSendToBoxReason().getReason());
			}
		}
		playerText.append("</html>");
		addPlayerMenuItem(fMissingPlayersMenu, pPlayer, playerText.toString());
	}

	private void addPlayerMenuItem(JMenu pPlayersMenu, Player<?> pPlayer, String pText) {
		if ((pPlayer == null) || !StringTool.isProvided(pText)) {
			return;
		}
		UserInterface userInterface = getClient().getUserInterface();
		PlayerIconFactory playerIconFactory = userInterface.getPlayerIconFactory();
		Icon playerIcon = new ImageIcon(playerIconFactory.getIcon(getClient(), pPlayer));
		JMenuItem playersMenuItem = new JMenuItem(pText, playerIcon);
		playersMenuItem.addMouseListener(new MenuPlayerMouseListener(pPlayer));
		pPlayersMenu.add(playersMenuItem);
	}

	private Map<CardType, List<Card>> buildCardMap(InducementSet pInducementSet) {
		Card[] allCards = pInducementSet.getAllCards();

		return Arrays.stream(allCards).collect(Collectors.groupingBy(Card::getType));
	}

}