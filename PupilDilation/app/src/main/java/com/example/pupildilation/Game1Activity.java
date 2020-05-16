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

        this.random = new Random();
        this.cards = getRandomCards();
    }

    private Card[] getRandomCards(){
        Card[] cardsTemp = new Card[3];
        Card.Suit[] suits = Card.getSuits();
        for(int i = 0; i < 3; i++){
            int rank = this.random.nextInt(13) + 1;
            int suitVal = this.random.nextInt(4);
            cardsTemp[i] = new Card(rank, suits[suitVal]);
        }
        return cardsTemp;
    }
}
