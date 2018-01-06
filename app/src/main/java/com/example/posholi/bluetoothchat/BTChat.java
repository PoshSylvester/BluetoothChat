package com.example.posholi.bluetoothchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class BTChat extends AppCompatActivity implements UIEvents{

    private Button newConnectionBtn, quitBTchatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btchat);
        newConnectionBtn = findViewById(R.id.newBTConnection);
        quitBTchatBtn=findViewById(R.id.quitBTchat);

        newConnectionBtn.setOnClickListener(BTChat.this);
        quitBTchatBtn.setOnClickListener(BTChat.this);
    }

    @Override
    public void onClick (View v){

        switch (v.getId()){

            case R.id.newBTConnection:

                Intent setupNewConnections = new Intent(this, BTConnections.class);
                startActivity(setupNewConnections);
                break;


            case R.id.quitBTchat:
                //Quit the main activity of the game app
                Toast.makeText(getApplicationContext(), "Quiting BTchat.", Toast.LENGTH_SHORT).show();
                try {
                    TimeUnit.SECONDS.sleep(1);

                }catch (Exception e){

                }

                BTChat.this.finish();
        }

    }

    @Override
    protected void onDestroy(){

        //TODO: save values, conversation, and new profiles
        super.onDestroy();

    }
}
