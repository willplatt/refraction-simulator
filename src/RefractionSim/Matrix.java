package RefractionSim;
/**
 * Class for two-dimensional column-major (each column is a vector) matrices
 * @author William Platt
 *
 */
public class Matrix {
	private int m; // Number of columns
	private int n; // Number of rows
	private double[][] mat; // Elements of the matrix
	private double det; // The determinant of the matrix
	private boolean detKnown; /* States whether the value stored in det is correct; the determinant will change
								when the matrix is changed but we don't want to recalculate the determinant unless
								we have to. This will be false for a non-square matrix because it won't have a
								determinant */
	
	/**
	 * Constructor for the Matrix class which sets all elements in the new matrix to zero
	 * @param m number of columns that the new matrix is to have
	 * @param n number of rows that the new matrix is to have
	 * @throws IllegalArgumentException if either m or n is less than 2 (as this would produce a vector or single
	 * value)
	 * */
	public Matrix(int m, int n) {
		if ((m < 2) || (n < 2)) {
			throw new IllegalArgumentException("Matrix is not two-dimensional");
		} else {
			this.m = m;
			this.n = n;
			this.mat = new double[m][n];
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					this.mat[i][j] = 0;
				}
			}
			this.det = 0;
			if (m == n) {
				this.detKnown = true;
			}
			this.detKnown = false;
		}
	}
	
	/**
	 * Returns the number of columns the matrix has
	 * @return the number of columns the matrix has
	 */
	public int getM() {
		return m;
	}
	
	/**
	 * Returns the number of rows the matrix has
	 * @return the number of rows the matrix has
	 */
	public int getN() {
		return n;
	}
	
	/**
	 * Sets the value of a single element in the matrix to that of the newValue parameter
	 * @param i the column of the element to change (indices start at 0)
	 * @param j the row of the element to change (indices start at 0)
	 * @param newValue the value which the specified element should be changed to
	 * @throws ArrayIndexOutOfBoundsException if i >= number of columns or j >= number of rows
	 */
	public void setElement(int i, int j, double newValue) {
		if ((i >= this.m) || (j >= this.n) || (i < 0) || (j < 0)) {
			throw new ArrayIndexOutOfBoundsException("Matrix element does not exist");
		} else {
			this.mat[i][j] = newValue;
			this.detKnown = false;
		}
	}
	
	/**
	 * Sets all elements of the matrix to the values in the newValues array (the array represents the matrix with
	 * columns joined end-to-end)
	 * @param newValues the array of new values that the matrix elements should be set to; the matrix elements are
	 * changed down the columns starting with the leftmost column
	 * @throws IllegalArgumentException if the length of the array is not equal to the number of elements in the
	 * matrix
	 */
	public void setElements(double[] newValues) {
		if (newValues.length != this.m * this.n) {
			throw new IllegalArgumentException("Array argument of different length to number of matrix elements");
		} else {
			for (int i = 0; i < this.m; i++) {
				for (int j = 0; j < this.n; j++) {
					this.mat[i][j] = newValues[i * this.n + j];
				}
			}
			this.detKnown = false;
		}
	}
	
	/**
	 * Copies the values of the elements of the matrix newValues to the corresponding elements in the matrix for
	 * which this method is being called (the two matrices then do not reference the same memory locations)
	 * @param newValues the matrix of new values that the matrix elements should be set to
	 * @throws IllegalArgumentException if the newValues matrix does not have the same dimensions as the matrix for
	 * which this method is being called
	 */
	public void setElements(Matrix newValues) {
		if ((newValues.m != this.m) || (newValues.n != this.n)) {
			throw new IllegalArgumentException("Matrices size mismatch");
		} else {
			for (int i = 0; i < this.m; i++) {
				for (int j = 0; j < this.n; j++) {
					this.mat[i][j] = newValues.mat[i][j];
				}
			}
			if (newValues.detKnown) {
				this.detKnown = true;
				this.det = newValues.det;
			} else {
				this.detKnown = false;
			}
		}
	}
	
	/**
	 * Sets the elements of the 3 by 3 matrix to represent an a specified 3-D angular displacement (rotation by a specific amount about a vector in 3-D space)
	 * @param axis the 3-D vector/axis about which to rotate
	 * @param angle the number of radians by which to rotate
	 * @throws IllegalArgumentException if the matrix is not 3 by 3 or if the axis is not a 3-row vector
	 */
	public void setToRotation(Vector axis, double angle) {
		if ((this.m != 3) || (this.n != 3)) {
			throw new IllegalArgumentException("A non-3 by 3 matrix cannot be made a rotation matrix");
		} else if (axis.getN() != 3) {
			throw new IllegalArgumentException("A 3-D axis is needed to construct a 3-D rotation matrix");
		} else {
			axis = axis.normalise();
			// Store values needed multiple times so as to reduce the number of calculations
			double x = axis.getElement(0);
			double y = axis.getElement(1);
			double z = axis.getElement(2);
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double oneMinusCos = 1 - cos;
			double xsin = x * sin;
			double ysin = y * sin;
			double zsin = z * sin;
			double xy = x * y * oneMinusCos;
			double xz = x * z * oneMinusCos;
			double yz = y * z * oneMinusCos;
			// Apply the general formula for a 3-D matrix rotating about an arbitrary axis
			this.setElement(0, 0, x * x * oneMinusCos + cos);
			this.setElement(0, 1, xy + zsin);
			this.setElement(0, 2, xz - ysin);
			this.setElement(1, 0, xy - zsin);
			this.setElement(1, 1, y * y * oneMinusCos + cos);
			this.setElement(1, 2, yz + xsin);
			this.setElement(2, 0, xz + ysin);
			this.setElement(2, 1, yz - xsin);
			this.setElement(2, 2, z * z * oneMinusCos + cos);
		}
	}
	
	/**
	 * Gets the value of a single element in the matrix
	 * @param i the column of the element to return (indices start at 0)
	 * @param j the row of the element to return (indices start at 0)
	 * @return the value of the specified element
	 * @throws ArrayIndexOutOfBoundsException if i >= number of columns or j >= number of rows or i or j < 0
	 */
	public double getElement(int i, int j) {
		if ((i >= this.m) || (j >= this.n) || (i < 0) || (j < 0)) {
			throw new ArrayIndexOutOfBoundsException("Matrix element does not exist");
		} else {
			return this.mat[i][j];
		}
	}
	
	/**
	 * Returns the values of all elements in a one-dimensional array where the matrix's columns have been joined
	 * end-to-end
	 * @return the array of values in the matrix where the columns have been joined end-to-end
	 */
	public double[] getElements() {
		double[] elements = new double[this.m * this.n];
		for (int i = 0; i < this.m; i++) {
			for (int j = 0; j < this.n; j++) {
				elements[i * this.n + j] = this.mat[i][j];
			}
		}
		return elements;
	}
	
	/**
	 * Returns a column of the matrix as a Vector object (this program uses column-vectors)
	 * @param i the index of the column to return as a Vector object (indices start at zero)
	 * @return a Vector object representing the specified column of the matrix
	 * @throws IllegalArgumentException if a the specified column does not exist
	 */
	public Vector getVector(int i) {
		if (i >= this.m) {
			throw new IllegalArgumentException("Matrix column does not exist");
		} else {
			Vector result = new Vector(this.n);
			result.setElements(this.mat[i]);
			return result;
		}
	}
	
	/**
	 * Returns the sum of this matrix and the toAdd parameter matrix
	 * @param toAdd the two-dimensional matrix to add to the matrix for which this method is being called
	 * @return the sum of this matrix and the matrix passed as a parameter (the returned matrix will be the same
	 * size as both matrices being added)
	 * @throws IllegalArgumentException if the toAdd matrix is not the same size as the matrix for which this method
	 * is being called
	 */
	public Matrix add(Matrix toAdd) {
		if ((this.m != toAdd.m) || (this.n != toAdd.n)) {
			throw new IllegalArgumentException("Matrices size mismatch for addition");
		} else {
			Matrix result = new Matrix(this.m, this.n);
			for (int i = 0; i < this.m; i++) {
				for (int j = 0; j < this.n; j++) {
					result.setElement(i, j, this.mat[i][j] + toAdd.mat[i][j]);
				}
			}
			return result;
		}
	}
	
	/**
	 * Returns A - B where A is the matrix for which this method is called and B is the toSubtract parameter matrix
	 * @param toSubtract the matrix to subtract from the matrix for which this method is called
	 * @return A - B where A is the matrix for which this method is called and B is the toSubtract parameter matrix
	 * @throws IllegalArgumentException if the two matrices don't have the same dimensions
	 */
	public Matrix subtract(Matrix toSubtract) {
		if ((this.m != toSubtract.m) || (this.n != toSubtract.n)) {
			throw new IllegalArgumentException("Matrices size mismatch for subtraction");
		} else {
			// Scaling toSubtract by -1 and adding it to `this` is avoided in order to reduce computation time
			Matrix result = new Matrix(this.m, this.n);
			for (int i = 0; i < this.m; i++) {
				for (int j = 0; j < this.n; j++) {
					result.setElement(i, j, this.mat[i][j] - toSubtract.mat[i][j]);
				}
			}
			return result;
		}
	}
	
	/**
	 * Returns the matrix AB, where A is the matrix for which this method is being called and B is the toMultiply
	 * parameter matrix
	 * @param toMultiply the matrix to post-multiply by (toMultiply is pre-multiplied by the matrix for which this
	 * method is being called)
	 * @return the matrix AB, where A is the matrix for which this method is being called and B is the toMultiply
	 * parameter
	 * @throws IllegalArgumentException if the number of rows in the toMultiply matrix does not equal the number of
	 * columns in the matrix for which this method is being called
	 */
	public Matrix multiply(Matrix toMultiply) {
		if (this.m != toMultiply.n) {
			throw new IllegalArgumentException("Matrices size mismatch for multiplication");
		} else {
			Matrix result = new Matrix(toMultiply.m, this.n);
			for (int thisRow = 0; thisRow < this.n; thisRow++) {
				for (int toMultiplyCol = 0; toMultiplyCol < toMultiply.m; toMultiplyCol++) {
					double sum = 0;
					for (int thisCol = 0; thisCol < this.m; thisCol++) {
						sum += this.mat[thisCol][thisRow] * toMultiply.mat[toMultiplyCol][thisCol];
					}
					result.mat[toMultiplyCol][thisRow] = sum;
				}
			}
			if ((this.detKnown) && (toMultiply.detKnown)) {
				result.det = this.det * toMultiply.det;
			} else {
				result.detKnown = false;
			}
			return result;
		}
	}
	
	/**
	 * Returns the vector AB where A is the matrix for which this method is being called and B is the toMultiply
	 * parameter vector
	 * @param toMultiply the vector to post-multiply by (toMultiply is pre-multiplied by the matrix for which this
	 * method is being called)
	 * @return the vector AB, where A is the matrix for which this method is being called and B is the toMultiply
	 * parameter
	 * @throws IllegalArgumentException if the number of rows in the toMultiply vector does not equal the number of
	 * columns in the matrix for which this method is being called
	 */
	public Vector multiply(Vector toMultiply) {
		if (this.m != toMultiply.getN()) {
			throw new IllegalArgumentException("Matrix/Vector size mismatch for multiplication");
		} else {
			Vector result = new Vector(this.n);
			for (int thisRow = 0; thisRow < this.n; thisRow++) {
				double sum = 0;
				for (int thisCol = 0; thisCol < this.m; thisCol++) {
					sum += this.mat[thisCol][thisRow] * toMultiply.getElement(thisCol);
				}
				result.setElement(thisRow, sum);
			}
			return result;
		}
	}
	
	/**
	 * Returns the matrix multiplied by a scalar value
	 * @param scaleFactor the scalar value by which the matrix is to be multiplied
	 * @return the matrix produced when the original matrix is multiplied by scaleFactor
	 */
	public Matrix scale(double scaleFactor) {
		Matrix result = new Matrix(this.m, this.n);
		for (int i = 0; i < this.m; i++) {
			for (int j = 0; j < this.n; j++) {
				result.mat[i][j] = this.mat[i][j] * scaleFactor;
			}
		}
		if (this.m == this.n) {
			if (this.detKnown) {
				result.det = this.det * Math.pow(scaleFactor, this.m);
			} else {
				result.detKnown = false;
			}
		}
		return result;
	}
	
	/**
	 * Returns the inverse of the matrix (if A is the original matrix and B is the inverse, AB = BA = I where I is
	 * the identity matrix (all elements are zero except for the diagonal from top left to bottom right on which the
	 * elements have the value 1)
	 * @return the inverse of the matrix
	 * @throws Exception if the matrix is not square (the number of columns equals the number of rows if a matrix
	 * is square) or the matrix has no inverse
	 */
	public Matrix inverse() throws Exception {
		if (this.m != this.n) {
			throw new Exception("Non-square matrices have no inverse");
		} else {
			Matrix inverse = new Matrix(this.m, this.n);
			double determinant;
			if (this.m == 2) { // Method for 2 by 2 matrices
				if (!this.detKnown) {
					this.det = this.det2By2();
					this.detKnown = true;
				}
				determinant = this.det;
				if (determinant == 0) { // Preventing runtime error of dividing by zero
					throw new Exception("Singular matrix has no inverse");
				} else {
					inverse.mat[0][0] = this.mat[1][1];
					inverse.mat[1][1] = this.mat[0][0];
					inverse.mat[0][1] = - this.mat[0][1];
					inverse.mat[1][0] = - this.mat[1][0];
					inverse = inverse.scale(1 / determinant);
					System.out.println("Determinant:" + determinant);
				}
			} else {
				Matrix cofactors = this.cofactors();
				if (!this.detKnown) {
					this.det = this.detFromCofactors(cofactors);	/* More efficient than det() because the
																		required cofactors have already been
																		calculated */
					this.detKnown = true;
				}
				determinant = this.det;
				if (determinant == 0) {
					throw new Exception("Singular matrix has no inverse");
				} else {
					inverse = cofactors.transpose().scale(1 / determinant);	/* Find the adjugate and scale by the
																			reciprocal of the determinant */
				}
			}
			return inverse;
		}
	}
	
	/**
	 * Returns the transpose of the matrix in which the columns of the original become the rows of the transpose and
	 * the rows of the original become the columns of the transpose (equivalent to reflecting the elements in the
	 * diagonal through the top left and bottom right elements of the matrix
	 * @return the transpose of the matrix
	 */
	public Matrix transpose() {
		Matrix transpose = new Matrix(this.n, this.m);	/* Number of cols of transpose equals number of rows of
															original and vice versa */
		for (int i = 0; i < this.m; i++) {
			for (int j = 0; j < this.n; j++) {
				transpose.mat[j][i] = this.mat[i][j];	// Rows become columns and columns become rows
			}
		}
		transpose.det = this.det; /* If the transpose is not square, transpose.detKnown will be false anyway and
									this.det will be the default value, so transpose.det will not change */
		return transpose;
	}
	
	/**
	 * Returns the determinant of the matrix
	 * @return the determinant of the matrix
	 * @throws IllegalArgumentException if the matrix is not square (the number of columns equals the number of rows
	 * if a matrix is square)
	 */
	public double det() throws IllegalArgumentException {
		if (this.m == this.n) {
			if (!this.detKnown) {
				this.detKnown = true;
				this.det = this.expand();
			}
			return this.det;
		} else {
			throw new IllegalArgumentException("Rectangular matrices have no determinant");
		}
	}
	
	/**
	 * Returns the matrix of cofactors corresponding to the matrix for which this method is being called
	 * @return the matrix of cofactors corresponding to the matrix for which this method is being called
	 */
	private Matrix cofactors() {
		Matrix cofactors = new Matrix(this.m, this.n);
		for (int i = 0; i < this.m; i++) {
			for (int j = 0; j < this.n; j++) {
				if ((i + j) % 2 == 0) {
					cofactors.mat[i][j] = this.crop(i, j).expand();
				} else {
					cofactors.mat[i][j] = - this.crop(i, j).expand();
				}
			}
		}
		return cofactors;
	}
	
	/**
	 * An alternative to the det() method which resuses the cofactors calculated for the inverse method so that they 
	 * don't need to be recalculated
	 * @param cofactors the matrix of cofactors corresponding to the matrix for which this method is being called
	 * @return the determinant of the matrix for which this method is being called
	 * @throws IllegalArgumentException if the matrix of cofactors doesn't have the same number of rows as the
	 * matrix for which this method is called
	 */
	private double detFromCofactors(Matrix cofactors) {
		if (this.m != cofactors.m) {
			throw new IllegalArgumentException("A matrix and its matrix of cofactors must have the same number of rows");
		} else {
			double determinant = 0;
			for (int i = 0; i < this.m; i++) {
				determinant += this.mat[i][0] * cofactors.mat[i][0];
			}
			return determinant;
		}
	}
	
	/**
	 * Recursive method for finding the determinant of a matrix by finding the determinants of sub-matrices
	 * @return the determinant of the matrix for which the method is being called
	 */
	private double expand() {
		// This is a private method only called by other methods which first checked that the matrix is square, so this check is not needed here
		if (this.m == 3) {
			return this.det3By3();
		} else if (this.m == 2) {
			return this.det2By2();
		} else {
			Matrix reducedMatrix = this.eliminated();
			double determinant = 0;
			for (int i = 0; i < reducedMatrix.m; i++) {
				if (reducedMatrix.mat[i][0] == 0) { /* We know that the determinant will not change if the
														element is zero */
					continue;
				}
				if (i % 2 == 0) {
					determinant += reducedMatrix.mat[i][0] * reducedMatrix.crop(i, 0).expand();
				} else {
					determinant -= reducedMatrix.mat[i][0] * reducedMatrix.crop(i, 0).expand();
				}
			}
			return determinant;
		}
	}
	
	/**
	 * Returns a version of the matrix where the columns have been manipulated and rows swapped such that the
	 * determinant is unchanged but calculation of the determinant through expanding of the top row is quicker
	 * (because the top row has many zeroes, so the determinant will be a sum of values of which many will be zero
	 * and we know which will be zero without having to do the full calculations)
	 * @return a 'simpler' matrix with the same determinant as the original
	 */
	public Matrix eliminated() {
		Matrix result = new Matrix(this.m, this.n);
		result.setElements(this);
		int zeroes;
		int maxZeroes = 0;
		int bestRow = 0;
		for (int i = 0; i < result.n; i++) {
			zeroes = 0;
			for (int j = 0; j < result.m; j++) {
				if (result.mat[j][i] == 0) {
					zeroes++;
				}
			}
			if (zeroes > maxZeroes) {
				maxZeroes = zeroes;
				bestRow = i;
			}
		}
		if (bestRow != 0) {
			/* 3 rows are swapped (equivalent of swapping 0 and bestRow then bestRow and another row (not 0))
			 * because a swap negates the determinant, so an even number of swaps are used to leave the determinant
			 * unchanged*/
			double tempElement;
			int swapRow = 1;
			if (bestRow == 1) {
				swapRow = 2;
			}
			for (int i = 0; i < result.m; i++) {
				tempElement = result.mat[i][0];
				result.mat[i][0] = result.mat[i][bestRow];
				result.mat[i][bestRow] = result.mat[i][swapRow];
				result.mat[i][swapRow] = tempElement;
			}
		}
		if (maxZeroes < result.m - 1) {
			int preserveCol = -1;
			for (int i = 0; i < result.m; i++) {
				if ((preserveCol >= 0) && (result.mat[i][0] != 0)) {
					double factor = result.mat[i][0] / result.mat[preserveCol][0];
					result.mat[i][0] = 0;
					for (int j = 1; j < result.n; j++) {
						result.mat[i][j] -= result.mat[preserveCol][j] * factor;
					}
				}
				if (result.mat[i][0] != 0) {
					preserveCol = i;
				}
			}
		}
		if (this.detKnown) {
			result.detKnown = true;
			result.det = this.det;
		}
		return result;
	}
	
	/**
	 * Returns a sub-matrix (known as a minor) of the matrix for which this method is called by removing the column and the row
	 * specified
	 * @param col the column to crop in order to create the sub-matrix
	 * @param row the row to crop in order to create the sub-matrix
	 * @return the matrix for which the method is called but with a column and a row removed
	 */
	private Matrix crop(int col, int row) {
		Matrix result = new Matrix(this.m - 1, this.n - 1);
		for (int i = 0; i < col; i++) {
			for (int j = 0; j < row; j++) {
				result.mat[i][j] = this.mat[i][j];
			}
			for (int j = row + 1; j < this.n; j++) {
				result.mat[i][j - 1] = this.mat[i][j];
			}
		}
		for (int i = col + 1; i < this.m; i++) {
			for (int j = 0; j < row; j++) {
				result.mat[i - 1][j] = this.mat[i][j];
			}
			for (int j = row + 1; j < this.n; j++) {
				result.mat[i - 1][j - 1] = this.mat[i][j];
			}
		}
		return result;
	}
	
	/**
	 * Quick non-recursive method for calculating the determinant of a 3 by 3 matrix
	 * @return the determinant of the matrix (must be 3 by 3)
	 */
	private double det3By3() {
		// This private method is only called when the matrix is already known to be 3 by 3, so we don't need a check here
		return this.mat[0][0] * this.mat[1][1] * this.mat[2][2]
				+ this.mat[1][0] * this.mat[2][1] * this.mat[0][2]
				+ this.mat[2][0] * this.mat[0][1] * this.mat[1][2]
				- this.mat[0][2] * this.mat[1][1] * this.mat[2][0]
				- this.mat[1][2] * this.mat[2][1] * this.mat[0][0]
				- this.mat[2][2] * this.mat[0][1] * this.mat[1][0];
	}
	
	/**
	 * Quick non-recursive method for calculating the determinant of a 2 by 2 matrix
	 * @return the determinant of the matrix (must be 2 by 2)
	 */
	private double det2By2() {
		// This private method is only called when the matrix is already known to be 2 by 2, so we don't need a check here
		return this.mat[0][0] * this.mat[1][1] - this.mat[1][0] * this.mat[0][1];
	}
	
	/**
	 * Returns the EulerTriple (three euler angles) representing the orientation of an object when this matrix is interpreted as converting points from an object space to the corresponding upright space
	 * @return the orientation/angular displacement represented by the matrix when interpreted as converting points from object space to upright space in EulerTriple form
	 * @throws IllegalArgumentException if the matrix is not 3 by 3 or if the matrix does not have determinant 1 (a matrix that satisfied these conditions is not necessarily a rotation matrix, so it is the responsibility of the code that calls this method to ensure the matrix represents a rotation)
	 */
	public EulerTriple eulerObToUp() {
		if ((this.m != 3) && (this.n != 3)) {
			throw new IllegalArgumentException("Only 3 by 3 matrices can be converted to Euler angles");
		} else if ((det() < 0.9999) || (det() > 1.0001)) { // Allow some leeway due to matrix creep
			throw new IllegalArgumentException("Only rotation matrices (which have a determinant of 1) can be converted to Euler angles");
		} else {
			double heading; // Rotation about the body y-axis
			double pitch; // Rotation about the body x-axis
			double bank; // Rotation about the body z-axis
			double sinPitch = - this.mat[2][1];
			if (sinPitch <= -1) {
				pitch = - Math.PI / 2;
			} else if (sinPitch >= 1) {
				pitch = Math.PI / 2;
			} else {
				pitch = Math.asin(sinPitch);
			}
			if (Math.abs(sinPitch) > 0.9999) {
				bank = 0;
				heading = Math2.atan(-this.mat[0][2], this.mat[0][0]);
			} else {
				heading = Math2.atan(this.mat[2][0], this.mat[2][2]);
				bank = Math2.atan(this.mat[0][1], this.mat[1][1]);
			}
			return new EulerTriple(heading, pitch, bank);
		}
	}
	
	/**
	 * Returns the EulerTriple (three euler angles) representing the orientation of an object when this matrix is interpreted as converting points from an upright space to the corresponding upright space
	 * @return the orientation/angular displacement represented by the matrix when interpreted as converting points from object space to upright space in EulerTriple form
	 * @throws IllegalArgumentException if the matrix is not 3 by 3 or if the matrix does not have determinant 1 (a matrix that satisfied these conditions is not necessarily a rotation matrix, so it is the responsibility of the code that calls this method to ensure the matrix represents a rotation)
	 */
	public EulerTriple eulerUpToOb() {
		try {
			return this.transpose().eulerObToUp(); // Rotation matrices can be inverted by transposition (object-to-upright and upright-to-object matrices are inverses of each other)
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
}
