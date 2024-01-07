package com.fumbbl.ffb.client.dialog;

import com.fumbbl.ffb.IIconProperty;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.client.StyleProvider;
import com.fumbbl.ffb.client.ui.swing.JLabel;
import com.fumbbl.ffb.dialog.DialogId;
import com.fumbbl.ffb.dialog.DialogPenaltyShootoutParameter;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kalimar
 */
public class DialogPenaltyShootout extends Dialog {

	private static final Color HIGHLIGHT = Color.lightGray;

	private final JPanel rootPanel;

	public DialogPenaltyShootout(FantasyFootballClient pClient, DialogPenaltyShootoutParameter parameter) {
		super(pClient, "Penalty Shootout", true);

		rootPanel = new JPanel();

		Border innerBorder = BorderFactory.createEmptyBorder(0, 20, 0, 20);
		Border middleBorder = BorderFactory.createLineBorder(Color.BLACK, 1, true);
		Border outerBorder = BorderFactory.createLineBorder(Color.WHITE, 5);
		rootPanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, BorderFactory.createCompoundBorder(middleBorder, innerBorder)));


		getContentPane().add(rootPanel);

		int limit = Math.min(Math.min(parameter.getAwayRolls().size(), parameter.getHomeRolls().size()), parameter.getHomeWon().size());

		rootPanel.setLayout(new GridLayout(limit + 2, 3, 0, 5));


		List<JLabel> labels = new ArrayList<>();
		labels.add(headerLabel("Home", true));
		labels.add(new JLabel(dimensionProvider()));
		labels.add(headerLabel("Away", false));

		for (int i = 0; i < limit; i++) {
			labels.addAll(rollPanel(parameter.getHomeRolls().get(i), parameter.getAwayRolls().get(i), parameter.getHomeWon().get(i)));
		}

		labels.addAll(scorePanel(parameter.getHomeScore(), parameter.getAwayScore(), parameter.homeTeamWins()));

		labels.forEach(this::addDecorated);

		pack();
		setLocationToCenter();
	}

	public DialogId getId() {
		return DialogId.PENALTY_SHOOTOUT;
	}

	public void internalFrameClosing(InternalFrameEvent pE) {
		if (getCloseListener() != null) {
			getCloseListener().dialogClosed(this);
		}
	}

	private void addDecorated(JLabel label) {
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		rootPanel.add(label);
	}

	private JLabel headerLabel(String text, boolean home) {
		StyleProvider styleProvider = getClient().getUserInterface().getStyleProvider();
		JLabel label = new JLabel(dimensionProvider(), text);
		label.setForeground(home ? styleProvider.getHome() : styleProvider.getAway());
		return boldLabel(label);
	}


	private List<JLabel> scorePanel(int homeRoll, int awayRoll, boolean homeWin) {
		List<JLabel> labels = new ArrayList<>();
		labels.add(new JLabel(dimensionProvider(), String.valueOf(homeRoll)));
		labels.add(new JLabel(dimensionProvider(), "Score"));
		labels.add(new JLabel(dimensionProvider(), String.valueOf(awayRoll)));

		JLabel winnerLabel = labels.get(homeWin ? 0 : 2);
		boldLabel(winnerLabel);
		JLabel loserLabel = labels.get(homeWin ? 2 : 0);
		loserLabel.setForeground(Color.LIGHT_GRAY);
		return labels;
	}

	private JLabel boldLabel(JLabel label) {
		Font font = label.getFont();
		label.setFont(new Font(font.getFamily(), Font.BOLD, font.getSize()));
		return label;
	}

	private List<JLabel> rollPanel(int home, int away, boolean homeWin) {
		List<JLabel> labels = new ArrayList<>();
		labels.add(new JLabel(dimensionProvider(), icon(home)));
		labels.add(new JLabel(dimensionProvider()));
		labels.add(new JLabel(dimensionProvider(), icon(away)));
		JLabel winnerLabel = labels.get(homeWin ? 0 : 2);
		winnerLabel.setBackground(HIGHLIGHT);
		winnerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		return labels;
	}


	private Icon icon(int roll) {
		return new ImageIcon(getClient().getUserInterface().getIconCache().getIconByProperty(iconProperty(roll)));
	}

	private String iconProperty(int roll) {
		switch (roll) {
			case 1:
				return IIconProperty.PENALTY_DIE_1;
			case 2:
				return IIconProperty.PENALTY_DIE_2;
			case 3:
				return IIconProperty.PENALTY_DIE_3;
			case 4:
				return IIconProperty.PENALTY_DIE_4;
			case 5:
				return IIconProperty.PENALTY_DIE_5;
			default:
				return IIconProperty.PENALTY_DIE_6;
		}
	}

}
