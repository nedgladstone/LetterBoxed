package com.gmail.nedgladstone.LetterBoxed;

import java.io.PrintStream;

class Dictionary {
    public Dictionary() {
    }

    public void addWord(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); ++i) {
            final boolean isWord = (i == word.length() - 1);
            node = node.children.computeIfAbsent(word.charAt(i), n -> new TrieNode(isWord));
        }
    }

    public Cursor checkFragment(String fragment) {
        TrieNode node = root;
        for (int i = 0; i < fragment.length(); ++i) {
            node = node.children.get(fragment.charAt(i));
            if (node == null) {
                break;
            }
        }
        return new Cursor(node);
    }

    public Cursor getCursor() {
        return new Cursor(root);
    }

    public Cursor advanceCursor(Cursor cursor, char nextChar) {
        return new Cursor(cursor.node.children.get(nextChar));
    }

    public int numWords() {
        return root.countWords();
    }

    public int numNodes() {
        return root.countNodes();
    }

    public void dumpWords(PrintStream out) {
        out.printf("Dictionary contents (%d words):\n", numWords());
        root.dumpTrieWords(new StringBuilder(), out);
    }

    public void dumpStructure(PrintStream out) {
        out.printf("Dictionary structure (%d nodes):\n", numNodes());
        root.dumpTrieStructure(0, out);
    }

    public void dumpWordsUsingAllLetters(String letters, PrintStream out) {
        out.printf("Words containing all letters %s:\n", letters);
        root.dumpTrieWordsUsingAllLetters(letters, out);
    }
    

    private final TrieNode root = new TrieNode(false);
}

