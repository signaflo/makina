package org.platanios.learn.math.matrix;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implements a class representing sparse vectors and supporting operations related to them. The sparse vectors are
 * stored in an internal hash map.
 * TODO: Add toDenseVector() method (or appropriate constructors).
 * TODO: Make this implementation faster by iterating over key-value pairs instead of iterating over keys and then retrieving values.
 * TODO: Add Builder class and remove constructors.
 *
 * @author Emmanouil Antonios Platanios
 */
public class HashVector extends Vector {
    private static final long serialVersionUID = 7959600302555012181L;

    /** The size which the internal hash map uses as its initial capacity. */
    protected int initialSize = 128;
    /** The size of the vector. */
    protected int size;

    /** Hash map for internal storage of the vector elements. */
    protected OpenIntDoubleHashMap hashMap;

    /**
     * Constructs a sparse vector of the given size and fills it with zeros.
     *
     * @param   size    The size of the vector.
     */
    protected HashVector(int size) {
        this.size = size;
        hashMap = new OpenIntDoubleHashMap(initialSize);
    }

    /**
     * Constructs a sparse vector of the given size from a hash map.
     *
     * @param   size        The size of the vector.
     * @param   elements    Hash map containing the indexes of elements as keys and the values of the corresponding
     *                      elements as values.
     */
    protected HashVector(int size, OpenIntDoubleHashMap elements) {
        this.size = size;
        hashMap = (OpenIntDoubleHashMap) elements.copy();
    }

    /** {@inheritDoc} */
    @Override
    public VectorType type() {
        return VectorType.SPARSE;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector copy() {
        return new HashVector(size, hashMap);
    }

    /** {@inheritDoc} */
    @Override
    public double[] getDenseArray() {
        double[] resultArray = new double[size];
        hashMap.forEachPair((key, value) -> { resultArray[key] = value; return true; });
        return resultArray;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public int cardinality() {
        return hashMap.keys().size();
    }

    /** {@inheritDoc} */
    @Override
    public double get(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException(
                    "The provided index must be between 0 (inclusive) and the size of the vector (exclusive)."
            );
        }
        return hashMap.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public HashVector get(int initialIndex, int finalIndex) {
        if (initialIndex < 0 || initialIndex >= size || finalIndex < 0 || finalIndex >= size) {
            throw new IllegalArgumentException(
                    "The provided indexes must be between 0 (inclusive) and the size of the vector (exclusive)."
            );
        }
        if (initialIndex > finalIndex) {
            throw new IllegalArgumentException("The initial index must be smaller or equal to the final index.");
        }
        HashVector resultVector = new HashVector(finalIndex - initialIndex + 1);
        for (int i = initialIndex; i <= finalIndex; i++) {
            resultVector.set(i - initialIndex, hashMap.get(i));
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector get(int[] indexes) {
        HashVector resultVector = new HashVector(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            if (i < 0 || i >= size) {
                throw new IllegalArgumentException(
                        "The provided indexes must be between 0 (inclusive) and the size of the vector (exclusive)."
                );
            }
            resultVector.set(i, hashMap.get(indexes[i]));
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public void set(int index, double value) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException(
                    "The provided index must be between 0 (inclusive) and the size of the vector (exclusive)."
            );
        }
        if (Math.abs(value) >= epsilon) {
            hashMap.put(index, value);
        } else {
            hashMap.removeKey(index);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void set(int initialIndex, int finalIndex, Vector vector) {
        if (initialIndex < 0 || initialIndex >= size || finalIndex < 0 || finalIndex >= size) {
            throw new IllegalArgumentException(
                    "The provided indexes must be between 0 (inclusive) and the size of the vector (exclusive)."
            );
        }
        if (initialIndex > finalIndex) {
            throw new IllegalArgumentException("The initial index must be smaller or equal to the final index");
        }
        for (int i = initialIndex; i <= finalIndex; i++) {
            double value = vector.get(i - initialIndex);
            if (Math.abs(value) >= epsilon) {
                hashMap.put(i, value);
            } else {
                hashMap.removeKey(i);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void set(int[] indexes, Vector vector) {
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] < 0 || indexes[i] >= size) {
                throw new IllegalArgumentException(
                        "The provided indexes must be between 0 (inclusive) and the size of the vector (exclusive)."
                );
            }
            double value = vector.get(i);
            if (Math.abs(value) >= epsilon) {
                hashMap.put(indexes[i], value);
            } else {
                hashMap.removeKey(indexes[i]);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setAll(double value) {
        if (Math.abs(value) >= epsilon) {
            for (int i = 0; i < size; i++) {
                hashMap.put(i, value);
            }
        } else {
            hashMap.clear();
        }
    }

    /** {@inheritDoc} */
    @Override
    public double max() {
        double[] values = hashMap.values().elements();
        double maxValue = values[0];
        for (int i = 1; i < size; i++) {
            maxValue = Math.max(maxValue, values[i]);
        }
        return maxValue;
    }

    /** {@inheritDoc} */
    @Override
    public double min() {
        double[] values = hashMap.values().elements();
        double minValue = values[0];
        for (int i = 1; i < size; i++) {
            minValue = Math.min(minValue, values[i]);
        }
        return minValue;
    }

    /** {@inheritDoc} */
    @Override
    public double sum() {
        double[] values = hashMap.values().elements();
        double sum = values[0];
        for (int i = 1; i < size; i++) {
            sum += values[i];
        }
        return sum;
    }

    /** {@inheritDoc} */
    @Override
    public double norm(VectorNorm normType) {
        return normType.compute(hashMap.values().elements());
    }

    /** {@inheritDoc} */
    @Override
    public HashVector add(double scalar) {
        HashVector resultVector = new HashVector(size, hashMap);
        resultVector.hashMap.assign(element -> element + scalar);
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector addInPlace(double scalar) {
        hashMap.assign(element -> element + scalar);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector add(Vector vector) {
        checkVectorSize(vector);
        HashVector resultVector = new HashVector(size, hashMap);
        if (vector.type() != VectorType.SPARSE) {
            for (int i = 0; i < size; i++) {
                resultVector.set(i, this.get(i) + vector.get(i));
            }
        } else {
            IntArrayList keysUnion = new IntArrayList(hashMap.keys().elements());
            keysUnion.addAllOf(((HashVector) vector).hashMap.keys());
            for (int key : keysUnion.elements()) {
                resultVector.set(key, this.get(key) + vector.get(key));
            }
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector addInPlace(Vector vector) {
        checkVectorSize(vector);
        if (vector.type() != VectorType.SPARSE) {
            for (int i = 0; i < size; i++) {
                this.set(i, this.get(i) + vector.get(i));
            }
        } else {
            IntArrayList keysUnion = new IntArrayList(hashMap.keys().elements());
            keysUnion.addAllOf(((HashVector) vector).hashMap.keys());
            for (int key : keysUnion.elements()) {
                this.set(key, this.get(key) + vector.get(key));
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector sub(double scalar) {
        HashVector resultVector = new HashVector(size, hashMap);
        resultVector.hashMap.assign(element -> element - scalar);
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector subInPlace(double scalar) {
        hashMap.assign(element -> element - scalar);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector sub(Vector vector) {
        checkVectorSize(vector);
        HashVector resultVector = new HashVector(size, hashMap);
        if (vector.type() != VectorType.SPARSE) {
            for (int i = 0; i < size; i++) {
                resultVector.set(i, this.get(i) - vector.get(i));
            }
        } else {
            IntArrayList keysUnion = new IntArrayList(hashMap.keys().elements());
            keysUnion.addAllOf(((HashVector) vector).hashMap.keys());
            for (int key : keysUnion.elements()) {
                resultVector.set(key, this.get(key) - vector.get(key));
            }
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector subInPlace(Vector vector) {
        checkVectorSize(vector);
        if (vector.type() != VectorType.SPARSE) {
            for (int i = 0; i < size; i++) {
                this.set(i, this.get(i) - vector.get(i));
            }
        } else {
            IntArrayList keysUnion = new IntArrayList(hashMap.keys().elements());
            keysUnion.addAllOf(((HashVector) vector).hashMap.keys());
            for (int key : keysUnion.elements()) {
                this.set(key, this.get(key) - vector.get(key));
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector multElementwise(Vector vector) {
        checkVectorSize(vector);
        HashVector resultVector = new HashVector(size);
        if (vector.type() != VectorType.SPARSE) {
            for (int key : hashMap.keys().elements()) {
                resultVector.set(key, hashMap.get(key) * vector.get(key));
            }
        } else {
            if (hashMap.keys().size() <= ((HashVector) vector).hashMap.keys().size()) {
                for (int key : hashMap.keys().elements()) {
                    resultVector.set(key, hashMap.get(key) * vector.get(key));
                }
            } else {
                for (int key : ((HashVector) vector).hashMap.keys().elements()) {
                    resultVector.set(key, hashMap.get(key) * vector.get(key));
                }
            }
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector multElementwiseInPlace(Vector vector) {
        checkVectorSize(vector);
        if (vector.type() != VectorType.SPARSE) {
            for (int key : hashMap.keys().elements()) {
                this.set(key, hashMap.get(key) * vector.get(key));
            }
        } else {
            if (this.cardinality() <= vector.cardinality()) {
                for (int key : hashMap.keys().elements()) {
                    this.set(key, hashMap.get(key) * vector.get(key));
                }
            } else {
                for (int key : ((HashVector) vector).hashMap.keys().elements()) {
                    this.set(key, hashMap.get(key) * vector.get(key));
                }
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector divElementwise(Vector vector) {
        checkVectorSize(vector); // TODO: Need to check whether any element of vector is zero.
        HashVector resultVector = new HashVector(size, hashMap);
        for (int key : hashMap.keys().elements()) {
            resultVector.set(key, hashMap.get(key) / vector.get(key));
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector divElementwiseInPlace(Vector vector) {
        checkVectorSize(vector); // TODO: Need to check whether any element of vector is zero.
        for (int key : hashMap.keys().elements()) {
            this.set(key, hashMap.get(key) / vector.get(key));
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mult(double scalar) {
        HashVector resultVector = new HashVector(size, hashMap);
        resultVector.hashMap.assign(element -> element * scalar);
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector multInPlace(double scalar) {
        hashMap.assign(element -> element * scalar);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector div(double scalar) {
        HashVector resultVector = new HashVector(size, hashMap);
        resultVector.hashMap.assign(element -> element / scalar);
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector divInPlace(double scalar) {
        hashMap.assign(element -> element / scalar);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector saxpy(double scalar, Vector vector) {
        checkVectorSize(vector);
        HashVector resultVector = new HashVector(size, hashMap);
        if (vector.type() != VectorType.SPARSE) {
            for (int i = 0; i < size; i++) {
                resultVector.set(i, this.get(i) + scalar * vector.get(i));
            }
        } else {
            IntArrayList keysUnion = new IntArrayList(hashMap.keys().elements());
            keysUnion.addAllOf(((HashVector) vector).hashMap.keys());
            for (int key : keysUnion.elements()) {
                resultVector.set(key, this.get(key) + scalar * vector.get(key));
            }
        }
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector saxpyInPlace(double scalar, Vector vector) {
        checkVectorSize(vector);
        if (vector.type() != VectorType.SPARSE) {
            for (int i = 0; i < size; i++) {
                this.set(i, this.get(i) + scalar * vector.get(i));
            }
        } else {
            IntArrayList keysUnion = new IntArrayList(hashMap.keys().elements());
            keysUnion.addAllOf(((HashVector) vector).hashMap.keys());
            for (int key : keysUnion.elements()) {
                this.set(key, this.get(key) + scalar * vector.get(key));
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public double inner(Vector vector) {
        checkVectorSize(vector);
        double result = 0;
        if (vector.type() != VectorType.SPARSE) {
            for (int key : hashMap.keys().elements()) {
                result += hashMap.get(key) * vector.get(key);
            }
        } else {
            if (this.cardinality() <= vector.cardinality()) {
                for (int key : hashMap.keys().elements()) {
                    result += hashMap.get(key) * vector.get(key);
                }
            } else {
                for (int key : ((HashVector) vector).hashMap.keys().elements()) {
                    result += hashMap.get(key) * vector.get(key);
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector hypotenuse(Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector hypotenuseInPlace(Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector hypotenuseFast(Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector hypotenuseFastInPlace(Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector map(Function<Double, Double> function) {
        HashVector resultVector = new HashVector(size, hashMap); // TODO: What happens when the function is applied to zeros?
        resultVector.hashMap.assign(function::apply);
        return resultVector;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapInPlace(Function<Double, Double> function) { // TODO: What happens when the function is applied to zeros?
        hashMap.assign(function::apply);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapBiFunction(BiFunction<Double, Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapBiFunctionInPlace(BiFunction<Double, Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapAdd(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapAddInPlace(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapSub(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapSubInPlace(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapMultElementwise(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapMultElementwiseInPlace(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapDivElementwise(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public HashVector mapDivElementwiseInPlace(Function<Double, Double> function, Vector vector) {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public Matrix outer(Vector vector) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Vector gaxpy(Matrix matrix, Vector vector) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Vector gaxpyInPlace(Matrix matrix, Vector vector) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Vector transMult(Matrix matrix) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(type());
        outputStream.writeInt(size);
        outputStream.writeInt(hashMap.keys().size());
        for (int key : hashMap.keys().elements()) {
            outputStream.writeInt(key);
            outputStream.writeDouble(hashMap.get(key));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        VectorType storedVectorType = (VectorType) inputStream.readObject();
        if (storedVectorType != type())
            throw new InvalidObjectException("The stored vector is of type " + storedVectorType.name() + "!");
        size = inputStream.readInt();
        hashMap = new OpenIntDoubleHashMap(initialSize);
        int numberOfNonzeroEntries = inputStream.readInt();
        for (int i = 0; i < numberOfNonzeroEntries; i++) {
            int key = inputStream.readInt();
            double value = inputStream.readDouble();
            hashMap.put(key, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HashVector))
            return false;
        if (object == this)
            return true;

        HashVector other = (HashVector) object;
        return new EqualsBuilder()
                .append(size, other.size)
                .append(hashMap, other.hashMap)
                .isEquals();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31) // Two randomly chosen prime numbers.
                .append(size)
                .append(hashMap)
                .toHashCode();
    }
}