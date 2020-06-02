package RefractionSim;
import java.awt.event.*;
import javax.swing.SwingUtilities;

/**
 * Class for all-purpose viewport listeners handling both mouse and keyboard events
 * @author William Platt
 *
 */
public class ViewportListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	private boolean dragging;
	private int prevX;
	private int prevY;
	
	/**
	 * Called when any of the mouse buttons are depressed in the viewport to give the viewport focus and prepare for dragging
	 */
	public void mousePressed(MouseEvent event) {
		dragging = true;
		// Store the co-ordinates of the cursor so that the change in position can be determined if the user drags
		prevX = event.getX();
		prevY = event.getY();
		Viewport viewport = (Viewport)(event.getSource());
		viewport.requestFocusInWindow();
	}
	
	/**
	 * Called when the user drags after depressing any of the mouse buttons in the viewport to orbit the camera around the world's origin or zoom if the right mouse button is used
	 */
	public void mouseDragged(MouseEvent event) {
		if (dragging) {
			Viewport viewport = (Viewport)(event.getSource());
			int newX = event.getX();
			int newY = event.getY();
			if (SwingUtilities.isRightMouseButton(event)) {
				viewport.zoom((newY - prevY) * 0.01);
			} else {
				viewport.orbit(newX - prevX, newY - prevY);
			}
			// Store the new co-ordinates so that the change in position can be calculated again if the user keeps dragging
			prevX = newX;
			prevY = newY;
		}
	}
	
	/**
	 * Called when any mouse button is released to end a drag
	 */
	public void mouseReleased(MouseEvent event) {
		dragging = false;
	}
	
	/**
	 * Called when a mouse button is released without having moved since it was depressed to change the selection if the left mouse button was used
	 */
	public void mouseClicked(MouseEvent event) {
		if (SwingUtilities.isLeftMouseButton(event)) {
			Viewport viewport = (Viewport)(event.getSource());
			viewport.click(event.getX(), event.getY());
		}
	}
	
	/**
	 * Called when the user scrolls with the mouse wheel (middle mouse button) to move the camera closer to or further away from the world's origin (zooming)
	 */
	public void mouseWheelMoved(MouseWheelEvent event) {
		Viewport viewport = (Viewport)(event.getSource());
		viewport.zoom(event.getWheelRotation());
	}
	
	/**
	 * Called when a key on the keyboard is released or held down to set the camera position and orientation or toggle between orthographic and perspective projection
	 */
	public void keyReleased(KeyEvent event) {
		int key = event.getKeyCode();
		Viewport viewport = (Viewport)(event.getSource());
		switch (key) {
			case KeyEvent.VK_1:
				viewport.setViewFront();
				break;
			case KeyEvent.VK_2:
				viewport.setViewBack();
				break;
			case KeyEvent.VK_3:
				viewport.setViewLeft();
				break;
			case KeyEvent.VK_4:
				viewport.setViewRight();
				break;
			case KeyEvent.VK_5:
				viewport.setViewTop();
				break;
			case KeyEvent.VK_6:
				viewport.setViewBottom();
				break;
			case KeyEvent.VK_P:
				viewport.toggleOrthographic(); // This method also regenerates the menu bar so that the perspective checkbox is toggled
		}
	}
	
	// Method definitions required by KeyListener, MouseListener and MouseMotionListener
	
	/**
	 * Called when a key is pressed; no action is taken
	 */
	public void keyPressed(KeyEvent event) {
		
	}
	
	/**
	 * Called when a key is pressed or held such that a character would be typed if a text field was in focus
	 */
	public void keyTyped(KeyEvent event) {
		
	}
	
	/**
	 * Called when the cursor enters the viewport
	 */
	public void mouseEntered(MouseEvent event) {
		
	}
	
	/**
	 * Called when the mouse exits the viewport
	 */
	public void mouseExited(MouseEvent event) {
		
	}
	
	/**
	 * Called when the mouse is moved inside the viewport
	 */
	public void mouseMoved(MouseEvent event) {
		
	}
}
