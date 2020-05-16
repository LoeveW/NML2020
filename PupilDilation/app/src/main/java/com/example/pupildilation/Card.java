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

    private String rankToString() {
        if(this.rank > 10) {
            switch(rank) {
                case 11:
                    return "J";
                case 12:
                    return "Q";
                case 13:
                    return "K";
                case 14:
                    return "A";
            }
        }
        return "" + rank;
    }

    private String suitToString() {
        switch(this.suit) {
            case HEARTS:
                return "H";
            case SPADES:
                return "S";
            case CLUBS:
                return "C";
            default:
                return "D";
        }
    }

    public String getFileName() {
        return rankToString() + suitToString() + ".png";
    }
}
