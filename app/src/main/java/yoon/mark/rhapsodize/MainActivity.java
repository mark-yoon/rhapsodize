package yoon.mark.rhapsodize;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.Random;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;
import android.view.View;
import android.os.CountDownTimer;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class MainActivity extends Activity implements RecognitionListener, OnClickListener {
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
    private boolean[] notificationArray;
    private boolean notifications;
    private int notificationIdx = 0;
    private Button startB;
    private final long startTime = 960 * 1000;
    private final long interval = 1 * 1000;
    private long timeRemaining = 960 * 1000;
    public TextView time;
    private CountDownTimer counter;
    private boolean timerHasStarted = false;
    public TextView counts;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Display data
        setContentView(R.layout.activity_main);
        startB = (Button) this.findViewById(R.id.timer_button);
        startB.setOnClickListener(this);
        time = (TextView) this.findViewById(R.id.timer);
        counter = new MyCountDownTimer(startTime, interval);
        time.setText(time.getText() + String.valueOf(startTime / 60000) + ":" + "00");
        counts = (TextView) this.findViewById(R.id.counts);
        boolean[][] notificationArrays = {{true, true, false, false}, {true, false, true, false}, {true, false, false, true} ,{false, false, true, true}, {false, true, false, true}, {false, true, true, false}};

        Random r = new Random();
        int i1 = r.nextInt(6);

        notificationArray = notificationArrays[i1];
        notifications = notificationArray[0];

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
            if (notifications) {
                v.vibrate(300);
            }
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
                totalCount_1 += currentCount_1;
                counts.setText(counts.getText() + "Minute " + String.valueOf(Math.abs((millisUntilFinished / 60000) - 8)) + " count: " + String.valueOf(currentCount_1) + "\n");
                notificationIdx += 1;
                if (notificationIdx == 4) {
                    notificationIdx = 0;
                }
                notifications = notificationArray[notificationIdx];
                currentCount_1 = 0;
            }

        }
    }
}
