This library aims to provide a reliable data stream for client–server applications over unreliable transport protocols

So I wanted to develop my own server-client application that communicates with my server via DNS tunneling. If you're not already familiar with DNS 
tunneling it's a means of enabling communication with "the internet" through severely limited or restricted connections (such as those found at airports or 
other 'pay to use' wifi points) through a DNS network. Most tunneling implementations will try to provide full TCP (Transmission Control Protocol) 
emulation over DNS. This is very desirable as a lot of our communication happens through TCP because of its reliability. POP (mail), IRC (chat), HTTP 
(www), FTP (file transfer) and XAMPP (facebook messenger, hangouts) are but a few protocols that use TCP at the transport layer for communication. So if 
you can tunnel TCP you can now visit websites, chat, send and receive messages and send and receive files and basically most things you probably want to use 
the internet for through your severely limited connection.

When we program something that uses TCP we can rest knowing that at the application level our data packets will indeed arrive and in the correct sequence. 
In most cases you don't even need to worry about things like resending data or data arriving jumbled up when you use TCP sockets. So for most applications 
they might prefer using TCP because it's guaranteeing painless sequential stream communication between 2 nodes.
There are however 2 major problems with 'TCP over DNS' and this is mostly recovery and speed. DNS primarily uses UDP at the transport layer and it 
usually is restricted to 512 octets per transaction. From a perspective of tunneling that is translated to 512 octet chunks that can arrive whenever they like and 
may not even arrive at all. Of course there are mechanisms (subject to DNS protocol versions) to overcome this 512 octet limitation which good TCP over 
DNS implementations will indeed make full use of. However eventually the TCP connections will get broken, this is usually due to a combination of 
congestion and adherence to the TCP.

In my case I don't care for all the complexities of TCP but I do enjoy it's reliability. For the needs of my application which are tiny text payloads it makes an 
ideal candidate for transmission over DNS but I am still faced with the problem that such communication is unreliable though I am not too bothered about it 
being slow. To overcome this limitation I have created a protocol that works at the application level through UDP. This protocol delivers the reliability of 
TCP whilst retaining the simplicity and statelessness of UDP. In other words a best of both approach.
Given the fact it is stateless I decided to call it the Zombie Transfer Protocol (ZTP), though a more apt name would be a stream transport protocol because it 
creates a sequentially correct data stream at 2 end points.  Step 1 was to create the protocol. This initial stage is complete and the complete source code can be 
found here (for scientific and testing purposes only)
