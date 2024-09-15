package com.gmail.nedgladstone.LetterBoxed;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class State {
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
        outputBuilder.append("sides: ");
        sides.stream().forEach(s -> outputBuilder.append(s).append(" "));
        outputBuilder.append(String.format(", lastSideUsedIndex: %d, words: ", lastSideUsedIndex));
        words.stream().forEach(w -> outputBuilder.append(w).append(" "));
        outputBuilder.append(String.format(", fragment: %s", fragment));
        return outputBuilder.toString();
    }

    public final List<Side> sides;
    public final int lastSideUsedIndex;
    public final List<String> words;
    public final String fragment;
}