package nodomain.freeyourgadget.gadgetbridge.entities;

import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;

/**
 * The watch records steps, not an intensity or a kind. The steps of a minute stand in for the
 * intensity, and a minute with data counts as activity; the sample provider lays the sleep the
 * watch detected over that. Neither is stored.
 */
public abstract class AbstractPolarIgnite3ActivitySample extends AbstractActivitySample {
    private int rawKind = NOT_MEASURED;
    private int rawIntensity = NOT_MEASURED;
    private boolean intensityOverridden;

    @Override
    public abstract int getSteps();

    @Override
    public int getRawKind() {
        if (rawKind != NOT_MEASURED) {
            return rawKind;
        }
        return getSteps() >= 0 || getHeartRate() > 0 ? ActivityKind.ACTIVITY.getCode() : NOT_MEASURED;
    }

    @Override
    public void setRawKind(final int kind) {
        rawKind = kind;
    }

    @Override
    public int getRawIntensity() {
        return intensityOverridden ? rawIntensity : getSteps();
    }

    @Override
    public void setRawIntensity(final int intensity) {
        rawIntensity = intensity;
        intensityOverridden = true;
    }
}
