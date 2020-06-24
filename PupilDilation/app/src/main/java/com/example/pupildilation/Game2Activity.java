package com.example.pupildilation;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.listeners.PictureCapturingListener;
import com.example.services.APictureCapturingService;
import com.example.services.PictureCapturingServiceImpl;
import com.example.services.PupilSeeker;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.example.pupildilation.R.id.progressBar;

public class Game2Activity extends AppCompatActivity {

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback() {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("TAG", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;

    //The capture service
    private APictureCapturingService pictureService;

    private static final int NR_TRIALS = 1;
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

    private ImageButton yes;
    private ImageButton no;

    private int progress = 0;
    private int DELAY = 8000; //time in ms for delay in between 2 game screens
    private int STEPS = 250; // how much steps there are for the progressbar, app and its timings may slow down when set too high.

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_two);
        ContextCompat.checkSelfPermission(this, Manifest.permission. WRITE_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_ACCESS_CODE);

        this.pictureService = PictureCapturingServiceImpl.getInstance(this);
        final PictureCapturingListener listener = new PictureCapturingListener() {
            @Override
            public void onCaptureDone(final String pictureUrl, final byte[] pictureData) {
                System.out.println("Image captured");
                if (pictureData != null && pictureUrl != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                            final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                            final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                            PupilSeeker ps = new PupilSeeker(scaled);
                            ps.start();
//                    if (pictureUrl.contains("0_pic.jpg")) {
//                        uploadBackPhoto.setImageBitmap(scaled);
//                    } else if (pictureUrl.contains("1_pic.jpg")) {
//                        uploadFrontPhoto.setImageBitmap(scaled);
//                    }
                        }
                    });
                }
            }

            @Override
            public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
                System.out.println("all photos taken");
                System.out.println(picturesTaken.size());
                for(Map.Entry<String, byte[]> entry : picturesTaken.entrySet()){
                    String pictureUrl = entry.getKey();
                    final byte[] pictureData = entry.getValue();
                    System.out.println(pictureUrl);
                    System.out.println(pictureData);
                    if (pictureData != null && pictureUrl != null) {
                        System.out.println("start processing of image");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                                PupilSeeker ps = new PupilSeeker(scaled);
                                ps.start();
//                    if (pictureUrl.contains("0_pic.jpg")) {
//                        uploadBackPhoto.setImageBitmap(scaled);
//                    } else if (pictureUrl.contains("1_pic.jpg")) {
//                        uploadFrontPhoto.setImageBitmap(scaled);
//                    }
                            }
                        });
                    }
                }
            }
        };
        pictureService.startCapturing(listener);

        Intent intent = getIntent();
        init(intent);
        this.yes = (ImageButton) findViewById(R.id.yes);
        this.no = (ImageButton) findViewById(R.id.no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!clicked) {
                    userAnswers = userAnswers + "O";
                    no.setAlpha(0.5f);
                    clicked = true;
                    System.out.println("O added");
                    pictureService.startCapturing(listener);


                }

            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!clicked) {
                    userAnswers = userAnswers + "-";
                    yes.setAlpha(0.5f);
                    clicked = true;
                    System.out.println("- added");
                    pictureService.startCapturing(listener);

                }
            }
        });

        final ProgressBar pb = (ProgressBar) findViewById(progressBar);
        pb.setMax(STEPS);
        pb.setProgress(0);
        final Handler handler = new Handler();
        final ImageView queryView = (ImageView) findViewById(R.id.imageView4);

        final Runnable runnable = new Runnable() {

            public void run() {
                progress++;
                pb.setProgress(progress);
                int resID = getResId(cardsQuery[count].toString(), R.drawable.class); //changed method to a toString.
                queryView.setImageResource(resID);

                if (pb.getProgress() < STEPS) {
                    handler.postDelayed(this

                            , DELAY / STEPS);
                } else {
                    handler.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         if (!clicked) {
                                             userAnswers = userAnswers + "x";
                                         } else {
                                             clicked = false;
                                         }
                                     }
                                 }
                    );
                    count++;
                    yes.setAlpha(1f);
                    no.setAlpha(1f);
                    if (count < cardsQuery.length) {
                        progress = 0;
                        handler.post(this

                        );
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                nextTask();
                            }
                        });
                    }
                }
            }
        };

        handler.post(runnable);
    }

    /**
     * Collection of basic stuff that is needed to get this class up and running.
     * @param intent Intent that started this activity.
     */
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

    /**
     * Used to pass on the result strings: reads the strings from the initiating intent.
     * @param i Intent that started this activity.
     */
    private void readResultStrings(Intent i) {
        this.userAnswers = i.getStringExtra("userAnswers");
        this.trueAnswers = i.getStringExtra("trueAnswers");
        this.liedAnswers = i.getStringExtra("liedAnswers");
    }

    /**
     * Determines and starts the next tasks, depending on what trial it is currently at.
     */
    private void nextTask() {
        if (this.trial <= NR_TRIALS) { //nr of trials / games to be played
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
            i.putExtra("trial", trial);
            i.putExtra("nrCards", this.cardsQuery.length);
            startActivityForResult(i, 1);
        }
    }

    /**
     * Sets the array of cards as read by the cardFromString cardreader.
     * @param deck
     */
    private void setCards(String[] deck) {
        for (int i = 0; i < deck.length; i++)
            this.cards[i] = cardFromString(deck[i]);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Used to read cards from a string, usually received via intents.
     * @param s input string
     * @return Card that was specified in this string
     */
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
     * Used to create an array of random cards
     * @return an array of random cards
     */
    private Card[] getRandomCards() {
        Card[] cardsTemp = new Card[this.cards.length];
        Card.Suit[] suits = Card.getSuits();
        for (int i = 0; i < this.cards.length; i++) {
            int rank = this.random.nextInt(12) + 2;
            int suitVal = this.random.nextInt(4);
            cardsTemp[i] = new Card(rank, suits[suitVal]);
            for (int j = 0; j < this.cards.length; j++) {
                if (cards[j].equals(cardsTemp[i])) {
                    return getRandomCards();
                }
            }
        }
        if (cardsTemp[0].equals(cardsTemp[1]) ||
                cardsTemp[0].equals(cardsTemp[2]) ||
                cardsTemp[1].equals(cardsTemp[0]) ||
                cardsTemp[1].equals(cardsTemp[2]) ||
                cardsTemp[2].equals(cardsTemp[0]) ||
                cardsTemp[2].equals(cardsTemp[1])) {
            return getRandomCards();
        }

        return cardsTemp;
    }

    /**
     * Used to merge and shuffle two arrays of Cards.
     * @param arr1 Array to merge and shuffle
     * @param arr2 Array to merge and shuffle
     * @return Card[] that consists of arr1 and arr2, shuffled.
     */
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
                    this.trueAnswers = this.trueAnswers + "O";
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

}