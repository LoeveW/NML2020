package com.example.pupildilation;

import java.util.Random;

public class Card {

    private Random random;

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;
    }

    private static final int MIN_RANK = 2;
    private static final int MAX_RANK = 14;

    private int rank;
    private Suit suit;

    public Card(int rank, Suit suit) {
        this.random = new Random();

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

    /**
     * Converts card rank to a string representation, according to the filename of the image resource.
     * @return String representationn of rank.
     */
    private String rankToString() {
        if(this.rank > 10) {
            switch(rank) {
                case 11:
                    return "j";
                case 12:
                    return "q";
                case 13:
                    return "k";
                case 14:
                    return "a";
            }
        }
        return "" + rank;
    }

    /**
     * Converts card suit to a string representation, according to the filename of the image resource.
     * @return String representationn of suit.
     */
    private String suitToString() {
        switch(this.suit) {
            case HEARTS:
                return "h";
            case SPADES:
                return "s";
            case CLUBS:
                return "c";
            default:
                return "d";
        }
    }

    @Override
    public String toString() {
        return suitToString() + rankToString();
    }

    /**
     * Checks whether two cards are equal.
     * @param that the other card
     * @return true if equal, false if not.
     */
    public boolean equals(Card that) {
        return this.rank == that.rank && this.suit == that.suit;
    }
}
