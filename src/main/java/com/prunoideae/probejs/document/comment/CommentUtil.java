package com.prunoideae.probejs.document.comment;

import com.prunoideae.probejs.document.DocumentComment;
import com.prunoideae.probejs.document.comment.special.CommentHidden;
import com.prunoideae.probejs.document.comment.special.CommentMod;

public class CommentUtil {
    public static boolean isLoaded(DocumentComment comment) {
        if (comment == null)
            return true;
        CommentMod mod = (CommentMod) comment.getSpecialComment(CommentMod.class);
        return mod == null || mod.isLoaded();
    }

    public static boolean isHidden(DocumentComment comment) {
        if (comment == null)
            return false;
        return comment.getSpecialComment(CommentHidden.class) != null;
    }
}
