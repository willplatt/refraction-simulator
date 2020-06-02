package RefractionSim;
import java.awt.Color;

/**
 * Class for light beams
 * @author William Platt
 *
 */
public class Beam extends Object3D {
	
	private Vector[] points = new Vector[100];
	private int numOfPoints = 0;
	private double[] angles = new double[196]; // 2 angles for every point other than the first and last
	private Vector[] anglePositions = new Vector[196]; // Position in 3-D space at which to write angles when displaying them in the viewport
	private int numOfAngles = 0;
	private double radius;
	private boolean anglesVisible;
	
	/**
	 * Constructor for the Beam class that sets its colour, radius, default position and orientation, and sets angles to be displayed in the viewport
	 * @param color the colour of the light beam (the beam will be a solid colour and not have shading to imitate lighting)
	 * @param radius half of the width of the square cross-section of the beam
	 */
	public Beam(Color color, double radius) {
		super(null, color); // Create the light beam as a 3-D object with no geometry/mesh
		this.orientation = new Matrix(3, 3); // heading: 0, pitch: 0
		this.origin = new Vector(3); // [0, 0, 0]
		this.radius = radius;
		this.anglesVisible = true;
	}
	
	/**
	 * Sets the colour of the beam to newColor
	 * @param newColor the new colour for the beam
	 */
	public void setColor(Color newColor) { // getColor is already defined by the parent class
		this.color = newColor;
	}
	
	/**
	 * Recalculates the beam's path and regenerates its geometry
	 */
	public void update() {
		calculateRays();
		generateMesh();
	}
	
	/**
	 * Returns half of the width of the square cross-section of the beam
	 * @return half of the width of the square cross-section of the beam
	 */
	public double getRadius() {
		return radius;
	}
	
	/**
	 * Sets the width of the square cross-section of the beam to be 2 * newRadius and regenerates the geometry of the beam
	 * @param newRadius half of the width of the new square cross-section of the beam
	 */
	public void setRadius(double newRadius) {
		radius = newRadius;
		generateMesh();
	}
	
	/**
	 * Returns whether or not angles are set to be visible for the light beam
	 * @return if angles are to be visible
	 */
	public boolean getAnglesVisible() {
		return anglesVisible;
	}
	
	/**
	 * Sets whether or not angles should be displayed for the light beam
	 * @param showAngles if angles are to be visible
	 */
	public void setAnglesVisible(boolean showAngles) {
		anglesVisible = showAngles;
	}
	
	/**
	 * Returns the list of angles in order from the ray box
	 * @return the list of angles between the beam and the surface normals in order from the ray box
	 */
	public double[] getAngles() {
		return angles;
	}
	
	/**
	 * Returns the list of positions in 3-D space for each of the angles in order from the ray box
	 * @return the list of positions for the angles in the same order as the list of angles
	 */
	public Vector[] getAnglePositions() {
		return anglePositions;
	}
	
	/**
	 * Returns the number of angles in the list of angles
	 * @return the number of angles in the list of angles
	 */
	public int getNumOfAngles() {
		return numOfAngles;
	}
	
	/**
	 * Generates the geometry of the beam if the rays have been calculated
	 */
	private void generateMesh() {
		int[][] faces = new int[(numOfPoints - 1) * 8][3]; // Between every square of vertices (every point) there are 4 square surfaces each comprised of 2 triangular faces
		Vector[] verts = new Vector[4 * numOfPoints]; // There is a square of vertices at each point
		Vector displace0 = new Vector(3);
		displace0.setElements(new double[] {-radius, radius, 0}); // Displacement of the top left vertex of a square from the point where the ray and face intersect
		Vector displace1 = new Vector(3);
		displace1.setElements(new double[] {radius, radius, 0}); // Displacement of the top right vertex
		int j = 0; // Vertex counter
		int k = 0; // Face counter
		Matrix rotation = orientation.transpose();
		for (int i = 0; i < numOfPoints; i++) {
			Vector centerPoint = rotation.multiply((points[i].subtract(origin))); // Map point from world space to object space
			verts[j] = centerPoint.add(displace0); // Create top left vertex
			verts[j+1] = centerPoint.add(displace1); // Create top right vertex
			verts[j+2] = centerPoint.subtract(displace0); // Create bottom right vertex
			verts[j+3] = centerPoint.subtract(displace1); // Create bottom left vertex
			if (i > 0) { // Create faces between the vertices just created and the last square of vertices
				// For each face, list vertices in clockwise order when looking at the face from outside of the beam
				faces[k] = new int[] {j-4, j, j+1};
				faces[k+1] = new int[] {j-4, j+1, j-3};
				
				faces[k+2] = new int[] {j-3, j+1, j+2};
				faces[k+3] = new int[] {j-3, j+2, j-2};
				
				faces[k+4] = new int[] {j-2, j+2, j+3};
				faces[k+5] = new int[] {j-2, j+3, j-1};
				
				faces[k+6] = new int[] {j-1, j+3, j};
				faces[k+7] = new int[] {j-1, j, j-4};
				k += 8;
			}
			j += 4;
		}
		this.mesh = new Mesh(faces, verts); // Set the faces and vertices as the mesh of the beam so that it can be rendered
		this.boxVerts = this.mesh.getBoxVerts(); // Store the vertices of the bounding box for the new mesh
	}
	
	/**
	 * Calculates the path of the beam as a sequence of rays and stores the points in 3-D space where the path switches between rays and the angles of rays to surface normals
	 */
	private void calculateRays() {
		numOfPoints = 0;
		numOfAngles = 0;
		Vector p = this.origin; // Starting point of the beam in world space
		Vector v = this.orientation.getVector(2); // Initial direction of the beam in world space
		Ray currentRay = new Ray(p, v);
		Target target = (Target)(Viewport.getObjectList()[1]);
		double[] refractiveIndices = Viewport.getRefractiveIndices();
		double targetIndexRelToWorld = refractiveIndices[target.getMaterial()] / refractiveIndices[Viewport.getWorldMaterial()];
		double criticalAngle;
		if (targetIndexRelToWorld > 1) {
			criticalAngle = Math.asin(1 / targetIndexRelToWorld);
		} else {
			criticalAngle = Math.asin(targetIndexRelToWorld);
		}
		int i = 0;
		do {
			points[i] = currentRay.getP(); // Store the starting point of the ray
			v = currentRay.getV();
			currentRay = calcNextRay(currentRay, targetIndexRelToWorld, criticalAngle); // Calculate the next ray based on the current one. Null is returned if the current ray doesn't intersect any faces of the target object
			i++;
		} while ((currentRay != null) && (i < points.length - 1)); // Repeat until the beam carries on to infinity without hitting a boundary between media or no more points can be stored (given that one more point is added after this loop)
		if (i == 1) { // The beam never hit the target object
			points[i] = points[i - 1].add(v.scale(10)); // Continue the beam in along its original line for 10 units
		} else {
			points[i] = points[i - 1].add(v.scale(8)); // Continue the beam along the line of the last ray for 8 units
		}
		numOfPoints = i + 1; // i started at 0
	}
	
	/**
	 * Calculates and returns the next ray of the beam based on the intersection of the current ray and the target object
	 * @param incidentRay the last ray that was calculated
	 * @param targetIndexRelToWorld the refractive index of the target material relative to the world
	 * @param criticalAngle the minimum angle from the normal needed for total internal reflection within the denser material
	 * @return the next ray which the beam follows
	 */
	private Ray calcNextRay(Ray incidentRay, double targetIndexRelToWorld, double criticalAngle) {
		Target target = (Target)(Viewport.getObjectList()[1]);
		Mesh mesh = target.getMesh();
		int[][] faces = mesh.getFaces();
		Vector[] verts = mesh.getVerts();
		Vector[] normals = mesh.getNormals();
		double[] ds = mesh.getDs(); // The equation of a plane is p.n = d where p is a point in the plane and n is the normal to the plane; ds is a list containing the value of d for each face
		Vector p = incidentRay.getP(); // The starting point of the current ray
		Vector v = incidentRay.getV(); // The direction of the current ray
		Vector n;
		double d;
		double vMultiple = -1; // The displacement of the closest point of intersection so far from p in terms of v; it remains -1 until an intersection is found
		int faceIntersected = -1; // The face of the target that is first intersected by the current ray; it remains -1 until an intersection is found
		Vector finalPoint = new Vector(3);
		for (int i = 0; i < faces.length; i++) { // Iterate through all of the target's faces and check if the current ray intersects the face
			n = normals[i]; // Normal for the current face
			d = ds[i]; // Value of d for the current face
			double nDotP = n.dotProduct(p);
			double nDotV = n.dotProduct(v); // The compononet of v in the direction of n
			// Go to the next face if the value of lambda will not be positive (meaning the face is in the opposite direction to v from p)
			if (nDotP < d) {
				if (nDotV <= 0) {
					continue;
				}
			} else if (nDotP == d) {
				continue;
			} else if (nDotV >= 0) {
				continue;
			}
			double lambda = (d - nDotP) / nDotV; // d - nDotP gives the shortest distance from p to the plane, so lambda is the number of times p must be displaced by v to be in the plane of the face
			if (lambda > 0.0001) { // Prevent a beam interacting with the same face twice consecutively due to floating point error
				if ((vMultiple == -1) || (lambda < vMultiple)) { // If there have been no intersections so far or the distance to the plane in terms of v is less than the shortest found so far
					// The three edges of the face
					Edge2D edge0;
					Edge2D edge1;
					Edge2D edge2;
					double intersectX;
					double intersectY;
					Vector pointInPlane = p.add(v.scale(lambda)); // Point where the current ray intersects the face
					// Orthographicallly project the plane into 2-D
					if (n.getElement(2) == 0) { // The face is not tilted forwards or backwards
						if (n.getElement(0) == 0) { // The face is horizontal, so x and z co-ordinates can be used without the vertices becoming colinear in two dimensions
							// In this projection, x co-ordinates remain x co-ordinates and z co-ordinates become y co-ordinates
							edge0 = new Edge2D(verts[faces[i][0]].getElement(0), verts[faces[i][0]].getElement(2), verts[faces[i][1]].getElement(0), verts[faces[i][1]].getElement(2));
							edge1 = new Edge2D(verts[faces[i][1]].getElement(0), verts[faces[i][1]].getElement(2), verts[faces[i][2]].getElement(0), verts[faces[i][2]].getElement(2));
							edge2 = new Edge2D(verts[faces[i][2]].getElement(0), verts[faces[i][2]].getElement(2), verts[faces[i][0]].getElement(0), verts[faces[i][0]].getElement(2));
							intersectX = pointInPlane.getElement(0); // x --> x
							intersectY = pointInPlane.getElement(2); // z --> y
						} else { // The face is not titled forwards/backwards but is tilted left/right, so z and y co-ordinates can be used
							// z --> x, y --> y
							edge0 = new Edge2D(verts[faces[i][0]].getElement(2), verts[faces[i][0]].getElement(1), verts[faces[i][1]].getElement(2), verts[faces[i][1]].getElement(1));
							edge1 = new Edge2D(verts[faces[i][1]].getElement(2), verts[faces[i][1]].getElement(1), verts[faces[i][2]].getElement(2), verts[faces[i][2]].getElement(1));
							edge2 = new Edge2D(verts[faces[i][2]].getElement(2), verts[faces[i][2]].getElement(1), verts[faces[i][0]].getElement(2), verts[faces[i][0]].getElement(1));
							intersectX = pointInPlane.getElement(2); // z --> x
							intersectY = pointInPlane.getElement(1); // y --> y
						}
					} else { // We can use x and y co-ordinates and the vertices won't become colinear
						// x --> x, y --> y
						edge0 = new Edge2D(verts[faces[i][0]].getElement(0), verts[faces[i][0]].getElement(1), verts[faces[i][1]].getElement(0), verts[faces[i][1]].getElement(1));
						edge1 = new Edge2D(verts[faces[i][1]].getElement(0), verts[faces[i][1]].getElement(1), verts[faces[i][2]].getElement(0), verts[faces[i][2]].getElement(1));
						edge2 = new Edge2D(verts[faces[i][2]].getElement(0), verts[faces[i][2]].getElement(1), verts[faces[i][0]].getElement(0), verts[faces[i][0]].getElement(1));
						intersectX = pointInPlane.getElement(0); // x --> x
						intersectY = pointInPlane.getElement(1); // y --> y
					}
					// Find the tallest of the edges (greatest change in y)
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
					if ((tallEdge.getY0() != shortEdge0.getY0()) || (tallEdge.getX0() != shortEdge0.getX0())) { // shortEdge0 should share the lowest point with tallEdge
						if ((tallEdge.getY0() == shortEdge0.getY0()) && (tallEdge.getX0() == shortEdge0.getX1())){
							
						} else {
							Edge2D temp = shortEdge0;
							shortEdge0 = shortEdge1;
							shortEdge1 = temp;
						}
					}
					
					boolean pointInFace = false;
					if (intersectY > shortEdge0.getY1()) { // The ray passes through the top part of the face if at all
						// Check that the intersection of the ray and plane is between the long edge and top edge
						double dxTall = (tallEdge.getX1() - tallEdge.getX0()) / tallEdge.getHeight(); // Increase in x for the tallest edge when y increases by 1
						double dxShort = (shortEdge1.getX1() - shortEdge1.getX0()) / shortEdge1.getHeight(); // Increase in x for shortEdge1 when y increases by 1
						double yFromTop = intersectY - tallEdge.getY1(); // Change in y from the top of the intersection to the top of the face
						double xTall = yFromTop * dxTall + tallEdge.getX1(); // x co-ordinate of the tallest edge when its y co-ordinate is that of the point of intersection
						double xShort = yFromTop * dxShort + tallEdge.getX1(); // x co-ordinate of shortEdge1 when its y co-ordinate is that of the point of intersection
						if (dxTall > dxShort) { // If shortEdge1 has a greater gradient than tallEdge, meaning that shortEdge1 is always to the right of tallEdge within the top part of the face
							if ((intersectX <= xShort) && (intersectX >= xTall)) { // If the point of intersection is between the two edges in the top part of the face
								pointInFace = true;
							}
						} else if ((intersectX >= xShort) && (intersectX <= xTall)) { // If the point of intersection is between the two edges in the top part of the face
							pointInFace = true;
						}
					} else { // The ray passes through the bottom part of the face if at all
						// Check that the intersection of the ray and plane is between the long edge and bottom edge
						double dxTall = (tallEdge.getX1() - tallEdge.getX0()) / tallEdge.getHeight(); // Increase in x for the tallest edge when y increases by 1
						double dxShort = (shortEdge0.getX1() - shortEdge0.getX0()) / shortEdge0.getHeight();
						double yFromBottom = intersectY - tallEdge.getY0();
						double xTall = yFromBottom * dxTall + tallEdge.getX0();
						double xShort = yFromBottom * dxShort + tallEdge.getX0();
						if (dxShort > dxTall) { // If tallEdge is always right of shortEdge in the bottom part of the face
							if ((intersectX <= xShort) && (intersectX >= xTall)) { // If the point of intersection is in the bottom part of the face
								pointInFace = true;
							}
						} else if ((intersectX >= xShort) && (intersectX <= xTall)) {
							pointInFace = true;
						}
					}
					if (pointInFace) {
						vMultiple = lambda; // Update the shortest distance (multiple of v) so far
						faceIntersected = i; // Update the closest face intersected so far
						// Update the point of intersection with the closest face so far
						finalPoint.setElement(0, pointInPlane.getElement(0));
						finalPoint.setElement(1, pointInPlane.getElement(1));
						finalPoint.setElement(2, pointInPlane.getElement(2));
					}
				}
			}
		}
		if (faceIntersected == -1) { // If the ray didn't intersect any faces
			return null;
		} else {
			return new Ray(finalPoint, nextVector(v, normals[faceIntersected], targetIndexRelToWorld, criticalAngle, finalPoint)); // Calculate the direction of the next ray and return create a new ray which the method will return
		}
	}
	
	/**
	 * Calculates the direction of the next ray and returns the result as a normalised 3-row vector
	 * @param vector the direction of the current ray
	 * @param normal the vector perpendicular to the face being intersected
	 * @param targetIndexRelToWorld the refractive index of the target material relative to the world
	 * @param criticalAngle the minimum angle between vector and normal within the denser material that would cause total internal reflection
	 * @param intersection the point of intersection with the face in world space
	 * @return the direction (as a unit vector) of the next ray after the one with direction vector that intersects the face with normal normal at the point intersection
	 */
	private Vector nextVector(Vector vector, Vector normal, double targetIndexRelToWorld, double criticalAngle, Vector intersection) {
		vector = vector.normalise(); // Ensure that vector has a length of 1
		Vector incidentAnglePosition;
		normal = normal.normalise(); // Ensure that the normal to the face has unit length
		double vDotN = vector.dotProduct(normal); // v.n = |v||n|cos(x) where x is the angle between v and n. |v| and |n| are both 1 in this case, so v.n = cos(x)
		if (vDotN == 0) { // If the current ray is perpendicular to the face (in the plane of the face)
			return vector; // Treat the ray as not intersecting the face
		} else {
			if (vDotN > 0) { // If the angle between v and n is less than pi/2 radians
				incidentAnglePosition = intersection.subtract(Math2.midpoint(vector.scale(0.3), normal.scale(0.3))); // Set the position of the angle of incidence halfway between vector and normal at about 0.3 units from the point of intersection
			} else {
				incidentAnglePosition = intersection.subtract(Math2.midpoint(vector.scale(0.3), normal.scale(-0.3))); // Use a negative scale factor for normal because it is facing the opposite way to vector
			}
			// There is a 2-D plane containing both vector and normal; working in 2-D is simpler than 3-D. Derive the matrix to rotate the vectors so that they lie horizontally (in the plane y = 0; each vector has its tail at the origin)
			Vector p2 = vector.scale(-1); // Negations such as these are used to ensure the new co-ordinate system is left-handed so that the resulting matrix doesn't represent reflection as well as rotation
			Matrix rotateVecsToYEquals0 = new Matrix(3, 3);
			Vector xBasis = normal.scale(-1); // In the co-ordinate system where vector and normal are in the plane y = 0, the x-axis is -normal
			rotateVecsToYEquals0.setElement(0, 0, xBasis.getElement(0));
			rotateVecsToYEquals0.setElement(0, 1, xBasis.getElement(1));
			rotateVecsToYEquals0.setElement(0, 2, xBasis.getElement(2));
			Vector yBasis = p2.crossProduct(normal).normalise(); // Use the cross product on two points in the plane to give a vector perpendicular to y = 0, the y-axis
			rotateVecsToYEquals0.setElement(1, 0, yBasis.getElement(0));
			rotateVecsToYEquals0.setElement(1, 1, yBasis.getElement(1));
			rotateVecsToYEquals0.setElement(1, 2, yBasis.getElement(2));
			Vector zBasis = yBasis.scale(-1).crossProduct(xBasis).normalise(); // Use the cross-product on two points in the plane z = 0 to give the z-axis
			rotateVecsToYEquals0.setElement(2, 0, zBasis.getElement(0));
			rotateVecsToYEquals0.setElement(2, 1, zBasis.getElement(1));
			rotateVecsToYEquals0.setElement(2, 2, zBasis.getElement(2));
			rotateVecsToYEquals0 = rotateVecsToYEquals0.transpose(); // This inverts an orthogonal matrix such as this. Before doing this the matrix of basis vectors for the new co-ordinate space converts points in this co-ordinate space to world space; we want the reverse
			
			double angle = Math.acos(vDotN); // The angle between vector and normal because they were both normalised
			if (angle > Math.PI / 2) { // If the vectors were in opposite directions (vDotN < 0), then we will have the larger of the two angles between them
				angle = Math.PI - angle; // This gives the desired angle because angles on a straight line sum to pi/2 radians
			}
			addAngle(angle, incidentAnglePosition); // Add an angle to the list of angles and a position to the list of angle positions
			
			vector = rotateVecsToYEquals0.multiply(vector); // Map vector to the new co-ordinate system where it is in the plane y = 0. normal doesn't need mapping because it is along the x-axis
			if (vDotN < 0) { // World to target transition
				
				if (targetIndexRelToWorld > 1) { // Target is the denser material
					vector = refract(vector, targetIndexRelToWorld); // Calculate the next (normalised) vector in the new co-ordinate system
				} else { // World is the denser material
					if (angle >= criticalAngle) {
						vector.setElement(0, -vector.getElement(0)); // Reflect the vector
					} else {
						vector = refract(vector, targetIndexRelToWorld);
					}
				}
			} else { // Target to world transition
				if (targetIndexRelToWorld > 1) { // Target is the denser material
					if (angle >= criticalAngle) {
						vector.setElement(0, -vector.getElement(0)); // Reflect the vector
					} else {
						vector = refract(vector, 1 / targetIndexRelToWorld); // Pass the refractive index of the world relative to the target material
					}
				} else {
					vector = refract(vector, 1 / targetIndexRelToWorld); // Pass the refractive index of the world relative to the target material
				}
			}
			vector = rotateVecsToYEquals0.transpose().multiply(vector); // Map the next vector from the new co-ordinate space to world space
		}
		vDotN = vector.dotProduct(normal); // Cosine of the angle between the new vector and the normal
		Vector finalAnglePosition;
		// Calculate the position of the angle of refraction similarly to incidentAnglePosition
		if (vDotN > 0) {
			finalAnglePosition = intersection.add(Math2.midpoint(vector.scale(0.3), normal.scale(0.3)));
		} else {
			finalAnglePosition = intersection.add(Math2.midpoint(vector.scale(0.3), normal.scale(-0.3)));
		}
		double angle = Math.acos(vDotN);
		if (angle > Math.PI / 2) { // Find the smaller of the two angles between normal and the new vector
			angle = Math.PI - angle;
		}
		addAngle(angle, finalAnglePosition); // Store the angle and its position in 3-D space
		return vector; // Reurn the new vector
	}
	
	/**
	 * Calculates and returns the vector produced by the refraction of incidentVector passing from material A to material B where refractiveIndex is the refractive index of material B relative to material A
	 * @param incidentVector the direction of the previous ray rotated into the plane y = 0 with x-axis -normal
	 * @param refractiveIndex the refractive index of the destination material relative to the source material
	 * @return the direction of the next ray
	 */
	private Vector refract(Vector incidentVector, double refractiveIndex) { // Take the x-axis to be the normal and all y-values should be zero
		// Snell's law: refractive index = sin(i) / sin(r)
		double sinR = incidentVector.getElement(2) / refractiveIndex; // getElement(2) represents the component of incidentVector perpendicular to the normal (sin(i)). sinR represents the component of the new vector perpendicular to the normal
		double cosR = Math.cos(Math.asin(sinR)); // The component of the new vector parallel to the normal
		Vector refractedVector = new Vector(3);
		if (incidentVector.getElement(0) < 0) {
			refractedVector.setElement(0, -cosR); // If the incident ray was going against the direction of the normal, the refracted ray will also be more than pi/2 radians from the direction of the normal
		} else {
			refractedVector.setElement(0, cosR); // If the incident ray was in a similar direction to the normal, the refracted ray will also be in a similar direction to the normal
		}
		refractedVector.setElement(2, sinR); // Set the new vector's z component to sinR (z-axis is perpendicular to x-axis and normal)
		return refractedVector;
	}
	
	/**
	 * Appends an angle to the list of angles and an angle position to the list of angle positions
	 * @param angle the angle to append to the list
	 * @param position the position in 3-D space of the angle to append to the list
	 */
	private void addAngle(double angle, Vector position) {
		angles[numOfAngles] = angle; // ArrayIndexOutOfBoundsException should be avoided because the number of points is limited
		anglePositions[numOfAngles] = position; // Exception avoided here also
		numOfAngles++;
	}
}
