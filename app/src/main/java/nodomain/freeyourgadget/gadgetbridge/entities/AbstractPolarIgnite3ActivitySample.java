package nodomain.freeyourgadget.gadgetbridge.entities;

/**
 * The watch records steps, not an intensity, so the steps of a minute stand in for it.
 */
public abstract class AbstractPolarIgnite3ActivitySample extends AbstractActivitySample {
    @Override
    public abstract int getSteps();

    @Override
    public int getRawIntensity() {
        return getSteps();
    }
}
