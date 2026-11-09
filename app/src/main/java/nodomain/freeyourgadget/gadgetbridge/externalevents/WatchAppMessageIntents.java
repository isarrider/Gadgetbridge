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

import android.content.Intent;

import androidx.annotation.Nullable;

public final class WatchAppMessageIntents {
    public static final String ACTION_WATCH_APP_MESSAGE = "nodomain.freeyourgadget.gadgetbridge.action.WATCH_APP_MESSAGE";
    public static final String EXTRA_APP_ID = "appId";
    public static final String EXTRA_PAYLOAD = "payload";

    private WatchAppMessageIntents() {
    }

    public static boolean isCompanionPackage(@Nullable final String packageName) {
        if (packageName == null || packageName.length() < 3 || packageName.length() > 200) {
            return false;
        }
        final char first = packageName.charAt(0);
        if (first < 'A' || (first > 'Z' && first < 'a') || first > 'z') {
            return false;
        }
        boolean dot = false;
        char previous = 0;
        for (int i = 0; i < packageName.length(); i++) {
            final char c = packageName.charAt(i);
            final boolean letter = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
            final boolean digit = c >= '0' && c <= '9';
            if (c == '.') {
                if (previous == '.' || i == packageName.length() - 1) {
                    return false;
                }
                dot = true;
            } else if (!letter && !digit && c != '_') {
                return false;
            }
            previous = c;
        }
        return dot;
    }

    @Nullable
    public static Intent incoming(@Nullable final String device, @Nullable final String appId,
                                  @Nullable final String payload, @Nullable final String packageName) {
        if (device == null || device.isEmpty() || appId == null || payload == null
                || !isCompanionPackage(packageName) || packageName.equals(appId)) {
            return null;
        }
        return new Intent(ACTION_WATCH_APP_MESSAGE)
                .setPackage(packageName)
                .putExtra(IntentApiReceiver.EXTRA_DEVICE, device)
                .putExtra(EXTRA_APP_ID, appId)
                .putExtra(EXTRA_PAYLOAD, payload);
    }
}
