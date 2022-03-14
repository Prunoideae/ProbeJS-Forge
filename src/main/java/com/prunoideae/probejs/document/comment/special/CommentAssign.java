package com.prunoideae.probejs.document.comment.special;

import com.prunoideae.probejs.document.comment.AbstractComment;
import com.prunoideae.probejs.document.type.IType;
import com.prunoideae.probejs.document.type.Resolver;

public class CommentAssign extends AbstractComment {
    private final IType type;

    public CommentAssign(String line) {
        super(line);
        type = Resolver.resolveType(line.substring(8));
    }

    public IType getType() {
        return type;
    }
}
