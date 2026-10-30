package nodomain.freeyourgadget.gadgetbridge.companion;

import android.net.Uri;
import android.os.Bundle;

/** Versioned, local-only structured workout uploads. Access is approved per app and device. */
interface IWorkoutSyncService {
    int getApiVersion();
    Bundle[] getDevices();
    Bundle submitWorkout(String requestId, String deviceAddress, in Uri contentUri);
    Bundle getJob(String requestId, String deviceAddress);
    void forgetJob(String requestId, String deviceAddress);
    Bundle cancelJob(String requestId, String deviceAddress);
}
