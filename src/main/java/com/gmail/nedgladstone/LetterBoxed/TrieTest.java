package com.gmail.nedgladstone.LetterBoxed;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieTest {
    public static void main(String[] args) {
        TrieNode trie = buildTrie(Arrays.asList(args));
        dumpTrieWords(trie, System.out);
        dumpTrieStructure(trie, System.out);
    }

    private static class TrieNode {
        boolean isWord = false;
        Map<Character, TrieNode> children = new HashMap<>();
    }

    private static TrieNode buildTrie(List<String> words) {
        TrieNode root = new TrieNode();
        for (String word : words) {
            TrieNode node = root;
            for (int i = 0; i < word.length(); ++i) {
                node = node.children.computeIfAbsent(word.charAt(i), n -> new TrieNode());
            }
            node.isWord = true;
        }
        return root;
    }

    private static void dumpTrieWords(TrieNode trie, PrintStream out) {
        dumpTrieWordsNode(trie, new StringBuilder(), out);
    }

    private static void dumpTrieWordsNode(TrieNode trie, StringBuilder partialWord, PrintStream out) {
        if (trie.isWord) {
            out.println(partialWord);
        }
        trie.children.entrySet().stream().forEach(e -> {
            StringBuilder sb = new StringBuilder(partialWord);
            sb.append(e.getKey());
            dumpTrieWordsNode(e.getValue(), sb, out);
        });
    }

    private static void dumpTrieStructure(TrieNode trie, PrintStream out) {
        dumpTrieStructureNode(trie, 0, out);
    }

    private static void dumpTrieStructureNode(TrieNode trie, int levels, PrintStream out) {
        trie.children.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            out.printf("%s%c%c\n", "-".repeat(levels), e.getKey(), (e.getValue().isWord ? '*' : ' '));
            dumpTrieStructureNode(e.getValue(), levels + 1, out);
        });
    }
}