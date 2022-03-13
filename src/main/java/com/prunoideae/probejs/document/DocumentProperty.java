package com.prunoideae.probejs.document;

import java.util.List;

public abstract class DocumentProperty implements IConcrete {
    protected DocumentComment comment;

    @Override
    public void acceptDeco(List<IDecorative> decorates) {
        for (IDecorative decorative : decorates) {
            if (decorative instanceof DocumentComment) {
                this.comment = (DocumentComment) decorative;
            }
        }
    }
}
