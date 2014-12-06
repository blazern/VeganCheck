package vscanner.android;

import android.content.res.Resources;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

// TODO: do something about that:
// It's really dangerous to call methods of App from body of this class since its instances
// are created by App itself - it's possible that at some point in the future this class would call
// method of not yet initialized App
public final class Config {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String DEFAULT_URL = "http://lumeria.ru/vscaner/";
    private JSONObject config;

    public String getServerUrl() {
        if (config != null) {
            try {
                return config.getString("server_url");
            } catch (final JSONException e) {
                App.logError(this, e.getMessage());
                return DEFAULT_URL;
            }
        }
        return DEFAULT_URL;
    }

    public Config(final Resources resources) {
        if (resources == null) {
            throw new IllegalArgumentException("can't initialize without Resources");
        }

        try {
            config = getConfigFromExternalFolder(resources);
        } catch (final IOException externalConfigException) {
            App.error("this", externalConfigException.getMessage());

            try {
                config = getConfigFromAssetsFolder(resources);
            } catch (final IOException assetsConfigException) {
                App.error("this", assetsConfigException.getMessage());
                config = null;
            }
        }
    }

    private JSONObject getConfigFromExternalFolder(final Resources resources) throws IOException {
        if (!isExternalStorageReadable()) {
            throw new IOException("is device not ok or did the programmer make an error?");
        }

        final File externalConfig =
                getExternalConfigFile(resources.getString(R.string.app_name));

        if (!externalConfig.exists()) {
            final String assetsConfig = getAssetConfig(resources);
            final PrintWriter printWriter = new PrintWriter(externalConfig);
            try {
                printWriter.print(assetsConfig);
            } finally {
                printWriter.close();
            }
        }

        final FileInputStream fileInputStream = new FileInputStream(externalConfig);
        final String resultAsString;
        try {
            resultAsString = convertStreamToString(fileInputStream);
        } finally {
            fileInputStream.close();
        }

        final JSONObject result;
        try {
            result = new JSONObject(resultAsString);
        } catch (final JSONException e) {
            throw new IOException("for some data read from external config file was not valid", e);
        }
        return result;
    }

    private boolean isExternalStorageReadable() {
        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private File getExternalConfigFile(final String appName) throws IOException {
        final File externalDirectory = Environment.getExternalStorageDirectory();
        final File appDir = new File(externalDirectory, appName + "/");

        if (!appDir.exists()) {
            final boolean appDirWasCreated = appDir.mkdir();
            if (!appDirWasCreated) {
                throw new IOException("couldn't create the app directory");
            }
        }

        return new File(appDir, CONFIG_FILE_NAME);
    }

    private String getAssetConfig(final Resources resources) throws IOException {
        final InputStream inputStream = resources.getAssets().open(CONFIG_FILE_NAME);
        final String assetConfig;
        try {
            assetConfig = convertStreamToString(inputStream);
        } finally {
            inputStream.close();
        }

        App.assertCondition(!assetConfig.equals(""));

        return assetConfig;
    }

    private String convertStreamToString(final InputStream inputStream) {
        java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private JSONObject getConfigFromAssetsFolder(final Resources resources) throws IOException {
        try {
            return new JSONObject(getAssetConfig(resources));
        } catch (final JSONException e) {
            throw new IOException("the json file in assets is not valid! wtf?");
        }
    }
}
