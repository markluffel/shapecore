package shapecore.rendering;

/**
 * A class to describe a two or three dimensional vector.
 * <p>
 * The result of all functions are applied to the vector itself, with the
 * exception of cross(), which returns a new MVector (or writes to a specified
 * 'target' MVector). That is, add() will add the contents of one vector to
 * this one. Using add() with additional parameters allows you to put the
 * result into a new MVector. Functions that act on multiple vectors also
 * include static versions. Because creating new objects can be computationally
 * expensive, most functions include an optional 'target' MVector, so that a
 * new MVector object is not created with each operation.
 * <p>
 * Initially based on the Vector3D class by <a href="http://www.shiffman.net">Dan Shiffman</a>.
 */
public final class MVector {

  /** The x component of the vector. */
  public float x;

  /** The y component of the vector. */
  public float y;

  /** The z component of the vector. */
  public float z;


  /**
   * Constructor for an empty vector: x, y, and z are set to 0.
   */
  MVector() {
  }


  /**
   * Constructor for a 3D vector.
   *
   * @param  x the x coordinate.
   * @param  y the y coordinate.
   * @param  z the y coordinate.
   */
  public MVector(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }


  /**
   * Constructor for a 2D vector: z coordinate is set to 0.
   *
   * @param  x the x coordinate.
   * @param  y the y coordinate.
   */
  public MVector(float x, float y) {
    this.x = x;
    this.y = y;
    this.z = 0;
  }


  public MVector(MVector start) {
    this.x = start.x;
    this.y = start.y;
    this.z = start.z;
  }


  /**
   * Set x, y, and z coordinates.
   *
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param z the z coordinate.
   */
  public void set(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }


  /**
   * Set x, y, and z coordinates from a Vector3D object.
   *
   * @param v the MVector object to be copied
   */
  public void set(MVector v) {
    x = v.x;
    y = v.y;
    z = v.z;
  }


  /**
   * Set the x, y (and maybe z) coordinates using a float[] array as the source.
   * @param source array to copy from
   */
  public  void set(float[] source) {
    if (source.length >= 2) {
      x = source[0];
      y = source[1];
    }
    if (source.length >= 3) {
      z = source[2];
    }
  }


  /**
   * Get a copy of this vector.
   */
  public MVector get() {
    return new MVector(x, y, z);
  }


  public float[] get(float[] target) {
    if (target == null) {
      return new float[] { x, y, z };
    }
    if (target.length >= 2) {
      target[0] = x;
      target[1] = y;
    }
    if (target.length >= 3) {
      target[2] = z;
    }
    return target;
  }


  /**
   * Calculate the magnitude (length) of the vector
   * @return the magnitude of the vector
   */
  public float mag() {
    return (float) Math.sqrt(x*x + y*y + z*z);
  }


  /**
   * Add a vector to this vector
   * @param v the vector to be added
   */
  public void add(MVector v) {
    x += v.x;
    y += v.y;
    z += v.z;
  }

  void add(float x, float y, float z) {
    this.x += x;
    this.y += y;
    this.z += z;
  }


  /**
   * Add two vectors
   * @param v1 a vector
   * @param v2 another vector
   * @return a new vector that is the sum of v1 and v2
   */
  static MVector add(MVector v1, MVector v2) {
    return add(v1, v2, null);
  }


  /**
   * Add two vectors into a target vector
   * @param v1 a vector
   * @param v2 another vector
   * @param target the target vector (if null, a new vector will be created)
   * @return a new vector that is the sum of v1 and v2
   */
  static MVector add(MVector v1, MVector v2, MVector target) {
    if (target == null) {
      target = new MVector(v1.x + v2.x,v1.y + v2.y, v1.z + v2.z);
    } else {
      target.set(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }
    return target;
  }


  /**
   * Subtract a vector from this vector
   * @param v the vector to be subtracted
   */
  void sub(MVector v) {
    x -= v.x;
    y -= v.y;
    z -= v.z;
  }


  void sub(float x, float y, float z) {
    this.x -= x;
    this.y -= y;
    this.z -= z;
  }


  /**
   * Subtract one vector from another
   * @param v1 a vector
   * @param v2 another vector
   * @return a new vector that is v1 - v2
   */
  public static MVector sub(MVector v1, MVector v2) {
    return sub(v1, v2, null);
  }


  static MVector sub(MVector v1, MVector v2, MVector target) {
    if (target == null) {
      target = new MVector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    } else {
      target.set(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }
    return target;
  }


  /**
   * Multiply this vector by a scalar
   * @param n the value to multiply by
   */
  public void mult(float n) {
    x *= n;
    y *= n;
    z *= n;
  }


  /**
   * Multiply a vector by a scalar
   * @param v a vector
   * @param n scalar
   * @return a new vector that is v1 * n
   */
  public static MVector mult(MVector v, float n) {
    return mult(v, n, null);
  }


  /**
   * Multiply a vector by a scalar, and write the result into a target MVector.
   * @param v a vector
   * @param n scalar
   * @param target MVector to store the result
   * @return the target vector, now set to v1 * n
   */
  static MVector mult(MVector v, float n, MVector target) {
    if (target == null) {
      target = new MVector(v.x*n, v.y*n, v.z*n);
    } else {
      target.set(v.x*n, v.y*n, v.z*n);
    }
    return target;
  }


  /**
   * Multiply each element of one vector by the elements of another vector.
   * @param v the vector to multiply by
   */
  void mult(MVector v) {
    x *= v.x;
    y *= v.y;
    z *= v.z;
  }


  /**
   * Multiply each element of one vector by the individual elements of another
   * vector, and return the result as a new MVector.
   */
  static MVector mult(MVector v1, MVector v2) {
    return mult(v1, v2, null);
  }


  /**
   * Multiply each element of one vector by the individual elements of another
   * vector, and write the result into a target vector.
   * @param v1 the first vector
   * @param v2 the second vector
   * @param target MVector to store the result
   */
  static MVector mult(MVector v1, MVector v2, MVector target) {
    if (target == null) {
      target = new MVector(v1.x*v2.x, v1.y*v2.y, v1.z*v2.z);
    } else {
      target.set(v1.x*v2.x, v1.y*v2.y, v1.z*v2.z);
    }
    return target;
  }


  /**
   * Divide this vector by a scalar
   * @param n the value to divide by
   */
  public void div(float n) {
    x /= n;
    y /= n;
    z /= n;
  }


  /**
   * Divide a vector by a scalar and return the result in a new vector.
   * @param v a vector
   * @param n scalar
   * @return a new vector that is v1 / n
   */
  static MVector div(MVector v, float n) {
    return div(v, n, null);
  }


  static MVector div(MVector v, float n, MVector target) {
    if (target == null) {
      target = new MVector(v.x/n, v.y/n, v.z/n);
    } else {
      target.set(v.x/n, v.y/n, v.z/n);
    }
    return target;
  }


  /**
   * Divide each element of one vector by the elements of another vector.
   */
  void div(MVector v) {
    x /= v.x;
    y /= v.y;
    z /= v.z;
  }


  /**
   * Multiply each element of one vector by the individual elements of another
   * vector, and return the result as a new MVector.
   */
  static MVector div(MVector v1, MVector v2) {
    return div(v1, v2, null);
  }


  /**
   * Divide each element of one vector by the individual elements of another
   * vector, and write the result into a target vector.
   * @param v1 the first vector
   * @param v2 the second vector
   * @param target MVector to store the result
   */
  static MVector div(MVector v1, MVector v2, MVector target) {
    if (target == null) {
      target = new MVector(v1.x/v2.x, v1.y/v2.y, v1.z/v2.z);
    } else {
      target.set(v1.x/v2.x, v1.y/v2.y, v1.z/v2.z);
    }
    return target;
  }


  /**
   * Calculate the Euclidean distance between two points (considering a point as a vector object)
   * @param v another vector
   * @return the Euclidean distance between
   */
  float dist(MVector v) {
    float dx = x - v.x;
    float dy = y - v.y;
    float dz = z - v.z;
    return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
  }


  /**
   * Calculate the Euclidean distance between two points (considering a point as a vector object)
   * @param v1 a vector
   * @param v2 another vector
   * @return the Euclidean distance between v1 and v2
   */
  static float dist(MVector v1, MVector v2) {
    float dx = v1.x - v2.x;
    float dy = v1.y - v2.y;
    float dz = v1.z - v2.z;
    return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
  }


  /**
   * Calculate the dot product with another vector
   * @return the dot product
   */
  public float dot(MVector v) {
    return x*v.x + y*v.y + z*v.z;
  }


  float dot(float x, float y, float z) {
    return this.x*x + this.y*y + this.z*z;
  }


  /**
   * Return a vector composed of the cross product between this and another.
   */
  public MVector cross(MVector v) {
    return cross(v, null);
  }


  /**
   * Perform cross product between this and another vector, and store the
   * result in 'target'. If target is null, a new vector is created.
   */
  MVector cross(MVector v, MVector target) {
    float crossX = y * v.z - v.y * z;
    float crossY = z * v.x - v.z * x;
    float crossZ = x * v.y - v.x * y;

    if (target == null) {
      target = new MVector(crossX, crossY, crossZ);
    } else {
      target.set(crossX, crossY, crossZ);
    }
    return target;
  }


  static MVector cross(MVector v1, MVector v2, MVector target) {
    float crossX = v1.y * v2.z - v2.y * v1.z;
    float crossY = v1.z * v2.x - v2.z * v1.x;
    float crossZ = v1.x * v2.y - v2.x * v1.y;

    if (target == null) {
      target = new MVector(crossX, crossY, crossZ);
    } else {
      target.set(crossX, crossY, crossZ);
    }
    return target;
  }


  /**
   * Normalize the vector to length 1 (make it a unit vector)
   */
  public void normalize() {
    float m = mag();
    if (m != 0 && m != 1) {
      div(m);
    }
  }


  /**
   * Normalize this vector, storing the result in another vector.
   * @param target Set to null to create a new vector
   * @return a new vector (if target was null), or target
   */
  MVector normalize(MVector target) {
    if (target == null) {
      target = new MVector();
    }
    float m = mag();
    if (m > 0) {
      target.set(x/m, y/m, z/m);
    } else {
      target.set(x, y, z);
    }
    return target;
  }


  /**
   * Limit the magnitude of this vector
   * @param max the maximum length to limit this vector
   */
  void limit(float max) {
    if (mag() > max) {
      normalize();
      mult(max);
    }
  }


  /**
   * Calculate the angle of rotation for this vector (only 2D vectors)
   * @return the angle of rotation
   */
  float heading2D() {
    float angle = (float) Math.atan2(-y, x);
    return -1*angle;
  }


  /**
   * Calculate the angle between two vectors, using the dot product
   * @param v1 a vector
   * @param v2 another vector
   * @return the angle between the vectors
   */
  static float angleBetween(MVector v1, MVector v2) {
    double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    double v1mag = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
    double v2mag = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
    return (float) Math.acos(dot / (v1mag * v2mag));
  }


  public String toString() {
    return "[ " + x + ", " + y + ", " + z + " ]";
  }


  /**
   * Return a representation of this vector as a float array. This is only for
   * temporary use. If used in any other fashion, the contents should be copied
   * by using the get() command to copy into your own array.
   */
  float[] array() {
    float[] array = new float[3];
    array[0] = x;
    array[1] = y;
    array[2] = z;
    return array;
  }
}