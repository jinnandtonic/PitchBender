package rmillett.pitchbender;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PitchDetectionActivity extends AppCompatActivity {

    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;
    private static final int DENIED = PackageManager.PERMISSION_DENIED;

    private double frequencyAverage;
    private int collectionLimit;
    private int collectionCounter;

    private float mFreq;

    private TextView pitchText;
    private TextView noteText;

    Thread audioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_detection);

        frequencyAverage = 0;
        collectionCounter = 0;
        collectionLimit = 50;

        List<String> permsList = new ArrayList<>();



        int hasAudioPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (hasAudioPerm == DENIED) permsList.add(Manifest.permission.RECORD_AUDIO);

        // Some permissions have not been granted
        if (permsList.size() > 0)
        {
            // Convert the permsList to an array
            String[] permsArray = new String[permsList.size()];
            permsList.toArray(permsArray);

            // Ask user for them
            ActivityCompat.requestPermissions(this, permsArray, 1337);
        }


        pitchText = (TextView) findViewById(R.id.pitchTextView);
        noteText = (TextView) findViewById(R.id.noteTextView);

        if (hasAudioPerm == GRANTED) {

            AudioDispatcher dispatcher =
                    AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                    final float pitchInHz = res.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processPitch(pitchInHz);
                        }
                    });
                }
            };
            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
            dispatcher.addAudioProcessor(pitchProcessor);


            audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();
        }
    }

    public void processPitch(float pitchInHz) {

        pitchText.setText("" + pitchInHz);


        if (pitchInHz > 0 && collectionCounter < collectionLimit) {
            frequencyAverage += pitchInHz;
            collectionCounter++;
        }

//        if (collectionCounter <= collectionLimit) {
//            //Log.i("PD Act", "collectionCounter->" + collectionCounter);
//        }


        if (collectionCounter >= collectionLimit) {
            pitchInHz = (float) frequencyAverage / collectionLimit;
            mFreq = pitchInHz;

            //Log.i("PD Act", "pitchInHz->" + pitchInHz);

//            if(pitchInHz >= 110 && pitchInHz < 123.47) {
//                //A
//                noteText.setText("A");
//            }
//            else if(pitchInHz >= 123.47 && pitchInHz < 130.81) {
//                //B
//                noteText.setText("B");
//            }
//            else if(pitchInHz >= 130.81 && pitchInHz < 146.83) {
//                //C
//                noteText.setText("C");
//            }
//            else if(pitchInHz >= 146.83 && pitchInHz < 164.81) {
//                //D
//                noteText.setText("D");
//            }
//            else if(pitchInHz >= 164.81 && pitchInHz <= 174.61) {
//                //E
//                noteText.setText("E");
//            }
//            else if(pitchInHz >= 174.61 && pitchInHz < 185) {
//                //F
//                noteText.setText("F");
//            }
//            else if(pitchInHz >= 185 && pitchInHz < 196) {
//                //G
//                noteText.setText("G");
//            }
        }
    }

    public void getNote(View view) {
        String note = "None";
        Log.i("PD Act", "before->" + note);
        if (collectionCounter >= 0) {
            note = Music.parsePitchClassFromFrequency(mFreq, Music._12_TET_PITCH_FREQUENCIES);
            noteText.setText(note);
        }
        Log.i("PD Act", "after->" + note);
    }
}
