/*  Copyright (C) 2024-2026 Daniele Gobbetti, Vadim Radu, Thomas Kuehne

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

import java.util.Random;

import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.FileType;

public class CreateFileMessage extends GFDIMessage {

    private final int fileSize;
    private final FileType.FILETYPE filetype;
    private final boolean generateOutgoing;

    public CreateFileMessage(GarminMessage garminMessage, int fileSize, FileType.FILETYPE filetype) {
        this.fileSize = fileSize;
        this.filetype = filetype;
        this.garminMessage = garminMessage;
        this.statusMessage = this.getStatusMessage();
        this.generateOutgoing = false;
    }

    public CreateFileMessage(int fileSize, FileType.FILETYPE filetype) {
        this(fileSize, filetype, true);
    }

    public CreateFileMessage(int fileSize, FileType.FILETYPE filetype, boolean sendOutgoing) {
        this.garminMessage = GarminMessage.CREATE_FILE;
        this.fileSize = fileSize;
        this.filetype = filetype;
        this.statusMessage = this.getStatusMessage();
        this.generateOutgoing = sendOutgoing;
    }

    public static CreateFileMessage parseIncoming(MessageReader reader, GarminMessage garminMessage) {

        final int fileSize = reader.readInt();
        final int dataType = reader.readByte(); //SupportedFileTypesStatusMessage.FileTypeInfo.type
        final int subType = reader.readByte();//SupportedFileTypesStatusMessage.FileTypeInfo.subtypetype
        final FileType.FILETYPE filetype = FileType.FILETYPE.fromDataTypeSubType(dataType, subType);
        final int fileIndex = reader.readShort(); //???
        reader.readByte(); //unk
        final int subTypeMask = reader.readByte(); //???
        final int numberMask = reader.readShort(); //???
        if (reader.remaining() >= 2) {
            final String path = reader.readString2();
            if (reader.remaining() >= 8) {
                final long longFileId = reader.readLong();
            }
        }

        return new CreateFileMessage(garminMessage, fileSize, filetype);
    }

    @Override
    protected boolean generateOutgoing() { //TODO: adjust variables
        Random random = new Random();
        final MessageWriter writer = new MessageWriter(response);
        writer.writeShort(0); // packet size will be filled below
        writer.writeShort(this.garminMessage.getId());
        writer.writeInt(this.fileSize);
        writer.writeByte(this.filetype.getType());
        writer.writeByte(this.filetype.getSubType());
        writer.writeShort(0); //fileIndex
        writer.writeByte(0); //reserved
        writer.writeByte(0); //subtypemask
        writer.writeShort(65535); //numbermask
        writer.writeShort(0); // path length
        writer.writeLong(random.nextLong()); // long fileId

        return generateOutgoing;
    }

}
