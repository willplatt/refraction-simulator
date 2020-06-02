package RefractionSim;
/**
 * Class for Euler angle triples (these represent an orientation in 3-D space)
 * @author William Platt
 *
 */
public class EulerTriple {
	
	private double heading; // Rotation about the vertical (y) axis
	private double pitch; // Rotation about the object space x-axis after applying the heading rotation
	private double bank; // Rotation about the object space z-axis after applying the heading and pitch rotations
	
	/**
	 * Constructor for the EulerTriple class which sets the three angles to the three parameters
	 * @param heading the rotation about the vertical (y) axis
	 * @param pitch the rotation about the object space x-axis after applying the heading rotation
	 * @param bank the rotation about the object space z-axis after applying the heading and pitch rotations
	 */
	public EulerTriple(double heading, double pitch, double bank) {
		// Ensure angles are in canonical form (-pi <= heading <= pi, -halfPi <= pitch <= halfPi, -pi <= bank <= pi)
		double pi = Math.PI;
		double twoPi = pi * 2;
		double halfPi = pi / 2;
		
		if (Math.abs(pitch) > halfPi) {
			pitch += halfPi;
			pitch = pitch % twoPi;
			if (pitch > pi) {
				heading += pi;
				pitch = (3 * pi / 2) - pitch;
			} else {
				pitch -= halfPi;
			}
		}
		if (Math.abs(pitch) >= 0.99999 * halfPi) { // If there is gimbal lock, assign all rotation to pitch and bank
			bank += heading;
			heading = 0;
		} else {
			if (Math.abs(heading) > pi) {
				heading += pi;
				heading = heading % twoPi;
				heading -= pi;
			}
		}
		if (Math.abs(bank) > pi) {
			bank += pi;
			bank = bank % twoPi;
			bank -= pi;
		}
		this.heading = heading;
		this.pitch = pitch;
		this.bank = bank;
	}
	
	/**
	 * Returns the heading angle
	 * @return the angle of rotation about the vertical axis
	 */
	public double getHeading() {
		return heading;
	}
	
	/**
	 * Returns the pitch angle
	 * @return the angle of declination
	 */
	public double getPitch() {
		return pitch;
	}
	
	/**
	 * Returns the bank angle
	 * @return the angle of rotation along the body z-axis
	 */
	public double getBank() {
		return bank;
	}
	
	/**
	 * Returns the matrix for transforming points from object space to upright space where the EulerTriple is the angular displacement of object space from upright space
	 * @return the object space to upright space matrix represented by the EulerTriple
	 */
	public Matrix matrixObToUp() {
		Matrix objectToUpright = new Matrix(3, 3);
		double ch = Math.cos(this.heading);
		double sh = Math.sin(this.heading);
		double cp = Math.cos(this.pitch);
		double sp = Math.sin(this.pitch);
		double cb = Math.cos(this.bank);
		double sb = Math.sin(this.bank);
		double chcb = ch * cb;
		double shsb = sh * sb;
		double shcb = sh * cb;
		double chsb = ch * sb;
		objectToUpright.setElement(0, 0, chcb + shsb * sp);
		objectToUpright.setElement(0, 1, sb * cp);
		objectToUpright.setElement(0, 2, chsb * sp - shcb);
		objectToUpright.setElement(1, 0, shcb * sp - chsb);
		objectToUpright.setElement(1, 1, cb * cp);
		objectToUpright.setElement(1, 2, shsb + chcb * sp);
		objectToUpright.setElement(2, 0, sh * cp);
		objectToUpright.setElement(2, 1, -sp);
		objectToUpright.setElement(2, 2, ch * cp);
		return objectToUpright;
	}
	
	/**
	 * Returns the matrix for transforming points from upright space to object space where the EulerTriple is the angular displacement of object space from upright space
	 * @return the upright space to object space matrix represented by the EulerTriple
	 */
	public Matrix matrixUpToOb() {
		return this.matrixObToUp().transpose(); // The transpose is the same as the inverse for rotation matrices
	}
	
	/**
	 * Returns the sum of the EulerTriple for which this method is called and the toAdd parameter
	 * @param toAdd representation of an angular displacement to add to the angular displacement represented by the EulerTriple for which this method is called
	 * @return the EulerTriple representing the sum of the two angular displacements
	 */
	public EulerTriple add(EulerTriple toAdd) {
		return new EulerTriple(this.heading + toAdd.heading, this.pitch + toAdd.pitch, this.bank + toAdd.bank);
	}
	
}
