package com.prunoideae.probejs.bytecode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ClasspathHelper {
    public static Stream<Path> walkClassFiles(ClassLoader classLoader) {
        return walkClassResources(classLoader)
            .filter(distinctByKey(URL::toExternalForm))
            .flatMap(ClasspathHelper::scanFiles)
            .filter(Files::exists)
            .filter(it -> !Files.isDirectory(it))
            .filter(it -> it.toString().endsWith(".class"))
            .distinct();
    }
    public static Stream<URL> walkClassResources(ClassLoader classLoader) {
        Stream<URL> urlStream = iterate(classLoader, Objects::nonNull, ClassLoader::getParent)
            .filter(it -> it instanceof URLClassLoader)
            .map(it -> ((URLClassLoader) it).getURLs())
            .flatMap(Arrays::stream);
        try {
            Enumeration<URL> resources = classLoader.getResources("");
            Stream<URL> resourcesStream = convert(resources);
            return Stream.concat(urlStream, resourcesStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlStream;
    }
    public static Stream<Path> scanFiles(URL file) {
        try {
            FileSystem fs = FileSystems.getFileSystem(file.toURI());
            return Files.walk(fs.getPath("/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return Stream.empty();
    }


    public static <T> Predicate<T> distinctByKey(
        Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    public static <T> Stream<T> convert(Enumeration<T> enumeration) {
        if (enumeration == null) {
            return Stream.empty();
        }
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (enumeration.hasMoreElements()) {
                    action.accept(enumeration.nextElement());
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                while (enumeration.hasMoreElements())
                    action.accept(enumeration.nextElement());
            }
        };

        return StreamSupport.stream(spliterator, false);
    }


    //Java 9 stream API
    public static <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE,
            Spliterator.ORDERED | Spliterator.IMMUTABLE) {

            T prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return false;
                T t;
                if (started)
                    t = next.apply(prev);
                else {
                    t = seed;
                    started = true;
                }
                if (!hasNext.test(t)) {
                    prev = null;
                    finished = true;
                    return false;
                }
                action.accept(prev = t);
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return;
                finished = true;
                T t = started ? next.apply(prev) : seed;
                prev = null;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.apply(t);
                }
            }
        };
        return StreamSupport.stream(spliterator, false);
    }


}
