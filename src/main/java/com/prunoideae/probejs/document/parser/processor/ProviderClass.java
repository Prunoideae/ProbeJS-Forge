package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.DocumentClass;
import com.prunoideae.probejs.document.IConcrete;
import com.prunoideae.probejs.document.IDecorative;
import com.prunoideae.probejs.document.IDocument;
import com.prunoideae.probejs.document.parser.handler.IStateHandler;
import com.prunoideae.probejs.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ProviderClass implements IStateHandler<String>, IDocumentProvider<DocumentClass> {
    public static List<Pair<Predicate<String>, BiFunction<String, ProviderClass, IStateHandler<String>>>> handlers = new ArrayList<>();
    private final List<IDocumentProvider<?>> elements = new ArrayList<>();

    public void addMultiHandler(Predicate<String> condition, BiFunction<String, ProviderClass, IStateHandler<String>> handler) {
        handlers.add(new Pair<>(condition, handler));
    }

    public void addSingleHandler(Predicate<String> condition, BiConsumer<String, ProviderClass> handler) {
        handlers.add(new Pair<>(condition, (s, documentHandler) -> {
            handler.accept(s, documentHandler);
            return null;
        }));
    }

    public void addElement(IDocumentProvider<?> element) {
        this.elements.add(element);
    }

    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        // TODO: Add trial for head and tail

        for (Pair<Predicate<String>, BiFunction<String, ProviderClass, IStateHandler<String>>> multiHandler : handlers) {
            if (multiHandler.getFirst().test(element)) {
                IStateHandler<String> layer = multiHandler.getSecond().apply(element, this);
                if (layer != null)
                    stack.add(layer);
                return;
            }
        }
    }

    @Override
    public DocumentClass provide() {
        DocumentClass document = new DocumentClass();
        List<IDecorative> decos = new ArrayList<>();
        for (IDocumentProvider<?> provider : elements) {
            IDocument doc = provider.provide();
            if (doc instanceof IDecorative) {
                decos.add((IDecorative) doc);
            } else {
                if (doc instanceof IConcrete) {
                    ((IConcrete) doc).acceptDeco(decos.stream().toList());
                }
                decos.clear();
                document.acceptProperty(doc);
            }
        }
        return document;
    }
}
