
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import com.lee_soft.ztp.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class QuoteServerThread extends Thread {
 
    protected DatagramSocket socket = null;
    
    protected ZTPSessionManager sessionManager;
    protected QuoteServerManager manager;
 
    public QuoteServerThread() throws IOException {
        this("QuoteServerThread");
    }
 
    public QuoteServerThread(String name) throws IOException {
        super(name);
        
        sessionManager = new ZTPSessionManager();
        manager = new QuoteServerManager();
        
        sessionManager.addListener(manager);
        
        socket = new DatagramSocket(4445);
    }
 
    public void run() {
 
        while (true) {
            try {
                byte[] in = new byte[512];
                byte[] in2;
 
                // receive request
                DatagramPacket packet = new DatagramPacket(in, in.length);
                socket.receive(packet);
                
                in2 = Arrays.copyOf(packet.getData(), packet.getLength());
                
                // send out response
                byte[] out = sessionManager.processRawChunk(in2);
 
                // send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(out, out.length, address, port);
                socket.send(packet);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}