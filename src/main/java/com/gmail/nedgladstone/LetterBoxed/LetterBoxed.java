package com.gmail.nedgladstone.LetterBoxed;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
        dictionary.dumpWords(System.out);
        System.out.println();
        dictionary.dumpStructure(System.out);
        System.out.println();
    }

    public void findTopSolutions() {
        State state = new State(rawSides);
        tryNext(state, 0);
    }

    public void dumpSolutions(PrintStream out) {
        out.printf("\nTop solutions: (%d fragments tested)\n", fragmentsTested);
        solutions.stream().sorted(Comparator.reverseOrder()).forEach(s -> out.println(s)); 
    }

    private void tryNext(State state, int depth) {
        for (int s = 0; s < state.sides.size(); ++s) {
            if (state.lastSideUsedIndex == s) {
                continue;
            }
            Side side = state.sides.get(s);
            for (int l = 0; l < side.letters.size(); ++l) {
                Letter letter = side.letters.get(l);
                if (state.fragment.length() == 0) {
                    System.out.printf("Processing side %d, letter %s, fragments tested %d\n", s, letter, fragmentsTested);
                }
                String newFragment = state.fragment + letter.letter;
                ++fragmentsTested;
                // System.out.printf("%sNew fragment: %s, State: %s\n", " ".repeat(depth + 1), newFragment, state.toString());
                if ((state.words.size() > 0)
                        && (newFragment.equals(state.words.get(state.words.size() - 1)))) {
                    // no need to keep processing this fragment if we've just repeated the last word
                    // System.out.printf("%sDuplicated word %s\n", " ".repeat(depth + 2), newFragment);
                    continue;
                }
                Cursor fragmentCursor = dictionary.checkFragment(newFragment);
                if (fragmentCursor.isValidPrefix()) {
                    State newState = new State(state, s, l, false);
                    tryNext(newState, depth + 1);
                } else {
                    // System.out.printf("%sNot valid prefix\n", " ".repeat(depth + 2));
                }
                if (fragmentCursor.isValidWord()) {
                    State newState = new State(state, s, l, true);
                    if (newState.isComplete()) {
                        System.out.printf("Solution: %s\n", newState.words.toString());
                        addSolution(new Solution(newState.words));
                    } else {
                        if (newState.words.size() < maxNumWords) {
                            // System.out.printf("%s%s is a Word! New state: %s\n", " ".repeat(depth + 1), newFragment, newState.toString());
                            tryNext(newState, depth + 1);
                        } else {
                            // System.out.printf("%s%s is a word, but we're at max words. New state: %s\n", " ".repeat(depth + 2), newFragment, newState.toString());
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
        System.out.println("Building dictionary...");
        try (Stream<String> lines = Files.lines(Paths.get(dictionaryPath))) {
            lines.filter(word -> isValidWord(word)).forEach(word -> dictionary.addWord(word));
        } catch (IOException e) {
            System.err.println("Unable to read dictionary from file " + dictionaryPath);
        }
        System.out.println();
    }

    private boolean isValidWord(String word) {
        if (word.length() < 3) {
            // System.out.printf("Not adding %s because it is too short\n", word);
            return false;
        }

        String lettersToFind = rawSides.stream().collect(StringBuilder::new, (x, y) -> x.append(y), (a, b) -> a.append(b)).toString();
        int prevSideNum = -1;
        for (int c = 0; c < word.length(); ++c) {
            char letter = word.charAt(c);
            int sideNum = letterSides[letter - 'A'];
            if ((sideNum == -1) || (sideNum == prevSideNum)) {
                return false;
            }
            lettersToFind = lettersToFind.replaceFirst(String.valueOf(letter), "");
            prevSideNum = sideNum;
        }
        // System.out.printf("Adding %s\n", word);
        if (lettersToFind.length() == 0) {
            System.out.printf("%s is a one-word solution!\n", word);
        }
        return true;
    }

    private static void displayUsage() {
        System.err.println("Usage: LetterBoxed <north> <east> <south> <west>");
    }


    private static final int MAX_NUM_SOLUTIONS = 20;

    private List<String> rawSides;

    private int letterSides[] = new int[26];

    private Dictionary dictionary;

    private SortedSet<Solution> solutions = new TreeSet<>();

    private int maxNumWords = 4;

    int fragmentsTested = 0;
}