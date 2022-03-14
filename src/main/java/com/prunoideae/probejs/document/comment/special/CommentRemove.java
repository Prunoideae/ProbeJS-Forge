package com.prunoideae.probejs.document.comment.special;

import com.prunoideae.probejs.document.comment.AbstractComment;

public class CommentRemove extends AbstractComment {
    private final String name;

    public CommentRemove(String line) {
        super(line);
        name = line.substring(8);
    }

    public String getName() {
        return name;
    }
}
