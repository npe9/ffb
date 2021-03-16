package com.balancedbytes.games.ffb.client.dialog;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import com.balancedbytes.games.ffb.client.FantasyFootballClient;
import com.balancedbytes.games.ffb.client.PlayerIconFactory;
import com.balancedbytes.games.ffb.model.Game;
import com.balancedbytes.games.ffb.model.Player;
import com.balancedbytes.games.ffb.util.ArrayTool;

public class PlayerCheckList extends JList<PlayerCheckListItem> {

	// Handles rendering cells in the list using a check box

	private class PlayerCheckListRenderer extends JPanel implements ListCellRenderer<PlayerCheckListItem> {

		private JCheckBox fCheckBox;
		private JLabel fLabel;

		public PlayerCheckListRenderer() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			fCheckBox = new JCheckBox();
			add(fCheckBox);
			fLabel = new JLabel();
			add(fLabel);
		}

		public Component getListCellRendererComponent(JList<? extends PlayerCheckListItem> pList,
				PlayerCheckListItem pValue, int pIndex, boolean pIsSelected, boolean pCellHasFocus) {
			setEnabled(pList.isEnabled());
			setFont(pList.getFont());
			setBackground(pList.getBackground());
			setForeground(pList.getForeground());
			fCheckBox.setBackground(pList.getBackground());
			PlayerCheckListItem listItem = (PlayerCheckListItem) pValue;
			fCheckBox.setSelected(listItem.isSelected());
			fLabel.setIcon(listItem.getIcon());
			fLabel.setText(listItem.getText());
			return this;
		}
	}

	private class PlayerCheckListMouseAdapter extends MouseAdapter {

		private int fMinSelects;
		private int fMaxSelects;
		private JButton fSelectButton;

		public PlayerCheckListMouseAdapter(int minSelects, int maxSelects, JButton selectButton) {
			fMinSelects = minSelects;
			fMaxSelects = maxSelects;
			fSelectButton = selectButton;
		}

		public void mouseReleased(MouseEvent event) {
			JList list = (JList) event.getSource();
			int index = list.locationToIndex(event.getPoint());
			PlayerCheckListItem selectedItem = (PlayerCheckListItem) list.getModel().getElementAt(index);
			if (!selectedItem.isSelected()) {
				if (fMaxSelects > 1) {
					int nrOfSelectedItems = findNrOfSelectedItems();
					if (nrOfSelectedItems < fMaxSelects) {
						selectedItem.setSelected(true);
					}
				} else {
					for (int i = 0; i < list.getModel().getSize(); i++) {
						PlayerCheckListItem item = (PlayerCheckListItem) list.getModel().getElementAt(i);
						if (item.isSelected()) {
							item.setSelected(false);
							list.repaint(list.getCellBounds(i, i));
						}
					}
					selectedItem.setSelected(true);
				}
			} else {
				selectedItem.setSelected(false);
			}
			int nrOfSelectedItems = findNrOfSelectedItems();
			if (fMinSelects > 0) {
				fSelectButton.setEnabled(nrOfSelectedItems >= fMinSelects);
			} else {
				fSelectButton.setEnabled(nrOfSelectedItems > 0);
			}
			list.repaint(list.getCellBounds(index, index));
		}

	}

	public PlayerCheckList(FantasyFootballClient client, String[] playerIds, String[] descriptions, int minSelects,
			int maxSelects, boolean preSelected, JButton selectButton) {

		if (!ArrayTool.isProvided(playerIds)) {
			throw new IllegalArgumentException("Argument players must not be empty or null.");
		}

		Game game = client.getGame();
		List<PlayerCheckListItem> checkListItems = new ArrayList<>();
		PlayerIconFactory playerIconFactory = client.getUserInterface().getPlayerIconFactory();
		for (int i = 0; i < playerIds.length; i++) {
			Player<?> player = game.getPlayerById(playerIds[i]);
			if (player != null) {
				boolean homePlayer = game.getTeamHome().hasPlayer(player);
				BufferedImage playerIcon = playerIconFactory.getBasicIcon(client, player, homePlayer, false, false, false);
				StringBuilder text = new StringBuilder();
				text.append(player.getName());
				if (ArrayTool.isProvided(descriptions)) {
					int descriptionIndex = i;
					if (descriptionIndex >= descriptions.length) {
						descriptionIndex = descriptions.length - 1;
					}
					text.append(" ").append(descriptions[descriptionIndex]);
				}
				PlayerCheckListItem checkListItem = new PlayerCheckListItem(player, new ImageIcon(playerIcon), text.toString());
				checkListItem.setSelected((playerIds.length == 1) || preSelected);
				checkListItems.add(checkListItem);
			}
		}
		setListData(checkListItems.toArray(new PlayerCheckListItem[checkListItems.size()]));

		// Use a CheckListRenderer (see below) to renderer list cells
		setCellRenderer(new PlayerCheckListRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Add a mouse listener to handle changing selection
		addMouseListener(new PlayerCheckListMouseAdapter(minSelects, maxSelects, selectButton));

	}

	public Player<?> getPlayerAtIndex(int pIndex) {
		PlayerCheckListItem checkListItem = (PlayerCheckListItem) getModel().getElementAt(pIndex);
		if (checkListItem != null) {
			return checkListItem.getPlayer();
		} else {
			return null;
		}
	}

	public Player<?>[] getSelectedPlayers() {
		List<Player<?>> selectedPlayers = new ArrayList<>();
		for (int i = 0; i < getModel().getSize(); i++) {
			PlayerCheckListItem item = (PlayerCheckListItem) getModel().getElementAt(i);
			if (item.isSelected()) {
				selectedPlayers.add(item.getPlayer());
			}
		}
		return selectedPlayers.toArray(new Player[selectedPlayers.size()]);
	}

	private int findNrOfSelectedItems() {
		int nrOfSelectedItems = 0;
		for (int i = 0; i < getModel().getSize(); i++) {
			PlayerCheckListItem item = (PlayerCheckListItem) getModel().getElementAt(i);
			if (item.isSelected()) {
				nrOfSelectedItems++;
			}
		}
		return nrOfSelectedItems;
	}

}
