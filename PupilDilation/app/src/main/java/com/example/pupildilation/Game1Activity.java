package com.example.pupildilation;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.Random;

public class Game1Activity extends AppCompatActivity {
    private Card[] cards;
    private ImageView[] imageViews;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_game_one);

        this.random = new Random();
        this.cards = getRandomCards();
        this.imageViews = getImageViews();
        setCardImages();
    }

    private void setCardImages() {
        for (int i = 0; i < this.imageViews.length; i++){
            int resID = getResId(cards[i].getFileName(), R.drawable.class);
            this.imageViews[i].setImageResource(resID);
        }

    }

    private ImageView[] getImageViews(){
        ImageView[] iv = new ImageView[3];

        iv[0] = (ImageView) findViewById(R.id.imageView1);
        iv[1] = (ImageView) findViewById(R.id.imageView2);
        iv[2] = (ImageView) findViewById(R.id.imageView3);


        return iv;
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

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
