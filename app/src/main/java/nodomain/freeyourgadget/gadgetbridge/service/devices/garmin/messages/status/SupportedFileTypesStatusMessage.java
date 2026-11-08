/*  Copyright (C) 2024-2026 Daniele Gobbetti, José Rebelo, Thomas Kuehne

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


import static nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.FileType.FILETYPE.ACTIVITY;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.FileType.FILETYPE.GOLF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.FileType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.deviceevents.SupportedFileTypesDeviceEvent;

public class SupportedFileTypesStatusMessage extends GFDIStatusMessage {
    private final Status status;
    private final List<FileType> fileTypeInfoList;

    public SupportedFileTypesStatusMessage(GarminMessage garminMessage, Status status, List<FileType> fileTypeInfoList) {
        this.garminMessage = garminMessage;
        this.status = status;
        this.fileTypeInfoList = fileTypeInfoList;
    }

    public static SupportedFileTypesStatusMessage parseIncoming(MessageReader reader, GarminMessage garminMessage) {
        final Status status = Status.fromCode(reader.readByte());

        if (Status.ACK != status) {
            // see opengarmin analysis:
            // The offical client assumes support for FIT_TYPE_4 and FIT_TYPE_36
            // and continues to the next step in file synchronization.
            LOG.warn("Applying compatibility fix: SupportedFileTypesStatus");
            final List<FileType> types = new ArrayList<>(2);

            types.add(new FileType(ACTIVITY.getType(), ACTIVITY.getSubType(), ACTIVITY.typeName));
            types.add(new FileType(GOLF.getType(), GOLF.getSubType(), GOLF.typeName));
            return new SupportedFileTypesStatusMessage(garminMessage, status, types);
        }

        final int typeCount = reader.readByte();
        final List<FileType> types = new ArrayList<>(typeCount);
        for (int i = 0; i < typeCount; ++i) {
            final int fileDataType = reader.readByte();
            final int fileSubType = reader.readByte();
            final String garminDeviceFileType = reader.readString();
            FileType fileType = new FileType(fileDataType, fileSubType, garminDeviceFileType);
            if (fileType.getFileType() == null) {
                LOG.warn("Watch supports a filetype that we do not support: {}/{}: {}", fileDataType, fileSubType, garminDeviceFileType);
                continue;
            }
            types.add(fileType);
        }

        return new SupportedFileTypesStatusMessage(garminMessage, status, types);
    }

    @Override
    public List<GBDeviceEvent> getGBDeviceEvent() {
        return Collections.singletonList(new SupportedFileTypesDeviceEvent(fileTypeInfoList));
    }
}
