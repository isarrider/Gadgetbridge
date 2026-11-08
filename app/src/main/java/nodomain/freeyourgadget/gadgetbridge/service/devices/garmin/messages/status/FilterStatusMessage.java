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
package nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.messages.status;

public class FilterStatusMessage extends GFDIStatusMessage {
    private final Status status;

    public FilterStatusMessage(GarminMessage garminMessage, Status status, int unk) {
        this.garminMessage = garminMessage;
        this.status = status;

        LOG.info("Received ACK for message {}", garminMessage);

    }

    public static FilterStatusMessage parseIncoming(MessageReader reader, GarminMessage garminMessage) {
        final Status status = Status.fromCode(reader.readByte());

        // see opengarmin analysis:
        // The offical client continues to the next step in file synchronization
        // regardless of the status or what unk reports.
        final int unk;
        if (status == Status.ACK) {
            unk = reader.readByte();
        } else {
            LOG.warn("Applying compatibility fix: FilterStatus");
            unk = 1;
        }

        return new FilterStatusMessage(garminMessage, status, unk);
    }
}
