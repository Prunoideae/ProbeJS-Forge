package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.IDocument;

public interface IDocumentProvider<T extends IDocument> {
    T provide();
}
