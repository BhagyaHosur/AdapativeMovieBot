package com.example.bhagya.moviebot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Button b1=(Button) findViewById(R.id.button1);
        // final WebView w= (WebView) findViewById(R.id.webView1);
        // w.loadUrl("file:///android_asset/login.html");

        final EditText e1 = (EditText) findViewById(R.id.editText1);
        final EditText e2 = (EditText) findViewById(R.id.textView3);
        // final EditText e3 = (EditText) findViewById(R.id.editText3);
        // final EditText e4 = (EditText) findViewById(R.id.editText4);

        b1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String usr = e1.getText().toString();
                String pw = e2.getText().toString();
                //String attn= e3.getText().toString();
                //String gpa = e4.getText().toString();

                // TODO Auto-generated method stub
                if(usr.equals("admin") &&
                        pw.equals("admin")){
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    Toast.makeText(getApplicationContext(),
                            "Login Successful", Toast.LENGTH_LONG).show();
                    startActivity(intent);}

                else
                {
                    Intent intent = new Intent(Login.this, Login.class);
                    Toast.makeText(getApplicationContext(),
                            "Invalid Credentials", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }

            }
        });
    }





}
