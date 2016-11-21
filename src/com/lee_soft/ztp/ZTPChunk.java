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

import java.io.IOException;
import java.util.*;

/**
 * Parses a raw binary ZTPChunk into a ZTPChunk object
 */
public class ZTPChunk {
   
    private int off;
    private int len;
    public byte data[];
    
    public int sequenceNo;
    public int sessionKey;

    public List<ZTPPacket> ztpPackets = new ArrayList();
    
    /**
     * Default constructor
     * @param chunkData
     * @throws java.io.IOException
     */
    public ZTPChunk(byte[] chunkData) throws IOException {
        
        ztpPackets = new ArrayList<>();
        
        data = chunkData;
        len = chunkData.length;
        off = 0;
        
        sequenceNo = readUnsignedShort();
        sessionKey = readUnsignedShort();

        while(off < len) {
            try {
                ztpPackets.add(readPacket());
                
            } catch (IOException e) {
                throw new IOException("unexpected packet end");
            } finally {   
                
            }
        }
        
    }
    
    public ZTPChunk(int newSequenceNo, int newSessionKey) {
        sequenceNo = newSequenceNo;
        sessionKey = newSessionKey;
    }
    
    private int readInt8() throws IOException
    {
        return get(off++);
    }
    
    private ZTPPacket readPacket() throws IOException
    {
        int payloadSize = readUnsignedShort();
        int packetType = readInt8();
        byte[] payloadData = new byte[0];
        
        if (payloadSize > 0)
            payloadData  = readBytes(payloadSize);
         
        return new ZTPPacket(packetType, payloadData);
    }

    private int readUnsignedShort() throws IOException
    {
        return (get(off++) << 8) + get(off++);
    }
    
    private byte[] readBytes(int size) throws IOException
    {

        if ((size < 0) || (size >= len))
        {
            throw new IOException("parser error: offset=" + off);
        }     

        byte[] out =  Arrays.copyOfRange(data, off, off + size);
        off += size;
        
        return out;
    }
    
    private int get(int off) throws IOException
    {
        if ((off < 0) || (off >= len))
        {
            throw new IOException("parser error: offset=" + off);
        }
        return data[off] & 0xFF;
    }

}