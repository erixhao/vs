package com.vs.dao.utility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.vs.common.domain.Entity;
import com.vs.common.domain.HistoricalData;
import com.vs.common.utils.PropertieUtils;

public class DataAccessService {
    public static ConcurrentHashMap<String, List<?>> cache = new ConcurrentHashMap<String, List<?>>();
    public static AtomicInteger saveRequest = new AtomicInteger(0);
    public static Thread saveThread = null;

    public static long UPDATE_SIZE = 0;
    public static long UPDATE_INTERVAL = 0;

    private static long getUpdateSize() {
        if (UPDATE_SIZE == 0) {
            UPDATE_SIZE = Long.valueOf(PropertieUtils.getProperty("config.properties", "db.update.size"));
        }
        return UPDATE_SIZE;
    }

    private static long getUpdateInterval() {
        if (UPDATE_INTERVAL == 0) {
            UPDATE_INTERVAL = Long.valueOf(PropertieUtils.getProperty("config.properties", "db.update.interval"));
        }
        return UPDATE_INTERVAL;
    }

    public static <T extends Entity> List<T> findAll(Class<T> clazz) {
        Predicate<T> criteria = (a -> true);
        return findAllBy(clazz, criteria);
    }

    public static <T extends Entity> List<T> findAllBy(Class<T> clazz, Predicate<T> criteria) {
        List<T> result = read(clazz);
        result = result.stream().filter(criteria).filter(distinctByKey(Entity::getKey)).collect(Collectors.toList());
        return result;
    }

    public static <T extends Entity> void save(Class<T> clazz, T item) {
        save(clazz, Lists.newArrayList(item), true);
    }

    public static <T extends Entity> void save(Class<T> clazz, List<T> entities) {
        save(clazz, entities, true);
    }

    public static <T extends Entity> void save(Class<T> clazz, List<T> updatedItems, boolean incremental) {
        synchronized (clazz) {
            List<T> existing = Lists.newArrayList();
            if (incremental) {
                existing = findAll(clazz);
                existing.removeAll(updatedItems);
                existing.addAll(updatedItems);
            } else {
                existing = updatedItems;
            }
            existing = existing.stream().filter(distinctByKey(Entity::getKey)).collect(Collectors.toList());
            write(clazz, existing);
        }
    }

    public static <T extends Entity> void remove(Class<T> clazz, List<T> removeItems) throws Exception {
        synchronized (clazz) {
            List<T> existing = findAll(clazz);
            existing.removeAll(removeItems);
            existing = existing.stream().filter(distinctByKey(Entity::getKey)).collect(Collectors.toList());
            write(clazz, existing);
        }
    }

    public static <T extends Entity> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T extends Entity> void write(Class<T> clazz, List<T> items) {
        cache.put(clazz.getSimpleName(), items);
        saveRequest.incrementAndGet();
//        FileUtil.writeFile(clazz.getSimpleName(), items);
        if (saveThread == null) {
            saveThread = new Thread(new SaveThread());
            saveThread.setDaemon(true);
            saveThread.start();
        }
    }

    public static <T extends Entity> List<T> read(Class<T> clazz) {
        if (cache.get(clazz.getSimpleName()) != null) {
            return (List<T>) cache.get(clazz.getSimpleName());
        } else {
            List<T> result = FileUtil.readFile(clazz.getSimpleName(), clazz);
            cache.put(clazz.getSimpleName(), result);
            return result;
        }
    }

    /*---------------------------------------------*/
    public static List<HistoricalData> findAllMktBy(String code) {
        Predicate<HistoricalData> criteria = (a -> true);
        return findAllMktBy(code, criteria);
    }

    public static List<HistoricalData> findAllMktBy(String code, Predicate<HistoricalData> criteria) {
        List<HistoricalData> result = readMkt("HistoricalData_" + code, HistoricalData.class);
        result = result.stream().filter(criteria).filter(distinctByKey(Entity::getKey)).collect(Collectors.toList());
        return result;
    }

    public static void saveMkt(HistoricalData item) {
        saveMkt(Lists.newArrayList(item));
    }

    public static void saveMkt(List<HistoricalData> entities) {
        saveMkt(entities, true);
    }

    public static void saveMkt(List<HistoricalData> updatedItems, boolean incremental) {
        String stockCode = updatedItems.get(0).getStockCode();
        synchronized (HistoricalData.class) {
            List<HistoricalData> existing = Lists.newArrayList();
            if (incremental) {
                existing = findAllMktBy(stockCode);
                existing.removeAll(updatedItems);
                existing.addAll(updatedItems);
            } else {
                existing = updatedItems;
            }
            existing = existing.stream().filter(distinctByKey(Entity::getKey)).collect(Collectors.toList());
            writeMkt("HistoricalData_" + stockCode, existing);
        }
    }

    public static <T extends Entity> void writeMkt(String fileName, List<T> items) {
        cache.put(fileName, items);
        saveRequest.incrementAndGet();
//        FileUtil.writeFile(clazz.getSimpleName(), items);
        if (saveThread == null) {
            saveThread = new Thread(new SaveThread());
            saveThread.setDaemon(true);
            saveThread.start();
        }
    }

    public static <T extends Entity> List<T> readMkt(String fileName, Class<T> clazz) {
        if (cache.get(fileName) != null) {
            return (List<T>) cache.get(fileName);
        } else {
            List<T> result = FileUtil.readFile(fileName, clazz);
            cache.put(fileName, result);
            return result;
        }
    }
    /*---------------------------------------------*/

    public static class SaveThread implements Runnable {
        private LocalDateTime lastSaveTime = LocalDateTime.now();

        public SaveThread() {
            System.out.println("Start Damon thread for DataAccessService now...");
        }

        @Override
        public void run() {
            while (true) {
                int cur = saveRequest.get();
                if (cur > getUpdateSize() || Duration.between(lastSaveTime, LocalDateTime.now()).toMillis() > getUpdateInterval()) {
                    if (saveRequest.compareAndSet(cur, 0)) {
                        System.out.println("Damon thread saving cache to file now.....");
                        for (Map.Entry<String, List<?>> entry : cache.entrySet()) {
                            FileUtil.writeFile(entry.getKey(), (List<? extends Entity>) entry.getValue());
                        }
                        lastSaveTime = LocalDateTime.now();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}