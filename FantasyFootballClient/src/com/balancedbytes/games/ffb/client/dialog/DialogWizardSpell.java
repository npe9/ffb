package com.balancedbytes.games.ffb.client.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.balancedbytes.games.ffb.SpecialEffect;
import com.balancedbytes.games.ffb.client.FantasyFootballClient;
import com.balancedbytes.games.ffb.dialog.DialogId;

/**
 * 
 * @author Kalimar
 */
@SuppressWarnings("serial")
public class DialogWizardSpell extends Dialog implements ActionListener, KeyListener {
  
  private JButton fButtonLightning;
  private JButton buttonZap;
  private JButton fButtonFireball;
  private JButton fButtonCancel;
  private SpecialEffect fWizardSpell;
  
  public DialogWizardSpell(FantasyFootballClient pClient) {
    
    super(pClient, "Wizard Spell", false);
    
    JPanel panelText = new JPanel();
    panelText.setLayout(new BoxLayout(panelText, BoxLayout.X_AXIS));
    panelText.add(new JLabel("Which spell should your wizard cast?"));
    
    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));

    panelButtons.add(Box.createHorizontalGlue());
    //TODO only show buttons if spell is available
    fButtonLightning = new JButton("Lightning");
    fButtonLightning.addActionListener(this);
    fButtonLightning.addKeyListener(this);
    panelButtons.add(fButtonLightning);
    
    fButtonFireball = new JButton("Fireball");
    fButtonFireball.addActionListener(this);
    fButtonFireball.addKeyListener(this);
    panelButtons.add(fButtonFireball);

    buttonZap = new JButton("Zap");
    buttonZap.addActionListener(this);
    buttonZap.addKeyListener(this);
    panelButtons.add(buttonZap);

    fButtonCancel = new JButton("Cancel");
    fButtonCancel.addActionListener(this);
    fButtonCancel.addKeyListener(this);
    panelButtons.add(fButtonCancel);

    panelButtons.add(Box.createHorizontalGlue());

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    getContentPane().add(Box.createVerticalStrut(5));
    getContentPane().add(panelText);
    getContentPane().add(Box.createVerticalStrut(5));
    getContentPane().add(panelButtons);
    
    pack();
    setLocationToCenter();
    
  }

  public DialogId getId() {
    return DialogId.WIZARD_SPELL;
  }
  
  public void actionPerformed(ActionEvent pActionEvent) {
    if (pActionEvent.getSource() == fButtonLightning) {
    	fWizardSpell = SpecialEffect.LIGHTNING;
    }
    if (pActionEvent.getSource() == buttonZap) {
      fWizardSpell = SpecialEffect.ZAP;
    }
    if (pActionEvent.getSource() == fButtonFireball) {
    	fWizardSpell = SpecialEffect.FIREBALL;
    }
    if (pActionEvent.getSource() == fButtonCancel) {
    	fWizardSpell = null;
    }
    if (getCloseListener() != null) {
      getCloseListener().dialogClosed(this);
    }
  }
  
  public void keyPressed(KeyEvent pKeyEvent) {
  }
  
  public void keyReleased(KeyEvent pKeyEvent) {
    boolean keyHandled = true;
    switch (pKeyEvent.getKeyCode()) {
      case KeyEvent.VK_L:
        fWizardSpell = SpecialEffect.LIGHTNING;
      case KeyEvent.VK_Z:
        fWizardSpell = SpecialEffect.ZAP;
        break;
      case KeyEvent.VK_F:
        fWizardSpell = SpecialEffect.FIREBALL;
        break;
      case KeyEvent.VK_C:
      	fWizardSpell = null;
      	break;
      default:
        keyHandled = false;
        break;
    }
    if (keyHandled) {
      if (getCloseListener() != null) {
        getCloseListener().dialogClosed(this);
      }
    }
  }
  
  public void keyTyped(KeyEvent pKeyEvent) {
  }
  
  public SpecialEffect getWizardSpell() {
		return fWizardSpell;
	}
  
}
