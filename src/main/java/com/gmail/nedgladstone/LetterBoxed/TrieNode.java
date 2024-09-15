package com.gmail.nedgladstone.LetterBoxed;

import java.util.HashMap;
import java.util.Map;
import java.io.PrintStream;

class TrieNode {
    TrieNode(boolean isWord) {
        this.isWord = isWord;
    }

    int countWords() {
        return isWord ? 1 : 0 + children.values().stream()
                .mapToInt(node -> node.countWords())
                .sum();
    }

    int countNodes() {
        return 1 + children.values().stream()
                .mapToInt(node -> node.countNodes())
                .sum();
    }

    void dumpTrieWords(StringBuilder partialWord, PrintStream out) {
        if (isWord) {
            out.println(partialWord);
        }
        children.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            StringBuilder sb = new StringBuilder(partialWord);
            sb.append(e.getKey());
            e.getValue().dumpTrieWords(sb, out);
        });
    }

    void dumpTrieStructure(int levels, PrintStream out) {
        children.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            out.printf("%s%c%c\n", "-".repeat(levels), e.getKey(), (e.getValue().isWord ? '*' : ' '));
            e.getValue().dumpTrieStructure(levels + 1, out);
        });
    }

    void dumpTrieWordsUsingAllLetters(String letters, PrintStream out) {
    }


    final boolean isWord;
    final Map<Character, TrieNode> children = new HashMap<>();
}