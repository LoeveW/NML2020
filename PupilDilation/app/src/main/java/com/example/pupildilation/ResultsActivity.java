package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    private String userAnswers;
    private String trueAnswers;
    private String liedAnswers;
    private TextView[] tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_results);
        getTextViews();
        Intent i = getIntent();
        setResultFields(i);

        Button shareButton = (Button)findViewById(R.id.shareButton);
        Button mainButton = (Button)findViewById(R.id.mainButton);

        mainButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "I have played 'I Can See It In Your Eyes' and got the following results:"+
                        "\n User answers: " + userAnswers +
                        "\n True answers: " + trueAnswers +
                        "\n Lied answers:" + liedAnswers;
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }

    private void setResultFields(Intent i) {
        readResultStrings(i);
        tv[0].setText(this.userAnswers);
        tv[1].setText(this.trueAnswers);
        tv[2].setText(this.liedAnswers);

    }

    private void getTextViews() {
        this.tv = new TextView[3];
        tv[0] = (TextView) findViewById(R.id.userAnswers);
        tv[1] = (TextView) findViewById(R.id.trueAnswers);
        tv[2] = (TextView) findViewById(R.id.liedAnswers);
    }

    private void readResultStrings(Intent i) {
//        int trials = i.getIntExtra("trial", -1);
//        int nrCards = i.getIntExtra("nrCards", -1);
        this.userAnswers= i.getStringExtra("userAnswers");
        this.trueAnswers = i.getStringExtra("trueAnswers");
        this.liedAnswers = i.getStringExtra("liedAnswers");

//        if(userA.length() > trials * nrCards) {
//            String usr = "";
//            for (int x = 0; x < trials - 1; x++) {
//                int s = x * nrCards;
//                usr = usr + userA.substring(s + 1, s + 1 + nrCards);
//            }
//            this.userAnswers = usr;
//        }
//        else
//            this.userAnswers = userA;
    }
}
