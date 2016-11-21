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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author littl
 */
public class UDPClient {
    
    InetAddress address;
    DatagramSocket dsocket;
    int port;
    
    public UDPClient(String server, int newPort) {
            
        try {
            // Get the internet address of the specified host

            address = InetAddress.getByName(server);
            this.port = newPort;
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try { 
            dsocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DatagramPacket readData(int byteCount) throws IOException {
        DatagramPacket incomming;
        
        byte[] buf = new byte[byteCount];
        incomming = new DatagramPacket(buf, buf.length);
        
        dsocket.receive(incomming);
        
        return incomming;
    }
    
    public void sendData(byte[] dataToSend) {
        

        DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length,
            address, this.port);

        try {
            dsocket.send(packet);
            //dsocket.receive(incomming);
            
        } catch (IOException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
