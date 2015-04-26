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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

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
                System.out.println("trying to start new activity");
                newActivity(v);
            }
        });
        // Show the ListView on the screen
        recordingsList.setAdapter(adapter);
//        recordingsList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onListItemClick(recordingsList.getPositionForView(v), recordingsList);
//            }
//        });
    }

    protected View onListItemClick(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        View item;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            item = listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            item = listView.getChildAt(childIndex);
        }
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        return item;
    }

    /** Called when the user clicks the newActivity button */
    public void newActivity(View view) {

        // Get the edit text UI element
//        EditText editText = (EditText) findViewById(R.id.edit_message);

        // Now get the message
//        String msg = editText.getText().toString();

//        Create a new Intent
        Intent intent = new Intent(this, RecordActivity.class);
//
//        //Send the message
//        intent.putExtra(EXTRA_MESSAGE, msg);

//        start the activity
        startActivity(intent);
    }
}

