package uk.ac.warwick.util.graphite;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.junit.Test;


public class StatsdClientTest {
    
    private final Object lock = new Object();
    
    private int serverPort = -1;
    
    @Test public void udp() throws Exception {
        final StringBuffer sb = new StringBuffer();
        
        // This thread receives one packet, puts it into a StringBuffer then ends.
        Thread t = new Thread() {
            public void run() {
                try {
                    DatagramChannel server = DatagramChannel.open();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    server.socket().bind(null);
                    serverPort = server.socket().getLocalPort();
                    synchronized(lock) { 
                        lock.notifyAll(); 
                    }
                    SocketAddress address = null;
                    while (address == null) {
                        address = server.receive(buffer);
                    }
                    buffer.flip();
                    readIntoString(sb, buffer);
                    synchronized(lock) { 
                        lock.notifyAll(); 
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void readIntoString(final StringBuffer sb, ByteBuffer buffer) {
                byte[] b = new byte[buffer.remaining()];
                buffer.get(b);
                sb.append(new String(b));
            };
        };
        
        t.start();
        
        // Wait for server DatagramSocket to be created and bound to a random port.
        while (serverPort == -1) {
            synchronized(lock) { lock.wait(); }
        }
        
        StatsdClient client = new StatsdClient("127.0.0.1", serverPort, "www2-test", "sb-edit-primary");
        client.increment("deploys");
        
        t.join();
        
        String text = sb.toString();
        assertThat( text, is("apps.www2-test.sb-edit-primary.deploys:1|c") );
        
    }
    
    
}
