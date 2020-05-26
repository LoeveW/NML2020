package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.Random;

import static com.example.pupildilation.R.id.progressBar;

public class Game1Activity extends AppCompatActivity {
    private static final int NR_CARDS = 3;

    private Card[] cards;
    private ImageView[] imageViews;
    private String[] deck;
    private int trial;
    private String userAnswers;
    private String trueAnswers;
    private String liedAnswers;
    private int progress = 0;

    private Random random;

    private int DELAY = 8000; //time in ms for delay in between 2 game screens

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

        final ProgressBar pb = (ProgressBar) findViewById(progressBar);
        pb.setMax(1000);
        pb.setProgress(0);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                progress++;
                pb.setProgress(progress);
                if (pb.getProgress() < 1000) {
                    handler.postDelayed(this

                            , DELAY / 1000);
                } else {
                    Intent intent = new Intent(Game1Activity.this, Game2Activity.class);
                    intent.putExtra("deck", deck);
                    intent.putExtra("trial", trial);
                    intent.putExtra("userAnswers", userAnswers);
                    intent.putExtra("trueAnswers", trueAnswers);
                    intent.putExtra("liedAnswers", liedAnswers);

                    startActivityForResult(intent, 1);
                }
            }
        };
        runnable.run();

        setCardImages();
    }

    /**
     * Used to pass on the result strings: reads the strings from the initiating intent.
     * @param i Intent that started this activity.
     */
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

    /**
     * @returnthe 3 cards that are shown to the user
     */
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

    /**
     * Finds the 3 imageViews that can be used to show the cards.
     * @return iv
     */
    private ImageView[] generateImageViews(){
        ImageView[] iv = new ImageView[3];
        iv[0] = (ImageView) findViewById(R.id.imageView1);
        iv[1] = (ImageView) findViewById(R.id.imageView2);
        iv[2] = (ImageView) findViewById(R.id.imageView3);
        return iv;
    }
    /**
     * Used to create an array of random cards
     * @return an array of random cards
     */
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
    /**
     * Transforms a String to an ID that can be used to call a certain resource.
     * @param resName Name of the resource
     * @param c Class of the resource
     * @return The ID of the resource
     */
    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Converts a given deck of cards to a string representation.
     * @param cards Card array
     * @return a string representation of the card array that was inputted.
     */
    private String[] deckToStringArr(Card[] cards){
        String[] s = new String[cards.length];
        for(int i = 0; i < cards.length; i++){
            s[i] = cards[i].toString();
        }
        return s;
    }
}
