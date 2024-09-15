package com.gmail.nedgladstone.LetterBoxed;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Side {
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