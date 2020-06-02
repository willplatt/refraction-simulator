package RefractionSim;
/**
 * Class for mathematical functions which are not already provided in Math.
 * @author William Platt
 *
 */
public class Math2 {
	
	/**
	 * The constructor for the Math2 class is private because there aren't supposed to be any instances of this
	 * class. The purpose of this class is the provide publicly accessible static methods like the Math class
	 */
	private Math2() {}
	
	/**
	 * Returns an angle (in radians) from a vertical component and a horizontal component. All vectors in the plane
	 * can be given a proper angle by this function; Math.atan limits angles to the interval (-pi/2, pi/2)
	 * @param y the vertical component of a 2-D vector
	 * @param x the horizontal component of a 2-D vector
	 * @return the angle (in radians) of a vector to the positive x-axis under standard mathematical conventions
	 */
	public static double atan(double y, double x) {
		if (x == 0) {
			if (y == 0) {
				return 0;
			} else if (y > 0) {
				return (Math.PI / 2);
			} else {
				return (-Math.PI / 2);
			}
		} else if (x > 0) {
			return Math.atan(y / x);
		} else {
			if (y >= 0) {
				return (Math.atan(y / x) + Math.PI);
			} else {
				return (Math.atan(y / x) - Math.PI);
			}
		}
	}
	
	/**
	 * Returns the midpoint of the line segment between p0 and p1 in n-dimensional space
	 * @param p0 one end of a line segment
	 * @param p1 the other end of the line segment
	 * @return the point on the line between p0 and p1 that is halfway between the two points
	 */
	public static Vector midpoint(Vector p0, Vector p1) {
		if (p0.getN() != p1.getN()) {
			throw new IllegalArgumentException("A line segment must be between two points in the same number of dimensions");
		} else {
			Vector midpoint = new Vector(p0.getN());
			for (int i = 0; i < p0.getN(); i++) {
				midpoint.setElement(i, (p0.getElement(i) + p1.getElement(i)) / 2);
			}
			return midpoint;
		}
	}
	
}
