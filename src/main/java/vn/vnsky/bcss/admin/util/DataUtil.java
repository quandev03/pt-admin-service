package vn.vnsky.bcss.admin.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@UtilityClass
public class DataUtil {

    private final int DEFAULT_BATCH_SIZE = 100;

    public <T> void batchProcess(List<T> objList, Consumer<List<T>> processorFunction) {
        batchProcess(objList, processorFunction, DEFAULT_BATCH_SIZE);
    }

    public <T> void batchProcess(List<T> objList, Consumer<List<T>> processorFunction, int batchSize) {
        IntStream.range(0, objList.size())
                .filter(i -> i % batchSize == 0)
                .mapToObj(i -> objList.subList(i, Math.min(i + batchSize, objList.size() )))
                .forEach(processorFunction);
    }

    public <T> void batchProcess(Set<T> objSet, Consumer<Set<T>> processorFunction) {
        batchProcess(objSet, processorFunction, DEFAULT_BATCH_SIZE);
    }

    public <T> void batchProcess(Set<T> objSet, Consumer<Set<T>> processorFunction, int batchSize) {
        List<T> objList = new ArrayList<>(objSet);
        IntStream.range(0, objList.size())
                .filter(i -> i % batchSize == 0)
                .mapToObj(i -> objList.subList(i, Math.min(i + batchSize, objList.size() )))
                .forEach(subList -> {
                    Set<T> subSet = new HashSet<>(subList);
                    processorFunction.accept(subSet);
                });
    }

    public <K, V> void batchProcess(Map<K, V> objMap, Consumer<Map<K, V>> processorFunction) {
        batchProcess(objMap, processorFunction, DEFAULT_BATCH_SIZE);
    }

    public <K, V> void batchProcess(Map<K, V> objMap, Consumer<Map<K, V>> processorFunction, int batchSize) {
        List<K> keyList = new ArrayList<>(objMap.keySet());
        IntStream.range(0, keyList.size())
                .filter(i -> i % batchSize == 0)
                .mapToObj(i -> keyList.subList(i, Math.min(i + batchSize, keyList.size() )))
                .forEach(keyBatch -> {
                    Map<K, V> newMap = new HashMap<>(objMap);
                    newMap.keySet().retainAll(keyBatch);
                    processorFunction.accept(newMap);
                });
    }

}
