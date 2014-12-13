package test.vegancheck.android.ui.scan;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Assert;

import vegancheck.android.ui.scan.ScanActivity;
import vegancheck.android.ui.scan.ScanActivityState;

public class TestScanActivity extends ActivityInstrumentationTestCase2<ScanActivity> {
    static {
        ScanActivity.setFirstStateCreator(new ScanActivity.FirstStateCreator() {
            @Override
            public ScanActivityState createFor(final ScanActivity activity) {
                return new StateStayingLast(activity);
            }
        });
    }

    private ScanActivity activity;
    private Instrumentation instrumentation;

    public TestScanActivity() {
        super(ScanActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        activity = getActivity();
        instrumentation = getInstrumentation();
    }

    public void testRestorersStackDoesNotExceedLimit() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int index = 0; index <= ScanActivity.MAX_STATES_RESTORERS_COUNT; ++index) {
                    activity.onStateRequestsChangeTo(new StateNotStayingLast(activity));
                }
            }
        });

        instrumentation.waitForIdleSync();
        Assert.assertEquals(ScanActivity.MAX_STATES_RESTORERS_COUNT, activity.getRestorersCopy().size());
    }

    /**
     * Read description of ScanActivityState.Restorer.doesStayLast().
     */
    public void testLast2NotRelativeRestorersNotCollapsing() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int index = 0; index < ScanActivity.MAX_STATES_RESTORERS_COUNT; ++index) {
                    activity.onStateRequestsChangeTo(new StateNotStayingLast(activity));
                }
            }
        });

        instrumentation.waitForIdleSync();

        for (int index = 0; index < ScanActivity.MAX_STATES_RESTORERS_COUNT - 2; ++index) {
            pressBack();
        }

        Assert.assertEquals(2, activity.getRestorersCopy().size());

        pressBack();
        Assert.assertEquals(1, activity.getRestorersCopy().size());

        pressBack();
        Assert.assertEquals(0, activity.getRestorersCopy().size());
    }

    private void pressBack() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.onBackPressed();
            }
        });
        instrumentation.waitForIdleSync();
    }

    /**
     * Read description of ScanActivityState.Restorer.doesStayLast().
     */
    public void testLast2RelativeRestorersCollapsing() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int index = 0; index < ScanActivity.MAX_STATES_RESTORERS_COUNT; ++index) {
                    activity.onStateRequestsChangeTo(new StateStayingLast(activity));
                }
            }
        });

        instrumentation.waitForIdleSync();

        for (int index = 0; index < ScanActivity.MAX_STATES_RESTORERS_COUNT - 2; ++index) {
            pressBack();
        }

        Assert.assertEquals(2, activity.getRestorersCopy().size());

        pressBack();
        Assert.assertEquals(0, activity.getRestorersCopy().size());
    }
}
