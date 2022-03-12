package com.prunoideae.probejs.document.comment;

import java.util.HashMap;
import java.util.function.Function;

public class CommentHandler {
    public static final HashMap<String, Function<String, AbstractComment>> specialCommentHandler = new HashMap<>();

    public static void init() {
        specialCommentHandler.put("@hidden", CommentHidden::new);
        specialCommentHandler.put("@modify", CommentModify::new);
        specialCommentHandler.put("@remove", CommentRemove::new);
        specialCommentHandler.put("@target", CommentTarget::new);
    }
}
