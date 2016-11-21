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
import com.lee_soft.ztp.ZTPConstants;
import com.lee_soft.ztp.ZTPPacket;
import com.lee_soft.ztp.ZTPSession;
import static com.lee_soft.ztp.ZTPUtil.debugPrint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZTPClient extends ZTPSession {
    
    private int clientSequenceId;
    
    public boolean hasMissingChunksToTransmit() {
        return !(chunksToRetransmit.isEmpty());
    }
    
    public int missingChunkTotal() {
        return chunksToRetransmit.size();
    }
    
    public byte[] prepareNextOutChunk() throws IOException {
        byte[] out;
        
        
        clientSequenceId++;
        out = super.prepareNextSequenceOutChunk(clientSequenceId);
        
        return out;
    } 
    
    public byte[] generateHandshakeChunk() throws IOException {
        clientSequenceId = ZTPConstants.SEQUENCE_BEGIN;
        return super.initiateHandshake();
    }
    
    public byte[] generateRequestRemoteBufferChunk() throws IOException {

        ByteArrayOutputStream outChunk = new ByteArrayOutputStream(); 

        //write chunk header
        outChunk.write(new byte[]{
            (byte)(ZTPConstants.OUT_OF_SEQUENCE >> 8), (byte)(ZTPConstants.OUT_OF_SEQUENCE), 
            (byte)(super.sessionKey >> 8), (byte)(super.sessionKey)}
        );
        
        //write packet header
        outChunk.write(ZTPConstants.REQUEST_BUFFER_STATE_PACKET);

        byte[] rawChunkData = outChunk.toByteArray();
        return rawChunkData;
        
    }
            
    
    public byte[] generateSyncChunk() throws IOException {

        ByteArrayOutputStream outChunk = new ByteArrayOutputStream(); 

        outChunk.write(super.generateOutOfSequenceChunk());
        outChunk.write(ZTPConstants.SYNC_REQUEST_PACKET);

        byte[] rawChunkData = outChunk.toByteArray();
        return rawChunkData;
        
    }
    
    public void recieveData(byte[] rawData) throws IOException {
        
        debugPrint("ZTPClient::recieveData " + ZTPUtil.bytesToHex(rawData));
        
        ZTPChunk nextChunk = new ZTPChunk(rawData);
        super.acceptInChunk(nextChunk);
    }
    
    public void flushBuffer() {
        
    }
    
    public void sendData(byte[] rawData) {
        
        
        try {
            super.queueOutData(rawData);
        } catch (IOException ex) {
            Logger.getLogger(ZTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ZTPClient() {
        super();
        super.maximumTransmissionUnit = 145;
    }
    
}
