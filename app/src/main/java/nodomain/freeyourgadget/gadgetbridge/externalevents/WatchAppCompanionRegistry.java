/*  Copyright (C) 2026 The Gadgetbridge contributors

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
package nodomain.freeyourgadget.gadgetbridge.externalevents;

import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.services.ZeppOsJsMessage;

public final class WatchAppCompanionRegistry {
    public static final String EXTRA_REPLY_TO = "replyTo";

    private static final WatchAppCompanionRegistry INSTANCE = new WatchAppCompanionRegistry();

    private final ConcurrentHashMap<String, String> recipients = new ConcurrentHashMap<>();

    public static WatchAppCompanionRegistry getInstance() {
        return INSTANCE;
    }

    public boolean bind(@Nullable final String deviceAddress, @Nullable final String appId,
                        @Nullable final String packageName) {
        final String canonical = ZeppOsJsMessage.canonicalAppId(appId);
        if (deviceAddress == null || deviceAddress.isEmpty() || canonical == null
                || !WatchAppMessageIntents.isCompanionPackage(packageName)) {
            return false;
        }
        recipients.put(key(deviceAddress, canonical), packageName);
        return true;
    }

    @Nullable
    public String packageFor(@Nullable final String deviceAddress, @Nullable final String appId) {
        final String canonical = ZeppOsJsMessage.canonicalAppId(appId);
        if (deviceAddress == null || deviceAddress.isEmpty() || canonical == null) {
            return null;
        }
        return recipients.get(key(deviceAddress, canonical));
    }

    public void clear() {
        recipients.clear();
    }

    private static String key(final String deviceAddress, final String canonicalAppId) {
        return deviceAddress + "\n" + canonicalAppId;
    }
}
