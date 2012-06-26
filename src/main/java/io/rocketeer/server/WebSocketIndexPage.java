package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WebSocketIndexPage {

     public static ChannelBuffer getContent(String webSocketLocation) {
         return ChannelBuffers.copiedBuffer(fetch("io/rocketeer/server/client.html"), CharsetUtil.UTF_8);
     }

     private static String fetch( final String target ) {
       System.out.println("feching from CL: " + target );
       final StringBuffer buf = new StringBuffer();
       try {
            final BufferedReader reader =
              new BufferedReader(
                new InputStreamReader(
                  WebSocketIndexPage.class.
                    getClassLoader().getResourceAsStream(target)
                )
              );
            if ( reader != null ) {
            try {
             String line;
             while ((line = reader.readLine()) != null) {
               buf.append(line).
                   append("\r\n");
             }
            } catch ( Exception x ) { }
            finally {
             reader.close();
            }
           } else {
                System.out.println(target+" no found :(");
           }
      } catch ( Exception io ) {
           System.err.println("feching html");
           io.printStackTrace();
      }
        return buf.toString();
     }

}



