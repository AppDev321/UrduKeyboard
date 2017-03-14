/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiletin.inputmethod.indic.setup;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.inputmethod.latin.utils.LeakGuardHandlerWrapper;
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils;
import com.google.android.gms.ads.InterstitialAd;
import com.mobiletin.inputmethod.Ads.AdIntegration;
import com.mobiletin.inputmethod.MySuperAppApplication;
import com.mobiletin.inputmethod.indic.R;
import com.mobiletin.inputmethod.indic.settings.SettingsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// TODO: Use Fragment to implement welcome screen and setup steps.
public final class SetupWizardActivity extends AdIntegration implements View.OnClickListener {
    static final String TAG = SetupWizardActivity.class.getSimpleName();

    private InputMethodManager mImm;
    ImageView imgMenuBtn;
    private View mSetupWizard;
    private View mWelcomeScreen;
    private View mSetupScreen;
    private View mActionStart;
    private View mActionNext;
    private TextView mStep1Bullet;
    //private TextView mActionFinish;
    private SetupStepGroup mSetupStepGroup;
    private static final String STATE_STEP = "step";
    private int mStepNumber;
    private boolean mNeedsToAdjustStepNumberToSystemState;
    private boolean finishState;
    private static final int STEP_WELCOME = 0;
    private static final int STEP_1 = 1;
    private static final int STEP_2 = 2;
    private static final int STEP_3 = 4;//changes here
    private static final int STEP_4 = 4;
    private static final int STEP_LAUNCHING_IME_SETTINGS = 5;
    private static final int STEP_BACK_FROM_IME_SETTINGS = 6;
    LinearLayout continerFinish;
    private SettingsPoolingHandler mHandler;

    private static final class SettingsPoolingHandler
            extends LeakGuardHandlerWrapper<SetupWizardActivity> {
        private static final int MSG_POLLING_IME_SETTINGS = 0;
        private static final long IME_SETTINGS_POLLING_INTERVAL = 200;

        private final InputMethodManager mImmInHandler;

        public SettingsPoolingHandler(final SetupWizardActivity ownerInstance,
                                      final InputMethodManager imm) {
            super(ownerInstance);
            mImmInHandler = imm;
        }

        @Override
        public void handleMessage(final Message msg) {
            final SetupWizardActivity setupWizardActivity = getOwnerInstance();
            if (setupWizardActivity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_POLLING_IME_SETTINGS:
                    if (UncachedInputMethodManagerUtils.isThisImeEnabled(setupWizardActivity,
                            mImmInHandler)) {
                        setupWizardActivity.invokeSetupWizardOfThisIme();
                        return;
                    }
                    startPollingImeSettings();
                    break;
            }
        }

        public void startPollingImeSettings() {
            sendMessageDelayed(obtainMessage(MSG_POLLING_IME_SETTINGS),
                    IME_SETTINGS_POLLING_INTERVAL);
        }

        public void cancelPollingImeSettings() {
            removeMessages(MSG_POLLING_IME_SETTINGS);
        }
    }


    boolean hidden = true;
    LinearLayout mRevealView;
    LinearLayout ib_more, ib_share;
    LinearLayout ib_about, ib_rate;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
       // setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        super.onCreate(savedInstanceState);

        mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mHandler = new SettingsPoolingHandler(this, mImm);

        setContentView(R.layout.setup_wizard);
        mSetupWizard = findViewById(R.id.setup_wizard);


        super.showAdd(this, (LinearLayout) findViewById(R.id.adView), false);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                InterstitialAd ad = MySuperAppApplication.preLoadIntersitial.getAd();
                if (ad.isLoaded()) {
                    MySuperAppApplication.preLoadIntersitial.getAd().show();
                }

            }
        }, 800);


        mActionStart = findViewById(R.id.setup_start_label);
        mActionStart.setOnClickListener(this);
        if (savedInstanceState == null) {
            mStepNumber = determineSetupStepNumberFromLauncher();
        } else {
            mStepNumber = savedInstanceState.getInt(STATE_STEP);
        }

        final String applicationName = getResources().getString(getApplicationInfo().labelRes);
        mWelcomeScreen = findViewById(R.id.setup_welcome_screen);
        final TextView welcomeTitle = (TextView) findViewById(R.id.setup_welcome_title);
        welcomeTitle.setText(getString(R.string.setup_welcome_title, applicationName));
        welcomeTitle.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            welcomeTitle.setTextSize(getResources().getDimension(R.dimen._25sdp));
        } else {
            welcomeTitle.setTextSize(getResources().getDimension(R.dimen._15sdp));
        }
        mSetupScreen = findViewById(R.id.setup_steps_screen);
        final TextView stepsTitle = (TextView) findViewById(R.id.setup_title);
        stepsTitle.setText(getString(R.string.setup_steps_title, applicationName));

        final SetupStepIndicatorView indicatorView =
                (SetupStepIndicatorView) findViewById(R.id.setup_step_indicator);
        mSetupStepGroup = new SetupStepGroup(indicatorView);

        mStep1Bullet = (TextView) findViewById(R.id.setup_step1_bullet);
        // mStep1Bullet.setOnClickListener(this);
        final SetupStep step1 = new SetupStep(STEP_1, applicationName,
                mStep1Bullet, findViewById(R.id.setup_step1),
                R.string.setup_step1_title, R.string.setup_step1_instruction,
                R.string.setup_step1_finished_instruction, R.drawable.ic_setup_step1,
                R.string.setup_step1_action);
        final SettingsPoolingHandler handler = mHandler;
        step1.setAction(new Runnable() {
            @Override
            public void run() {
                invokeLanguageAndInputSettings();
                handler.startPollingImeSettings();
            }
        });
        mSetupStepGroup.addStep(step1);

        final SetupStep step2 = new SetupStep(STEP_2, applicationName,
                (TextView) findViewById(R.id.setup_step2_bullet), findViewById(R.id.setup_step2),
                R.string.setup_step2_title, R.string.setup_step2_instruction,
                0 /* finishedInstruction */, R.drawable.ic_setup_step2,
                R.string.setup_step2_action);
        step2.setAction(new Runnable() {
            @Override
            public void run() {
                invokeInputMethodPicker();
            }
        });
        mSetupStepGroup.addStep(step2);
/*

        final SetupStep step3 = new SetupStep(STEP_3, applicationName,
                (TextView) findViewById(R.id.setup_step3_bullet), findViewById(R.id.setup_step3),
                R.string.setup_step3_title, R.string.setup_step3_instruction,
                0 , R.drawable.ic_setup_step3,
                R.string.setup_step3_action);
        step3.setAction(new Runnable() {
            @Override
            public void run() {
                invokeSubtypeEnablerOfThisIme();

            }
        });
        mSetupStepGroup.addStep(step3);
*/

        final SetupStep step4 = new SetupStep(STEP_4, applicationName,
                (TextView) findViewById(R.id.setup_step4_bullet), findViewById(R.id.setup_step4),
                R.string.setup_step4_title, R.string.setup_step4_instruction,
                0 /* finishedInstruction */, R.drawable.ic_setup_finish,
                R.string.setup_finish_action);
        step4.setAction(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
        mSetupStepGroup.addStep(step4);


        mActionNext = findViewById(R.id.setup_next);
        mActionNext.setOnClickListener(this);

        /*
        mActionFinish = (TextView)findViewById(R.id.setup_finish);
        TextViewCompatUtils.setCompoundDrawablesRelativeWithIntrinsicBounds(mActionFinish,
                getResources().getDrawable(R.drawable.ic_setup_finish), null, null, null);
        mActionFinish.setOnClickListener(this);
        */


//Ripple menu
        imgMenuBtn = (ImageView) findViewById(R.id.menu_btn);
        imgMenuBtn.setOnClickListener(this);



        //Perfome get Started Action
        mActionNext.performClick();


        MySuperAppApplication.mAnalyticSingaltonClass.sendScreenAnalytics("Main Index");
    }

    @Override
    public void onClick(final View v) {
        /*
        if (v == mActionFinish) {
            finish();
            return;
        }
        */


        final int currentStep = determineSetupStepNumber();
        final int nextStep;


        if (v == mActionStart) {

            nextStep = STEP_1;

        } else if (v == mActionNext) {
            nextStep = mStepNumber + 1;
        } else if (v == mStep1Bullet && currentStep == STEP_2) {
            nextStep = STEP_1;
        } else {
            nextStep = mStepNumber;
        }
        if (mStepNumber != nextStep) {
            mStepNumber = nextStep;
            updateSetupStepView();


        }


        //For ripple menu button
        switch (v.getId()) {
            case R.id.share:
                shareMessage(getResources().getString(R.string.english_ime_name), getResources().getString(R.string.share_message));

                break;
            case R.id.rate:
                rateApp();

                break;
            case R.id.more:
                String s = "market://search?q=pub:Mobile+Tin";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                startActivity(browserIntent);

                break;
            case R.id.about:
                MySuperAppApplication.mAnalyticSingaltonClass.sendScreenAnalytics("About Index");
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.menu_btn:
                showPopupMenu(v);
                MySuperAppApplication.mAnalyticSingaltonClass.sendEventAnalytics("Main-Index", "menu");
                break;
        }

    }


    void invokeSetupWizardOfThisIme() {
        final Intent intent = new Intent();
        intent.setClass(this, SetupWizardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        mNeedsToAdjustStepNumberToSystemState = true;
    }

    private void invokeSettingsOfThisIme() {
        final Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    void invokeLanguageAndInputSettings() {
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);
        mNeedsToAdjustStepNumberToSystemState = true;
    }

    void invokeInputMethodPicker() {
        // Invoke input method picker.
        mImm.showInputMethodPicker();
        mNeedsToAdjustStepNumberToSystemState = true;
    }

    void invokeSubtypeEnablerOfThisIme() {
        final InputMethodInfo imi =
                UncachedInputMethodManagerUtils.getInputMethodInfoOf(getPackageName(), mImm);
        if (imi == null) {
            return;
        }
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, imi.getId());
        startActivity(intent);
        mNeedsToAdjustStepNumberToSystemState = true;
        finishState = true;
    }

    private int determineSetupStepNumberFromLauncher() {
        final int stepNumber = determineSetupStepNumber();
        if (stepNumber == STEP_1) {
            return STEP_WELCOME;
        }
        if (stepNumber == STEP_4) {
            return STEP_LAUNCHING_IME_SETTINGS;
        }
        return stepNumber;
    }

    private int determineSetupStepNumber() {
        mHandler.cancelPollingImeSettings();
        if (!UncachedInputMethodManagerUtils.isThisImeEnabled(this, mImm)) {
            return STEP_1;
        }
        if (!UncachedInputMethodManagerUtils.isThisImeCurrent(this, mImm)) {
            return STEP_2;
        }
        if (finishState) {
            return STEP_4;
        }

        return STEP_3;
        // return STEP_4;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_STEP, mStepNumber);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mStepNumber = savedInstanceState.getInt(STATE_STEP);
    }

    private static boolean isInSetupSteps(final int stepNumber) {
        return stepNumber >= STEP_1 && stepNumber <= STEP_4;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Probably the setup wizard has been invoked from "Recent" menu. The setup step number
        // needs to be adjusted to system state, because the state (IME is enabled and/or current)
        // may have been changed.
        if (isInSetupSteps(mStepNumber)) {
            mStepNumber = determineSetupStepNumber();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (mStepNumber == STEP_LAUNCHING_IME_SETTINGS) {
            // Prevent white screen flashing while launching settings activity.
            mSetupWizard.setVisibility(View.INVISIBLE);
            invokeSettingsOfThisIme();
            mStepNumber = STEP_BACK_FROM_IME_SETTINGS;
            return;
        }
        if (mStepNumber == STEP_BACK_FROM_IME_SETTINGS) {
            //finish();
            return;
        }
        updateSetupStepView();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (mRevealView.getVisibility() == View.VISIBLE) {
            mRevealView.setVisibility(View.GONE);
            hidden = true;
            return;
        } /*else {
            if (mStepNumber == STEP_1) {
                *//*mStepNumber = STEP_WELCOME;
                updateSetupStepView();*//*
                this.finish();
                return;
            }
        }*/

        super.onBackPressed();
    }


    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mNeedsToAdjustStepNumberToSystemState) {
            mNeedsToAdjustStepNumberToSystemState = false;
            mStepNumber = determineSetupStepNumber();
            updateSetupStepView();

        }
    }

    private void updateSetupStepView() {
        mSetupWizard.setVisibility(View.VISIBLE);
        final boolean welcomeScreen = (mStepNumber == STEP_WELCOME);
        mWelcomeScreen.setVisibility(welcomeScreen ? View.VISIBLE : View.GONE);
        mSetupScreen.setVisibility(welcomeScreen ? View.GONE : View.VISIBLE);
        if (welcomeScreen) {
            return;
        }
        final boolean isStepActionAlreadyDone = mStepNumber < determineSetupStepNumber();
        // mSetupStepGroup.enableStep(mStepNumber, isStepActionAlreadyDone);


        mSetupStepGroup.enableStep(determineSetupStepNumber(), isStepActionAlreadyDone);
        mActionNext.setVisibility(isStepActionAlreadyDone ? View.VISIBLE : View.GONE);
        //mActionFinish.setVisibility((mStepNumber == STEP_4) ? View.VISIBLE : View.GONE);


        continerFinish = (LinearLayout) findViewById(R.id.containerFinish);
        continerFinish.setVisibility(View.GONE);
        if (determineSetupStepNumber() == STEP_4 || determineSetupStepNumber() == 3) {
            continerFinish.setVisibility(View.VISIBLE);
            showFinishedView();
        }

    }

    static final class SetupStep implements View.OnClickListener {
        public final int mStepNo;
        private final View mStepView;
        private final TextView mBulletView;
        private final int mActivatedColor;
        private final int mDeactivatedColor;
        private final String mInstruction;
        private final String mFinishedInstruction;
        private final TextView mActionLabel;
        private Runnable mAction;

        public SetupStep(final int stepNo, final String applicationName, final TextView bulletView,
                         final View stepView, final int title, final int instruction,
                         final int finishedInstruction, final int actionIcon, final int actionLabel) {
            mStepNo = stepNo;
            mStepView = stepView;
            mBulletView = bulletView;
            final Resources res = stepView.getResources();
            mActivatedColor = res.getColor(R.color.setup_text_action);
            mDeactivatedColor = res.getColor(R.color.setup_text_dark);

            final TextView titleView = (TextView) mStepView.findViewById(R.id.setup_step_title);
            titleView.setText(res.getString(title, applicationName));
            titleView.setVisibility(View.GONE);

            mInstruction = (instruction == 0) ? null
                    : res.getString(instruction, applicationName);
            mFinishedInstruction = (finishedInstruction == 0) ? null
                    : res.getString(finishedInstruction, applicationName);

            mActionLabel = (TextView) mStepView.findViewById(R.id.setup_step_action_label);
            mActionLabel.setText(res.getString(actionLabel));
            mActionLabel.setBackground(MySuperAppApplication.getContext().getResources().getDrawable(R.drawable.btn_selector_setup));
            mActionLabel.setTextColor(Color.WHITE);
            mActionLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mActionLabel.getLayoutParams();

            int marging = (int) MySuperAppApplication.getContext().getResources().getDimension(R.dimen._15sdp);
            params.setMargins(marging, 0, marging, 0); //substitute parameters for left, top, right, bottom
            mActionLabel.setLayoutParams(params);
           /* if (actionIcon == 0) {
                final int paddingEnd = ViewCompatUtils.getPaddingEnd(mActionLabel);
                ViewCompatUtils.setPaddingRelative(mActionLabel, paddingEnd, 0, paddingEnd, 0);
            } else {
                TextViewCompatUtils.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        mActionLabel, res.getDrawable(actionIcon), null, null, null);
            }*/

        }

        public void setEnabled(final boolean enabled, final boolean isStepActionAlreadyDone) {
            mStepView.setVisibility(enabled ? View.VISIBLE : View.GONE);
            mBulletView.setTextColor(enabled ? mActivatedColor : mDeactivatedColor);
            final TextView instructionView = (TextView) mStepView.findViewById(
                    R.id.setup_step_instruction);
            instructionView.setText(isStepActionAlreadyDone ? mFinishedInstruction : mInstruction);
            mActionLabel.setVisibility(isStepActionAlreadyDone ? View.GONE : View.VISIBLE);
        }

        public void setAction(final Runnable action) {
            mActionLabel.setOnClickListener(this);
            mAction = action;
        }

        @Override
        public void onClick(final View v) {

            if (v == mActionLabel && mAction != null) {
                mAction.run();
                return;
            }
        }
    }

    static final class SetupStepGroup {
        private final SetupStepIndicatorView mIndicatorView;
        private final ArrayList<SetupStep> mGroup = new ArrayList<>();

        public SetupStepGroup(final SetupStepIndicatorView indicatorView) {
            mIndicatorView = indicatorView;
        }

        public void addStep(final SetupStep step) {
            mGroup.add(step);
        }

        public void enableStep(final int enableStepNo, final boolean isStepActionAlreadyDone) {
            for (final SetupStep step : mGroup) {
                step.setEnabled(step.mStepNo == enableStepNo, isStepActionAlreadyDone);
            }
            mIndicatorView.setIndicatorPosition(enableStepNo - STEP_1, mGroup.size());
        }
    }



    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(SetupWizardActivity.this, view);
        MenuInflater inflater = popup.getMenuInflater();

        inflater.inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();

    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_share:
                    shareMessage(getResources().getString(R.string.english_ime_name), getResources().getString(R.string.share_message));

                    break;
                case R.id.action_rate:
                    rateApp();

                    break;
                case R.id.action_more:
                    String s = "market://search?q=pub:Mobile+Tin";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    startActivity(browserIntent);

                    break;
                case R.id.action_about:
                    MySuperAppApplication.mAnalyticSingaltonClass.sendScreenAnalytics("About Index");
                    startActivity(new Intent(SetupWizardActivity.this, AboutActivity.class));
                    break;


            }
            return false;
        }
    }

    public void showFinishedView() {

        Button btnInputMethod = (Button) findViewById(R.id.toggle);
        btnInputMethod.setGravity(Gravity.CENTER);


      /*  if (UncachedInputMethodManagerUtils.isThisImeCurrent(this, mImm)) {
            btnInputMethod.setChecked(true);
        } else {
            btnInputMethod.setChecked(false);
        }*/
        btnInputMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRevealView.setVisibility(View.GONE);
                hidden = true;
                invokeInputMethodPicker();
                MySuperAppApplication.mAnalyticSingaltonClass.sendEventAnalytics("Main-Index", "Disable Asaan Urdu keyboard");
            }
        });

    }

    private void shareMessage(String subject, String body) {

        if (saveShareImage()) {
            shareAppWithAppIcon(subject, body);
        } else {
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

    private void shareAppWithAppIcon(String subject, String body) {
        String fileName = "ic_launcher.png";
        String completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        // shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private boolean saveShareImage() {
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon), 300, 300, false);
        File sd = Environment.getExternalStorageDirectory();
        String fileName = "ic_launcher.png";
        File dest = new File(sd, fileName);
        try {
            FileOutputStream out;
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private void rateApp() {

        try {
            Intent mintent = new Intent(Intent.ACTION_VIEW);
            mintent.setData(Uri.parse("market://details?id=com.mobiletin.inputmethod"));
            startActivity(mintent);
        } catch (Exception e1) {
            try {
                Uri uriUrl = Uri.parse("https://market.android.com/details?id=com.mobiletin.inputmethod");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);

                startActivity(launchBrowser);
                finish();
            } catch (Exception e2) {
                // Toast.makeText(context, "No Application Found to open link", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
