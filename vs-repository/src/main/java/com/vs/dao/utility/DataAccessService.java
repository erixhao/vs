package com.vs.dao.utility;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.vs.common.domain.Entity;

public class DataAccessService {
    public static ConcurrentHashMap<Class<?>, List<?>> cache = new ConcurrentHashMap<Class<?>, List<?>>();

    // TODO can have cache here
    public static <T extends Entity> List<T> findAll(Class<T> clazz) {
        Predicate<T> criteria = (a -> true);
        return findAllBy(clazz, criteria);
    }

    public static <T extends Entity> List<T> findAllBy(Class<T> clazz, Predicate<T> criteria) {
        List<T> result = FileUtil.readFile(clazz.getSimpleName(), clazz);
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
            FileUtil.writeFile(clazz.getSimpleName(), existing);
        }
    }

    public static <T extends Entity> void remove(Class<T> clazz, List<T> removeItems) throws Exception {
        synchronized (clazz) {
            List<T> existing = findAll(clazz);
            existing.removeAll(removeItems);
            existing = existing.stream().filter(distinctByKey(Entity::getKey)).collect(Collectors.toList());
            FileUtil.writeFile(clazz.getSimpleName(), existing);
        }
    }

    public static <T extends Entity> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}