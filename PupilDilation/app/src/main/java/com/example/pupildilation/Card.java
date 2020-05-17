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

    public String getFileName() {
        return suitToString() + rankToString();
    }
}
