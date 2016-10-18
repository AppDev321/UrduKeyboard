package com.mobiletin.inputmethod.indic.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils;
import com.mobiletin.inputmethod.indic.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 10/17/2016.
 */

public class Test extends Activity {
    private InputMethodManager mImm;
    private String currentIme;
    private String[] arrayIme;
    private List packageInstalledKeyboard;
    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.packageInstalledKeyboard = new ArrayList();

        mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        setContentView(R.layout.test);
        b1 = (Button) findViewById(R.id.button2);
        b2 = (Button) findViewById(R.id.button3);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableKeyBoard();
            }
        });


        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImm.showInputMethodPicker();

            }
        });


    }

    //When dialog apper and then perform
    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            checkKeybordExit();
        }
    }

    public void enableKeyBoard() {
        startActivityForResult(new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0);

        Toast.makeText(this, "Please enable " + getResources().getString(R.string.english_ime_name) + " and press back", Toast.LENGTH_LONG).show();
    }

    private void checkKeybordExit() {

        if (UncachedInputMethodManagerUtils.isThisImeEnabled(this, mImm)) {
            b1.setText("Enabled");
        } else {
            b1.setText("Select");
        }
        checkCurrentIme();
    }

    private void checkCurrentIme() {

        if (UncachedInputMethodManagerUtils.isThisImeCurrent(this, mImm)) {
            b2.setText("Enabled-Keyva");
        } else {
            b2.setText("Diable");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkKeybordExit();
    }
}
