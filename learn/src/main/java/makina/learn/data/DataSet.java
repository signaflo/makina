package makina.learn.data;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author Emmanouil Antonios Platanios
 */
public interface DataSet<D extends DataInstance> extends Iterable<D> {
    int size();
    <S extends DataInstance> DataSet<S> newDataSet(); // TODO: Shouldn't this be static?
    void add(D dataInstance);
    void add(List<D> dataInstances);
    D remove(int index);
    D remove(D dataInstance);
    D get(int index);
    void set(int index, D dataInstance);
    DataSet<D> subSet(int fromIndex, int toIndex);
    DataSet<D> subSetComplement(int fromIndex, int toIndex);
    DataSet<D> sort(Comparator<? super D> comparator);
    DataSet<D> shuffle();
    @Override
    Iterator<D> iterator();
    Iterator<List<D>> batchIterator(int batchSize);
    Iterator<List<D>> continuousRandomBatchIterator(int batchSize, boolean sampleWithReplacement);
    Iterator<List<D>> continuousRandomBatchIterator(int batchSize, boolean sampleWithReplacement, Random random);
}
