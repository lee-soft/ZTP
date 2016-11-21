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

public final class ZTPConstants
{
    //packet types
    public final static int HANDSHAKE_START = 0x1;
    public final static int HANDSHAKE_GRANTED = 0x2; //used to be 0x11
    
    public final static int END_DATA_SEQUENCE = 0x0;
    public final static int PARTIAL_DATA_SEQUENCE = 0xFFFF; //used to be 0x2
    
    public final static int REQUEST_BUFFER_STATE = 0x3; 
    public final static int SYNC_REQUEST = 0x4;
    
    public final static int CHUNK_RETRANSMIT_REQUEST = 0x10;
    
    //final sequence id's
    public final static int SEQUENCE_BEGIN = 0x1;
    public final static int OUT_OF_SEQUENCE = 0x0; //control packets
    
    //full packets
    public final static byte[] SYNC_REQUEST_PACKET = new byte[]
        {(byte)(0), (byte)(0), (byte)(ZTPConstants.SYNC_REQUEST)};
    public final static byte[] REQUEST_BUFFER_STATE_PACKET = new byte[]
        {(byte)(0), (byte)(0), (byte)(ZTPConstants.REQUEST_BUFFER_STATE)};
    public final static byte[] HANDSHAKE_START_PACKET = new byte[]
        {(byte)(0), (byte)(0), (byte)(ZTPConstants.HANDSHAKE_START)};
    
    public final static int UNSIGNED_SHORT_MAX = 65535;
    
    
    public final static byte[] EMPTY_BYTE_ARRAY = {}; 

}
