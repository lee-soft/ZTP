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

import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ZTPUtil {
    
    public static void debugPrint(String info) {
        printText(info, "D");
    }
    
    public static void infoPrint(String info) {
        printText(info, "I");
    }
    
    public static void errorPrint(String info) {
        printText(info, "!");
    }
    
    private static void printText(String info, String textType) {
        
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date dateobj = new Date();
        System.out.println(textType + ": " + df.format(dateobj) + " - " + info);
        
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b) + " ");
        }
        return builder.toString();
    }
}
