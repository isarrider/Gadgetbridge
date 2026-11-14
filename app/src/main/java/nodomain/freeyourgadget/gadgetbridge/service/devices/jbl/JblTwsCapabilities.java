/*  Copyright (C) 2026

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.jbl;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol capabilities that vary between JBL TWS models. Flags not set on the
 * builder are unsupported.
 */
public final class JblTwsCapabilities {
    private static final Logger LOG = LoggerFactory.getLogger(JblTwsCapabilities.class);

    /**
     * Result of evaluating a firmware-gated feature against a device firmware version.
     */
    public enum FirmwareStatus {
        /** The firmware version is missing or could not be parsed. */
        UNKNOWN,
        /** The product does not provide the feature, or the firmware is below the minimum. */
        UNSUPPORTED,
        /** The firmware provides the feature. */
        SUPPORTED,
    }

    private final boolean ambientLevelSupported;
    private final boolean earTipsSupported;
    @Nullable
    private final String accidentalTouchMinFirmware;

    private JblTwsCapabilities(final Builder builder) {
        this.ambientLevelSupported = builder.ambientLevelSupported;
        this.earTipsSupported = builder.earTipsSupported;
        this.accidentalTouchMinFirmware = builder.accidentalTouchMinFirmware;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isAmbientLevelSupported() {
        return ambientLevelSupported;
    }

    /**
     * Whether the product has selectable ear tip types (open / sealing).
     */
    public boolean isEarTipsSupported() {
        return earTipsSupported;
    }

    /**
     * Whether this product has accidental touch prevention at all, independent of
     * the firmware. Use {@link #isAccidentalTouchAvailableOnFirmware(String)} for
     * the firmware-dependent check.
     */
    public boolean supportsAccidentalTouch() {
        return accidentalTouchMinFirmware != null;
    }

    /**
     * Minimum firmware required for accidental touch prevention, or null if the
     * product does not support it.
     */
    @Nullable
    public String getAccidentalTouchMinFirmware() {
        return accidentalTouchMinFirmware;
    }

    /**
     * Evaluates accidental touch prevention against the given firmware version.
     * Unknown or unparseable versions are reported as
     * {@link FirmwareStatus#UNKNOWN} instead of being assumed supported, so
     * callers can fail closed.
     */
    public FirmwareStatus getAccidentalTouchFirmwareStatus(@Nullable final String firmwareVersion) {
        if (accidentalTouchMinFirmware == null) {
            return FirmwareStatus.UNSUPPORTED;
        }
        final int[] current = parseVersion(firmwareVersion);
        final int[] minimum = parseVersion(accidentalTouchMinFirmware);
        if (current == null || minimum == null) {
            if (firmwareVersion != null && !firmwareVersion.isEmpty()) {
                LOG.warn("JBL: could not parse firmware version '{}'", firmwareVersion);
            }
            return FirmwareStatus.UNKNOWN;
        }
        for (int i = 0; i < current.length; i++) {
            if (current[i] != minimum[i]) {
                return current[i] > minimum[i]
                        ? FirmwareStatus.SUPPORTED
                        : FirmwareStatus.UNSUPPORTED;
            }
        }
        return FirmwareStatus.SUPPORTED;
    }

    /**
     * Firmware-dependent check for accidental touch prevention. Unknown versions
     * count as unsupported.
     */
    public boolean isAccidentalTouchAvailableOnFirmware(@Nullable final String firmwareVersion) {
        return getAccidentalTouchFirmwareStatus(firmwareVersion) == FirmwareStatus.SUPPORTED;
    }

    /**
     * Parses a dotted firmware version into three numeric components. Missing
     * components are zero-filled so "2.8" compares equal to "2.8.0". Returns null
     * for anything else, including empty or non-numeric input.
     */
    @Nullable
    private static int[] parseVersion(@Nullable final String version) {
        if (version == null) {
            return null;
        }
        final String trimmed = version.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        final String[] parts = trimmed.split("\\.", -1);
        if (parts.length > 3) {
            return null;
        }
        final int[] numbers = new int[3];
        for (int i = 0; i < parts.length; i++) {
            try {
                numbers[i] = Integer.parseInt(parts[i].trim());
            } catch (final NumberFormatException e) {
                return null;
            }
            if (numbers[i] < 0) {
                return null;
            }
        }
        return numbers;
    }

    public static final class Builder {
        private boolean ambientLevelSupported;
        private boolean earTipsSupported;
        @Nullable
        private String accidentalTouchMinFirmware;

        private Builder() {
        }

        public Builder ambientLevelSupported() {
            this.ambientLevelSupported = true;
            return this;
        }

        public Builder earTipsSupported() {
            this.earTipsSupported = true;
            return this;
        }

        /**
         * Enables accidental touch prevention from the given firmware version on.
         * An invalid minimum disables the capability instead of bypassing the
         * firmware gate.
         */
        public Builder accidentalTouchSupportedSince(@Nullable final String minimumFirmware) {
            if (minimumFirmware == null || parseVersion(minimumFirmware) == null) {
                LOG.warn("JBL: invalid accidental touch minimum firmware '{}', disabling the capability",
                        minimumFirmware);
                this.accidentalTouchMinFirmware = null;
            } else {
                this.accidentalTouchMinFirmware = minimumFirmware;
            }
            return this;
        }

        public JblTwsCapabilities build() {
            return new JblTwsCapabilities(this);
        }
    }
}
