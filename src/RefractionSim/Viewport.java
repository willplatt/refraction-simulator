package RefractionSim;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Class for 3-D viewports which deals with the user interface requirements of being a subclass of JPanel as well as handling its own 'scene' of objects
 * @author William Platt
 *
 */
public class Viewport extends JPanel {
	private int frameWidth;
	private int frameHeight;
	private Color[][] frameBuffer;
	private double[][] depthBuffer;
	private int[][] objectBuffer; // Stores the ID of the object in the foreground for each pixel
	private Color bgColor = new Color(0, 0, 0); // Black
	private static boolean orthographic = false; // By default, parallel lines converge to a vanishing point (as in real life)
	private double zoomX;
	private double zoomY;
	private static final double NEAR_CLIP = 0.01; // The closest a point on a face can be to the camera before it is no longer rendered
	private static final double FAR_CLIP = 10000; // The furthest a point on a face can be from the camera before it is no longer rendered
	private Matrix clipMatrix = new Matrix(4, 4); // Matrix for transforming camera-space co-ordinates into clip space co-ordinates
	private static Object3D[] objectList = new Object3D[100]; // Array of all objects in the scene where an object's index in this list is equal to its ID
	private static int objectListLength = 0;
	private static int worldMaterial; // Index of the material of the surroundings
	private int selectedObjID = -1; // No object selected by default
	private boolean anglesInDegrees; // Whether angles should be output in degrees or radians
	private static String[] materials = new String[100]; // List of the names of all materials
	private static double[] refractiveIndices = new double[100]; // List of the refractive indices of all materials
	private static int numOfMaterials = 0;
	
	/**
	 * Constructor for the Viewport class which sets its size, prepares it for rendering, adds the camera and target to the scene and sets a listener for all events within the viewport that need handling
	 * @param frameX width of the viewport in pixels
	 * @param frameY height of the viewport in pixels
	 */
	public Viewport(int frameX, int frameY) {
		this.frameWidth = frameX;
		this.frameHeight = frameY;
		this.frameBuffer = new Color[frameX][frameY];
		this.depthBuffer = new double[frameX][frameY];
		this.objectBuffer = new int[frameX][frameY];
		this.clearBuffers();
		initialiseMaterials();
		
		this.anglesInDegrees = true;
		setBackground(this.bgColor);
		double verticalFOV = 20 * Math.PI / 180; // Up/down field of view in radians
		double horizontalFOV = 2 * Math.atan(Math.tan(verticalFOV / 2) * frameX / frameY); // Left/right field of view in radians
		// Set how quickly objects shrink as they get further away
		this.zoomX = 1 / Math.tan(horizontalFOV);
		this.zoomY = 1 / Math.tan(verticalFOV);
		calcClipMatrix();
		initialiseScene();
		ViewportListener listener = new ViewportListener();
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		this.addMouseWheelListener(listener);
		this.addKeyListener(listener);
	}
	
	/**
	 * Defines some common materials by giving them names and refractive indices
	 */
	private void initialiseMaterials() {
		Viewport.worldMaterial = 0;
		materials[0] = "Air";
		materials[1] = "Water";
		materials[2] = "Typical glass (soda-lime)";
		materials[3] = "Human eye";
		materials[4] = "Ice";
		materials[5] = "Diamond";
		materials[6] = "Ethanol";
		materials[7] = "PLA plastic";
		materials[8] = "Sapphire";
		refractiveIndices[0] = 1.00;
		refractiveIndices[1] = 1.33;
		refractiveIndices[2] = 1.52;
		refractiveIndices[3] = 1.39;
		refractiveIndices[4] = 1.31;
		refractiveIndices[5] = 2.42;
		refractiveIndices[6] = 1.36;
		refractiveIndices[7] = 1.46;
		refractiveIndices[8] = 1.77;
		numOfMaterials = 9;
	}
	
	/**
	 * Sets up the default camera and target and adds them to the scene/objectList; objectList[0] is always the camera and objectList[1] is always the target
	 */
	private void initialiseScene() {
		Object3D camera = new Object3D(null, null);
		Vector cameraOffset = new Vector(3);
		cameraOffset.setElement(0, 0);
		cameraOffset.setElement(1, 0);
		cameraOffset.setElement(2, -6);
		camera.displace(cameraOffset);
		objectList[0] = camera;
		camera.setID(0);
		objectList[1] = new Target(Mesh.Primitive.CUBE, new Color(50, 200, 100, 100), 2);
		objectList[1].setID(1);
		objectListLength = 2;
	}
	
	/**
	 * Redraws the contents of the viewport; the scene is re-rendered into the buffers including the outline for the selected ray box, then the buffers are drawn inside the viewport component and ray box labels and angles drawn on top.
	 * This method should be called via repaint() which decides whether it is worthwhile drawing the component and passes an appropriate parameter.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // Call the method as it is defined in the JComponent class of which the Viewport class is a descendant
		render(); // Clear the buffers and re-render the 3-D objects to them
		outlineSelectedObj(); // Add to the buffers the outline around the selected ray box so the user can see which is selected
		
		// Draw frame in the viewport
		int[] pixelValues = new int[frameWidth * frameHeight];
		for (int i = 0; i < frameHeight; i++) {
			for (int j = 0; j < frameWidth; j++) {
				pixelValues[j + frameWidth * i] = frameBuffer[j][i].getRGB();
			}
		}
		// Putting pixel values into an Image and drawing the Image inside the viewport is faster than drawing each pixel straight to the viewport
		Image img = createImage(new MemoryImageSource(frameWidth, frameHeight, pixelValues, 0, frameWidth));
		g.drawImage(img, 0, 0, null); // Draw the image to the viewport with top left at (0, 0) relative to the viewport (the top left of the viewport)
		
		// Write angles and ray box labels
		Graphics2D g2 = (Graphics2D)(g);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Use anti-aliasing for text with smoother edges
		FontRenderContext frc = g2.getFontRenderContext();
		writeAngles(g, frc); // Write the angles over the image in the viewport
		writeRayBoxLabels(g, frc); // Write the ray box labels over the image in the viewport (this includes the angles which were drawn first)
	}
	
	/**
	 * Clears the buffers and renders the 3-D scene to the buffers from the camera's point of view
	 */
	private void render() {
		clearBuffers();
		Matrix uprightToCamera = objectList[0].getOrientation().transpose(); // Store the matrix for transforming points from the camera's upright space to the camera's object space
		for (int i = objectListLength - 1; i > 0; i--) { // objectList[0] is the camera and isn't rendered
			if (objectList[i] == null) { // Deleted objects leave null pointers in objectList where they once were, so skip the rendering of these
				continue; // Skip to the end of this iteration (meaning move on to the next object in the scene)
			}
			if (inView(objectList[i])) { // Check the object is potentially in view of the camera before spending time attempting to render it
				Color objectColor = objectList[i].getColor();
				Matrix objectToUpright = objectList[i].getOrientation(); // Store the matrix for transforming points from the current object's object space to the current object's upright space
				int[][] faces = objectList[i].getMesh().getFaces();
				Vector[] verts = objectList[i].getMesh().getVerts();
				Vector[] normalisedSpaceVerts = new Vector[verts.length];
				Vector[] screenSpaceVerts = new Vector[verts.length];
				for (int j = 0; j < faces.length; j++) { // Iterate through each face of the object
					for (int k = 0; k < faces[j].length; k++) { // Iterate through each vertex of the face
						int vertIndex = faces[j][k];
						if (screenSpaceVerts[vertIndex] == null) { // Faces share vertices, so some vertices may have been mapped to screen space already
							Vector worldCoord = objectToUpright.multiply(verts[vertIndex]).add(objectList[i].getOrigin()); // Map the point from object space to world space (via the object's upright space)
							Vector cameraCoord = uprightToCamera.multiply(worldCoord.subtract(objectList[0].getOrigin())); // Map the point from world space to camera space (via the camera's upright space)
							Vector normalisedCoord = project(cameraCoord); // Map the point from camera space to normalised clip space
							normalisedSpaceVerts[vertIndex] = new Vector(3);
							normalisedSpaceVerts[vertIndex].setElements(normalisedCoord);
							Vector screenCoord = new Vector(3);
							screenCoord.setElement(0, (normalisedCoord.getElement(0) + 1) * frameWidth / 2); // Map normalised x co-ordinate to screen space
							screenCoord.setElement(1, frameHeight * (0.5 -normalisedCoord.getElement(1) * 0.5)); // Map normalised y co-ordinate to screen space - notice that the normalised y co-ordinate is negated, causing the face's normal to flip
							screenCoord.setElement(2, normalisedCoord.getElement(2)); // Store the normalised depth along with each screen space co-ordinate
							screenSpaceVerts[vertIndex] = screenCoord;
						}
					}
					Vector p0 = screenSpaceVerts[faces[j][0]];
					Vector p1 = screenSpaceVerts[faces[j][1]];
					Vector p2 = screenSpaceVerts[faces[j][2]];
					Vector normal = p1.subtract(p0).crossProduct(p2.subtract(p0)).normalise(); // Calculate a normalised (length 1) screen space normal to the face
					if ((normal.getElement(2) > 0) || (objectColor.getAlpha() < 255)) { // Don't render the face if it is facing away from the camera and the object is opaque; remember that the normal is flipped in the mapping to screen space
						double d = p0.dotProduct(normal); // The equation of a plane is p.n = d where p is a point in the plane and n is the normal
						if ((p0.getElement(0) >= 0) && (p0.getElement(0) <= frameWidth) && (p0.getElement(1) >= 0) && (p0.getElement(1) <= frameHeight) && (p0.getElement(2) >= 0) && (p0.getElement(2) <= 1) ||
								(p1.getElement(0) >= 0) && (p1.getElement(0) <= frameWidth) && (p1.getElement(1) >= 0) && (p1.getElement(1) <= frameHeight) && (p1.getElement(2) >= 0) && (p1.getElement(2) <= 1)
								|| (p2.getElement(0) >= 0) && (p2.getElement(0) <= frameWidth) && (p2.getElement(1) >= 0) && (p2.getElement(1) <= frameHeight) && (p2.getElement(2) >= 0) && (p2.getElement(2) <= 1)) { // Render the face if at least one of the vertices is visible to the camera
							Color faceColor = calcFaceColor(normalisedSpaceVerts[faces[j][0]], normalisedSpaceVerts[faces[j][1]], normalisedSpaceVerts[faces[j][2]], objectList[i]); // Shading calculations work better in normalised clip space than screen space
							rasterise(p0, p1, p2, normal, d, faceColor, i); // Draw the visible parts of the triangle into the buffers using screen space co-ordinates and a screen space normal vector
						}
					}
				}
			}
		}
	}
	
	/**
	 * Overwrites areas of the frame buffer in order to create a bright orange outline 1 pixel thick around the selected object so that the user can identify which object is selected
	 */
	private void outlineSelectedObj() {
		if ((selectedObjID > 1) && (selectedObjID < objectListLength)) { // Check there is an object selected
			if (objectList[selectedObjID] != null) { // Check that the selected object wasn't deleted
				if (objectList[selectedObjID].getMesh() != null) { // Check the selected object has a mesh (unlike the camera)
					
					Color outlineColor = new Color(255, 170, 64); // Bright orange
					for (int i = 0; i < frameWidth; i++) { // For each column of pixels, place dots where there are boundaries of the selected object
						boolean lastBelongsToObject = false;
						for (int j = 0; j < frameHeight; j++) {
							if (!lastBelongsToObject) {
								if (objectBuffer[i][j] == selectedObjID) {
									if (j > 0) { // If the object starts above the screen or at the very top then the outline cannot be drawn here
										frameBuffer[i][j - 1] = outlineColor;
									}
									lastBelongsToObject = true;
								}
							} else {
								if (objectBuffer[i][j] != selectedObjID) {
									frameBuffer[i][j] = outlineColor;
									lastBelongsToObject = false;
								}
							}
						}
					}
					for (int j = 0; j < frameHeight; j++) { // For each row of pixels, place dots where there are boundaries of the selected object
						boolean lastBelongsToObject = false;
						for (int i = 0; i < frameWidth; i++) {
							if (!lastBelongsToObject) {
								if (objectBuffer[i][j] == selectedObjID) {
									if (i > 0) { // If the object starts left of the screen or at the very left then the outline cannot be drawn here
										frameBuffer[i - 1][j] = outlineColor;
									}
									lastBelongsToObject = true;
								}
							} else {
								if (objectBuffer[i][j] != selectedObjID) {
									frameBuffer[i][j] = outlineColor;
									lastBelongsToObject = false;
								}
							}
						}
					}
					
				}
			}
		}
	}
	
	/**
	 * Draws/Writes all angles of incidence, refraction and reflection inside the viewport (on top of anything already drawn in the viewport)
	 * @param g the graphics context for the viewport which allows text to be drawn in the viewport
	 * @param frc the FontRenderContext of the 2-D graphics context (which should have anti-aliasing) which allows the width of text to be determined without drawing
	 */
	private void writeAngles(Graphics g, FontRenderContext frc) {
		Font angleFont = new Font("SansSerif", Font.PLAIN, 13);
		double halfWidth;
		if (anglesInDegrees) {
			halfWidth = angleFont.getStringBounds("00.00\u00B0", frc).getWidth() / 2.0; // Calculate half the typical width of an angle written in degrees (in pixels)
		} else {
			halfWidth = angleFont.getStringBounds("0.000\u03C0", frc).getWidth() / 2.0; // Calculate half the typical width of an angle written in radians (in pixels)
		}
		g.setFont(angleFont);
		g.setColor(Color.WHITE);
		Matrix uprightToCamera = objectList[0].getOrientation().transpose(); // Matrix for transforming points from the camera's upright space to the camera's object space (camera space)
		for (int i = 2; i < objectListLength; i++) { // Iterate through each object in the scene except for the camera and target (IDs 0 and 1)
			if (objectList[i] instanceof Beam) {
				Beam lightBeam = (Beam)(objectList[i]);
				if (lightBeam.getAnglesVisible()) {
					int numOfAngles = lightBeam.getNumOfAngles();
					double[] angles = lightBeam.getAngles();
					Vector[] anglePositions = lightBeam.getAnglePositions(); // Points in world space of the angles
					for (int j = 0; j < numOfAngles; j++) {
						Vector cameraCoord = uprightToCamera.multiply(anglePositions[j].subtract(objectList[0].getOrigin())); // Map the world space points to camera space (via the camera's upright space)
						Vector normalisedCoord = project(cameraCoord);
						Vector screenCoord = new Vector(3);
						// Map the points in normalised clip space to screen space and offset slightly because text position is defined by its top left, not its centre
						screenCoord.setElement(0, Math.round(((normalisedCoord.getElement(0) + 1) * frameWidth / 2) - halfWidth));
						screenCoord.setElement(1, Math.round(frameHeight * (0.5 -normalisedCoord.getElement(1) * 0.5) - 5));
						if (anglesInDegrees) {
							double angle = Math.round(18000.0 * angles[j] / Math.PI) / 100.0; // 2 decimal places
							g.drawString(Double.toString(angle) + "\u00B0", (int)(screenCoord.getElement(0)), (int)(screenCoord.getElement(1))); // Write the angle in the appropriate place within the viewport
						} else {
							double angle = Math.round(1000.0 * angles[j] / Math.PI) / 1000.0; // 3 decimal places
							g.drawString(Double.toString(angle) + "\u03C0", (int)(screenCoord.getElement(0)), (int)(screenCoord.getElement(1)));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Draws/Writes all ray box labels inside the viewport (on top of anything that has already been drawn)
	 * @param g the graphics context for the viewport which allows text to be drawn in the viewport
	 * @param frc the FontRenderContext of the 2-D graphics context (which should have anti-aliasing) which allows the ascent (maximum height above the baseline) of text to be determined without drawing
	 */
	private void writeRayBoxLabels(Graphics g, FontRenderContext frc) {
		Font rayBoxLabelFont = new Font("SansSerif", Font.BOLD, 20);
		int minX = 5; // Left margin preventing the label being written too close to the left edge of the viewport
		int minY = (int)(Math.round(rayBoxLabelFont.getLineMetrics("W", frc).getAscent() + 5)); // Top margin preventing the label being written too close to the top of the viewport
		g.setFont(rayBoxLabelFont);
		g.setColor(Color.BLUE);
		for (int i = 1; i < objectListLength; i++) {
			if (objectList[i] instanceof RayBox) {
				boolean found = false;
				// Working from left to right down the screen, find the first pixel belonging to this particular ray box
				for (int y = 0; (y < frameHeight) && (found == false); y++) {
					for (int x = 0; (x < frameWidth) && (found == false); x++) {
						if (objectBuffer[x][y] == i) {
							if (x < minX) {
								x = minX;
							}
							if (y < minY) {
								y = minY;
							}
							RayBox rayBox = (RayBox)(objectList[i]);
							g.drawString(rayBox.getLabel(), x, y);
							found = true;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the list of 3-D objects in the scene where the item at index 0 is the camera and the item at index 1 is the target
	 * @return the list of all 3-D objects in the scene
	 */
	public static Object3D[] getObjectList() {
		return objectList;
	}
	
	/**
	 * Returns true if the bounding box of an object is at least partially visible to the camera (but the object itself may still be completely out of sight)
	 * @param obj the 3-D object to be checked for visibility
	 * @return whether or not the bounding box of obj is in view of the camera
	 */
	private boolean inView(Object3D obj) { // Even if this returns true, the object may not be in view, as this is a quick algorithm
		boolean inView = false;
		// For each array variable, index 0 represents the x-axis, index 1 represents the y-axis and index 2 represents the z-axis
		boolean[] below = {false, false, false};
		boolean[] above = {false, false, false};
		boolean[] span = {false, false, false}; // Each span element is set to true if there is a normalised clip space point in the visible range or one point above the range and one point below the range
		int[][] bounds = {{-1, 1}, {-1, 1}, {0, 1}}; // For each inner array, index 0 is the lower bound for that axis and index 1 is the upper bound
		Matrix objectToUpright = obj.getOrientation(); // Matrix for transforming points from object space to upright space
		Matrix uprightToCamera = objectList[0].getOrientation().transpose(); // Matrix for transforming points from the camera's upright space to the camera's object space (camera space)
		Vector[] boxVerts = obj.getBoxVerts();
		int i = 0;
		while ((i < boxVerts.length) && (!inView)) {
			Vector worldCoord = objectToUpright.multiply(boxVerts[i]).add(obj.getOrigin()); // Map the point in object space to world space (via upright space)
			Vector cameraCoord = uprightToCamera.multiply(worldCoord.subtract(objectList[0].getOrigin())); // Map the point in world space to camera space (via the camera's upright space)
			Vector normalisedCoord = project(cameraCoord);
			for (int j = 0; (j < 3) && (inView == false); j++) { // For each of the x, y and z axes
				if (span[j] == false) {
					if (normalisedCoord.getElement(j) >= bounds[j][0]) {
						if (normalisedCoord.getElement(j) <= bounds[j][1]) {
							span[j] = true; // A point within the bounds is treated like one on each side because both contribute to making the box visible on that axis
						} else {
							above[j] = true;
						}
					} else {
						below[j] = true;
					}
					if ((below[j]) && (above[j])) {
						span[j] = true;
					}
					if ((span[0]) && (span[1]) && (span[2])) {
						inView = true;
					}
				}
			}
			i++;
		}
		return inView;
	}
	
	/**
	 * Maps a point from camera space to normalised clip space and returns the point as a Vector object
	 * @param cameraCoord the point in camera space which is to be mapped to normalised clip space
	 * @return cameraCoord mapped to normalised clip space
	 * @throws IllegalArgumentException if the cameraCoord parameter is not a 3-row vector
	 */
	private Vector project(Vector cameraCoord) {
		if (cameraCoord.getN() != 3) {
			throw new IllegalArgumentException("A point in camera space must be a 3-D vector");
		} else {
			if (orthographic) {
				double zoom = objectList[0].getOrigin().modulus() / 3000; // Orthographic visualisation means that object size is independent of distance, but this means that moving further away does not give a wider view, so the view cube (rather than the view frustum in perspective projection) is stretched with distance to make the view wider when the camera is further away
				// The clip space is the same as camera space
				Vector normalised3D = new Vector(3);
				normalised3D.setElement(0, cameraCoord.getElement(0) / (frameWidth * zoom));
				normalised3D.setElement(1, cameraCoord.getElement(1) / (frameHeight * zoom));
				normalised3D.setElement(2, cameraCoord.getElement(2) / FAR_CLIP);
				return normalised3D;
			} else {
				Vector camera4D = new Vector(4); // Perspective projection in 3-D is not a linear transformation in three dimensions, so cannot be performed using 3 by 3 matrices and 3-row vectors; 4 by 4 matrices and 4-row vectors are needed
				camera4D.setElement(0, cameraCoord.getElement(0));
				camera4D.setElement(1, cameraCoord.getElement(1));
				camera4D.setElement(2, cameraCoord.getElement(2));
				camera4D.setElement(3, 1); // Element 3 is set to 1 so that it becomes the old value of element 2 after the clip matrix has been applied
				Vector clip4D = clipMatrix.multiply(camera4D);
				if (clip4D.getElement(3) == 0) {
					clip4D.setElement(3, 0.0001); // Avoid divide by zero
				}
				Vector normalised3D = new Vector(3);
				// Perform the perspective divide to make clip space into normalised clip space
				normalised3D.setElement(0, clip4D.getElement(0) / clip4D.getElement(3));
				normalised3D.setElement(1, clip4D.getElement(1) / clip4D.getElement(3));
				normalised3D.setElement(2, clip4D.getElement(2) / clip4D.getElement(3));
				return normalised3D;
			}
		}
	}
	
	/**
	 * Returns the colour to render a particular face in based on the object's overall colour and how much the face is pointing towards the camera in normalised clip space
	 * @param p0 the first vertex of the face in normalised clip space. It is important that the order of the vertices is correct
	 * @param p1 the second vertex of the face
	 * @param p2 the third vertex of the face
	 * @param object the object to which the face belongs
	 * @return the colour to render the face defined by the three input points
	 * @throws IllegalArgumentException if any of p0, p1 and p2 is not a 3-row vector
	 */
	private Color calcFaceColor(Vector p0, Vector p1, Vector p2, Object3D object) {
		if ((p0.getN() != 3) || (p1.getN() != 3) || (p2.getN() != 3)) {
			throw new IllegalArgumentException("Face colour can only be calculated from points in 3-D space");
		} else {
			Color faceColor; // The more the face is pointing towards the camera, the lighter the colour will be
			Color objectColor = object.getColor();
			if (object instanceof Beam) { // Beams are rendered as solid colour without shadows (although a face in front of it with some transparency may affect its colour)
				faceColor = objectColor;
			} else {
				Vector screenNormal = p1.subtract(p0).crossProduct(p2.subtract(p0)).normalise();
				
				double brightFactor; // A value between 0.2 and 1 where 0 (if zero were allowed) would make faceColor completely black and 1 would make faceColor the same as objectColor
				if (objectColor.getAlpha() < 255) {
					if (screenNormal.getElement(2) < 0) { // If the face is pointing towards the camera
						brightFactor = 1 - 262144 * (1 + screenNormal.getElement(2)); // 262144 is a value obtained through experimentation and is a power of 2, reducing computation time
						if (brightFactor < 0.5) {
							brightFactor = 0.5;
						}
					} else {
						brightFactor = -(screenNormal.getElement(2) - 1);
						if (brightFactor < 0.2) {
							brightFactor = 0.2;
						}
					}
				} else {
					brightFactor = 1 - 524288 * (1 + screenNormal.getElement(2)); // 524288 is another experimental power of 2
					if (brightFactor < 0.7) {
						brightFactor = ((brightFactor - 0.7) / 2) + 0.7;
						if (brightFactor < 0.6) {
							brightFactor = ((brightFactor - 0.6) / 2) + 0.6;
							if (brightFactor < 0.5) {
								brightFactor = ((brightFactor - 0.5) / 2) + 0.5;
							}
						}
					}
					if (brightFactor < 0.4) {
						brightFactor = 0.4;
					}
				}
				faceColor = new Color((int)(Math.round(objectColor.getRed() * brightFactor)), (int)(Math.round(objectColor.getGreen() * brightFactor)),
						(int)(Math.round(objectColor.getBlue() * brightFactor)), objectColor.getAlpha());
			}
			return faceColor;
		}
	}
	
	/**
	 * Adds the parameter ray box and its beam to the scene and makes the ray box the selected object before updating the user interface and viewport. A dialog box informs the user if they have too many ray boxes to add any more
	 * @param newRayBox the ray box to add to the scene along with its beam
	 */
	public void addRayBox(RayBox newRayBox) {
		RefractionSimulator window = (RefractionSimulator)(SwingUtilities.windowForComponent(this));
		int i = 1; // IDs 0 and 1 are the camera and target, so can be skipped
		// Find the first null element in objectList, the next element will also be null (unless there is an overflow) because ray boxes and their beams are consecutive and both are removed at a time
		while (i < objectListLength) {
			if (objectList[i] == null) {
				break;
			}
			i++;
		}
		if (i < objectList.length - 1) {
			newRayBox.setID(i);
			objectList[i] = newRayBox;
			Beam newBeam = newRayBox.getLightBeam();
			newBeam.setID(i + 1);
			objectList[i + 1] = newBeam;
			selectedObjID = i;
			if (i + 1 >= objectListLength) {
				objectListLength = i + 2;
			}
			if (window != null) {
				window.updatePropertiesPanel(newRayBox);
			}
			newBeam.update();
			repaint();
		} else { // Not enough room for a new ray box and light beam
			JOptionPane.showMessageDialog(window, "You have too many ray boxes to add another one. You must delete an existing ray box if you wish to add another");
		}
	}
	
	/**
	 * Removes the selected ray box and its beam from the scene and updates the user interface and viewport; there is no longer a selected object
	 * @throws IllegalArgumentException if the selected object isn't a ray box
	 */
	public void removeRayBox() {
		if (objectList[selectedObjID] instanceof RayBox) {
			RayBox toRemove = (RayBox)(objectList[selectedObjID]);
			objectList[toRemove.getID()] = null;
			objectList[toRemove.getLightBeam().getID()] = null;
			if (toRemove.getLightBeam().getID() + 1 == objectListLength) { // The light beam was the last non-null object
				objectListLength -= 2; // Reduce the length to reflect the last two items being removed
			}
			selectedObjID = -1;
			RefractionSimulator window = (RefractionSimulator)(SwingUtilities.windowForComponent(this));
			window.updatePropertiesPanel(null);
			repaint();
		} else {
			throw new IllegalArgumentException("Cannot remove a ray box if one is not selected");
		}
	}
	
	/**
	 * Resets the frame buffer, depth buffer and object buffer in preparation for re-rendering
	 */
	private void clearBuffers() {
		for (int i = 0; i < this.frameWidth; i++) {
			for (int j = 0; j < this.frameHeight; j++) {
				this.frameBuffer[i][j] = this.bgColor;
				this.depthBuffer[i][j] = 1; // Clip space points have a depth mapped between 0 (near clip) and 1 (far clip)
				this.objectBuffer[i][j] = -1; // No object
			}
		}
	}
	
	/**
	 * Returns the width of the viewport in pixels
	 * @return the width of the viewport in pixels
	 */
	@Override
	public int getWidth() {
		return this.frameWidth;
	}
	
	/**
	 * Returns the height of the viewport in pixels
	 * @return the height of the viewport in pixels
	 */
	@Override
	public int getHeight() {
		return this.frameHeight;
	}
	
	/**
	 * Returns an array of the names of all the materials - preset and custom - in the same order as the refractive indices array
	 * @return an array of the names of all preset and custom materials
	 */
	public static String[] getMaterials() {
		return materials;
	}
	
	/**
	 * Returns an array of the absolute refractive indices of all the preset and custom materials in the same order as the material names array.
	 * @return the refractive indices of all the materials
	 */
	public static double[] getRefractiveIndices() {
		return refractiveIndices;
	}
	
	/**
	 * Returns the index of the material that the world is set to
	 * @return the index of the material that the world is set to
	 */
	public static int getWorldMaterial() {
		return worldMaterial;
	}
	
	/**
	 * Sets the material of the world to that with the index passed as a parameter
	 * @param materialID the index of the material that the world should be
	 * @throws IllegalArgumentException if there is currently not a material with the index materialID
	 */
	public void setWorldMaterial(int materialID) {
		if ((materialID < 0) || (materialID >= numOfMaterials)) {
			throw new IllegalArgumentException("World material cannot be set to one which does not exist");
		} else {
			worldMaterial = materialID;
			recalculateBeams();
			repaint();
		}
	}
	
	/**
	 * Returns the index of the material that the target is set to
	 * @return the index of the material that the target is set to
	 */
	public int getTargetMaterial() {
		Target target = (Target)(objectList[1]);
		return target.getMaterial();
	}
	
	/**
	 * Sets the material of the target to that with the index passed as a parameter
	 * @param materialID the index of the material that the target should be set to
	 * @throws IllegalArgumentException if there is currently not a material with the index materialID
	 */
	public void setTargetMaterial(int materialID) {
		if ((materialID < 0) || (materialID >= numOfMaterials)) {
			throw new IllegalArgumentException("World material cannot be set to one which does not exist");
		} else {
			Target target = (Target)(objectList[1]);
			target.setMaterial(materialID);
			recalculateBeams();
			repaint();
		}
	}
	
	/**
	 * Returns the total number of materials that are defined
	 * @return the total number of materials
	 */
	public static int getNumOfMaterials() {
		return numOfMaterials;
	}
	
	/**
	 * Creates a new material with the properties specified by the parameters and adds it to the end of the list of materials (which is a combination of the list of material names and the list of refractive indices)
	 * @param materialName the name of the new material
	 * @param refractiveIndex the absolute refractive index of the new material
	 */
	public void addMaterial(String materialName, double refractiveIndex) {
		materials[numOfMaterials] = materialName;
		refractiveIndices[numOfMaterials] = refractiveIndex;
		numOfMaterials++;
		RefractionSimulator window = (RefractionSimulator)(SwingUtilities.windowForComponent(this));
		window.updateMenuBar(); // Material menus will need a new item
	}
	
	/**
	 * Returns the name of the current shape of the target material
	 * @return the name of the current shape of the target material
	 */
	public String getTargetShape() {
		Target target = (Target)(objectList[1]);
		return target.getShape();
	}
	
	/**
	 * Sets the geometry of the target to that defined by newShape and recalculates the paths of all beams in the scene
	 * @param newShape the new shape of the target material
	 */
	public void setTargetShape(Mesh.Primitive newShape) {
		Target oldTarget = (Target)(objectList[1]);
		objectList[1] = new Target(newShape, oldTarget.getColor(), oldTarget.getMaterial()); // Create a new target to replace the old one; the new target has the same colour and material as the old one but a different shape
		recalculateBeams();
		repaint();
	}
	
	/**
	 * Recalculates the paths of all beams in the scene but does not re-render
	 */
	public void recalculateBeams() {
		for (int i = 2; i < objectListLength; i++) {
			if (objectList[i] instanceof Beam) {
				Beam lightBeam = (Beam)(objectList[i]);
				lightBeam.update();
			}
		}
	}
	
	/**
	 * Returns true if orthographic projection is currently being used
	 * @return whether orthographic projection is being used
	 */
	public boolean isOrthographic() {
		return orthographic;
	}
	
	/**
	 * Switches from orthographic projection to perspective projection or from perspective to orthographic and re-renders
	 */
	public void toggleOrthographic() {
		orthographic = !orthographic;
		RefractionSimulator window = (RefractionSimulator)(SwingUtilities.windowForComponent(this));
		window.updateMenuBar(); // Perspective checkbox needs changing
		this.repaint();
	}
	
	/**
	 * Returns true if angles are set to display in degrees rather than radians
	 * @return whether angles are displayed in radians
	 */
	public boolean areAnglesInDegrees() {
		return anglesInDegrees;
	}
	
	/**
	 * Changes angles from degrees to radians or vice versa and updates the interface accordingly
	 */
	public void toggleAngleUnits() {
		anglesInDegrees = !anglesInDegrees;
		RayBox rayBox;
		try {
			rayBox = (RayBox)(objectList[selectedObjID]);
			RefractionSimulator window = (RefractionSimulator)(SwingUtilities.windowForComponent(this));
			window.updatePropertiesPanel(rayBox); // Labels on sliders need to be changed
		} catch (Exception e) { // If no ray box is selected
			
		}
		repaint(); // Angles in the viewport need to be changed
	}
	
	/**
	 * Orbits the selected ray box about the world origin by the heading and pitch specified
	 * @param heading the angular displacement about the world's y-axis
	 * @param pitch the angular displacement about the world's x-axis after rotation by the heading
	 */
	public void globallyRotateRayBox(double heading, double pitch) {
		RayBox rayBox = (RayBox)(objectList[selectedObjID]);
		rayBox.orbitAboutOrigin(heading, pitch);
		rayBox.getLightBeam().update(); // Recalculate the path and geometry of the ray box's light beam
		repaint(); // Re-render the viewport with the new ray box position and recalculated light beam
	}
	
	/**
	 * Rotates the selected ray box about its origin by the heading and pitch specified
	 * @param heading the angular displacement about the world's y-axis
	 * @param pitch the angular displacement about the world's x-axis after rotation by the heading
	 */
	public void locallyRotateRayBox(double heading, double pitch) {
		RayBox rayBox = (RayBox)(objectList[selectedObjID]);
		rayBox.rotate(heading, pitch);
		rayBox.getLightBeam().update();
		repaint();
	}
	
	/**
	 * Sets up clipMatrix to map camera space to clip space (when using perspective projection) for the given zoomX, zoomdY, NEAR_CLIP and FAR_CLIP
	 */
	private void calcClipMatrix() {
		clipMatrix.setElement(0, 0, zoomX);
		clipMatrix.setElement(1, 1, zoomY);
		double element = FAR_CLIP / (FAR_CLIP - NEAR_CLIP);
		clipMatrix.setElement(2, 2, element);
		clipMatrix.setElement(3, 2, -NEAR_CLIP * element);
		clipMatrix.setElement(2, 3, 1);
	}
	
	/**
	 * From information in extended screen space (screen space with depth), draws the visible parts of a face into the buffers
	 * @param point0 the first vertex (index 0) of the face in screen space
	 * @param point1 the second vertex (index 1) of the face in screen space
	 * @param point2 the third vertex (index 2) of the face in screen space
	 * @param normal the normalised normal to the face in extended screen space
	 * @param d the value in the expression p.n = d where p is a point on the face and n is the normalised normal to the face (all in extended screen space)
	 * @param faceColor the colour to render the face
	 * @param objectID the ID of the object to which this face belongs (the index of the object in objectList)
	 */
	private void rasterise(Vector point0, Vector point1, Vector point2, Vector normal, double d, Color faceColor, int objectID) {		
		// Find the depth of the closest point to save on depth calculations later
		double minDepth = point0.getElement(2);
		if (point1.getElement(2) < minDepth) {
			minDepth = point1.getElement(2);
		}
		if (point2.getElement(2) < minDepth) {
			minDepth = point2.getElement(2);
		}
		Edge2D edge0 = new Edge2D(point0.getElement(0), point0.getElement(1), point1.getElement(0), point1.getElement(1));
		Edge2D edge1 = new Edge2D(point1.getElement(0), point1.getElement(1), point2.getElement(0), point2.getElement(1));
		Edge2D edge2 = new Edge2D(point2.getElement(0), point2.getElement(1), point0.getElement(0), point0.getElement(1));
		// Find the tallest if the three edges
		Edge2D tallEdge = edge0;
		Edge2D shortEdge0 = edge1;
		Edge2D shortEdge1 = edge2;
		if (edge1.getHeight() > tallEdge.getHeight()) {
			tallEdge = edge1;
			shortEdge0 = edge0;
		}
		if (edge2.getHeight() > tallEdge.getHeight()) {
			tallEdge = edge2;
			shortEdge0 = edge0;
			shortEdge1 = edge1;
		}
		if (tallEdge.getY0() != shortEdge0.getY0()) { // shortEdge0 should share the lowest point with tallEdge (lowest in terms of y-value, not position on the screen)
			Edge2D temp = shortEdge0;
			shortEdge0 = shortEdge1;
			shortEdge1 = temp;
		}
		// Fill in the pixels with centres contained by the precise triangle where the triangle is in front of anything rendered so far
		int initialY = (int)(Math.round(tallEdge.getY0()));
		if (initialY < 0) {
			initialY = 0;
		}
		int finalY = (int)(Math.round(shortEdge0.getY1()));
		if (finalY >= frameHeight) {
			finalY = frameHeight - 1;
		}
		double dxTall = (tallEdge.getX1() - tallEdge.getX0()) / tallEdge.getHeight(); // Increase in x for the tallest edge when y increases by 1
		double dxShort = (shortEdge0.getX1() - shortEdge0.getX0()) / shortEdge0.getHeight(); // Increase in x for shortEdge0 when y increases by 1
		double ySkip = initialY - tallEdge.getY0(); // The signed change in y from the lowest point of the face to the first pixel with centre inside the triangle
		double xTall = (ySkip + 0.5) * dxTall + tallEdge.getX0(); // The precise x co-ordinate of the tallest edge for the y co-ordinate of the lowest row of pixels in the triangle
		double xShort = (ySkip + 0.5) * dxShort + shortEdge0.getX0(); // The precise x co-ordinate of shortEdge0 for the y co-ordinate of the lowest row of pixels in the triangle
		rasteriseHalfFace(initialY, finalY, xShort, xTall, dxShort, dxTall, minDepth, normal, d, faceColor, objectID); // Draw the rows of pixels spanned by shortEdge0
		
		xTall = xTall + dxTall * (finalY - initialY);
		initialY = finalY;
		finalY = (int)(Math.round(shortEdge1.getY1()));
		if (finalY >= frameHeight) {
			finalY = frameHeight - 1;
		}
		dxShort = (shortEdge1.getX1() - shortEdge1.getX0()) / shortEdge1.getHeight();
		ySkip = initialY - shortEdge1.getY0();
		xShort = (ySkip + 0.5) * dxShort + shortEdge1.getX0();
		rasteriseHalfFace(initialY, finalY, xShort, xTall, dxShort, dxTall, minDepth, normal, d, faceColor, objectID); // Draw the rows of pixels spanned by shortEdge1
	}
	
	/**
	 * Sets pixels in the buffers where the face is visible for initialY <= y < finalY
	 * @param initialY the first row of pixels (lowest y value)
	 * @param finalY the row of pixels after the last (highest y value)
	 * @param xShort the x co-ordinate of the shorter edge when the y co-ordinate is initialY
	 * @param xTall the x co-ordinate of the taller edge when the y co-ordinate is initailY
	 * @param dxShort the change in x of the shorter edge when y is increased by 1
	 * @param dxTall the change in x of the taller edge when y is increased by 1
	 * @param minDepth the lowest depth value of any point on the face in extended screen space
	 * @param normal the normalised normal to the face in extended screen space
	 * @param d the value in the expression p.n = d where p is a point on the face and n is the normalised normal to the face (all in extended screen space)
	 * @param faceColor the colour to render the face
	 * @param objectID the ID of the object to which the face belongs (the index of the object in objectList)
	 */
	private void rasteriseHalfFace(int initialY, int finalY, double xShort, double xTall, double dxShort, double dxTall, double minDepth, Vector normal, double d, Color faceColor, int objectID) {
		for (int pixelY = initialY; pixelY < finalY; pixelY++) {
			int roundedxShort = (int)(Math.round(xShort));
			int roundedxTall = (int)(Math.round(xTall));
			if (roundedxTall <= roundedxShort) {
				rasteriseFaceRow(roundedxTall, roundedxShort, pixelY, minDepth, normal, d, faceColor, objectID);
			} else {
				rasteriseFaceRow(roundedxShort, roundedxTall, pixelY, minDepth, normal, d, faceColor, objectID);
			}
			xTall += dxTall;
			xShort += dxShort;
		}
	}
	
	/**
	 * Sets pixels in the buffers where the face is visible for startX <= x < endX and y co-ordinate pixelY
	 * @param startX the x co-ordinate of the first pixel on this row contained by the triangle
	 * @param endX the x co-ordinate of the pixel after the last on this row contained by the triangle
	 * @param pixelY the y co-ordinate of the row of pixels being set
	 * @param minDepth the lowest depth value of any point on the face in extended screen space
	 * @param normal the normalised normal to the face in extended screen space
	 * @param d the value in the expression p.n = d where p is a point on the face and n is the normalised normal to the face (all in extended screen space)
	 * @param faceColor the colour to render the face
	 * @param objectID the ID of the object to which the face belongs (the index of the object in objectList)
	 */
	private void rasteriseFaceRow(int startX, int endX, int pixelY, double minDepth, Vector normal, double d, Color faceColor, int objectID) {
		for (int pixelX = startX; pixelX < endX; pixelX++) {
			if ((pixelX >= 0) && (pixelX < frameWidth)) {
				if ((pixelY >= 0) && (pixelY < frameHeight)) {
					if (minDepth < depthBuffer[pixelX][pixelY]) { // If minDepth is too large then the depth at this point will be
						// Find the 3rd element of p by rearranging p.n = d to p[2] = (d - n[0] * p[0] - n[1] * p[1]) / n[2]
						double depth = (d - normal.getElement(0) * (pixelX + 0.5) - normal.getElement(1) * (pixelY + 0.5)) / normal.getElement(2); // 0.5 is added to pixelX and pixelY to get the depth at the centre of the pixel
						if (depth < 0) { // Don't render in front of the near clip plane (or behind the camera for orthographic projection)
							continue;
						}
						if (depth < depthBuffer[pixelX][pixelY]) { // If this face is closer than anything else at this point so far, alter the buffers
							setPixel(pixelX, pixelY, depth, faceColor, objectID);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Changes the frame buffer, depth buffer and object buffer for a single pixel
	 * @param x the x co-ordinate of the pixel to change
	 * @param y the y co-ordinate of the pixel to change
	 * @param depth the depth of the current face at the centre of this pixel
	 * @param color the rendered colour of the face (including alpha)
	 * @param objectID the ID of the object this face belongs to (the index of the object in objectList)
	 */
	private void setPixel(int x, int y, double depth, Color color, int objectID) {
		if (color.getAlpha() != 255) { // Combine the previous colour of this pixel with the new semi-transparent colour
			double opacity = color.getAlpha() / 255.0;
			double transparency = 1 - opacity;
			color = new Color((int)(Math.round(frameBuffer[x][y].getRed() * transparency + color.getRed() * opacity)),
					(int)(Math.round(frameBuffer[x][y].getGreen() * transparency + color.getGreen() * opacity)),
					(int)(Math.round(frameBuffer[x][y].getBlue() * transparency + color.getBlue() * opacity))); // The produced colour will be opaque
		}
		this.frameBuffer[x][y] = color;
		this.depthBuffer[x][y] = depth;
		this.objectBuffer[x][y] = objectID;
	}
	
	/**
	 * Orbit the camera around the world's origin where xChange is directly proportional to the heading and yChange directly proportional to the pitch
	 * @param xChange the signed change in the x position of the user's cursor between dragging events
	 * @param yChange the signed change in the y position of the user's cursor between dragging events
	 */
	public void orbit(int xChange, int yChange) {
		double heading = Math.PI * xChange / frameWidth; // Dragging from far left to far right gives a heading of positive pi radians
		double pitch = 0.5 * Math.PI * yChange / frameHeight; // Dragging from top to bottom gives a pitch of positive 0.5pi radians
		objectList[0].orbit(heading, pitch);
		this.repaint();
	}
	
	/**
	 * Moves the camera towards or away from the world's origin
	 * @param scrollAmount the signed number of notches the mouse wheel is moved down or the equivalent amount dragged down with the right mouse button held
	 */
	public void zoom(double scrollAmount) {
		Vector newOrigin = objectList[0].getOrigin().scale(Math.pow(0.8, -scrollAmount));
		double distance = newOrigin.modulus();
		if (distance < 3) {
			newOrigin = newOrigin.scale(3 / distance); // Make newOrigin 0.5 units from the world's origin
		} else if (distance > 100) {
			newOrigin = newOrigin.scale(100 / distance); // Make newOrigin 100 units from the world's origin
		}
		objectList[0].setOrigin(newOrigin); // For each notch up the camera's distance from the origin reduces by 20%
		this.repaint();
	}
	
	/**
	 * Changes the selection to the ray box visible at position (x, y) in the viewport (otherwise the selection becomes empty) and updates the properties panel
	 * @param x the x co-ordinate of the pixel the user clicked relative to the top left of the viewport
	 * @param y the y co-ordinate of the pixel the user clicked relative to the top left of the viewport
	 */
	public void click(int x, int y) {
		RefractionSimulator window = (RefractionSimulator)(SwingUtilities.windowForComponent(this));
		if (!(window.getFocusOwner() instanceof JTextField)) { // Don't change the selected object while a text field is in focus because it is just about to lose focus and its value used to update the selected object
			if ((objectBuffer[x][y] > 0) && (objectBuffer[x][y] < objectListLength)) { // Check if an object was clicked
				if (objectList[objectBuffer[x][y]] instanceof RayBox) { // Check if the clicked object was a ray box
					if (selectedObjID != objectBuffer[x][y]) { // Only spend time updating if the clicked object wasn't already selected
						selectedObjID = objectBuffer[x][y]; // Select the ray box clicked
						window.updatePropertiesPanel((RayBox)(objectList[objectBuffer[x][y]])); // Change the properties panel to show details of the newly selected object
					}
				} else {
					selectedObjID = -1; // Make the selection empty
					window.updatePropertiesPanel(null); // Clear the properties panel
				}
			} else {
				selectedObjID = -1;
				window.updatePropertiesPanel(null);
			}
			this.repaint();
		}
	}
	
	/**
	 * Sets the label of the selected object to newLabel
	 * @param newLabel the string to set the label of the selected object to
	 */
	public void updateLabel(String newLabel) {
		RayBox rayBox = (RayBox)(objectList[selectedObjID]);
		rayBox.setLabel(newLabel);
		repaint(); // Redraw the label
	}
	
	/**
	 * Sets the thickness of the selected ray box's beam to newThickness
	 * @param newThickness the relative value that the thickness of the selected ray box's beam should be set to
	 */
	public void updateBeamThickness(int newThickness) {
		RayBox rayBox = (RayBox)(objectList[selectedObjID]);
		rayBox.setBeamThickness(newThickness);
		repaint();
	}
	
	/**
	 * Changes angle visibility from on to off or off to on for the selected ray box and refreshes the viewport including angles
	 */
	public void toggleShowAngles() {
		RayBox rayBox = (RayBox)(objectList[selectedObjID]);
		rayBox.setAnglesVisible(!rayBox.getAnglesVisible());
		repaint(); // Angles must be removed or drawn
	}
	
	/**
	 * Positions and orientates the camera to face directly forwards at the same distance from the origin as before
	 */
	public void setViewFront() {
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(2, -objectList[0].getOrigin().modulus()); // Keep the modulus the same
		Matrix newOrientation = new Matrix(3, 3);
		newOrientation.setElements(new double[] {1, 0, 0,   0, 1, 0,   0, 0, 1});
		objectList[0].setOrigin(newOrigin);
		objectList[0].setOrientation(newOrientation);
		this.repaint();
	}
	
	/**
	 * Positions and orientates the camera to face directly backwards at the same distance from the origin as before
	 */
	public void setViewBack() {
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(2, objectList[0].getOrigin().modulus());
		Matrix newOrientation = new Matrix(3, 3);
		newOrientation.setElements(new double[] {-1, 0, 0,   0, 1, 0,   0, 0, -1});
		objectList[0].setOrigin(newOrigin);
		objectList[0].setOrientation(newOrientation);
		this.repaint();
	}
	
	/**
	 * Positions and orientates the camera to face directly right at the same distance from the origin as before
	 */
	public void setViewLeft() {
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(0, -objectList[0].getOrigin().modulus());
		Matrix newOrientation = new Matrix(3, 3);
		newOrientation.setElements(new double[] {0, 0, -1,   0, 1, 0,   1, 0, 0});
		objectList[0].setOrigin(newOrigin);
		objectList[0].setOrientation(newOrientation);
		this.repaint();
	}
	
	/**
	 * Positions and orientates the camera to face directly left at the same distance from the origin as before
	 */
	public void setViewRight() {
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(0, objectList[0].getOrigin().modulus());
		Matrix newOrientation = new Matrix(3, 3);
		newOrientation.setElements(new double[] {0, 0, 1,   0, 1, 0,   -1, 0, 0});
		objectList[0].setOrigin(newOrigin);
		objectList[0].setOrientation(newOrientation);
		this.repaint();
	}
	
	/**
	 * Positions and orientates the camera to face directly down at the same distance from the origin as before
	 */
	public void setViewTop() {
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(1, objectList[0].getOrigin().modulus());
		Matrix newOrientation = new Matrix(3, 3);
		newOrientation.setElements(new double[] {1, 0, 0,   0, 0, 1,   0, -1, 0});
		objectList[0].setOrigin(newOrigin);
		objectList[0].setOrientation(newOrientation);
		this.repaint();
	}
	
	/**
	 * Positions and orientates the camera to face directly up at the same distance from the origin as before
	 */
	public void setViewBottom() {
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(1, -objectList[0].getOrigin().modulus());
		Matrix newOrientation = new Matrix(3, 3);
		newOrientation.setElements(new double[] {1, 0, 0,   0, 0, -1,   0, 1, 0});
		objectList[0].setOrigin(newOrigin);
		objectList[0].setOrientation(newOrientation);
		this.repaint();
	}
	
	/**
	 * Saves the contents of the viewport to a bitmapped image file with path outputFile and of type expressed by fileExtension
	 * @param outputFile the path (including name with file extension) to save the image under
	 * @param fileExtension one of "gif", "jpg" and "png" which indicates the format for the file
	 */
	public void saveImage(File outputFile, String fileExtension) {
		System.out.println(outputFile.getName());
		BufferedImage image = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		paint(image.getGraphics()); // Run paintComponent, but drawing to image rather than the viewport
		try {
			ImageIO.write(image, fileExtension, outputFile); // Write the image file
		} catch (IOException e) {
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), "There was a problem saving the image; you might not have space to save the image.");
		}
	}
}
