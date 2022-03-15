package com.prunoideae.probejs.document.comment;

import com.prunoideae.probejs.document.DocumentComment;
import com.prunoideae.probejs.document.comment.special.CommentHidden;
import com.prunoideae.probejs.document.comment.special.CommentMod;
import com.prunoideae.probejs.document.comment.special.CommentModify;
import com.prunoideae.probejs.document.type.IType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentUtil {
    public static boolean isLoaded(DocumentComment comment) {
        if (comment == null)
            return true;
        List<CommentMod> mod = comment.getSpecialComments(CommentMod.class).stream().map(c -> (CommentMod) c).collect(Collectors.toList());
        return mod.stream().allMatch(CommentMod::isLoaded);
    }

    public static boolean isHidden(DocumentComment comment) {
        if (comment == null)
            return false;
        return comment.getSpecialComment(CommentHidden.class) != null;
    }

    public static Map<String, IType> getTypeModifiers(DocumentComment comment) {
        Map<String, IType> modifiers = new HashMap<>();
        if (comment != null) {
            comment.getSpecialComments(CommentModify.class).forEach(m -> {
                if (m instanceof CommentModify modify) {
                    modifiers.put(modify.getName(), modify.getType());
                }
            });
        }
        return modifiers;
    }
}
