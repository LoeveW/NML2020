package com.example.pupildilation;

public class Card {

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;
    }

    private static final int MIN_RANK = 1;
    private static final int MAX_RANK = 13;

    private int rank;
    private Suit suit;

    public Card(int rank, Suit suit) {
        setRank(rank);
        setSuit(suit);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        if (rank < MIN_RANK || rank > MAX_RANK)
            throw new RuntimeException(
                    String.format("Invalid rank: %d (must be between %d and %d inclusive)",
                            rank, MIN_RANK, MAX_RANK));
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        if (suit == null)
            throw new RuntimeException("Suit must be non-null");
        this.suit = suit;
    }

    public static int getMinRank() {
        return MIN_RANK;
    }

    public static int getMaxRank() {
        return MAX_RANK;
    }

    public static Suit[] getSuits() {
        return Suit.values();
    }

}
