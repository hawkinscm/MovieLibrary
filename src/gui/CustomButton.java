package gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Abstract class for a standard button customized for better functionality and easier use.
 */
public abstract class CustomButton extends JButton {
	protected static final long serialVersionUID = 1L;

	/**
	 * Creates a new CustomButton button.
	 */
	public CustomButton() {
		super();
		addInputHandling();
	}
	
	/**
	 * Creates a new CustomButton button with the given action.
	 * @param a the Action used to specify the new button.
	 */
	public CustomButton(Action a) {
		super(a);
		addInputHandling();
	}
	
	/**
	 * Creates a new CustomButton button with the given icon.
	 * @param icon Icon that will be displayed on the button
	 */
	public CustomButton(Icon icon) {
		super(icon);
		addInputHandling();
	}
	
	/**
	 * Creates a new CustomButton button with the given text.
	 * @param text text that will be displayed on the button
	 */
	public CustomButton(String text) {
		super(text);
		addInputHandling();
	}
	
	/**
	 * Creates a new CustomButton button with the given text and icon.
	 * @param text text that will be displayed on the button
	 * @param icon Icon that will be displayed on the button
	 */
	public CustomButton(String text, Icon icon) {
		super(text, icon);
		addInputHandling();
	}
	
	/**
	 * Adds ease of use to the button.
	 */
	private void addInputHandling() {
		// Button can only be "clicked" if mouse is inside the button when mouse button is both pressed and released.
		// Also button does not require mouse to not move when being clicked; only has to be inside button for duration.
		addMouseListener(new MouseListener() {
			private boolean isInside = false;
			
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1 || !isInside || !isEnabled())
					return;
			
				buttonPressed();
			}
			public void mouseEntered(MouseEvent e) {isInside = true;}
			public void mouseExited(MouseEvent e) {isInside = false;}
		});
		
		// Lets the Enter key "click" the button when the button has the focus.
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && isEnabled())				
					buttonPressed();
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyTyped(KeyEvent arg0) {}
		});
	}
	
	/**
	 * Abstract method that will hold/define the functionality of the clicked button.
	 */
	public abstract void buttonPressed();
}
