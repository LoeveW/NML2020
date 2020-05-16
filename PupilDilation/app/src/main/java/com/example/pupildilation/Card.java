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
        if (rank >= MIN_RANK || rank <= MAX_RANK)
            this.rank = rank;
        else
            System.out.println("This rank does not exist.");

        if(suit != null)
            this.suit = suit;
        else
            System.out.println("You must specify a suit.");
    }

    public int getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public static Suit[] getSuits() {
        return Suit.values();
    }

}
