package com.prunoideae.probejs.old.resolver;

import com.prunoideae.probejs.document.parser.handler.AbstractStackedMachine;
import com.prunoideae.probejs.document.parser.handler.ICallback;
import com.prunoideae.probejs.document.parser.handler.IStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {
    private static class Extractor implements IStateHandler<String> {
        private final String push;
        private final String pop;
        public List<Object> extracted = new ArrayList<>();
        private StringBuilder current = new StringBuilder();

        public Extractor(String push, String pop) {
            this.push = push;
            this.pop = pop;
        }

        @Override
        public void trial(String element, List<IStateHandler<String>> stack) {
            if (element.equals(push)) {
                Extractor e = new Extractor(this.push, this.pop);
                extracted.add(current.toString());
                current = new StringBuilder();
                extracted.add(e);
                stack.add(e);
            } else if (element.equals(pop)) {
                extracted.add(current.toString());
                stack.remove(this);
            } else {
                current.append(element);
            }
        }

        public void finalizeString() {
            extracted.add(current.toString());
            current = new StringBuilder();
        }

        public String join() {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : extracted) {
                if (o instanceof String) {
                    stringBuilder.append((String) o);
                } else if (o instanceof Extractor) {
                    stringBuilder.append(this.push);
                    stringBuilder.append(((Extractor) o).join());
                    stringBuilder.append(this.pop);
                }
            }
            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return "Extractor{" +
                    "extracted=" + extracted +
                    '}';
        }
    }

    private static class SplitExtractor implements IStateHandler<String>, ICallback<String> {
        private final String push;
        private final String pop;
        private final String delimiter;
        private final List<String> result = new ArrayList<>();
        private StringBuilder current = new StringBuilder();

        private SplitExtractor(String push, String pop, String delimiter) {
            this.push = push;
            this.pop = pop;
            this.delimiter = delimiter;
        }

        @Override
        public void trial(String element, List<IStateHandler<String>> stack) {
            if (delimiter.equals(element)) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                if (push.equals(element))
                    stack.add(new SplitMask(this, push, pop));
                current.append(element);
            }
        }

        public void finalizeString() {
            result.add(current.toString());
            current = new StringBuilder();
        }

        @Override
        public void call(String value) {
            current.append(value);
        }

        public List<String> getResult() {
            return result;
        }
    }

    private static class SplitMask implements IStateHandler<String>, ICallback<String> {
        private final ICallback<String> parent;
        private final String push;
        private final String pop;
        private final StringBuilder result = new StringBuilder();

        private SplitMask(ICallback<String> parent, String push, String pop) {
            this.parent = parent;
            this.push = push;
            this.pop = pop;
        }

        @Override
        public void trial(String element, List<IStateHandler<String>> stack) {
            result.append(element);
            if (push.equals(element))
                stack.add(new SplitMask(this, push, pop));
            if (pop.equals(element)) {
                stack.remove(this);
                parent.call(this.result.toString());
            }
        }

        @Override
        public void call(String value) {
            result.append(value);
        }
    }


    private static class ExtractorState extends AbstractStackedMachine<String> {
    }

    public static Extractor extractString(String s, String push, String pop) {
        Stream<String> ss = s.chars().mapToObj(Character::toString);
        ExtractorState state = new ExtractorState();
        Extractor result = new Extractor(push, pop);
        state.getStack().add(result);
        ss.forEach(state::step);
        result.finalizeString();
        return result;
    }

    public static List<String> unwrapString(String s, String push, String pop) {
        Extractor recursive = extractString(s, push, pop);
        return recursive.extracted.stream().map(o -> {
            if (o instanceof String)
                return (String) o;
            if (o instanceof Extractor)
                return ((Extractor) o).join();
            return "";
        }).collect(Collectors.toList());
    }

    public static List<String> splitString(String s, String delimiter, String push, String pop) {
        Stream<String> ss = s.chars().mapToObj(Character::toString);
        SplitExtractor extractor = new SplitExtractor(push, pop, delimiter);
        ExtractorState state = new ExtractorState();
        state.getStack().add(extractor);
        ss.forEach(state::step);
        extractor.finalizeString();
        return extractor.getResult();
    }
}
