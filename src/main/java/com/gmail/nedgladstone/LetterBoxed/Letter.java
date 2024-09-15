package com.gmail.nedgladstone.LetterBoxed;

class Letter {
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