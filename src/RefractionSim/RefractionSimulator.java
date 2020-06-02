package RefractionSim;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Class for windows of the application also containing the public static main method which the system calls to start the application
 * @author William Platt
 *
 */
public class RefractionSimulator extends JFrame {
	private UIController userInterface;
	private JPanel content;
	
	/**
	 * Called by the system when the application is started. This creates an instance of RefractionSimulator and sets up the window
	 * @param args the parameter passed by the system which isn't needed in this application
	 */
	public static void main(String[] args) {
		JFrame window = new RefractionSimulator();
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
	}
	
	/**
	 * Constructor for the RefractionSimulator class which sets properties for the window as well as generating the contents of the window and drawing it
	 */
	public RefractionSimulator() {
		super("Refraction Simulator");
		setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize()); // Set the window to fill all of the screen except for the task bar
		setResizable(false);
		setVisible(true);
		content = new JPanel();
		content.setLayout(new BorderLayout());
		userInterface = new UIController(this); // Generate the viewport, menu bar and properties panel
		content.add(userInterface.getViewport(), BorderLayout.CENTER); // Add the viewport to the centre area of the content component
		content.add(userInterface.getPropertiesPanel(), BorderLayout.WEST); // Add the properties panel to the far left area of the content component
		setContentPane(content); // Set the content component as the window's content pane
		setJMenuBar(userInterface.getMenuBar()); // Set the menu bar as the window's menu bar
		setLocation(0, 0); // Position the window in the top left of the available screen area
		this.revalidate(); // Process the layout of the window so that it can be drawn correctly
		this.repaint(); // Paint the contents of the window
	}
	
	/**
	 * Regenerates the properties panel for rayBox as the selected ray box and replaces the old the old properties panel
	 * @param rayBox the selected ray box which the properties panel will display information for. For an empty selection, null should be used as the parameter
	 */
	public void updatePropertiesPanel(RayBox rayBox) {
		BorderLayout layout = (BorderLayout)(content.getLayout());
		content.remove(layout.getLayoutComponent(BorderLayout.WEST)); // Remove the old properties panel from the window
		userInterface.buildPropertiesPanel(rayBox); // Regenerate the new properties panel
		content.add(userInterface.getPropertiesPanel(), BorderLayout.WEST); // Add the new panel to the window in replace of the old panel
		userInterface.getPropertiesPanel().revalidate(); // Process the layout of the properties panel so that it can be drawn correctly
		userInterface.getPropertiesPanel().repaint(); // Paint the contents of the properties panel
	}
	
	/**
	 * Regenerates the menu bar and replaces the old menu bar
	 */
	public void updateMenuBar() {
		userInterface.createMenuBar(); // Regenerate the menu bar with the new settings
		setJMenuBar(userInterface.getMenuBar()); // Set the window's menu bar as the new menu bar (replacing the old one)
		userInterface.getMenuBar().revalidate(); // Process the layout of the menu bar so that it can be drawn correctly
		userInterface.getMenuBar().repaint(); // Pain the contents of the menu bar
	}
	
}
