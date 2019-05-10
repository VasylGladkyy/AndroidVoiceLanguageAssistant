package com.example.androidvoicelanguageassistant;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        LinearLayout linearLayout = findViewById(R.id.home_container);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        Button btnConversation = (Button) findViewById(R.id.start_new_conversation);
        Button btnTranslation = (Button) findViewById(R.id.start_new_translation);
        Button btnAbout = (Button) findViewById(R.id.about);

        btnConversation.setOnClickListener(this);
        btnTranslation.setOnClickListener(this);
        btnAbout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.start_new_conversation:
                intent = new Intent(HomeActivity.this, ConversationActivity.class);
                startActivity(intent);
                break;
            case R.id.start_new_translation:
                intent = new Intent(HomeActivity.this, TranslationActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
        }
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed(){
        if(exit){
            finish();
        }else {
            Toast.makeText(this, R.string.exit_string,Toast.LENGTH_SHORT).show();
            exit = true;
        }
    }


}
