package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


import java.lang.reflect.Field;
import java.util.Random;

public class Game1Activity extends AppCompatActivity {
    private static final int NR_CARDS = 3;

    private Card[] cards;
    private ImageView[] imageViews;
    private String[] deck;
    private int trial;
    private String userAnswers;
    private String trueAnswers;
    private String liedAnswers;

    private Random random;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_one);

        Intent i = getIntent();
        this.trial = i.getIntExtra("trial", -1);
        readResultStrings(i);
        this.random = new Random();
        this.cards = getRandomCards();
        this.imageViews = generateImageViews();
        this.deck = deckToStringArr(this.cards);

        setCardImages();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent intent = new Intent(Game1Activity.this, Game2Activity.class);
                intent.putExtra("deck", deck);
                intent.putExtra("trial", trial);
                intent.putExtra("userAnswers", userAnswers);
                intent.putExtra("trueAnswers", trueAnswers);
                intent.putExtra("liedAnswers", liedAnswers);

                startActivityForResult(intent, 1);
            }
        }, 5000);
    }

    private void readResultStrings(Intent i) {
        if(this.trial <= 1){
            this.userAnswers = new String();
            this.trueAnswers = new String();
            this.liedAnswers = new String();
        }
        else{
            this.userAnswers = i.getStringExtra("userAnswers");
            this.trueAnswers = i.getStringExtra("trueAnswers");
            this.liedAnswers = i.getStringExtra("liedAnswers");
        }
    }

    public Card[] getCards(){
        return this.cards;
    }
    public ImageView[] getImageViews(){
        return this.imageViews;
    }
    private void setCardImages() {
        for (int i = 0; i < this.imageViews.length; i++){
            System.out.println("filename: " + cards[i].toString());
            int resID = getResId(cards[i].toString(), R.drawable.class);
            this.imageViews[i].setImageResource(resID); //
        }
    }

    private ImageView[] generateImageViews(){
        ImageView[] iv = new ImageView[3];
        iv[0] = (ImageView) findViewById(R.id.imageView1);
        iv[1] = (ImageView) findViewById(R.id.imageView2);
        iv[2] = (ImageView) findViewById(R.id.imageView3);
        return iv;
    }

    private Card[] getRandomCards(){
        Card[] cardsTemp = new Card[3];
        Card.Suit[] suits = Card.getSuits();
        for(int i = 0; i < NR_CARDS; i++){
            int rank = this.random.nextInt(12) + 2;
            int suitVal = this.random.nextInt(4);
            cardsTemp[i] = new Card(rank, suits[suitVal]);
        }
                if(cardsTemp[0].equals(cardsTemp[1]) ||
                        cardsTemp[0].equals(cardsTemp[2])||
                        cardsTemp[1].equals(cardsTemp[0])||
                        cardsTemp[1].equals(cardsTemp[2])||
                        cardsTemp[2].equals(cardsTemp[0])||
                        cardsTemp[2].equals(cardsTemp[1])){
                    return getRandomCards();
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
    private String[] deckToStringArr(Card[] cards){
        String[] s = new String[cards.length];
        for(int i = 0; i < cards.length; i++){
            s[i] = cards[i].toString();
        }
        return s;
    }
}
