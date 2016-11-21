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
import com.lee_soft.ztp.ZTPEventListener;
import com.lee_soft.ztp.ZTPSession;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuoteServerManager implements ZTPEventListener {

    protected BufferedReader in = null;
    protected boolean moreQuotes = true;
    
    public QuoteServerManager() throws IOException {
        try {
            in = new BufferedReader(new FileReader("one-liners.txt"));
        }   
        catch (FileNotFoundException e){
            System.err.println("Couldn't open quote file.  Serving time instead.");
        }
    }
    
    @Override
    public void sessionCreated(ZTPSession newSession) {
        System.out.println("New Session Created: " + newSession.sessionKey);
       
    }

    @Override
    public void sessionRecievedBufferContainsData(ZTPSession targetSession) {
        System.out.println("Remote: " + new String(targetSession.getLatestRecievedData()));
        
        //read the next quote
        if (moreQuotes) {
            byte[] buf;
            String dString = null;
            if (in == null)
            {
                dString = new Date().toString();
                moreQuotes = false;
            }
            else
                dString = getNextQuote();
            
            buf = dString.getBytes();
            
            try {
                targetSession.queueOutData(buf);
            } catch (IOException ex) {
                Logger.getLogger(QuoteServerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
 
    protected String getNextQuote() {
        String returnValue = null;
        try {
            if ((returnValue = in.readLine()) == null) {
                in.close();
                moreQuotes = false;
                returnValue = "No more quotes. Goodbye.";
            }
        } catch (IOException e) {
            returnValue = "IOException occurred in server.";
        }
        return returnValue;
    }
}
