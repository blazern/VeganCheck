package vegancheck.android;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.Arrays;

import vegancheck.android.ui.MyActivityBase;

public class App extends Application {
    private static boolean isDebug = true;
    private static Application applicationInstance;
    private static Config config;
    private static MyActivityBase currentActivity;
    private static boolean scanBarcodeAppInstalled;

    @Override
    public void onCreate() {
        applicationInstance = this;
        com.google.zxing.integration.android.IntentIntegrator.titleStringId =
                R.string.barcode_app_install_request_title;
        com.google.zxing.integration.android.IntentIntegrator.messageStringId =
                R.string.barcode_app_install_request_message;
        com.google.zxing.integration.android.IntentIntegrator.yesStringId =
                R.string.barcode_app_install_request_reply_yes;
        com.google.zxing.integration.android.IntentIntegrator.noStringId =
                R.string.barcode_app_install_request_reply_no;
        config = new Config(getContext().getResources());
    }

    public static Context getContext() {
        return applicationInstance;
    }

    public static String getStringWith(final int stringId) {
        return applicationInstance.getString(stringId);
    }

    public static String getName() {
        return applicationInstance.getString(R.string.app_name);
    }

    public static void logError(final Object requester, final String message) {
        if (isDebug) {
            Log.e(getName(), requester.getClass().toString() + ": " + message);
        }
    }

    public static void logDebug(final Object requester, final String message) {
        if (isDebug) {
            Log.d(getName(), requester.getClass().toString() + ": " + message);
        }
    }

    public static void logInfo(final Object requester, final String message) {
        if (isDebug) {
            Log.i(getName(), requester.getClass().toString() + ": " + message);
        }
    }

    public static void logInfo(final Object requester, final String message, final Exception e) {
        if (isDebug) {
            Log.i(getName(), requester.getClass().toString() + ": " + message, e);
        }
    }

    public static void wtf(final Object requester, final String message) {
        if (isDebug) {
            Log.wtf(getName(), requester.getClass().toString() + ": " + message);
        }
    }

    public static void assertCondition(final boolean condition) {
        if (condition == false) {
            if (isDebug) {
                throw new AssertionError();
            } else {
                Crashlytics.log(
                        "ASSERTATION FAILED!\n"
                                + Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }
    }

    public static void assertCondition(final boolean condition, final String message) {
        if (condition == false) {
            logError(App.class, message);
            if (isDebug) {
                throw new AssertionError();
            } else {
                Crashlytics.log(
                        "ASSERTATION FAILED! message: '" + message + "'\n"
                                + Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }
    }

    public static void error(final String message) {
        logError(App.class, message);
        if (isDebug) {
            throw new Error(message);
        } else {
            Crashlytics.log(
                    "ERROR! message: '" + message + "'\n"
                            + Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }

    public static void error(final Object requester, final String message) {
        logError(requester, message);
        error(message);
    }

    public static boolean isOnline() {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) applicationInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static void onActivityPause(final MyActivityBase activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    public static void onActivityResumeFragments(final MyActivityBase activity) {
        final IntentIntegrator scanIntegrator = new IntentIntegrator(activity);
        if (!scanBarcodeAppInstalled) {
            try {
                final AlertDialog installScanBarcodeAppDialog = scanIntegrator.showDialogIfNoApp(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.finish();
                    }
                });
                scanBarcodeAppInstalled = installScanBarcodeAppDialog == null;
            } catch (final Exception e) {
                App.logError(
                        applicationInstance,
                        "an error occurred after an attempt to show a 'install XZing' dialog");
                activity.finish();
            }
        }

        currentActivity = activity;
    }

    /**
     * NOTE that an Activity IS NOT considered as the front one
     * before its 'onResumeFragments()' is called<br>
     * This means that there's is NO front activity during a 'onCreate()' call of an initializing activity
     *
     * @return front activity
     */
    public static MyActivityBase getFrontActivity() {
        return currentActivity;
    }

    public static String getDeviceID() {
        return Settings.Secure.getString(
                applicationInstance.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getAppVersion() {
        final PackageManager manager = applicationInstance.getPackageManager();
        try {
            final PackageInfo packageInfo = manager.getPackageInfo(applicationInstance.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            App.error(applicationInstance, e.getMessage());
            return "COULD NOT ACQUIRE APP VERSION (" + e.getMessage() + ")";
        }
    }

    public static Config getConfig() {
        return config;
    }
}
