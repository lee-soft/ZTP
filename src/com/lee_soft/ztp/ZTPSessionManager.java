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

import static com.lee_soft.ztp.ZTPUtil.debugPrint;
import java.io.ByteArrayOutputStream;

/**
 * This class is responsible for passing chunks to their respective session
 */
public class ZTPSessionManager {
    
    private Map<Integer, ZTPSession> ztpSessionMap = new HashMap<>();
    private List<ZTPEventListener> listeners = new ArrayList<>();

    public List Sessions;
        
    /**
     * Default constructor
     */
    public ZTPSessionManager() {
        
 
    }

    public void addListener(ZTPEventListener listener) {
        listeners.add(listener);
    }
    
    protected void notifyNewSessionCreated(ZTPSession newSession) {
        for (ZTPEventListener thisEvent : listeners)
            thisEvent.sessionCreated(newSession);
    }
    
    protected void notifySessionRecievedBufferContainsData(ZTPSession targetSession) {
        for (ZTPEventListener thisEvent : listeners)
            thisEvent.sessionRecievedBufferContainsData(targetSession);        
    }
    /**
     * reads the chunk's Session ID and adds it to the appropriate session object.
     */
    public byte[] processRawChunk(byte[] rawChunkData) throws IOException {
        
        ByteArrayOutputStream rawOutChunk = new ByteArrayOutputStream( );
        ZTPChunk nextChunk = new ZTPChunk(rawChunkData);
        ZTPSession selectedSession;

        debugPrint("Incomming raw chunk key[" + nextChunk.sessionKey + "] "
                + "sequence[" + nextChunk.sequenceNo + "]");

        if(!ztpSessionMap.containsKey(nextChunk.sessionKey)) {
            // Hand chunk to a new session object
            
            debugPrint("No prior session found by that key/n"
                    + "Handing chunk to a new session");
            
            selectedSession = new ZTPSession(nextChunk.sessionKey);

            ztpSessionMap.put(selectedSession.sessionKey, selectedSession); 
            notifyNewSessionCreated(selectedSession);
        } else {
            
            debugPrint("Found prior session by that session key");
            
            if((nextChunk.sequenceNo > ZTPConstants.SEQUENCE_BEGIN) || 
                    (nextChunk.sequenceNo == ZTPConstants.OUT_OF_SEQUENCE))
                
                selectedSession = ztpSessionMap.get(nextChunk.sessionKey);
            else 
            {
                System.out.println("Session state not previously shook hands, "
                        + "handing chunk to a new session");

                selectedSession = new ZTPSession(nextSessionKey());
                
                ztpSessionMap.put(selectedSession.sessionKey, selectedSession);
                notifyNewSessionCreated(selectedSession);                
            }
        }
        
        selectedSession.acceptInChunk(nextChunk);

        rawOutChunk.write(selectedSession.nextChunkToTransmit.toByteArray());
 
        //Send a buffer state request so the response isn't empty
        if(rawOutChunk.size() == 0) {
            selectedSession.handleBufferStateRequest();
            rawOutChunk.write(selectedSession.nextChunkToTransmit.toByteArray());
            
        }
        
        selectedSession.nextChunkToTransmit.reset();
        if(selectedSession.hasRecievedData())
            notifySessionRecievedBufferContainsData(selectedSession);

        return rawOutChunk.toByteArray();
    }

    
    /**
     * returns a unique tiny insecure session key
     */
    public int nextSessionKey()
    {
        Random r = new Random();
        int out;
        
        do {
            out = r.nextInt(ZTPConstants.UNSIGNED_SHORT_MAX);
        } while (ztpSessionMap.containsKey(out));
            
        return out;
    }

}