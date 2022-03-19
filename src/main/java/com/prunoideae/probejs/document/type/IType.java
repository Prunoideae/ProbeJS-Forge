package com.prunoideae.probejs.document.type;

import java.util.List;
import java.util.Set;

public interface IType {
    String getTypeName();

    Set<String> getAssignableNames();
}
