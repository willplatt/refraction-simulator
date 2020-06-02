package RefractionSim;
import java.awt.Color;

/**
 * General class for any object that exists in 3-D space such as the camera
 * @author William Platt
 *
 */
public class Object3D {
	protected int ID = -1; // Has no ID until added to the viewport's object list, so is set to -1
	protected Mesh mesh;
	protected Color color;
	protected Matrix orientation = new Matrix(3, 3); // Represents the object to upright transformation
	protected Vector origin = new Vector(3);
	protected Vector[] boxVerts; // Vertices of the arbitrarily orientated bounding box (AOBB)
	
	/**
	 * Constructor for the Object3D class
	 * @param mesh the geometry of the 3-D object (null if there isn't any)
	 * @param color the colour of the 3-D object (irrelevant if the mesh is null and may also be null in this case)
	 */
	public Object3D(Mesh mesh, Color color) {
		this.mesh = mesh;
		this.color = color;
		double[] elements = {1, 0, 0,   0, 1, 0,   0, 0, 1}; // 3 by 3 identity matrix
		this.orientation.setElements(elements); // Set the default orientation to that of the world
		if (mesh != null) { // Objects such as the camera may not have a mesh and won't need an AOBB
			boxVerts = mesh.getBoxVerts();
		} else {
			boxVerts = new Vector[0]; // Empty list of Vectors
		}
	}
	
	/**
	 * Sets the value of ID to newID
	 * @param newID the new value for ID
	 */
	public void setID(int newID) {
		this.ID = newID;
	}
	
	/**
	 * Returns ID, the index of the object in the viewport's objectList
	 * @return the object's ID
	 */
	public int getID() {
		return this.ID;
	}
	
	/**
	 * Returns the geometry of the object; null if there is no geometry
	 * @return the geometry of the object
	 */
	public Mesh getMesh() {
		return this.mesh;
	}
	
	/**
	 * Returns the a list of the vertices of the object's arbitrarily orientated bounding box (AOBB)
	 * @return the vertices of the object's bounding box
	 */
	public Vector[] getBoxVerts() {
		return this.boxVerts;
	}
	
	/**
	 * Returns the object's colour
	 * @return the colour of the object
	 */
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * Moves the object in 3-D space by the displacement vector
	 * @param displacement 3-row vector representing movement in the world's x, y and z directions
	 * @throws IllegalArgumentException if displacement is not a 3-row vector
	 */
	public void displace(Vector displacement) {
		if (displacement.getN() > 3) {
			throw new IllegalArgumentException("Displacements must be in three dimensions or fewer");
		} else {
			for (int i = 0; i < displacement.getN(); i++) {
				this.origin.setElement(i, this.origin.getElement(i) + displacement.getElement(i));
			}
		}
	}
	
	/**
	 * Returns the location of the object's origin which other points are measured relative to in object space
	 * @return the location of the object's origin in world space
	 */
	public Vector getOrigin() {
		return this.origin;
	}
	
	/**
	 * Moves the object to a new position
	 * @param origin the new position of the object's origin in world space
	 * @throws IllegalArgumentException if origin is not a 3-row vector
	 */
	public void setOrigin(Vector origin) {
		if (origin.getN() != 3) {
			throw new IllegalArgumentException("The origin of an object must be a 3-D vector");
		} else  {
			this.origin = origin;
		}
	}
	
	/**
	 * Rotates an object about its origin; the vertices' co-ordinates aren't changed, but the object's basis vectors (columns of the orientation matrix) are changed
	 * @param rotation a 3 by 3 matrix representing the angular displacement of the new orientation from the old one
	 * @throws IllegalArgumentException if rotation is not a 3 by 3 matrix; the matrix should also represent a rotation, although this will not throw an exception
	 */
	public void rotate(Matrix rotation) {
		if ((rotation.getM() != 3) || (rotation.getN() != 3)) {
			throw new IllegalArgumentException("A 3 by 3 matrix is needed to rotate an object");
		} else {
			this.orientation = rotation.multiply(this.orientation);
		}
	}
	
	/**
	 * Returns the matrix representing the orientation of the object relative to world/upright space
	 * @return the 3 by 3 matrix representing the object's orientation relative to world/upright space
	 */
	public Matrix getOrientation() {
		return this.orientation;
	}
	
	/**
	 * Sets the orientation of the object to the 
	 * @param orientation a 3 by 3 matrix representing an orientation
	 * @throws IllegalArgumentException if the matrix is not 3 by 3; it should also represent a rotation, although this will not throw an exception
	 */
	public void setOrientation(Matrix orientation) {
		if ((orientation.getM() != 3) && (orientation.getN() != 3)) {
			throw new IllegalArgumentException("The orientation of an object must be a 3 by 3 matrix");
		} else {
			this.orientation = orientation;
		}
	}
	
	/**
	 * Rotates the object about the origin of world space; the object's origin and rotation are affected. This method assumes the object to be facing the world's origin
	 * @param heading the angle of rotation clockwise around the world's y-axis
	 * @param pitch the angle of rotation clockwise around the object's x-axis
	 */
	public void orbit(double heading, double pitch) {
		Matrix verticalRot = new Matrix(3, 3);
		verticalRot.setToRotation(this.orientation.getVector(0), pitch); // Rotation about the object's x-axis in world space
		Matrix horizontalRot = new Matrix(3, 3);
		Vector verticalAxis = new Vector(3);
		verticalAxis.setElements(new double[] {0, 1, 0}); // World's y-axis
		horizontalRot.setToRotation(verticalAxis, heading);
		// Rotate about the world origin without changing the object's orientation
		setOrigin(horizontalRot.multiply(verticalRot.multiply(this.origin)));
		// Rotate about the object's origin (changing the orientation and not the origin)
		this.rotate(verticalRot);
		this.rotate(horizontalRot);
	}
	
}
