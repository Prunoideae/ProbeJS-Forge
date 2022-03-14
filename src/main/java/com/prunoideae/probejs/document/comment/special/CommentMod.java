package com.prunoideae.probejs.document.comment.special;

import com.prunoideae.probejs.document.comment.AbstractComment;
import dev.architectury.platform.Platform;

public class CommentMod extends AbstractComment {
    private final String mod;

    public CommentMod(String line) {
        super(line);
        mod = line.substring(5);
    }

    public boolean isLoaded() {
        return Platform.isModLoaded(mod);
    }
}
