package RefractionSim;

/**
 * Class for rays; lines with a starting point and a direction
 * @author William Platt
 *
 */
public class Ray {
	
	private Vector p;
	private Vector v;
	
	/**
	 * Constructor for the Ray class
	 * @param p the starting point of the ray
	 * @param v the direction of the ray
	 */
	public Ray(Vector p, Vector v) {
		this.p = p;
		this.v = v;
	}
	
	/**
	 * Returns the starting point of the ray
	 * @return the point where the ray starts
	 */
	public Vector getP() {
		return p;
	}
	
	/**
	 * Returns the direction the ray extends from its starting point
	 * @return the direction of the ray
	 */
	public Vector getV() {
		return v;
	}
	
}
