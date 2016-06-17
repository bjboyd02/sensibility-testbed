package com.snakei;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.sensibility_testbed.SensibilityApplication;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by lukas on 5/27/16.
 *
 * Pseudo service that facades some Android media services
 *
 * - Get info about the media
 * - Record audio to a file
 * - Text-to-speech
 *
 * Todo:
 *   currently we use most of the default settings for audio playing and recording
 *   should we allow the user to change these settings?
 *
 *   same story as in other sensibility service providers, how do we handle initialization
 *   release of resources?
 *
 *   Skips media info function for the moment (do we really need this?)
 */
public class MediaService implements TextToSpeech.OnInitListener, MediaRecorder.OnInfoListener {
    static final String TAG = "MediaService";

    AudioManager audio_manager;
    private MediaRecorder recorder;
    private TextToSpeech tts;
    private boolean tts_initialized = false;
    private int queue_mode = TextToSpeech.QUEUE_FLUSH;

    /* See Initialization on Demand Holder pattern */
    private static class MediaServiceHolder {
        private static final MediaService instance = new MediaService();
    }

    /* Classic Singleton Instance Getter */
    public static MediaService getInstance() {
        return MediaServiceHolder.instance;
    }

    public void start_media() {
        Context app_context = SensibilityApplication.getAppContext();
        tts = new TextToSpeech(app_context, this);
        audio_manager = (AudioManager)app_context.getSystemService(Context.AUDIO_SERVICE);

    }
    public void stop_media() {
        tts_initialized = false;
        tts.shutdown();
        //recorder.release();
    }

    /*
     * Records from the default microphone to a file at
     * passed `file_name` for a given `duration` (ms) if the
     * duration is exceeded, recording stops and automatically
     * and `onInfo` receives a message
     * `MEDIA_RECORDER_INFO_MAX_DURATION_REACHED`
     *
     * Todo:
     *      Think about initialization and freeing of resource
     *         Should this be done in start_media, stop_media?
     *
     *      There are a lot of things that can be parametrized
     *      e.g. Format, Encoder, ...
     *
     */
    public void microphoneRecord(String file_name, int duration) {
        Log.i(TAG, String.format("Trying to record to file: %s", file_name));
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(file_name);
        recorder.setMaxDuration(duration);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.i(TAG, "prepare failed()");
        }
        recorder.start();
    }

    public boolean isMediaPlaying() {
        return audio_manager.isMusicActive();
    }


    /*
     * Checks if TTS is speaking
     * Apparently also returns TRUE if something
     * is in the queue but utterance has not
     * started yet.
     */

    public boolean isTtsSpeaking() {
        if (tts_initialized) {
            return tts.isSpeaking();
        }
        return false;
    }
    /*
     * Adds method to TTS queue for playback.
     *
     * Todo:
     *      Should we return right away when the message was added to the queue
     *      or should we wait until the message was spoken?

     * Further possible parameters
     * Queue Mode:
     *   QUEUE_ADD - add to tts queue and play when ready
     *   QUEUE_FLUSH - flush queue and play immedeatly
     * KEY_PARAM_STREAM
     * KEY_PARAM_VOLUME
     * KEY_PARAM_PAN
     * Engine specific parameters
     */
    public int ttsSpeak(String message) throws InterruptedException {

        Log.i(TAG, String.format("In java we received: '%s'", message));
        if (tts != null) {
            while (true) {
                if (tts_initialized)
                    break;
                Log.i(TAG, "Waiting for TTS to init");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            UUID utterance_id = UUID.randomUUID();

            // XXX: UtteranceProgressListern is not really needed
            // This is just handy for debugging
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.i(TAG, "Start uttering");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.i(TAG, "Done uttering");
                }

                @Override
                public void onError(String utteranceId) {
                    Log.i(TAG, "Error while uttering");
                }
            });

            int retval = tts.speak(message, queue_mode, null, utterance_id.toString());
            Log.i(TAG, String.format("speak returned: %d", retval));
            return retval;
        }
        return -1;
    }

    /*
     * ###################################################
     * Text To Speech Initialization callback
     * ###################################################
     */
    @Override
    public void onInit(int status) {
        Log.i(TAG, String.format("On TTS init: %d ", status));
        if (status == TextToSpeech.SUCCESS) {
            tts_initialized = true;
        }
    }

    /*
     * ###################################################
     * Media Recorder Info Callback
     * e.g. gets called when maximum record time is reached
     * ###################################################
     */
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.i(TAG, String.format("Info: %d", what));
    }
}
