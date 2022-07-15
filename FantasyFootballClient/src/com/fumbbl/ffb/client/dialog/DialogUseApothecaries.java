package com.fumbbl.ffb.client.dialog;

import com.fumbbl.ffb.ApothecaryType;
import com.fumbbl.ffb.IIconProperty;
import com.fumbbl.ffb.bb2020.InjuryDescription;
import com.fumbbl.ffb.client.FantasyFootballClient;
import com.fumbbl.ffb.client.dialog.AbstractDialogMultiBlock.PressedKeyListener;
import com.fumbbl.ffb.dialog.DialogId;
import com.fumbbl.ffb.dialog.DialogUseApothecariesParameter;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.Player;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DialogUseApothecaries extends Dialog {

	public static final Color HIGHLIGHT = Color.lightGray;
	private final DialogUseApothecariesParameter dialogParameter;
	private String selectedPlayer;

	public DialogUseApothecaries(FantasyFootballClient pClient, DialogUseApothecariesParameter parameter) {

		super(pClient, "Use Apothecaries", false);

		dialogParameter = parameter;

		JButton fButtonNoReRoll = new JButton("No Apothecaries");
		fButtonNoReRoll.addActionListener(e -> close());
		this.addKeyListener(new PressedKeyListener('N') {
			@Override
			protected void handleKey() {
				close();
			}
		});
		fButtonNoReRoll.setMnemonic((int) 'N');

		Game game = getClient().getGame();

		JPanel mainMessagePanel = new JPanel();
		mainMessagePanel.setLayout(new BoxLayout(mainMessagePanel, BoxLayout.Y_AXIS));
		mainMessagePanel.setAlignmentX(CENTER_ALIGNMENT);
		mainMessagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

		JLabel mainLabel = new JLabel("<html>Do you want to use an Apothecary ?</html>");
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainMessagePanel.add(mainLabel);
		mainMessagePanel.add(Box.createVerticalStrut(5));

		JPanel detailPanel = new JPanel();
		detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
		detailPanel.setAlignmentX(CENTER_ALIGNMENT);
		for (int index = 0; index < parameter.getInjuryDescriptions().size(); index++) {

			InjuryDescription injuryDescription = parameter.getInjuryDescriptions().get(index);

			JPanel targetPanel = new JPanel();
			targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.Y_AXIS));
			targetPanel.setAlignmentX(CENTER_ALIGNMENT);


			String playerId = injuryDescription.getPlayerId();
			Player<?> player = game.getPlayerById(playerId);
			JPanel textPanel = new JPanel();
			textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
			textPanel.setAlignmentX(CENTER_ALIGNMENT);
			textPanel.setBackground(HIGHLIGHT);
			String injury = injuryDescription.getSeriousInjury() != null ? injuryDescription.getSeriousInjury().getDescription() : injuryDescription.getPlayerState().getDescription();
			Arrays.stream(new String[]{"<html>" + player.getName() + " suffered " + injury + "</html>",
					"<html>You can use a " + injuryDescription.getApothecaryType().getName() + " on " + player.getPlayerGender().getDative() + ".</html>"})
				.map(JLabel::new).forEach(label -> {
					label.setHorizontalAlignment(SwingConstants.CENTER);
					textPanel.add(label);
				});
			targetPanel.add(textPanel);


			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.setBackground(HIGHLIGHT);

			buttonPanel.add(createButton(playerId, injuryDescription.getApothecaryType(), index == 0 ? 'A' : 'p'));
			buttonPanel.add(Box.createHorizontalGlue());

			targetPanel.add(Box.createVerticalStrut(3));
			targetPanel.add(buttonPanel);
			targetPanel.add(Box.createVerticalStrut(3));
			targetPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			targetPanel.setBackground(HIGHLIGHT);
			detailPanel.add(targetPanel);
			detailPanel.add(Box.createVerticalStrut(5));

		}

		JPanel bottomPanel = new JPanel();
		bottomPanel.setAlignmentX(CENTER_ALIGNMENT);
		bottomPanel.add(fButtonNoReRoll);
		detailPanel.add(bottomPanel);

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.setAlignmentX(CENTER_ALIGNMENT);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		BufferedImage icon = getClient().getUserInterface().getIconCache().getIconByProperty(IIconProperty.RESOURCE_APOTHECARY);
		JLabel iconLabel = new JLabel(new ImageIcon(icon));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(iconLabel);
		infoPanel.add(Box.createHorizontalStrut(5));
		infoPanel.add(detailPanel);
		infoPanel.add(Box.createHorizontalGlue());

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(mainMessagePanel);
		getContentPane().add(infoPanel);

		pack();
		setLocationToCenter();

	}

	private JButton createButton(String target, ApothecaryType apothecaryType, char mnemonic) {
		JButton button = new JButton("Use Apothecary (" + apothecaryType.getName() + ")");
		button.addActionListener(e -> handleUserInteraction(target));
		this.addKeyListener(new PressedKeyListener(mnemonic) {
			@Override
			protected void handleKey() {
				handleUserInteraction(target);
			}
		});
		button.setMnemonic((int) mnemonic);
		return button;
	}

	private void handleUserInteraction(String target) {
		selectedPlayer = target;
		close();
	}

	private void close() {
		if (getCloseListener() != null) {
			getCloseListener().dialogClosed(DialogUseApothecaries.this);
		}
	}

	public DialogId getId() {
		return DialogId.USE_APOTHECARIES;
	}

	public String getSelectedPlayer() {
		return selectedPlayer;
	}

	public DialogUseApothecariesParameter getDialogParameter() {
		return dialogParameter;
	}

}