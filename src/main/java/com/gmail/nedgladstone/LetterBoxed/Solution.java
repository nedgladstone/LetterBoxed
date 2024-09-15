package com.gmail.nedgladstone.LetterBoxed;

import java.util.ArrayList;
import java.util.List;

class Solution implements Comparable<Solution> {
    public List<String> words;

    public Solution(List<String> words) {
        this.words = new ArrayList<String>(words);
    }

    // Better solution has fewer words
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