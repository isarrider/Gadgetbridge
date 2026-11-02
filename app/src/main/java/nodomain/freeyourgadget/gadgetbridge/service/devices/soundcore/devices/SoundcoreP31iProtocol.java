/*  Copyright (C) 2026 David Giron

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.devices.soundcore.SoundcoreEarbudsCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.soundcore.SoundcoreEarbudsCoordinator.ButtonAction;
import nodomain.freeyourgadget.gadgetbridge.devices.soundcore.SoundcoreEarbudsCoordinator.TapFunction;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore.protocol.impl.v1.SoundcoreProtocolImplV1;

public class SoundcoreP31iProtocol extends SoundcoreProtocolImplV1 {
    private static final Logger LOG = LoggerFactory.getLogger(SoundcoreP31iProtocol.class);

    private static final int DEVICE_INFO_CONTROL_OFFSET = 110;

    public SoundcoreP31iProtocol(final GBDevice device) {
        super(device);
    }

    @Override
    protected GBDeviceEvent[] decodeDeviceInfo(final byte[] payload) {
        final GBDeviceEvent[] events = super.decodeDeviceInfo(payload);
        if (events.length == 0 || payload.length < 118) {
            LOG.warn("CMD_GET_DEVICE_INFO has no complete P31i control map: {} bytes", payload.length);
            return events;
        }

        final ButtonAction[] actions = {
                ButtonAction.SINGLE_TAP,
                ButtonAction.DOUBLE_TAP,
                ButtonAction.TRIPLE_TAP,
                ButtonAction.LONG_PRESS,
        };
        final android.content.SharedPreferences.Editor editor = getDevicePrefs().getPreferences().edit();
        for (int index = 0; index < actions.length; index++) {
            final TapFunction left = functionFromCode(payload[DEVICE_INFO_CONTROL_OFFSET + index * 2] & 0x0f);
            final TapFunction right = functionFromCode(payload[DEVICE_INFO_CONTROL_OFFSET + index * 2 + 1] & 0x0f);
            editor.putString(actions[index].getLeftPreference(), left.getValue());
            editor.putString(actions[index].getRightPreference(), right.getValue());
        }
        editor.apply();
        return events;
    }

    private static TapFunction functionFromCode(final int code) {
        for (final TapFunction function : TapFunction.values()) {
            if (function.getCode() == code) {
                return function;
            }
        }
        return TapFunction.NONE;
    }
}
