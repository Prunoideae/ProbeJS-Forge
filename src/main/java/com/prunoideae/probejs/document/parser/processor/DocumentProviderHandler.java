package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.DocumentField;
import com.prunoideae.probejs.document.DocumentMethod;

public class DocumentProviderHandler {

    public static void init() {
        DocumentHandler.addMultiHandler(c -> {
            String cs = c.strip();
            return cs.startsWith("class") && c.endsWith("{");
        }, (s, d) -> {
            ProviderClass clazz = new ProviderClass();
            d.addElement(clazz);
            return clazz;
        });
        DocumentHandler.addMultiHandler(c -> c.strip().startsWith("/**"), (s, d) -> {
            ProviderComment comment = new ProviderComment();
            d.addElement(comment);
            return comment;
        });

        ProviderClass.addMultiHandler(s -> s.strip().startsWith("/**"), (s, d) -> {
            ProviderComment comment = new ProviderComment();
            d.addElement(comment);
            return comment;
        });

        ProviderClass.addSingleHandler(s -> s.contains(":") && !s.contains("("), (s, d) -> {
            if (s.endsWith(";"))
                s = s.substring(0, s.length() - 1);
            d.addElement(new DocumentField(s));
        });
        ProviderClass.addSingleHandler(s -> s.contains("("), (s, d) -> {
            if (s.endsWith(";"))
                s = s.substring(0, s.length() - 1);
            d.addElement(new DocumentMethod(s));
        });
    }
}
