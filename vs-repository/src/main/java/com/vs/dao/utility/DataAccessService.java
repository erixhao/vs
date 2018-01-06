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

public class DataAccessService {
    public static ConcurrentHashMap<Class<?>, List<?>> cache = new ConcurrentHashMap<Class<?>, List<?>>();
    public static AtomicInteger saveRequest = new AtomicInteger(0);
    public static Thread saveThread = null;

    private static int MAX_PENDING_REQUEST = 100;
    private static int MAX_SAVE_INTERVAL = 300_000;

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
        cache.put(clazz, items);
        saveRequest.incrementAndGet();
//        FileUtil.writeFile(clazz.getSimpleName(), items);
        if (saveThread == null) {
            saveThread = new Thread(new SaveThread());
            saveThread.setDaemon(true);
            saveThread.start();
        }
    }

    public static <T extends Entity> List<T> read(Class<T> clazz) {
        if (cache.get(clazz) != null) {
            return (List<T>) cache.get(clazz);
        } else {
            List<T> result = FileUtil.readFile(clazz.getSimpleName(), clazz);
            cache.put(clazz, result);
            return result;
        }
    }

    public static class SaveThread implements Runnable {
        private LocalDateTime lastSaveTime = LocalDateTime.now();

        public SaveThread() {
            System.out.println("Start Damon thread for DataAccessService now...");
        }

        @Override
        public void run() {
            while (true) {
                int cur = saveRequest.get();
                if (cur > MAX_PENDING_REQUEST || Duration.between(lastSaveTime, LocalDateTime.now()).toMillis() > MAX_SAVE_INTERVAL) {
                    if (saveRequest.compareAndSet(cur, 0)) {
                        System.out.println("Damon thread saving cache to file now.....");
                        for (Map.Entry<Class<?>, List<?>> entry : cache.entrySet()) {
                            FileUtil.writeFile(entry.getKey().getSimpleName(), (List<? extends Entity>) entry.getValue());
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