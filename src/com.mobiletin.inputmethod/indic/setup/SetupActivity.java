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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.TextUtils;

import com.mobiletin.inputmethod.indic.R;

import java.util.Locale;

public final class SetupActivity extends Activity {


    ProgressDialog progressDoalog;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean restoredText = prefs.getBoolean("execute", false);
        if (restoredText == true) {
            final Intent intent = new Intent();
            intent.setClass(getBaseContext(), SetupWizardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (!isFinishing()) {
                finish();
            }
        } else {
            BackgroundTask task = new BackgroundTask(SetupActivity.this);
            task.execute();
        }


    }

    private int deleteWord(String word) {
        if (TextUtils.isEmpty(word)) {
            return -1;
        }

        return getContentResolver().delete(UserDictionary.Words.CONTENT_URI,
                UserDictionary.Words.WORD + "=?", new String[]{word});
    }

    private int deleteAllWord() {
        return getContentResolver().delete(UserDictionary.Words.CONTENT_URI,
                null, null);
    }


    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(SetupActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean("execute", true);
            editor.commit();
            final Intent intent = new Intent();
            intent.setClass(getBaseContext(), SetupWizardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (!isFinishing()) {
                finish();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
              //  deleteAllWord();

               /* String[] englishList = null;
                englishList = getBaseContext().getResources().getStringArray(R.array.EngWords);
                String[] UrduList = null;
                UrduList = getBaseContext().getResources().getStringArray(R.array.UrduWords);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    for (int i = 0; i < englishList.length; i++) {
                      UserDictionary.Words.addWord(getBaseContext(), UrduList[i], 10, englishList[i], null);//null would add to all locale
                        // On JellyBean & above, you can provide a shortcut and an explicit Locale
                        // UserDictionary.Words.addWord(getBaseContext(), UrduList[i], 10, englishList[i], Locale.US);



                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    for (int i = 0; i < englishList.length; i++) {
                        // On JellyBean & above, you can provide a shortcut and an explicit Locale
                        UserDictionary.Words.addWord(getBaseContext(), UrduList[i], 10, UserDictionary.Words.LOCALE_TYPE_ALL);
                    }
                }
*/
                /*List<String> strArr = new ArrayList<String>();
                strArr .addAll(UrduDictionary.wordslist);
                strArr.addAll(UrduDictionary2.wordslist);
                strArr.addAll(EnglishDictionary.englishList);

                Log.e("ListSize", "" + strArr.size());

                for (int i = 0; i < strArr.size(); i++) {
                    UserDictionary.Words.addWord(getBaseContext(), strArr.get(i), 10, UserDictionary.Words.LOCALE_TYPE_ALL);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    // On JellyBean & above, you can provide a shortcut and an explicit Locale

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    //    UserDictionary.Words.addWord(this, "MadeUpWord", 10, UserDictionary.Words.LOCALE_TYPE_ALL);
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}
