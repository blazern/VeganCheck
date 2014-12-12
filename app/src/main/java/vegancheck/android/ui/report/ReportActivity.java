package vegancheck.android.ui.report;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import vegancheck.android.App;
import vegancheck.android.BarcodeToolkit;
import vegancheck.android.R;
import vegancheck.android.network.ParcelableNameValuePair;
import vegancheck.android.ui.BarcodeHttpActionActivity;
import vegancheck.android.ui.BarcodeHttpActionFragment;

public class ReportActivity extends BarcodeHttpActionActivity {
    public ReportActivity() {
        super(
                App.getConfig().getServerUrl() + "addcomment.php",
                R.string.report_activity_submit_request_sent_toast,
                R.string.report_activity_on_request_successfully_delivered,
                R.string.report_activity_title);
    }

    @Override
    protected final List<ParcelableNameValuePair> createPostParametersFor(final Object actionResult) {
        final List<ParcelableNameValuePair> postParameters = new ArrayList<ParcelableNameValuePair>();

        if (actionResult instanceof String) {
            final String reportText = (String) actionResult;

            postParameters.add(new ParcelableNameValuePair("bcod", getBarcode()));
            postParameters.add(new ParcelableNameValuePair("name", App.getDeviceID()));
            postParameters.add(new ParcelableNameValuePair("comment", reportText));
        } else {
            App.error(this, "(actionResult instanceof String) == false!");
        }

        return postParameters;
    }

    @Override
    protected final BarcodeHttpActionFragment createFragment() {
        return ReportFragment.createFor(getBarcode());
    }

    /**
     * @param barcode must be valid (i.e. BarcodeToolkit.isValid(barcode)==true)
     * @param context must not be null
     * @throws java.lang.IllegalArgumentException if any parameter is invalid
     */
    public static void startFor(final String barcode, final Context context) {
        if (!BarcodeToolkit.isValid(barcode)) {
            throw new IllegalArgumentException("barcode must be valid");
        } else if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        final Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(BARCODE_EXTRA, barcode);
        context.startActivity(intent);
    }
}
