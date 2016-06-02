package com.snakei;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.sensibility_testbed.SensibilityApplication;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by lukas on 5/27/16.
 *
 *
 *
 */
public class MediaService implements TextToSpeech.OnInitListener {
    static final String TAG = "MediaService";
    public class MediaPlayInfo {

    }

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
        tts = new TextToSpeech(SensibilityApplication.getAppContext(), this);
    }
    public void stop_media() {
        tts_initialized = false;
        tts.shutdown();
    }

    public void microphoneRecord(String file_name, int duration) {

    }

//    public boolean isMediaPlaying() {
//
//        return is_media_playing;
//    }
//
//    public MediaPlayInfo getMediaPlayInfo() {
//
//        return media_play_info;
//    }
//
//    public boolean isTtsSpeaking() {
//        return is_tts_speaking;
//    }
    /*
     * Adds method to TTS queue for playback.
     *
     * Todo:
     *      Should be return right away when the message was added to the queue
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
    public boolean ttsSpeak(String message) throws InterruptedException {

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

            int x = tts.speak(message, queue_mode, null, utterance_id.toString());
            Log.i(TAG, String.format("speak returned: %d", x));

        }

        return true;
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, String.format("On TTS init: %d ", status));
        if (status == TextToSpeech.SUCCESS) {
            tts_initialized = true;
        }
    }
}
