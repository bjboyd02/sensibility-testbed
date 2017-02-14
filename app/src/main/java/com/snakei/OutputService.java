package com.snakei;
// TODO Should we make this a proper Library Module? https://developer.android.com/tools/projects/index.html#LibraryModules

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat.Builder;

import com.sensibility_testbed.R;


/**
 * Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 * on 5/4/16.
 *
 * A pseudo Service class that provides static methods to write a a message
 * to an Android Toast to the device's UI and to ask for user input (yes/no)
 *
 * Note:
 * This and all of the other snakei.*Service.java classes (except for PythonInterpreterService)
 * are not real Android Services, they are never started as a Service in the app.
 * Nevertheless, they use PythonInterpreterService's context, i.e. they are
 * executed in the context of a Service.
 *
 */
public class OutputService {
    static final String TAG = "OutputService";
    private static Toast toast;
    // XXX LP: smells like a race condition! :/
    private static boolean click_value;

    /*
     * Prints message (toast) to Android GUI.
     * Runs on main thread of the Service.
     *
     * Useful for e.g. GPS debugging where I have to carry the phone around
     *
     */
    public static void toastMessage(final Context context, final String message) throws Exception {
        Log.d(TAG, String.format("toastMessage: %s", message));

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    /*
     * Prints message (toast) to Android GUI and returns user input.
     *
     * FIXME: This should only be used for debugging, I don't know how it behaves if multiple
     * threads simultaneously prompt for user input.
     *
     * Notes:
     * Prompting AlertDialogs from a Service and waiting for user input is generally discouraged
     * and Android makes it rather hard to do so.
     * A workaround (also discouraged) is using SYSTEM_ALERT, which requires extra user permissions
     * and does not seem to work in our Android API
     *
     * But disguising the alertdialog as Toast does work. See more information at:
     * http://stackoverflow.com/questions/30502191/launch-alertdialog-builder-from-service
     * http://stackoverflow.com/questions/7918571/how-to-display-a-dialog-from-a-service
     * http://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t
     * http://stackoverflow.com/questions/22627184/android-alert-dialog-from-inside-an-intent-service
     *
     */
    public static boolean promptMessage(final Context context, final String message) throws Exception {
        Log.d(TAG, String.format("promptMessage: %s", message));

        final Object click_sync = new Object();
        final Handler handler = new Handler(context.getMainLooper());

        // Post prompt disguised as Toast to the main thread (UI)
        handler.post(new Runnable() {
             @Override
             public void run() {
                 // Setup prompt (alert dialog)
                 final AlertDialog dialog = new AlertDialog.Builder(context).create();
                 dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                 dialog.setTitle("Repy wants to know...");
                 dialog.setMessage(message);
                 // Setup no putton and click listener
                 dialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 synchronized(click_sync){
                                     Log.d(TAG, "User clicked YES");
                                     click_sync.notify();
                                     click_value = true;
                                     dialog.dismiss();
                                 }
                             }
                         }
                 );
                 // Setup yes putton and click listener
                 dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "no",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 Log.d(TAG, "User clicked NO");
                                 synchronized (click_sync) {
                                     click_sync.notify();
                                     click_value = false;
                                     dialog.dismiss();
                                 }
                             }
                         }
                 );
                 dialog.show();
             }
         });

        // The function uses a sync token to wait for a user input
        // The token gets notified in when the user clicks on yes or no, see listener which also
        // sets a global (static class variable) to true or false, which gets returned.
        // FIXME: The static class variable was the easiest way to share data between
        // the click listener and the outer function, but might carry a race condition
        // FIXME: potential ANR (application not responding) if we wait too long ?
        synchronized (click_sync){
            click_sync.wait();
            return click_value;
        }
    }


    /*
     * Simpler than simple notification function to print message to Android UI notification
     * drawer.
     *
     * The notification has no click handler, so if the message is too long for the screen it gets
     * truncated by the system and there is no way to see the entire message.
     *
     * See https://developer.android.com/guide/topics/ui/notifiers/notifications.html
     *
     */
    public static void notifyMessage(final Context context, String message) {

        Log.d(TAG, String.format("notifyMessage: %s", message));

        Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("Repy says...")
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher);

        NotificationManager mNotificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        // First argument is an id to identify and e.g. update the notification
        // Because the ID remains unchanged an existing notification is updated
        mNotificationManager.notify(0, mBuilder.build());

    }
}
