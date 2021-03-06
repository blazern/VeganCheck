package test.vegancheck.android.ui.scan;

import android.content.Intent;
import android.os.Bundle;

import vegancheck.android.ui.scan.ScanActivity;
import vegancheck.android.ui.scan.ScanActivityState;

final class StateNotStayingLast extends ScanActivityState {
    public StateNotStayingLast(ScanActivity scanActivity) {
        super(scanActivity);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
    }
    @Override
    public void onResumeFragments() {
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }
    @Override
    public void onSaveStateData(Bundle outState) {
    }
    private static final class Restorer implements ScanActivityState.Restorer {
        @Override
        public ScanActivityState restoreFor(final ScanActivity activity) {
            return new StateNotStayingLast(activity);
        }
        @Override
        public boolean doesStayLast() {
            return false;
        }
    }
    @Override
    public ScanActivityState.Restorer save() {
        return new Restorer();
    }
}