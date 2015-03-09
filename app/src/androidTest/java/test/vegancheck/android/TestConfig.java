package test.vegancheck.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import android.text.TextUtils;

import junit.framework.Assert;

import vegancheck.android.Config;
import vegancheck.android.location.LocationKeeper;
import vegancheck.android.location.Locator;

public class TestConfig extends AndroidTestCase {
    private Context context;

    @Override
    protected void setUp () {
        context = new IsolatedContext(new MockContentResolver(), getContext());
    }

    @Override
    protected void tearDown () {
        context = null;
    }

    public void testConstructs() {
        Config config = null;
        try {
            config = new Config(context.getResources());
        } catch (final Throwable e) {
            config = null;
        }

        Assert.assertNotNull(config);
    }

    public void testHasVersion() {
        final Config config = new Config(context.getResources());
        final int configVersion = config.getVersion();
        Assert.assertTrue(configVersion >= 0);
    }
}
