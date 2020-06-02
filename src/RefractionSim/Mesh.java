package RefractionSim;

/**
 * Class for the geometries of objects based on the object3D class and its descendant classes
 * @author William Platt
 *
 */
public class Mesh {
	
	private int[][] faces; // All faces are triangles; the first index defines a triangle and the second defines a vertex number
	private Vector[] verts; // Each vector in this array has 3 rows
	private Vector[] normals;
	private double[] ds; // ds[i] is the d value for faces[i] where d = p.n (n is the normal to a plane and p is a point in that plane)
	private Vector[] boxVerts; // A list of the vertices for the smallest box that will contain all of the mesh's vertices (the box is aligned to the object space axes)
	
	/**
	 * An enumerated type that specifies the shapes for which the Mesh class can generate geometry
	 * @author William Platt
	 *
	 */
	public enum Primitive {
		// Calls to Primitive(shape) passing a String for shape
		CUBE("Cube"), CUBOID("Cuboid"), TRIANGULAR_PRISM("Triangular prism"), SPHERE("Sphere"), CONVEX_LENS("Convex lens"), CONCAVE_LENS("Concave lens"), HALF_CYLINDER("Half-cylinder");
		
		private String shape;
		
		/**
		 * Stores the user-friendly name of the shape
		 * @param shape the string representation of the shape
		 */
		private Primitive(String shape) {
			this.shape = shape;
		}
		
		/**
		 * Returns the user-friendly name of the shape
		 * @return the string representation of the shape
		 */
		public String toString() {
			return shape;
		}
		
	}
	
	/**
	 * A constructor for the mesh class for non-primitive geometries
	 * @param faces a list of the faces of the object; each item/face contains 3 items which are the indices of the vertices in the verts list that make up the face
	 * @param verts a list of vertices; each vertex is represented by a position in 3-D space
	 */
	public Mesh(int[][] faces, Vector[] verts) { // Will be read in from a file
		this.faces = faces;
		this.verts = verts;
		normals = new Vector[faces.length]; // One normal for each face
		ds = new double[faces.length]; // One d value for each face
		for (int i = 0; i < faces.length; i++) {
			normals[i] = this.normal(faces[i]); // Calculate the normal to the face
			ds[i] = verts[faces[i][0]].dotProduct(normals[i]); // Calculate the d value for the face
		}
		calcBoxVerts(); // Calculates and stores the vertices of the arbitrarily orientated bounding box
	}
	
	/**
	 * A constructor for the mesh class for primitive geometries
	 * @param shape the shape represented by the new mesh
	 * @throws IllegalArgumentException if shape is null
	 */
	public Mesh(Primitive shape) {
		switch (shape) {
			case CUBE:
				generateCube();
				break;
			case CUBOID:
				generateCube();
				scale(2, 1, 1); // Stretch the cube along the x-axis to give a cuboid
				break;
			case TRIANGULAR_PRISM:
				generatePrism();
				break;
			case SPHERE:
				generateSphere();
				break;
			case CONVEX_LENS:
				generateSphere();
				scale(0.6, 2, 2); // Increase the size (to allow a better demonstration of how the lens works) and then squash the sphere along the x-axis
				break;
			case CONCAVE_LENS:
				generateSphere();
				scale(0.6, 2, 2); // Increase the size and squash along the x-axis
				
				for (int i = 0; i < verts.length; i++) {
					if (verts[i].getElement(0) < -0.0001) { // Don't move points in the middle of the x-axis
						verts[i].setElement(0, verts[i].getElement(0) + 0.8); // Move points on the left to the right. This part still bulges out to the left, but when it is on the right it is concave
					} else if (verts[i].getElement(0) > 0.0001) {
						verts[i].setElement(0, verts[i].getElement(0) - 0.8); // Move points on the right to the left.
					}
				}
				// The faces are 'inside out', so the ordering of the vertices must be reversed in order to make the normals point the correct way
				for (int i = 0; i < faces.length; i++) {
					// Swap the first and last vertices (0 and 2)
					int temp = faces[i][0];
					faces[i][0] = faces[i][2];
					faces[i][2] = temp;
				}
				break;
			case HALF_CYLINDER:
				generateHalfCylinder();
				break;
			default:
				throw new IllegalArgumentException("Mesh constructor cannot take a null primitive");
		}
		normals = new Vector[faces.length];
		ds = new double[faces.length];
		for (int i = 0; i < faces.length; i++) {
			normals[i] = normal(faces[i]); // Calculate the normal for the face
			ds[i] = verts[faces[i][0]].dotProduct(normals[i]); // Calculate the value of d for the face
		}
		calcBoxVerts(); // Calculate and store the vertices of the AOBB
	}
	
	/**
	 * A constructor for the Mesh class for primitive geometries where the shape needs to be determined from its name
	 * @param shape the name/String representation of the shape
	 */
	public Mesh(String shape) {
		this(primitiveFromStr(shape)); // Call the other constructor for primitive geometries
	}
	
	/**
	 * Returns a primitive shape as a Primitive object from its name/String representation
	 * @param shape the name of the shape
	 * @return the shape represented by the name
	 */
	public static Primitive primitiveFromStr(String shape) {
		shape = shape.trim();
		switch (shape) {
			case "Cube":
				return Primitive.CUBE;
			case "Cuboid":
				return Primitive.CUBOID;
			case "Triangular prism":
				return Primitive.TRIANGULAR_PRISM;
			case "Sphere":
				return Primitive.SPHERE;
			case "Convex lens":
				return Primitive.CONVEX_LENS;
			case "Concave lens":
				return Primitive.CONCAVE_LENS;
			case "Half-cylinder":
				return Primitive.HALF_CYLINDER;
			default:
				return null;
		}
	}
	
	/**
	 * Creates the vertices and faces that define a cube of side length 2 units
	 */
	private void generateCube() {
		verts = new Vector[] {new Vector(3), new Vector(3), new Vector(3), new Vector(3), new Vector(3), new Vector(3), new Vector(3), new Vector(3)};
		verts[0].setElement(0, -1);
		verts[0].setElement(1, -1);
		verts[0].setElement(2, -1);
		
		verts[1].setElement(0, 1);
		verts[1].setElement(1, -1);
		verts[1].setElement(2, -1);
		
		verts[2].setElement(0, -1);
		verts[2].setElement(1, -1);
		verts[2].setElement(2, 1);
		
		verts[3].setElement(0, 1);
		verts[3].setElement(1, -1);
		verts[3].setElement(2, 1);
		
		verts[4].setElement(0, -1);
		verts[4].setElement(1, 1);
		verts[4].setElement(2, -1);
		
		verts[5].setElement(0, 1);
		verts[5].setElement(1, 1);
		verts[5].setElement(2, -1);
		
		verts[6].setElement(0, -1);
		verts[6].setElement(1, 1);
		verts[6].setElement(2, 1);
		
		verts[7].setElement(0, 1);
		verts[7].setElement(1, 1);
		verts[7].setElement(2, 1);
		
		faces = new int[][] {{0, 3, 2}, {0, 1, 3}, {0, 4, 5}, {0, 5, 1}, {0, 2, 6}, {0, 6, 4}, {2, 7, 6}, {2, 3, 7}, {3, 1, 5}, {3, 5, 7}, {4, 7, 5}, {4, 6, 7}}; // List the vertices of each face by index in verts. Vertices must be listed in clockwise order from outside of the shape so that the faces pointing away from the camera can be culled or shaded differently
	}
	
	/**
	 * Creates the vertices and faces that define a triangular prism with sides of length 2
	 */
	private void generatePrism() {
		double halfAltitude = Math.sin(Math.PI / 3); // The cross-section is an equilateral triangle with sides of length 2 units. The altitude is the height of the triangle with one edge horizontal
		verts = new Vector[] {new Vector(3), new Vector(3), new Vector(3), new Vector(3), new Vector(3), new Vector(3)};
		verts[0].setElements(new double[] {-1, -halfAltitude, -1});
		verts[1].setElements(new double[] {0, halfAltitude, -1});
		verts[2].setElements(new double[] {1, -halfAltitude, -1});
		// Use the same triangle of vertices but offset by 2 units along the z-axis
		verts[3].setElements(verts[0]);
		verts[4].setElements(verts[1]);
		verts[5].setElements(verts[2]);
		verts[3].setElement(2, 1);
		verts[4].setElement(2, 1);
		verts[5].setElement(2, 1);
		
		faces = new int[][] {{0, 1, 2}, {0, 5, 3}, {0, 2, 5}, {0, 3, 4}, {0, 4, 1}, {1, 4, 5}, {1, 5, 2}, {3, 5, 4}};
	}
	
	/**
	 * Creates the vertices and faces that define an approximation of a sphere with radius 1
	 */
	private void generateSphere() {
		int segments = 14;
		int rings = 15; // Use an odd number of rings of faces so that halfway up the sphere is the middle of a ring and not a loop of edges
		verts = new Vector[segments * (rings - 1) + 2]; // There are rings + 1 rings of vertices, but the first and last of these are each a single vertex
		faces = new int[2 * segments * (rings - 1)][3]; // Apart from the first and last, each ring has segments number of square faces, so 2 * segments triangular faces. The first and last each have segments triangular faces
		verts[0] = new Vector(3);
		verts[0].setElement(1, -1); // The lowest point of the sphere
		for (int i = 0; i < segments; i++) {
			if (i == segments - 1) {
				faces[i] = new int[] {0, i + 1, 1}; // The last face involves the last vertex in the second ring and loops back to the first vertex in the second ring
			} else {
				faces[i] = new int[] {0, i + 1, i + 2}; // Triangles involving the lowest vertex and two consecutive vertices in the second ring of vertices
			}
		}
		double pitchIncrement = Math.PI / rings; // The increment in pitch (angle above horizontal) between rings of vertices
		double pitch = pitchIncrement - Math.PI / 2; // The lowest point had a pitch of -pi/2
		double headingIncrement = Math.PI * 2.0 / segments; // The increment in heading between segments
		double heading = -Math.PI;
		for (int r = 0; r < rings - 1; r++) { // Last ring is a single point and must be treated separately
			double y = Math.sin(pitch); // The y co-ordinate for each vertex in this ring
			double modulus = Math.cos(pitch); // The radius of the circle which this ring lies on
			for (int s = 0; s < segments; s++) {
				double x = modulus * Math.cos(heading); // x co-ordinate for the next vertex
				double z = modulus * Math.sin(heading); // z co-ordinate for the next vertex
				verts[segments * r + s + 1] = new Vector(3);
				verts[segments * r + s + 1].setElements(new double[] {x, y, z});
				heading += headingIncrement;
			}
			// Make faces between the vertices just added and the next ring of vertices to be added
			if (r != rings - 2) { // The second to last ring doesn't make faces with the next ring up in the same way because the last ring is a single vertex
				for (int i = 0; i < segments; i++) {
					if (i == segments - 1) { // The last two faces make use of the first vertex in the next ring by looping back to the start
						// Two faces in the same plane
						faces[i * 2 + segments * (2 * r + 1)] = new int[] {segments * r + i + 1, (segments * r + i + 1) + segments, segments * r + 1 + segments};
						faces[i * 2 + segments * (2 * r + 1) + 1] = new int[] {segments * r + i + 1, segments * r + 1 + segments, segments * r + 1};
					} else {
						// Two faces that are in the same plane and appear as a quadrilateral
						faces[i * 2 + segments * (2 * r + 1)] = new int[] {segments * r + i + 1, (segments * r + i + 1) + segments, (segments * r + i + 1) + segments + 1};
						faces[i * 2 + segments * (2 * r + 1) + 1] = new int[] {segments * r + i + 1, (segments * r + i + 1) + segments + 1, (segments * r + i + 1) + 1};
					}
				}
			}
			pitch += pitchIncrement;
		}
		verts[verts.length - 1] = new Vector(3);
		verts[verts.length - 1].setElement(1, 1); // The last and highest vertex
		for (int i = 0; i < segments; i++) {
			if (i == segments - 1) { // Last face completes the ring and includes the last vertex of the second to last ring
				faces[2 * segments + segments * (2 * rings - 5) + i] = new int[] {segments * (rings - 2) + 1 + i, segments * (rings - 1) + 1, segments * (rings - 2) + 1};
			} else { // Faces involving the last vertex and two consecutive vertices in the second to last ring
				faces[2 * segments + segments * (2 * rings - 5) + i] = new int[] {segments * (rings - 2) + 1 + i, segments * (rings - 1) + 1, segments * (rings - 2) + 1 + i + 1};
			}
		}
	}
	
	/**
	 * Creates the vertices and faces that define the approximation of a cylinder of radius 1 and height 2 that has been cut in vertically in half
	 */
	private void generateHalfCylinder() {
		int segments = 32;
		verts = new Vector[segments * 2];
		faces = new int[4 * segments - 4][3];
		double heading = 0;
		double headingIncrement = Math.PI / (segments - 1); // The increment in heading between segments of vertices
		for (int s = 0; s < segments; s++) {
			double x = Math.cos(heading); // x co-ordinate of points on the segment
			double z = Math.sin(heading); // z co-ordinate of points on the segment
			verts[s] = new Vector(3);
			verts[s].setElements(new double[] {x, -1, z}); // Vertex on the bottom semi-circle
			verts[s + segments] = new Vector(3);
			verts[s + segments].setElements(new double[] {x, 1, z}); // Vertex on the top semi-circle
			heading += headingIncrement;
		}
		for (int i = 0; i < segments - 1; i++) { // Vertical faces approximating the curved surface
			faces[i * 2] = new int[] {i, i + segments, i + segments + 1}; // Face involving a point on the bottom semi-circle, the point directly above it (top semi-circle and the same segment) and the point directly above and one segment across
			faces[i * 2 + 1] = new int[] {i, i + segments + 1, i + 1}; // Face involving a point on the bottom semi-circle, the point above and one segment across and the point one segment across on the bottom semi-circle
		}
		for (int i = 0; i < segments - 2; i++) { // Horizontal faces approximating the semi-circles at the top and bottom
			faces[segments * 2 - 2 + i] = new int[] {0, i + 1, i + 2}; // For the bottom semi-circle, the first vertex connected to the (i + 1)th vertex and the (i + 2)th vertex
			faces[segments * 2 - 2 + i + segments - 2] = new int[] {segments, segments + i + 2, segments + i + 1}; // The same as above but for the top semi-circle
		}
		// Faces representing the vertical square cross-section
		faces[4 * segments - 6] = new int[] {0, segments * 2 - 1, segments}; // The first vertex, the last vertex and the one above the first
		faces[4 * segments - 5] = new int[] {0, segments - 1, segments * 2 - 1}; // The first vertex, the last vertex on the bottom and the last vertex (on the top)
	}
	
	/**
	 * Stretches the geometry parallel to the object space axes
	 * @param xScale the scale factor of enlargement parallel to the x-axis
	 * @param yScale the scale factor of enlargement parallel to the y-axis
	 * @param zScale the scale factor of enlargement parallel to the z-axis
	 */
	public void scale(double xScale, double yScale, double zScale) {
		// Create a matrix that will transform vertices to their new positions
		Matrix scaleMatrix = new Matrix(3, 3);
		scaleMatrix.setElement(0, 0, xScale);
		scaleMatrix.setElement(1, 1, yScale);
		scaleMatrix.setElement(2, 2, zScale);
		for (int i = 0; i < verts.length; i++) {
			verts[i] = scaleMatrix.multiply(verts[i]);
		}
	}
	
	/**
	 * Returns a list of the vertices for the mesh's arbitrarily orientated bounding box (AOBB)
	 * @return the AOBB vertices for the geometry
	 */
	public Vector[] getBoxVerts() {
		return boxVerts;
	}
	
	/**
	 * Calculates the vertices for the mesh's AOBB
	 */
	private void calcBoxVerts() {
		if (verts != null) {
			double minX = verts[0].getElement(0);
			double maxX = minX;
			double minY = verts[0].getElement(1);
			double maxY = minY;
			double minZ = verts[0].getElement(2);
			double maxZ = minZ;
			for (int i = 1; i < verts.length; i++) {
				if (verts[i].getElement(0) < minX) {
					minX = verts[i].getElement(0);
				} else if (verts[i].getElement(0) > maxX) {
					maxX = verts[i].getElement(0);
				}
				if (verts[i].getElement(1) < minY) {
					minY = verts[i].getElement(1);
				} else if (verts[i].getElement(1) > maxY) {
					maxY = verts[i].getElement(1);
				}
				if (verts[i].getElement(2) < minZ) {
					minZ = verts[i].getElement(2);
				} else if (verts[i].getElement(2) > maxZ) {
					maxZ = verts[i].getElement(2);
				}
			}
			Vector[] boxVerts = new Vector[8];
			boxVerts[0] = new Vector(3);
			boxVerts[0].setElements(new double[] {minX, minY, minZ});
			boxVerts[1] = new Vector(3);
			boxVerts[1].setElements(new double[] {maxX, minY, minZ});
			boxVerts[2] = new Vector(3);
			boxVerts[2].setElements(new double[] {minX, minY, maxZ});
			boxVerts[3] = new Vector(3);
			boxVerts[3].setElements(new double[] {maxX, minY, maxZ});
			boxVerts[4] = new Vector(3);
			boxVerts[4].setElements(new double[] {minX, maxY, minZ});
			boxVerts[5] = new Vector(3);
			boxVerts[5].setElements(new double[] {maxX, maxY, minZ});
			boxVerts[6] = new Vector(3);
			boxVerts[6].setElements(new double[] {minX, maxY, maxZ});
			boxVerts[7] = new Vector(3);
			boxVerts[7].setElements(new double[] {maxX, maxY, maxZ});
			this.boxVerts = boxVerts;
		} else {
			this.boxVerts = null;
		}
	}
	
	/**
	 * Returns the list of faces
	 * @return the list of faces; each face is a list of 3 integers which are indices for the list of vertices
	 */
	public int[][] getFaces() {
		return this.faces;
	}
	
	/**
	 * Returns the list of vertices
	 * @return the list of vertices
	 */
	public Vector[] getVerts() {
		return this.verts;
	}
	
	/**
	 * Returns the list of normals
	 * @return a list of normals corresponding to the faces with the same subscripts
	 */
	public Vector[] getNormals() {
		return this.normals;
	}
	
	/**
	 * Returns the list of values for d
	 * @return a list of the d values corresponding to the faces with the same indices
	 */
	public double[] getDs() {
		return this.ds;
	}
	
	/**
	 * Calculates and returns the normalised normal to face; the normal is in the direction the face is 'facing', which is the direction from which the face's vertices are listed in clockwise order
	 * @param face the face which the normal needs to be calculated for
	 * @return the unit length normal to face
	 */
	private Vector normal(int[] face) {
		Vector point0 = verts[face[0]];
		Vector point1 = verts[face[1]];
		Vector point2 = verts[face[2]];
		return point1.subtract(point0).crossProduct(point2.subtract(point0)).normalise();
	}
}
