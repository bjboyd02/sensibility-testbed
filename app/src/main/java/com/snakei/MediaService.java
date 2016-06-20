package com.snakei;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
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
public class MediaService  {
    static final String TAG = "MediaService";

    private Context app_context;
    private AudioManager audio_manager;
    private TextToSpeech tts;
    private boolean tts_initialized = false;
    private Object tts_sync;
    private int queue_mode = TextToSpeech.QUEUE_FLUSH;

    /* See Initialization on Demand Holder pattern */
    private static class MediaServiceHolder {
        private static final MediaService instance = new MediaService();
    }

    /* Classic Singleton Instance Getter */
    public static MediaService getInstance() {
        return MediaServiceHolder.instance;
    }

    public MediaService() {
        app_context = SensibilityApplication.getAppContext();
        audio_manager = (AudioManager)app_context.getSystemService(Context.AUDIO_SERVICE);
        tts_sync = new Object();
    }

    public void start_media() {
        tts = new TextToSpeech(app_context,new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    synchronized (tts_sync) {
                        tts_initialized = true;
                        tts_sync.notify();
                    }
                }
            }
        });
    }

    public void stop_media() {
        tts_initialized = false;
        if(tts != null) {
            tts.shutdown();
        }
    }

    /*
     * Records from the default microphone to a file at
     * passed `file_name` for a given `duration` (ms).
     * This is blocking operation.
     *
     * The function creates, prepares and starts a new MediaRecorder
     * Then it suspends the calling thread for the specified time
     * (recording is performed on another thread) and releases the
     * resource once it is finished.
     *
     * Reasoning:
     *      This strategy is preferred over non-blocking, because Android
     *      makes it hard to determine the current state of a MediaRecorder
     *      which in turn makes proper resource handling (release when finished)
     *      difficult.
     *
     * Possible non-blocking approach
     *      For a non blocking approach we could use a recording session duration:
     *          recorder.setMaxDuration(int max_duration_ms) or a custom timer
     *      together with info or error listener:
     *          recorder.setOnInfoListener(OnInfoListener) or
     *          recorder.setOnErrorListener(OnErrorListener)
     *
     * There are a lot of things that can be parametrized
     *     e.g. Format, Encoder, ...
     */
    public void microphoneRecord(String file_name, int duration) throws InterruptedException, IOException {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(file_name);
        recorder.prepare();
        recorder.start();
        Thread.sleep(duration);
        recorder.stop();
        recorder.release();
    }

    public boolean isMediaPlaying() {
        return audio_manager.isMusicActive();
    }

    /*
     * Checks if TTS is speaking
     * Also returns TRUE if something
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
     * Text-To-Speech
     * Non-blocking operation
     *
     * Reasoning:
     *     Initializing tts engine only once eliminates long initialization
     *     time (~8 seconds on developer device) for subsequent calls to ttsSpeak
     * Caveats:
     *     Have to call start_media before using ttsSpeak and stop_media to release resources
     *     Concurrent calls to ttsSpeak will cancel each other
     *
     * Further possible parameters
     *   Queue Modes:
     *      QUEUE_ADD - add to tts queue and play when ready
     *      QUEUE_FLUSH - flush queue and play immediately
     *   KEY_PARAM_STREAM
     *   KEY_PARAM_VOLUME
     *   KEY_PARAM_PAN
     *   Engine specific parameters
     */
    public int ttsSpeak(String message) throws InterruptedException {

        if (tts != null) {
            synchronized (tts_sync) {
                if (!tts_initialized) {
                    tts_sync.wait();
                }
                UUID utterance_id = UUID.randomUUID();
                return  tts.speak(message, queue_mode, null, utterance_id.toString());
            }
        }
        // Something went wrong
        return -1;
    }
}
