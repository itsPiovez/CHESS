package com.example.chess;

public class TurnChecker {

    public static boolean isWhiteTurn(String tag) {
        return tag.equals("wp") || tag.equals("wr") || tag.equals("wn") ||
                tag.equals("wb") || tag.equals("wq") || tag.equals("wk");
    }

    public static boolean isBlackTurn(String tag) {
        return tag.equals("bp") || tag.equals("br") || tag.equals("bn") ||
                tag.equals("bb") || tag.equals("bq") || tag.equals("bk");
    }

    public static boolean canMove(String tag, boolean isWhiteTurn) {
        if (isWhiteTurn) {
            return isWhiteTurn(tag);
        } else {
            return isBlackTurn(tag);
        }
    }
}
