package yoon.mark.rhapsodize;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.Random;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.View;
import android.os.CountDownTimer;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
//import android.widget.Toolbar;
import android.view.ViewConfiguration;
import android.util.Log;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import android.support.v7.widget.Toolbar;

public class MainActivity extends ActionBarActivity implements RecognitionListener, OnClickListener {
    /* class variables */
    private static final String KEYWORD_SEARCH = "like";
    private static final String KEYWORD_SEARCH_1 = "like";
    private static final String KEYWORD_SEARCH_2 = "testing";
    private static final String KEYWORD_SEARCH_3 = "uh";
    private SpeechRecognizer recognizer;
    private int currentCount_1 = 0;
    private int totalCount_1 = 0;
    private int currentCount_2 = 0;
    private int currentCount_3 = 0;
//    private boolean[] notificationArray;
    private boolean notifications = true;
//    private int notificationIdx = 0;
    private Button startB;
    private final long startTime = 960 * 1000;
    private final long interval = 1 * 1000;
    private long timeRemaining = 960 * 1000;
    public TextView time;
    private CountDownTimer counter;
    private boolean timerHasStarted = false;
    public TextView counts;
    public TextView notificationStatus;
    public TextView totalCount;
    public TextView notificationType;
    public TextView searchWord;

    /** Denotes selected notification state: 0 = none, 1 = vibrate, 2 = ring, 3 = both */
    public int notifyState = 1;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
//        getSupportActionBar().setDisplayOptions(getSupportActionBar().DISPLAY_SHOW_HOME | getSupportActionBar().DISPLAY_SHOW_TITLE);
//        getSupportActionBar().setIcon(R.drawable.logonobg);

        // Display data
        setContentView(R.layout.activity_main);
        startB = (Button) this.findViewById(R.id.timer_button);
        startB.setOnClickListener(this);
        time = (TextView) this.findViewById(R.id.timer);
        notificationStatus = (TextView) this.findViewById(R.id.notificationStatus);
        notificationType = (TextView) this.findViewById(R.id.notificationType);
        totalCount = (TextView) this.findViewById(R.id.totalCount);
        counter = new MyCountDownTimer(startTime, interval);
        time.setText(time.getText() + String.valueOf(startTime / 60000) + ":" + "00");
        counts = (TextView) this.findViewById(R.id.counts);
        searchWord = (TextView) this.findViewById(R.id.searchWord);
//        boolean[][] notificationArrays = {{true, true, false, false}, {true, false, true, false}, {true, false, false, true} ,{false, false, true, true}, {false, true, false, true}, {false, true, true, false}};
//
//        Random r = new Random();
//        int i1 = r.nextInt(6);

//        notificationArray = notificationArrays[i1];
//        notifications = notificationArray[0];

        if (notifications)
            notificationStatus.setText("Notification status: On");
        else
            notificationStatus.setText("Notification status: Off");

        switch(notifyState) {
            case 0:
                notificationType.setText("Notification type: Silent");
                break;
            case 1:
                notificationType.setText("Notification type: Vibrate");
                break;
            case 2:
                notificationType.setText("Notification type: Ring");
                break;
            case 3:
                notificationType.setText("Notification type: Vibrate and ring");
                break;
        }

        searchWord.setText("Currently detecting: "+KEYWORD_SEARCH);

        // Initialize recognizer (i/o heavy, put in asynchronous task)
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                }
                catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption)).setText("Failed to initialize recognizer " + result);
                }
                else {
                    switchSearch(KEYWORD_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        if (!timerHasStarted) {
            counter = new MyCountDownTimer(timeRemaining, interval);
            counter.start();
            timerHasStarted = true;
            startB.setText("Stop");
        } else {
            counter.cancel();
            timerHasStarted = false;
            startB.setText("Resume");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }

    /* Partial recogniztion - used for keyword spotting */
    @Override
    public void onPartialResult(Hypothesis hypo) {
        if (hypo == null)
            return;
        if (!timerHasStarted)
            switchSearch(KEYWORD_SEARCH);

        Vibrator v = (Vibrator) getSystemService(MainActivity.this.VIBRATOR_SERVICE);

        String text = hypo.getHypstr();
        if (text.equals(KEYWORD_SEARCH_1)) {
            currentCount_1 += 1;
            totalCount_1 += 1;
            if (notifications) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
                v.vibrate(300);
            }
            totalCount.setText("Total count: " + String.valueOf(totalCount_1));
            switchSearch(KEYWORD_SEARCH);
        }
        else if (text.equals(KEYWORD_SEARCH_2)) {
            currentCount_2 += 1;
            if (notifications) {
                v.vibrate(300);
            }
            switchSearch(KEYWORD_SEARCH);
        }
        else if (text.equals(KEYWORD_SEARCH_3)) {
            currentCount_3 += 1;
            if (notifications) {
                v.vibrate(300);
            }
            switchSearch(KEYWORD_SEARCH);
        }
        else {
            switchSearch(KEYWORD_SEARCH);
        }
    }

    @Override
    public void onResult(Hypothesis hypo) {
        if (hypo != null) {
            String text = hypo.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    // When talking ends
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KEYWORD_SEARCH))
            switchSearch(KEYWORD_SEARCH);
    }

    private void switchSearch(String searchQuery) {
        recognizer.stop();

        if (searchQuery.equals(KEYWORD_SEARCH))
            recognizer.startListening(searchQuery);
        else
            recognizer.startListening(searchQuery, 15000);

    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-1f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KEYWORD_SEARCH, KEYWORD_SEARCH);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KEYWORD_SEARCH);
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            time.setText("Time's up!");
            counts.setText(counts.getText() + "Minute 16 count: " + String.valueOf(currentCount_1) + "\n");
            counts.setText(counts.getText() + "Total count: " + String.valueOf(totalCount_1));
            recognizer.cancel();
            recognizer.shutdown();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            System.out.println(millisUntilFinished / 1000);
            if ((millisUntilFinished / 1000)%60 < 10) {
                time.setText("" + (millisUntilFinished / 1000) / 60 + ":0" + ((millisUntilFinished / 1000) % 60));
            }
            else {
                time.setText("" + (millisUntilFinished / 1000) / 60 + ":" + ((millisUntilFinished / 1000) % 60));
            }
            timeRemaining = millisUntilFinished;

            if ((millisUntilFinished/1000) % 120 == 0) {
                counts.setText(counts.getText() + "Minute " + String.valueOf(Math.abs((millisUntilFinished / 60000) - 16)) + " count: " + String.valueOf(currentCount_1) + "\n");
//                notificationIdx += 1;
//                if (notificationIdx == 4) {
//                    notificationIdx = 0;
//                }
//                notifications = notificationArray[notificationIdx];
                if (notifications)
                    notificationStatus.setText("Notification status: On");
                else
                    notificationStatus.setText("Notification status: off");
                currentCount_1 = 0;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

        // TODO
//            startActivityForResult(intent, 0);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
    // TODO
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //Retrieve data in the intent
//        String editTextValue = data.getStringExtra("valueId");
//    }
}
