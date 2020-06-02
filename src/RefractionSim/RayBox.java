package RefractionSim;
import java.awt.Color;

/**
 * Class for ray boxes
 * @author William Platt
 *
 */
public class RayBox extends Object3D {
	
	private String label = "Ray box"; // Default ray box label is "Ray box"
	private Beam lightBeam;
	private boolean localPitchInverted;
	
	/**
	 * Constructor for the RayBox class that generates the ray box's geometry, sets its colour, position and orientation and creates the light beam with geometry
	 */
	public RayBox() {
		super(new Mesh(Mesh.Primitive.CUBE), new Color(200, 200, 200)); // The ray box is a cube with a light grey object colour
		mesh.scale(0.5, 0.5, 0.5); // Shrink the ray box
		this.lightBeam = new Beam(new Color(200, 20, 20), 0.015); // Set the ray box's light beam as a new Beam with a red colour and radius of 0.015 units (3 in the beam thickness text field)
		Vector newOrigin = new Vector(3);
		newOrigin.setElement(2, -5); // heading: pi, pitch: 0
		this.setOrigin(newOrigin);
		this.orbit((Math.random() - 0.5) * 2 * Math.PI, 0); // Set a random heading
		localPitchInverted = false; // Ray box is not upside down
	}
	
	/**
	 * Sets the origin of both the ray box and light beam
	 * @param orgin the new origin for the ray box and light beam
	 * @throws IllegalArgumentException if origin is not a 3-row vector
	 */
	@Override
	public void setOrigin(Vector origin) {
		this.origin = origin;
		lightBeam.setOrigin(origin);		
	}
	
	/**
	 * Returns the label of the ray box
	 * @return the label of the ray box
	 */
	public String getLabel() {
		return this.label;
	}
	
	/**
	 * Sets the ray box's label to the newLabel parameter
	 * @param newLabel the new label for the ray box
	 */
	public void setLabel(String newLabel) {
		this.label = newLabel;
	}
	
	/**
	 * Returns the light beam for the ray box
	 * @return the light beam for the ray box
	 */
	public Beam getLightBeam() {
		return lightBeam;
	}
	
	/**
	 * Returns whether angles are set to be visible for the ray box/light beam
	 * @return whether or not angle visibility is on
	 */
	public boolean getAnglesVisible() {
		return lightBeam.getAnglesVisible();
	}
	
	/**
	 * Sets the visibility of angles for the ray box/light beam
	 * @param showAngles whether or not angles should be displayed for the ray box/light beam
	 */
	public void setAnglesVisible(boolean showAngles) {
		lightBeam.setAnglesVisible(showAngles);
	}
	
	/**
	 * Returns the thickness of the ray box's light beam between 1 and 10 inclusive
	 * @return the thickness of the light beam
	 */
	public int getBeamThickness() {
		return (int)(lightBeam.getRadius() * 200);
	}
	
	/**
	 * Sets the radius of the light beam from a thickness value between 1 and 10 inclusive
	 * @param newThickness
	 */
	public void setBeamThickness(int newThickness) {
		lightBeam.setRadius(newThickness * 0.005);
	}
	
	/**
	 * Returns whether or not the ray box is upside down, meaning that the effect of a change in the local pitch slider is negated
	 * @return whether or not the local pitch slider is inverted for the ray box
	 */
	public boolean isLocalPitchInverted() {
		return localPitchInverted;
	}
	
	/**
	 * Toggles whether or not the local pitch slider is inverted for the ray box
	 */
	public void toggleLocalPitchInverted() {
		localPitchInverted = !localPitchInverted;
	}
	
	/**
	 * Rotates both the ray box and light beam about their origins by the angular displacement represented by the rotation matrix
	 * @param rotation a 3 by 3 matrix representing the angular displacement of the new orientation from the old one
	 * @throws IllegalArgumentException if rotation is not a 3 by 3 matrix; the matrix should also represent a rotation, although this will not throw an exception
	 */
	@Override
	public void rotate(Matrix rotation) {
		if ((rotation.getM() != 3) || (rotation.getN() != 3)) {
			throw new IllegalArgumentException("A 3 by 3 matrix is needed to rotate an object");
		} else {
			this.orientation = rotation.multiply(this.orientation);
			lightBeam.setOrientation(this.orientation);
		}
	}
	
	/**
	 * Rotates both the ray box and light beam about their origins by the heading and pitch angles
	 * @param heading the angle of rotation clockwise about the world's y-axis
	 * @param pitch the angle of rotation about the ray box's x-axis
	 */
	public void rotate(double heading, double pitch) {
		Matrix verticalRot = new Matrix(3, 3);
		verticalRot.setToRotation(this.orientation.getVector(0), pitch);
		Matrix horizontalRot = new Matrix(3, 3);
		Vector verticalAxis = new Vector(3);
		verticalAxis.setElements(new double[] {0, 1, 0});
		horizontalRot.setToRotation(verticalAxis, heading);
		rotate(verticalRot);
		rotate(horizontalRot);
	}
	
	/**
	 * Rotates both the ray box and light beam about the world's origin by the heading and pitch angles. Unlike the orbit method of Object3D this doesn't assume that the ray box is facing the world's origin
	 * @param heading the angle of rotation clockwise about the world's y-axis
	 * @param pitch the angle of rotation clockwise about the horizontal vector perpendicular to the vector from the origin to the ray box if the ray box had a y co-ordinate of 0
	 */
	public void orbitAboutOrigin(double heading, double pitch) {
		Matrix verticalRot = new Matrix(3, 3);
		Vector pitchAxis = new Vector(3);
		pitchAxis.setElements(new double[] {origin.getElement(0), 0, origin.getElement(2)}); // Vector from the world's origin to the ray box's origin if the ray box was in the horizontal plane
		verticalRot.setElements(new double[] {0, 0, 1,   0, 1, 0,   -1, 0, 0}); // Represents a rotation pi/2 radians anticlockwise about the world's y-axis
		pitchAxis = verticalRot.multiply(pitchAxis); // Horizontal vector perpendicular to the horizontal vector from the world's origin to the ray box's origin
		verticalRot.setToRotation(pitchAxis, pitch);
		Matrix horizontalRot = new Matrix(3, 3);
		Vector verticalAxis = new Vector(3);
		verticalAxis.setElements(new double[] {0, 1, 0}); // World's y-axis
		horizontalRot.setToRotation(verticalAxis, heading);
		setOrigin(horizontalRot.multiply(verticalRot.multiply(this.origin)));
		this.rotate(verticalRot);
		this.rotate(horizontalRot);
	}
	
}
