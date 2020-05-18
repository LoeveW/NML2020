package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
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
        this.userAnswers = i.getStringExtra("userAnswers");
        this.trueAnswers = i.getStringExtra("trueAnswers");
        this.liedAnswers = i.getStringExtra("liedAnswers");
    }
}
