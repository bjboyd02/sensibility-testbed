package com.snakei;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.sensibility_testbed.SensibilityApplication;

import org.w3c.dom.Text;

import java.util.UUID;

/**
 * Created by lukas on 5/27/16.
 *
 *
 *
 */
public class MediaService implements TextToSpeech.OnInitListener {

    public class MediaPlayInfo {

    }

    private TextToSpeech tts;
    private boolean tts_initialized = false;
    private int queue_method = TextToSpeech.QUEUE_ADD;

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

    public void microphoneRecord(String file_name, Integer duration) {

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
    public boolean ttsSpeak(String message) {
        if (tts != null && tts_initialized) {
            UUID utterance_id = UUID.randomUUID();
            tts.speak(message, queue_method, null, utterance_id.toString());
        }
        return true;
    }


    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts_initialized = true;
        }
    }

    public void test(int a, int b, int c) {
        Log.i("TAG", String.format("a: %d, b: %d, c: %d", a, b, c));
    }
}
