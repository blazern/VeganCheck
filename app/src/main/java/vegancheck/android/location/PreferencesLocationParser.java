package vegancheck.android.location;

import android.content.SharedPreferences;
import android.location.Location;

import vegancheck.android.App;
import vegancheck.android.util.SharedPreferencesToolkit;

final class PreferencesLocationParser {
    private static final String KEY_START = PreferencesLocationParser.class.getCanonicalName() + ".";
    private static final String KEY_IS_ENCODED = KEY_START + "IS_ENCODED";
    private static final String KEY_PROVIDER = KEY_START + "PROVIDER";
    private static final String KEY_ACCURACY = KEY_START + "ACCURACY";
    private static final String KEY_LONGITUDE = KEY_START + "LONGITUDE";
    private static final String KEY_LATITUDE = KEY_START + "LATITUDE";
    private static final String KEY_TIME = KEY_START + "TIME";

    private PreferencesLocationParser() {
    }

    /**
     * If location is null, then the method encodes null into the preferences.
     */
    public static void encode(final SharedPreferences sharedPreferences, final Location location) {
        if (sharedPreferences == null) {
            App.error(PreferencesLocationParser.class, "can't encode into null");
            return;
        }

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if (location != null) {
            editor.putBoolean(KEY_IS_ENCODED, true);
            editor.putString(KEY_PROVIDER, location.getProvider());
            editor.putFloat(KEY_ACCURACY, location.getAccuracy());
            SharedPreferencesToolkit.putDouble(editor, KEY_LONGITUDE, location.getLongitude());
            SharedPreferencesToolkit.putDouble(editor, KEY_LATITUDE, location.getLatitude());
            editor.putLong(KEY_TIME, location.getTime());
        } else {
            editor.putBoolean(KEY_IS_ENCODED, false);
        }

        editor.apply();
    }

    public static Location parse(final SharedPreferences preferences) {
        if (preferences == null) {
            App.error(PreferencesLocationParser.class, "can't decode from null");
            return null;
        }

        if (preferences.getBoolean(KEY_IS_ENCODED, false)
                && preferences.contains(KEY_PROVIDER)
                && preferences.contains(KEY_ACCURACY)
                && preferences.contains(KEY_LONGITUDE)
                && preferences.contains(KEY_LATITUDE)
                && preferences.contains(KEY_TIME)) {
            final Location location = new Location(preferences.getString(KEY_PROVIDER, ""));
            location.setAccuracy(preferences.getFloat(KEY_ACCURACY, -1));
            location.setLongitude(SharedPreferencesToolkit.getDouble(preferences, KEY_LONGITUDE, -1));
            location.setLatitude(SharedPreferencesToolkit.getDouble(preferences, KEY_LATITUDE, -1));
            location.setTime(preferences.getLong(KEY_TIME, -1));
            return location;
        } else {
            return null;
        }
    }
}
