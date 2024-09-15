package com.gmail.nedgladstone.LetterBoxed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LetterBoxed {
    public static void main(String[] args) {
        List<String> sides = new ArrayList<>(Arrays.asList(args));
        if (sides.size() < 2) {
            // displayUsage();
            // return;
            sides.add("ABCD");
            sides.add("EFGH");
            sides.add("IJKL");
            sides.add("MNOP");
        }
        LetterBoxed letterBoxed = new LetterBoxed(sides);
        letterBoxed.findTopSolutions();
        letterBoxed.dumpSolutions(System.out);
    }

    private static void displayUsage() {
        System.err.println("Usage: LetterBoxed <north> <east> <south> <west>");
    }


    private static class Letter {
        public Letter(char letter, boolean hasBeenUsed) {
            this.letter = letter;
            this.hasBeenUsed = hasBeenUsed;
        }

        public Letter(Letter other) {
            this.letter = other.letter;
            this.hasBeenUsed = other.hasBeenUsed;
        }
        
        public String toString() {
            return String.format("%c", (hasBeenUsed ? letter - 'A' + 'a' : letter)); 
        }

        public final char letter;
        public final boolean hasBeenUsed;
    }


    private static class Side {
        public Side(String rawLetters) {
            this.letters = new ArrayList<Letter>();
            rawLetters.chars().forEach(rl -> this.letters.add(new Letter((char)rl, false)));
        }

        public Side(Side other) {
            this.letters = other.letters.stream().map(Letter::new).collect(Collectors.toList());
        }

        public void markLetterUsed(int letterIndex) {
            letters.set(letterIndex, new Letter(letters.get(letterIndex).letter, true));
        }

        public String toString() {
            StringBuilder outputBuilder = new StringBuilder();
            letters.stream().forEach(l -> outputBuilder.append(l));
            return outputBuilder.toString();
        }

        public final List<Letter> letters;
    }


    private static class State {
        public State(List<String> rawSides) {
            this.sides = new ArrayList<Side>();
            rawSides.stream().forEach(rs -> this.sides.add(new Side(rs)));
            this.lastSideUsedIndex = -1;
            this.words = new ArrayList<>();
            this.fragment = "";
        }

        public State(State other, int sideUsedIndex, int letterUsedIndex, boolean usedAsWord) {
            this.sides = other.sides.stream().map(Side::new).collect(Collectors.toList());
            Side sideUsed = this.sides.get(sideUsedIndex);
            sideUsed.markLetterUsed(letterUsedIndex);
            Letter letterUsed = sideUsed.letters.get(letterUsedIndex);
            this.lastSideUsedIndex = sideUsedIndex;
            this.words = new ArrayList<String>(other.words);
            String newFragment = other.fragment + letterUsed.letter;
            if (usedAsWord) {
                this.words.add(newFragment);
                this.fragment = "" + letterUsed.letter;
            } else {
                this.fragment = newFragment;
            }
        }

        public boolean isComplete() {
            return sides.stream().allMatch(s -> s.letters.stream().allMatch(l -> l.hasBeenUsed));
        }

        public String toString() {
            StringBuilder outputBuilder = new StringBuilder();
            outputBuilder.append("State: ");
            sides.stream().forEach(s -> outputBuilder.append(s).append(" "));
            outputBuilder.append("\n");
            return outputBuilder.toString();
        }

        public final List<Side> sides;
        public final int lastSideUsedIndex;
        public final List<String> words;
        public final String fragment;
    }


    private static class Solution implements Comparable<Solution> {
        public List<String> words;

        public Solution(List<String> words) {
            this.words = new ArrayList<String>(words);
        }

        // Best solutions have fewer words
        // For two solutions with the same number of words, the better solution has fewer letters
        public int compareTo(Solution other) {
            int wordCountDifference = other.words.size() - this.words.size();
            if (wordCountDifference != 0) {
                return wordCountDifference;
            }

            int letterCountDifference = other.getLetterCount() - this.getLetterCount();
            if (letterCountDifference != 0) {
                return letterCountDifference;
            };
            return other.words.hashCode() - this.words.hashCode();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            words.stream().forEach(w -> sb.append(w).append(" "));
            sb.append(String.format("[%d, %d]", words.size(), getLetterCount()));
            return sb.toString();
        }

        private int getLetterCount() {
            return words.stream().mapToInt(String::length).sum();
        }
    }


    private static class Dictionary {
        public static class Cursor {
            Cursor(TrieNode node) {
                this.node = node;
            }

            public boolean isValidFragment() {
                return node != null;
            }

            public boolean isValidWord() {
                return node != null && node.isWord;
            }

            final TrieNode node;
        }

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

        public void dumpWords(PrintStream out) {
            dumpTrieWordsNode(root, new StringBuilder(), out);
        }

        public void dumpStructure(PrintStream out) {
            dumpTrieStructureNode(root, 0, out);
        }
        
        private static class TrieNode {
            TrieNode(boolean isWord) {
                this.isWord = isWord;
            }

            final boolean isWord;
            final Map<Character, TrieNode> children = new HashMap<>();
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

        private static void dumpTrieStructureNode(TrieNode trie, int levels, PrintStream out) {
            trie.children.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
                out.printf("%s%c%c\n", "-".repeat(levels), e.getKey(), (e.getValue().isWord ? '*' : ' '));
                dumpTrieStructureNode(e.getValue(), levels + 1, out);
            });
        }


        private final TrieNode root = new TrieNode(false);
    }


    public LetterBoxed(List<String> rawSides) {
        this.rawSides = rawSides;

        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            this.letterSides[ch - 'A'] = -1;
        }
        for (int sideNum = 0; sideNum < rawSides.size(); ++sideNum) {
            String side = rawSides.get(sideNum);
            for (int c = 0; c < side.length(); ++c) {
                this.letterSides[side.charAt(c) - 'A'] = sideNum;
            }
        }

        this.dictionary = new Dictionary();

        readDictionary("src/main/resources/dict.txt");
        // dictionary.dumpWords(System.out);
        // dictionary.dumpStructure(System.out);
    }

    public void findTopSolutions() {
        State state = new State(rawSides);
        tryNext(state);
    }

    public void dumpSolutions(PrintStream out) {
        out.println("Solutions:");
        solutions.stream().sorted(Comparator.reverseOrder()).forEach(s -> out.println(s)); 
    }

    private void tryNext(State state) {
        for (int s = 0; s < state.sides.size(); ++s) {
            if (state.lastSideUsedIndex == s) {
                continue;
            }
            Side side = state.sides.get(s);
            for (int l = 0; l < side.letters.size(); ++l) {
                Letter letter = side.letters.get(l);
                if (state.fragment.length() == 0) {
                    System.out.printf("Processing side %d, letter %s\n", s, letter);
                }
                String newFragment = state.fragment + letter.letter;
                if ((state.words.size() > 0)
                        && (newFragment.equals(state.words.get(state.words.size() - 1)))) {
                    // no need to keep processing this fragment if we've just repeated the last word
                    continue;
                }
                Dictionary.Cursor fragmentCursor = dictionary.checkFragment(newFragment);
                if (fragmentCursor.isValidFragment()) {
                    State newState = new State(state, s, l, false);
                    // System.out.println("Frag " + newFragment);
                    tryNext(newState);
                }
                if (fragmentCursor.isValidWord()) {
                    State newState = new State(state, s, l, true);
                    if (newState.isComplete()) {
                        addSolution(new Solution(newState.words));
                    } else {
                        if (newState.words.size() < maxNumWords) {
                            tryNext(newState);
                        }
                    }
                }
            }
        }
    }

    private void addSolution(Solution solution) {
        // System.out.println(solution);
        if ((solutions.size() < MAX_NUM_SOLUTIONS)
                || (solution.compareTo(solutions.first()) > 0)) {
            solutions.add(solution);
        }
        if (solutions.size() > MAX_NUM_SOLUTIONS) {
            solutions.remove(solutions.first());
        }
        maxNumWords = solutions.first().words.size();
    }

    private void readDictionary(String dictionaryPath) {
        try (Stream<String> lines = Files.lines(Paths.get(dictionaryPath))) {
            lines.filter(word -> isValidWord(word)).forEach(word -> dictionary.addWord(word));
        } catch (IOException e) {
            System.err.println("Unable to read dictionary from file " + dictionaryPath);
        }
    }

    private boolean isValidWord(String word) {
        if (word.length() < 3) {
            System.out.printf("Not adding %s because it is too short\n", word);
            return false;
        }
        int prevSideNum = 
        if (! word.matches("^[" + validLetters + "]+$")) {
            // System.out.printf("Not adding %s because it contains characters other than %s\n", word, validLetters);
            return;
        }
        if (word.matches("\\b.*([A-Za-z])\\1.*\\b")) {
            System.out.printf("Not adding %s because it contains a double letter\n", word);
            return;
        }
        for (int i = 0; i < word.length(); ++i) {

        }

    }

    private static final int MAX_NUM_SOLUTIONS = 20;

    private List<String> rawSides;

    private int letterSides[] = new int[26];

    private Dictionary dictionary;

    private SortedSet<Solution> solutions = new TreeSet<>();

    private int maxNumWords = 4;
}