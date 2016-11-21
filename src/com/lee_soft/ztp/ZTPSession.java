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

import static com.lee_soft.ztp.ZTPUtil.debugPrint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * This class should not contain any server or client specific logic
 * Both the server and client can import this and use it differently
 * and expand on it to include client and server specific logic
 * 
 */
public class ZTPSession {
    
    public ZTPSession(int newKey) {
        this();   
        sessionKey = newKey;
        
    }

    public ZTPSession() {
        
        outBuffer = new ByteArrayOutputStream();
        recievedBuffer = new ByteArrayOutputStream();

        nextSequenceId = ZTPConstants.SEQUENCE_BEGIN;
        chunksIn = new TreeMap<Integer, ZTPChunk>(); // automatic ordering on keys
        chunksOut = new HashMap<Integer, byte[]>(); // no ordering on keys or values
        chunksToRetransmit = new ArrayList<Integer>();

        packetsToGo = new ArrayList<ZTPPacket>(); //sequence of packets
        nextChunkToTransmit = new ByteArrayOutputStream();
        maximumTransmissionUnit = 145;
        
        maxSequenceIdRecieved = 0;
        remoteDataBufferEmpty = true;
    }
    /**
     * Identifies session
     */
    public int sessionKey;

    /**
     * Tells us if the remote data buffer has any data for us
     */    
    public boolean remoteDataBufferEmpty;
   
    /**
     * stores outgoing chunks
     */
    private Map<Integer, byte[]> chunksOut;
    /**
     * as chunks arrive they will get sorted automatically.
     */
    private Map<Integer, ZTPChunk> chunksIn;
     
    private List<ZTPPacket> packetsToGo;
    private ZTPPacket nextOutOfSequencePacket;
    public ByteArrayOutputStream nextChunkToTransmit;
    
    final List<Integer> chunksToRetransmit;

    /**
     * Tracks the latest sequence ID to be processed
     */
    public int nextSequenceId;
    
    private int maxSequenceIdRecieved = 0;
    public int maximumTransmissionUnit;
    
    private ByteArrayOutputStream outBuffer;  //containts the sequential raw data to be sent to remote
    private ByteArrayOutputStream recievedBuffer; //containts the sequential raw data that's been recieved from remote
    
    public boolean hasRecievedData() {
        return recievedBuffer.size() != 0;
    }
    
    public byte[] getLatestRecievedData() {
        
        byte[] out = recievedBuffer.toByteArray();
        recievedBuffer.reset();
        
        return out;
        
    }

    /**
     * Prepares the next sequential chunk to send to remote
     */    
    final byte[] prepareNextSequenceOutChunk(int sequenceNo) throws IOException {
        
        ByteArrayOutputStream outChunk = new ByteArrayOutputStream(); 
        
        outChunk.write(generateSequenceChunk(sequenceNo));
        
        byte[] nextChunkPayload = this.getNextChunkPayload(maximumTransmissionUnit);
        outChunk.write(nextChunkPayload);
        
        byte[] rawChunkData = outChunk.toByteArray();
        chunksOut.put(sequenceNo, rawChunkData);
        
        return rawChunkData;
    }
    
    /**
     * Amends the buffer with new data
     * 
     * @param newData
     * @throws IOException 
     */
    public void queueOutData(byte[] newData) throws IOException {
        outBuffer.write(newData);
    }
    
    /**
     * Commits as much data as will fit in 1 packet payload
     */
    private void flushOutBuffer(int payloadMaxSize) {
        
        //if(outBuffer.size() == 0) {
        //    return;
        
        byte[] rawData;
        byte[] existingData;
        byte[] outBufferData = outBuffer.toByteArray();
        boolean completeSequence;
        
        outBuffer.reset();
        
        if(outBufferData.length < payloadMaxSize) {
            completeSequence = true;
            rawData = outBufferData;
        } else {
            completeSequence = false;

            rawData = new byte[payloadMaxSize];
            existingData = new byte[outBufferData.length - payloadMaxSize];
            
            System.arraycopy(outBufferData, 0, rawData, 0, payloadMaxSize);
            System.arraycopy(outBufferData, payloadMaxSize, existingData, 0, outBufferData.length - payloadMaxSize);
            
            try {
                outBuffer.write(existingData);
            } catch (IOException ex) {
                Logger.getLogger(ZTPSession.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

        queuePacket(new ZTPPacket(
                completeSequence ? 
                        ZTPConstants.END_DATA_SEQUENCE : 
                        ZTPConstants.PARTIAL_DATA_SEQUENCE, rawData)
        );
        
    }

    /**
     * Adds it to the "packetsToGo" List. The class using this object must 
     * decide how to send the chunks
     */
    private void queuePacket(ZTPPacket packetToQueue) {
        packetsToGo.add(packetToQueue);
        //packetBufferLength += packetToSend.size();
    }

    /**
     * checks if nextSequenceId is available at the start of the chunksIn list 
     * and then processes it repeats until it isn't.
     */
    public void processNextChunk() {
        
        //check we can sequentially process the next chunk
        if(!chunksIn.containsKey(nextSequenceId)) {
            return;
        }
        
        ZTPChunk nextChunk = chunksIn.get(nextSequenceId);
        chunksIn.remove(nextSequenceId);
        
        
        processChunk(nextChunk);
        nextSequenceId++;
        
        processNextChunk();
    }
    
    private void checkIfRemoteBufferIsEmpty(ZTPChunk nextChunk) {
        
        nextChunk.ztpPackets.forEach((ZTPPacket nextPacket) -> {
            if(nextPacket.packetType == ZTPConstants.END_DATA_SEQUENCE)
                remoteDataBufferEmpty = true;
            else if(nextPacket.packetType == ZTPConstants.PARTIAL_DATA_SEQUENCE)
                remoteDataBufferEmpty = false;
        });
    }
    
    private void processChunk(ZTPChunk nextChunk) {
        
        nextChunk.ztpPackets.forEach((ZTPPacket nextPacket) -> {
            if((nextPacket.packetType == ZTPConstants.END_DATA_SEQUENCE) || 
                    (nextPacket.packetType == ZTPConstants.PARTIAL_DATA_SEQUENCE)) {
                
                debugPrint("Remote: " + new String(nextPacket.payload));
                
                try {
                    recievedBuffer.write(nextPacket.payload);
                } catch (IOException ex) {
                    Logger.getLogger(ZTPSession.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //remoteDataBufferEmpty = (nextPacket.packetType == ZTPConstants.END_DATA_SEQUENCE);
                
            } else if(nextPacket.packetType == ZTPConstants.HANDSHAKE_START) {

                debugPrint("Client requested new session");
                
                if(nextSequenceId == ZTPConstants.SEQUENCE_BEGIN) {
                    debugPrint("Granted!");
                    
                    queuePacket(new ZTPPacket(ZTPConstants.HANDSHAKE_GRANTED));
                }
            } else if(nextPacket.packetType == ZTPConstants.HANDSHAKE_GRANTED) {
                
                debugPrint("Setting session key: " + nextChunk.sessionKey);
                
                this.sessionKey = nextChunk.sessionKey;
                
            }
        }); 
        
        
    }

    /**
     * takes a chunk and adds it to chunksIn (which sorts the list) so it's ready to be processed by processNextChunk.
     */
    public void acceptInChunk(ZTPChunk chunkToAccept) throws IOException {
        
        debugPrint("Accepting chunk: " + chunkToAccept.sequenceNo);
        
        //process out of sequence chunks right away
        if(chunkToAccept.sequenceNo == ZTPConstants.OUT_OF_SEQUENCE) {
            
           nextChunkToTransmit.reset();
            
            //the default packet to send back
           handleBufferStateRequest();

            //Remote is syncing basically
            if(!chunkToAccept.ztpPackets.isEmpty()) {
                
                chunkToAccept.ztpPackets.forEach((ZTPPacket nextPacket) -> {
                    if(nextPacket.packetType == ZTPConstants.SYNC_REQUEST) {
                        
                        handleMissingChunkRequests(chunkToAccept); //work out what's missing this end
                        
                    } else if(nextPacket.packetType == ZTPConstants.CHUNK_RETRANSMIT_REQUEST) {
                        try {
                            if (nextPacket.payloadSize() > 0) {
                                handleRetransmisionRequest(nextPacket.payload); //requeues requested chunk(s)
                                return;
                            }
                            
                        } catch (IOException ex) {
                            Logger.getLogger(ZTPSession.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    } else if(nextPacket.packetType == ZTPConstants.REQUEST_BUFFER_STATE) {
                        
                        handleBufferStateRequest(); //return the buffer state
                    } else { 
                        
                         if(nextPacket.packetType == ZTPConstants.END_DATA_SEQUENCE)
                         {
                             remoteDataBufferEmpty = true;
                             return;
                         }

                         if(nextPacket.packetType == ZTPConstants.PARTIAL_DATA_SEQUENCE)
                         {
                             remoteDataBufferEmpty = false;
                             
                                 //nextChunkToTransmit.write(
                                 //        prepareNextSequenceOutChunk(chunkToAccept.sequenceNo)
                                 //);

                          
                         }
                        
                    }
                }); 
            }
            
            
            if(nextChunkToTransmit.size() == 0) {
                
               nextChunkToTransmit.write(
                        new byte[]{(byte)ZTPConstants.OUT_OF_SEQUENCE, (byte)ZTPConstants.OUT_OF_SEQUENCE, 
                        (byte)(sessionKey >> 8), (byte)(sessionKey)});
               
               nextOutOfSequencePacket.writePacket(nextChunkToTransmit);
            }
            
            processChunk(chunkToAccept); //seems like a pointless formaility somehow
            return;
        }
        
        if(chunkToAccept.sequenceNo < nextSequenceId) {
            
            //dont acknowledge an acknoweldgement, chunks older than 5 sequences will be removed anyway
            if(chunksOut.containsKey(chunkToAccept.sequenceNo)) {
                debugPrint("ACK Recieved: " + chunkToAccept.sequenceNo);
                chunksOut.remove(chunkToAccept.sequenceNo);
            } else {

                //this chunk if queued would sit in the list forever
                debugPrint("Dropped chunk");
            }
            
            return;
        }
        
        if(maxSequenceIdRecieved <= chunkToAccept.sequenceNo)
        {
            checkIfRemoteBufferIsEmpty(chunkToAccept); //this would check the remote buffer status ahead of time, this might be a good thing
            maxSequenceIdRecieved = chunkToAccept.sequenceNo;
        }
        
        chunksIn.put(chunkToAccept.sequenceNo, chunkToAccept);

        processNextChunk();
        
        nextChunkToTransmit.write(
                prepareNextSequenceOutChunk(chunkToAccept.sequenceNo)
        );
        
    }
    
    public byte[] getNextMissingChunkToTransmit() throws IOException {
        
        if(chunksToRetransmit.isEmpty())
            return ZTPConstants.EMPTY_BYTE_ARRAY;
        
        int nextChunkSequenceId = chunksToRetransmit.get(0);
        chunksToRetransmit.remove(0);
        
        return chunksOut.get( nextChunkSequenceId );
        
    }
    
    public int failedToTransmitChunkCount() {
        return chunksToRetransmit.size();
    }
    
    /**
     * flush out as many packets as can fit in the specified size
     * @param payloadMaxSize
     */
    public byte[] getNextChunkPayload(int payloadMaxSize) {
        
        //3 [ZTPPacket header] + 4 [ZTPChunk header] 
        flushOutBuffer(payloadMaxSize - 7);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZTPPacket nextPacket;
        boolean getNextPacket = true;
        
        while( getNextPacket )
        {
            getNextPacket = false;
            
            if (packetsToGo.size() > 0)
            {            
                nextPacket = packetsToGo.get(0);

                if ((outputStream.size() + (nextPacket.size())) <= payloadMaxSize) {
                    
                    getNextPacket = true;

                    try {
                        nextPacket.writePacket(outputStream);
                        packetsToGo.remove(0);

                    } catch (IOException ex) {
                        Logger.getLogger(ZTPSession.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
            
        }
      
  
        return outputStream.toByteArray();
    }
    
    public void handleBufferStateRequest() {
        
        if(outBuffer.size() == 0)
            nextOutOfSequencePacket = new ZTPPacket(ZTPConstants.END_DATA_SEQUENCE);
        else
            nextOutOfSequencePacket = new ZTPPacket(ZTPConstants.PARTIAL_DATA_SEQUENCE);
    }

    /**
     * missingChunksData contains a byte list of all the requested chunks
     * adds all the requested chunks to the chunksToRetransmit list
     */
    private void handleRetransmisionRequest(byte[] missingChunksData) throws IOException {
        
        int missingChunkDataOffset = 0;
        int nextMissingSequenceId = 0;
        
        while (missingChunkDataOffset < missingChunksData.length)
        {      
            nextMissingSequenceId = ((missingChunksData[missingChunkDataOffset++] & 0xFF) << 8) + 
                    (missingChunksData[missingChunkDataOffset++] & 0xFF);
            
            debugPrint("Partner is requesting:: " + nextMissingSequenceId);
            
            if(chunksOut.containsKey(nextMissingSequenceId)) {
                chunksToRetransmit.add(nextMissingSequenceId);
            } else {
                debugPrint("We don't have:: " + nextMissingSequenceId);
            }
        }
        
        nextChunkToTransmit.write(getNextMissingChunkToTransmit());
    }

    /**
     * Remote is asking which chunks do you need from me
     * @param pollChunk
     */
    public void handleMissingChunkRequests(ZTPChunk pollChunk) {
        debugPrint("Checking for missing chunks");
        
        ByteArrayOutputStream packetData = new ByteArrayOutputStream();
        ZTPPacket out = new ZTPPacket(ZTPConstants.CHUNK_RETRANSMIT_REQUEST);
        
        if(chunksIn.isEmpty()) {
            nextOutOfSequencePacket = out;
            return;
        }
        
        try {
            
            for(int sequenceIndex = nextSequenceId; sequenceIndex < maxSequenceIdRecieved; sequenceIndex++)
            {
                if(!chunksIn.containsKey(sequenceIndex)) {
                
                    debugPrint("Missing chunk: " + sequenceIndex);

                    packetData.write(new byte[]{
                        (byte)(sequenceIndex >> 8), (byte)(sequenceIndex)}
                    );  
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ZTPSession.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        out.payload = packetData.toByteArray();
        nextOutOfSequencePacket = out;
    }

    public byte[] generateSequenceChunk(int sequenceNo) {
        
        return new byte[]{
            (byte)(sequenceNo >> 8), (byte)(sequenceNo), 
            (byte)(this.sessionKey >> 8), (byte)(this.sessionKey)};
        
    }    
    public byte[] generateOutOfSequenceChunk() {
        
        return new byte[]{(byte)ZTPConstants.OUT_OF_SEQUENCE, (byte)ZTPConstants.OUT_OF_SEQUENCE, 
                            (byte)(sessionKey >> 8), (byte)(sessionKey)};
    }
    
    final byte[] initiateHandshake() throws IOException {
        
        ByteArrayOutputStream outChunk = new ByteArrayOutputStream(); 
        int proposedSessionKey = new Random().nextInt(ZTPConstants.UNSIGNED_SHORT_MAX);
        sessionKey = 0;
        
        outChunk.write(new byte[]{
            (byte)(ZTPConstants.SEQUENCE_BEGIN >> 8), (byte)(ZTPConstants.SEQUENCE_BEGIN), 
            (byte)(proposedSessionKey >> 8), (byte)(proposedSessionKey)}
        );

        outChunk.write(ZTPConstants.HANDSHAKE_START_PACKET);

        byte[] rawChunkData = outChunk.toByteArray();
        chunksOut.put(ZTPConstants.SEQUENCE_BEGIN, rawChunkData);

        return rawChunkData;
    }
}
