/*  Copyright (C) 2024-2026 Daniele Gobbetti, Thomas Kuehne

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.messages;

public class SystemEventMessage extends GFDIMessage {

    private final GarminSystemEventType eventType;
    private final String value;

    public SystemEventMessage(GarminSystemEventType eventType, String value) {
        this.eventType = eventType;
        this.value = value;
        this.garminMessage = GarminMessage.SYSTEM_EVENT;
    }

    @Override
    protected boolean generateOutgoing() {
        final MessageWriter writer = new MessageWriter(response);
        writer.writeShort(0); // packet size will be filled below
        writer.writeShort(this.garminMessage.getId());
        writer.writeByte(eventType.ordinal());
        if (value == null) {
            writer.writeByte(0); // string length
        } else {
            writer.writeString(value);
        }
        return true;
    }

    public enum GarminSystemEventType {
        SYNC_COMPLETE,
        SYNC_FAIL,
        FACTORY_RESET,
        PAIR_START,
        PAIR_COMPLETE,
        PAIR_FAIL,
        HOST_DID_ENTER_FOREGROUND,
        HOST_DID_ENTER_BACKGROUND,
        SYNC_READY,
        NEW_DOWNLOAD_AVAILABLE,
        DEVICE_SOFTWARE_UPDATE,
        DEVICE_DISCONNECT,
        TUTORIAL_COMPLETE,
        SETUP_WIZARD_START,
        SETUP_WIZARD_COMPLETE,
        SETUP_WIZARD_SKIPPED,
        TIME_UPDATED,  // 16 - 0x10
        TRIGGER_REBOOT; // 17 - 0x11
    }
}
