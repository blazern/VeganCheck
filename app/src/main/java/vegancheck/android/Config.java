package vegancheck.android;

import android.content.res.Resources;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public final class Config {
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String DEFAULT_URL = "http://lumeria.ru/vscaner/1.11/";
    private static final int DEFAULT_VERSION = -1;
    private JSONObject config;

    /**
     * @param resources must be not null
     * @throws IllegalArgumentException any parameter is invalid
     */
    public Config(final Resources resources) {
        if (resources == null) {
            throw new IllegalArgumentException("can't initialize without Resources");
        }

        // TODO: return the commented code bellow
        // (not in the same form, there's no need in WRITE_EXTERNAL_STORAGE permission)
//        try {
//            config = getConfigFromExternalFolder(resources);
//        } catch (final IOException externalConfigException) {
//            App.error("this", externalConfigException.getMessage());
//
            try {
                config = getConfigFromAssetsFolder(resources);
            } catch (final IOException assetsConfigException) {
                App.error("this", assetsConfigException.getMessage());
                config = null;
            }
//        }
    }

    /**
     * If a config doesn't exist in the external folder, copies the assets one to it.
     * If the config from the external folder has a version lower than the config in assets, the external
     * one will be replaced with the assets one.
     */
    private JSONObject getConfigFromExternalFolder(final Resources resources) throws IOException {
        if (!isExternalStorageReadable()) {
            throw new IOException("is device not ok or did the programmer make an error?");
        }

        final File externalConfigFile = getExternalConfigFile(resources.getString(R.string.app_name));
        if (!externalConfigFile.exists()) {
            createExternalConfigFromAssets(resources, externalConfigFile);
        }

        final FileInputStream fileInputStream = new FileInputStream(externalConfigFile);
        final String externalConfigAsString;
        try {
            externalConfigAsString = convertStreamToString(fileInputStream);
        } finally {
            fileInputStream.close();
        }

        final JSONObject externalConfig;
        try {
            externalConfig = new JSONObject(externalConfigAsString);
        } catch (final JSONException e) {
            throw new IOException("for some reason data read from external config file was not valid", e);
        }

        final JSONObject assetsConfig = getConfigFromAssetsFolder(resources);
        JSONObject result = assetsConfig;
        try {
            if (assetsConfig.getInt("config_version") > externalConfig.getInt("config_version")) {
                result = assetsConfig;
                createExternalConfigFromAssets(resources, externalConfigFile);
            } else {
                result = externalConfig;
            }
        } catch (final JSONException e) {
            createExternalConfigFromAssets(resources, externalConfigFile);
            App.error(this, "looks like something went wrong with config parsing", e);
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

    private void createExternalConfigFromAssets(final Resources resources, final File externalConfigFile) throws IOException {
        final String assetsConfig = getAssetConfig(resources);
        final PrintWriter printWriter = new PrintWriter(externalConfigFile);
        try {
            printWriter.print(assetsConfig);
        } finally {
            printWriter.close();
        }
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

    public int getVersion() {
        if (config != null) {
            try {
                return config.getInt("config_version");
            } catch (final JSONException e) {
                App.logError(this, e.getMessage());
                return DEFAULT_VERSION;
            }
        }
        return DEFAULT_VERSION;
    }
}
