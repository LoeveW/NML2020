package com.example.pupildilation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class Game1Activity extends AppCompatActivity {
    private Card[] cards;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_game_one);

        this.cards = getRandomCards();
        this.random = new Random();
    }

    private Card[] getRandomCards(){
        Card[] cardsTemp = new Card[3];
        for(int i = 0; i < 3; i++){
            int rank = this.random.nextInt(13) + 1;
            int suitnum = this.random.nextInt(4);
            Card.Suit suit = null;
            switch (suitnum){
                case 0:
                    suit = Card.Suit.CLUBS;
                    break;
                case 1:
                    suit = Card.Suit.DIAMONDS;
                    break;
                case 2:
                    suit = Card.Suit.HEARTS;
                    break;
                default:
                    suit = Card.Suit.SPADES;
                    break;
            }

            cardsTemp[i] = new Card(rank, suit);
        }
        return cardsTemp;
    }

}
