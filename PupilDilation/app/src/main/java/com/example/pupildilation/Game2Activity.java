package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game2Activity extends AppCompatActivity {

    private Card[] cards;
    private Card[] cardsFalse;
    private Card[] cardsQuery;
    private int count = 0;

    private int trial;
    private Random random;

    private String userAnswers;
    private String trueAnswers;
    private String liedAnswers;
    private boolean clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_two);
        Intent intent = getIntent();
        init(intent);
        ImageButton yes = (ImageButton) findViewById(R.id.yes);
        ImageButton no = (ImageButton) findViewById(R.id.no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAnswers = userAnswers + "0";
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAnswers = userAnswers + "0";
                clicked = true;
            }
        });
        final Handler handler = new Handler();

        final Runnable runnable = new Runnable() {
            public void run() {
                ImageView queryView = (ImageView) findViewById(R.id.imageView4);
                int resID = getResId(cardsQuery[count].toString(), R.drawable.class); //changed method to a toString.
                queryView.setImageResource(resID);
                count++;
                if (count < 6) {
                    handler.postDelayed(this, 5000);

                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nextTask();
                        }
                    }, 5000);
                }
            }
        };

        handler.post(runnable);
    }

    private void init(Intent intent) {
        String[] deck = intent.getStringArrayExtra("deck");
        readResultStrings(intent);
        this.trial = intent.getIntExtra("trial", -1);
        this.random = new Random();
        this.cards = new Card[deck.length];
        this.cardsFalse = new Card[this.cards.length];
        this.cardsQuery = new Card[this.cards.length + this.cardsFalse.length];
        setCards(deck);
        this.cardsFalse = getRandomCards();
        this.cardsQuery = getQueryCardArr(this.cards, this.cardsFalse);
        this.trial++;
        this.clicked = false;
    }


    private void readResultStrings(Intent i) {
        this.userAnswers = i.getStringExtra("userAnswers");
        this.trueAnswers = i.getStringExtra("trueAnswers");
        this.liedAnswers = i.getStringExtra("liedAnswers");
    }

    private void nextTask() {
        if (this.trial <= 2) { //nr of trials / games to be played
            Intent i = new Intent(Game2Activity.this, Game1Activity.class);
            i.putExtra("trial", trial);
            i.putExtra("userAnswers", userAnswers);
            i.putExtra("trueAnswers", trueAnswers);
            i.putExtra("liedAnswers", liedAnswers);

            startActivityForResult(i, 1);
        } else {
            Intent i = new Intent(Game2Activity.this, ResultsActivity.class);
            i.putExtra("userAnswers", userAnswers);
            i.putExtra("trueAnswers", trueAnswers);
            i.putExtra("liedAnswers", liedAnswers);
            startActivityForResult(i, 1);
        }
    }

    private void setCards(String[] deck) {
        for (int i = 0; i < deck.length; i++)
            this.cards[i] = cardFromString(deck[i]);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Card cardFromString(String s) {
        Card.Suit suit;
        int rank;
        String x = s.substring(0, 1);
        String y = s.substring(1);

        if (x.equals("h"))
            suit = Card.Suit.HEARTS;
        else if (x.equals("s"))
            suit = Card.Suit.SPADES;
        else if (x.equals("c"))
            suit = Card.Suit.CLUBS;
        else
            suit = Card.Suit.DIAMONDS;

        if (y.equals("j"))
            rank = 11;
        else if (y.equals("q"))
            rank = 12;
        else if (y.equals("k"))
            rank = 13;
        else if (y.equals("a"))
            rank = 14;
        else
            rank = Integer.parseInt(y);

        return new Card(rank, suit);
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

    private Card[] getRandomCards() {
        Card[] cardsTemp = new Card[3];
        Card.Suit[] suits = Card.getSuits();
        for (int i = 0; i < 3; i++) {
            int rank = this.random.nextInt(12) + 2;
            int suitVal = this.random.nextInt(4);
            cardsTemp[i] = new Card(rank, suits[suitVal]);
            for (int j = 0; j < 3; j++) {
                if (cards[j].equals(cardsTemp[i])) {
                    getRandomCards();
                }
            }
        }
        return cardsTemp;
    }

    private Card[] getQueryCardArr(Card[] arr1, Card[] arr2) {
        Card[] arrFinal = new Card[arr1.length + arr2.length];
        for (int i = 0; i < arrFinal.length; i++) {
            if (i < arr1.length)
                arrFinal[i] = arr1[i];
            else
                arrFinal[i] = arr2[i - arr1.length];
        }
        List<Card> shuffleList = Arrays.asList(arrFinal);
        Collections.shuffle(shuffleList);
        Card[] arrShuffled = (Card[]) shuffleList.toArray();

        boolean b = false;
        for (int i = 0; i < arrShuffled.length; i++) {
            for (int j = 0; j < arr1.length; j++) {
                if (arr1[j].equals(arrShuffled[i])) {
                    this.trueAnswers = this.trueAnswers + "0";
                    b = true;
                }
            }
            if (!b) {
                this.trueAnswers = this.trueAnswers + "-";
            }
            b = false;
        }
        return arrShuffled;
    }

    private String[] deckToStringArr(Card[] cards) {
        String[] s = new String[cards.length];
        for (int i = 0; i < cards.length; i++) {
            s[i] = cards[i].toString();
        }
        return s;
    }
}
