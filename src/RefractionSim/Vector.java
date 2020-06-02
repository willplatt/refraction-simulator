package RefractionSim;
/**
 * Class for vectors of all kinds. All vectors are column vectors because matrices are column-major.
 * @author William Platt
 *
 */
public class Vector {
	private int n; // Size of vector (number of rows or number of elements)
	private double[] vec; // Elements of the vector
	private double modulus; /* The 'length' or 'magnitude' of a vector (or mathematically, the euclidean norm or
								2-norm) */
	private boolean modulusKnown; /* States whether the value stored in modulus is correct; the modulus will
									will change when the vector is changed but we don't want to recalculate the
									modulus unless we have to */
	
	/**
	 * Constructor for the Vector class which sets all elements to zero
	 * @param n number of elements that the new vector is to have
	 * @throws IllegalArgumentException if n is less than or equal to 2 (as this would produce nothing or a single
	 * value)
	 */
	public Vector(int n) {
		if (n <= 1) {
			throw new IllegalArgumentException("A vector needs at least two rows");
		} else {
			this.n = n;
			this.vec = new double[n];
			for (int i = 0; i < n; i++) {
				this.vec[i] = 0;
			}
			this.modulus = 0;
			this.modulusKnown = true;
		}
	}
	
	/**
	 * Returns the number of rows the matrix has
	 * @return the number of rows the matrix has
	 */
	public int getN() {
		return n;
	}
	
	/**
	 * Sets the value of a single element in the vector to that of the newValue parameter
	 * @param i the index (row) of the element to change (indices start at 0)
	 * @param newValue the value which the specified element should be changed to
	 * @throws ArrayIndexOutOfBoundsException if i >= number of elements (rows)
	 */
	public void setElement(int i, double newValue) {
		if (i >= this.n) {
			throw new ArrayIndexOutOfBoundsException("Vector element does not exist");
		} else {
			this.vec[i] = newValue;
			modulusKnown = false;
		}
	}
	
	/**
	 * Sets all elements of the vector to the values in the newValues array
	 * @param newValues the array of new values that the vector elements should be set to
	 * @throws IllegalArgumentException if the length of the array is not equal to the number of elements in the
	 * vector
	 */
	public void setElements(double[] newValues) {
		if (newValues.length != this.n) {
			throw new IllegalArgumentException("Array argument of different length to vector");
		} else {
			for (int i = 0; i < this.n; i++) {
				this.vec[i] = newValues[i];
			}
			modulusKnown = false;
		}
	}
	
	/**
	 * Copies the values of the elements of the vector newValues to the corresponding elements in the vector for
	 * which this method is being called (the two vectors then don't reference the same locations)
	 * @param newValues the vector of new values the vector elements should be set to
	 * @throws IllegalArgumentException if the newValues parameter is not the same length as the vector for which
	 * this method is called
	 */
	public void setElements(Vector newValues) {
		try {
			this.setElements(newValues.vec);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Vector size mismatch");
		}
	}
	
	/**
	 * Gets the value of a single element in the vector
	 * @param i the index (row) of the element to return the value of
	 * @return the value of the specified element
	 * @throws ArrayIndexOutOfBoundsException if i >= number of elements or i < 0
	 */
	public double getElement(int i) {
		if ((i >= this.n) || (i < 0)) {
			throw new ArrayIndexOutOfBoundsException("Vector element does not exist");
		} else {
			return this.vec[i];
		}
	}
	
	/**
	 * Returns the values of all elements in an array
	 * @return the array of values in the vector
	 */
	public double[] getElements() {
		double[] elements = new double[this.n];
		for (int i = 0; i < this.n; i++) {
			elements[i] = this.vec[i];
		}
		return elements;
	}
	
	/**
	 * Returns the sum of this vector and the toAdd parameter vector
	 * @param toAdd the vector to add to the vector for which this method is being called
	 * @return the sum of this vector and the vector passed as a parameter (the return vector will be the same size
	 * as both vectors being added)
	 * @throws IllegalArgumentException if the toAdd vector is not the same size as the vector for which this method
	 * is being called
	 */
	public Vector add(Vector toAdd) {
		if (this.n != toAdd.n) {
			throw new IllegalArgumentException("Vectors size mismatch for addition");
		} else {
			Vector result = new Vector(this.n);
			for (int i = 0; i < this.n; i++) {
				result.setElement(i, this.vec[i] + toAdd.vec[i]); // Set element sets result.modulusKnown to false
			}
			return result;
		}
	}
	
	/**
	 * Returns a - b where a is the vector for which this method is called and b is the toSubtract parameter vector
	 * @param toSubtract vector to subtract from the vector for which this method is called
	 * @return a - b where a is the vector for which this method is called and b is the toSubtract parameter vector
	 * @throws IllegalArgumentException if the two vectors don't have the same dimensions
	 */
	public Vector subtract(Vector toSubtract) {
		if (this.n != toSubtract.n) {
			throw new IllegalArgumentException("Vectors size mismatch for subtraction");
		} else {
			// Scaling toSubtract by -1 and adding it to `this` is avoided in order to reduce computation time
			Vector result = new Vector(this.n);
			for (int i = 0; i < this.n; i++) {
				result.setElement(i, this.vec[i] - toSubtract.vec[i]);
			}
			return result;
		}
	}
	
	/**
	 * Returns the dot product of the vector for which this method is called and the toDot parameter vector. The dot
	 * product is associative, meaning that a.b = b.a
	 * @param toDot the vector to dot with
	 * @return the dot product of the two vectors (representing |a||b|cos(x) where a and b are the two vectors and
	 * x is the angle between them - this is the same as the component of a parallel to b multiplied by |a|)
	 */
	public double dotProduct(Vector toDot) {
		if (this.n != toDot.n) {
			throw new IllegalArgumentException("Vectors size mismatch for dot product");
		} else {
			double result = 0;
			for (int i = 0; i < this.n; i++) {
				result += this.vec[i] * toDot.vec[i];
			}
			return result;
		}
	}
	
	/**
	 * Returns the vector multiplied by a scalar value
	 * @param scaleFactor the scalar value by which the vector is to be multiplied
	 * @return the vector produced when the original vector is multiplied by scaleFactor
	 */
	public Vector scale(double scaleFactor) {
		Vector result = new Vector(this.n);
		for (int i = 0; i < this.n; i++) {
			result.vec[i] = this.vec[i] * scaleFactor;
		}
		if (this.modulusKnown) {
			result.modulus = this.modulus * scaleFactor; /* result.modulusKnown will already be true because result
															was just constructed */
		} else {
			result.modulusKnown = false;
		}
		return result;
	}
	
	/**
	 * Returns a x b (a crossed with b) where a is the vector for which this method is being called and b is the
	 * toCross parameter vector. Both a and b must have 3 elements (rows) and the cross product will also have 3.
	 * The cross product represents a vector perpendicular to both vectors with modulus |a||b|sin(x) where x is the
	 * angle between the vectors a and b
	 * @param toCross the 3-row vector b in a x b where a is the vector for which this method is being called
	 * @return a vector with 3 elements (rows) which represents a x b (a crossed with b) a is the vector for which
	 * this method is being called and b is the toCross parameter vector
	 */
	public Vector crossProduct(Vector toCross) {
		if ((this.n != 3) || (toCross.n != 3)) {
			throw new IllegalArgumentException("Two vectors must have exactly 3 rows in order to be crossed");
		} else {
			Vector result = new Vector(3);
			result.setElement(0, this.vec[1] * toCross.vec[2] - this.vec[2] * toCross.vec[1]); /* setElement sets
																								result.modulusKnown
																								to false */
			result.setElement(1, this.vec[2] * toCross.vec[0] - this.vec[0] * toCross.vec[2]);
			result.setElement(2, this.vec[0] * toCross.vec[1] - this.vec[1] * toCross.vec[0]);
			return result;
		}
	}
	
	/**
	 * Returns the modulus (a.k.a. length, euclidean norm or 2-norm) of the vector
	 * @return the modulus/length/euclidean norm/2-norm of the vector
	 */
	public double modulus() {
		if (this.modulusKnown) {
			return this.modulus;
		} else {
			double result = 0;
			for (int i = 0; i < this.n; i++) {
				result += Math.pow(this.vec[i], 2);
			}
			result = Math.sqrt(result);
			this.modulus = result;
			this.modulusKnown = true;
			return result;
		}
	}
	
	/**
	 * Returns true if the vector is normalised (has modulus 1). Some leeway is acceptable for the vector to be
	 * considered normalised
	 * @return whether or not the vector is normalised
	 */
	public boolean isNormalised() {
		if ((this.modulus() < 1.00000001) && (this.modulus() > 0.99999999)) { /* Consider a vector with modulus between
																		1.0001 and 0.9999 inclusive to be normalised
																		(this is the convention throughout this
																		project) */
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the normalised version of the original vector (meaning it has modulus 1 with slight leeway)
	 * @return the normalised version of the original vector
	 */
	public Vector normalise() {
		if (this.isNormalised()) {
			return this;
		} else {
			return this.scale(1 / this.modulus());
		}
	}
}
