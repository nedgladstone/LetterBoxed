package com.gmail.nedgladstone.LetterBoxed;

public class Cursor {
    Cursor(TrieNode node) {
        this.node = node;
    }

    public boolean isValidFragment() {
        return (node != null);
    }

    public boolean isValidPrefix() {
        return ((node != null) && (node.children.size() > 0));
    }

    public boolean isValidWord() {
        return ((node != null) && node.isWord);
    }

    final TrieNode node;
}