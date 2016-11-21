/* 
 * Copyright (C) 2016 Lee Matthew Chantrey <Lee at lee-soft.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lee_soft.ztp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Parses a raw binary ZTPPacket into a ZTPPacket object and visa versa
 */
public class ZTPPacket {
    
    
    public int packetType;
    public byte[] payload = new byte[]{};

    public ZTPPacket(byte[] data) {
    }
    
    public ZTPPacket(int type) {
        this.packetType = type;
    }
    
    public ZTPPacket(int type, byte[] payload) {
        this.packetType = type;
        this.payload = payload;
        
        
    }
    
    public void writePacket(ByteArrayOutputStream outputStream) throws IOException {
        
        outputStream.write(new byte[]{
            (byte)(payload.length >> 8), (byte)(payload.length)}
        );
        
        outputStream.write((byte)this.packetType);
        outputStream.write(payload);
    }
    
    public int payloadSize() {
        return payload.length;
    }

    public int size() {
        return 3 + payload.length;
    }
}