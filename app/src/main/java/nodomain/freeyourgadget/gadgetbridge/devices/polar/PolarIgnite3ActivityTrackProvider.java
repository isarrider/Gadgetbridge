package nodomain.freeyourgadget.gadgetbridge.devices.polar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummary;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityPoint;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityTrack;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityTrackProvider;
import nodomain.freeyourgadget.gadgetbridge.model.GPSCoordinate;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto;

/** Builds the details of a workout from the samples kept in its summary. */
public class PolarIgnite3ActivityTrackProvider implements ActivityTrackProvider {
    @Nullable
    @Override
    public ActivityTrack getActivityTrack(@NonNull final BaseActivitySummary summary) {
        final byte[] raw = summary.getRawSummaryData();
        if (raw == null || raw.length == 0 || summary.getStartTime() == null) {
            return null;
        }
        final PolarProto.PbExerciseSamples samples;
        try {
            samples = PolarProto.PbExerciseSamples.parseFrom(raw);
        } catch (final InvalidProtocolBufferException e) {
            return null;
        }
        final PolarProto.PbDuration interval = samples.getRecordingInterval();
        final long intervalMillis = (interval.getHours() * 3600L + interval.getMinutes() * 60L + interval.getSeconds()) * 1000L
                + interval.getMillis();
        if (intervalMillis <= 0) {
            return null;
        }
        final ActivityTrack track = new ActivityTrack();
        track.setUser(summary.getUser());
        track.setDevice(summary.getDevice());
        track.setName(summary.getName());
        final long start = summary.getStartTime().getTime();
        final PolarProto.PbExerciseRouteSamples route = readRoute(summary.getRawDetailsPath());
        if (route == null) {
            for (int i = 0; i < samples.getHeartRateSamplesCount(); i++) {
                final ActivityPoint point = new ActivityPoint(new Date(start + i * intervalMillis));
                point.setHeartRate(samples.getHeartRateSamples(i));
                track.addTrackPoint(point);
            }
            return track;
        }
        // One point per GPS fix, with the heart rate the watch recorded at that time
        final int points = Math.min(route.getDurationCount(), Math.min(route.getLatitudeCount(), route.getLongitudeCount()));
        for (int i = 0; i < points; i++) {
            final long offset = route.getDuration(i);
            final ActivityPoint point = new ActivityPoint(new Date(start + offset));
            point.setLocation(i < route.getGpsAltitudeCount()
                    ? new GPSCoordinate(route.getLongitude(i), route.getLatitude(i), route.getGpsAltitude(i))
                    : new GPSCoordinate(route.getLongitude(i), route.getLatitude(i)));
            final int sample = (int) (offset / intervalMillis);
            if (sample < samples.getHeartRateSamplesCount()) {
                point.setHeartRate(samples.getHeartRateSamples(sample));
            }
            track.addTrackPoint(point);
        }
        return track;
    }

    @Nullable
    private static PolarProto.PbExerciseRouteSamples readRoute(@Nullable final String path) {
        if (path == null) {
            return null;
        }
        final File file = new File(path);
        if (!file.isFile()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            return PolarProto.PbExerciseRouteSamples.parseFrom(in);
        } catch (final IOException e) {
            return null;
        }
    }
}
