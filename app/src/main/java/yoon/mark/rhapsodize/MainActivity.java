package yoon.mark.rhapsodize;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.R.*;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements RecognitionListener {
    /* class variables */
    private static final String KEYWORD_SEARCH = "testing";
    private SpeechRecognizer recognizer;
    private ListView recordingsList;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_main);
        // Relate the listView from java to the one created in XML
        recordingsList = (ListView) findViewById(R.id.list);
        ImageButton fabImageButton = (ImageButton) findViewById(R.id.fab_image_button);
        final ArrayList<String> list = new ArrayList<>();

        // Populate list view
        for (int i=0; i<5; i++) {
            list.add("Item "+i);
        }
        final MyCustomAdapter adapter = new MyCustomAdapter(MainActivity.this, list);
        fabImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.add("New Item");
                adapter.notifyDataSetChanged();
                newActivity(recordingsList.);
            }
        });
        // Show the ListView on the screen
        recordingsList.setAdapter(adapter);

        // Display data
//        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.caption)).setText("Preparing the recognizer");

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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
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

        Vibrator v = (Vibrator) getSystemService(MainActivity.this.VIBRATOR_SERVICE);

        String text = hypo.getHypstr();
        if (text.equals(KEYWORD_SEARCH)) {
            v.vibrate(300);
            ((TextView) findViewById(R.id.result)).setText(text);
            switchSearch(KEYWORD_SEARCH);
        }
        else {
            ((TextView) findViewById(R.id.result)).setText(text);
        }
    }

    @Override
    public void onResult(Hypothesis hypo) {
        ((TextView) findViewById(R.id.result)).setText("");
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

        String caption = "Demo!";
        ((TextView) findViewById(R.id.caption)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

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

    /** Called when the user clicks the newActivity button */
    public void newActivity(View view) {
        // Get the edit text UI element
//        EditText editText = (EditText) findViewById(R.id.edit_message);

        // Now get the message
//        String msg = editText.getText().toString();

        // Create a new Intent
//        Intent intent = new Intent(this, Second.class);
//
//        //Send the message
//        intent.putExtra(EXTRA_MESSAGE, msg);

        // start the activity
//        startActivity(intent);
    }
}

