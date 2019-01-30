package com.kaitusoft.ratel;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketVersion;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.streams.ReadStream;
import org.junit.Test;

import java.io.IOException;
import java.util.Timer;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/14
 *          <p>
 *          write description here
 */
public class WsTest {

    Timer timer = new Timer();
    String[] commands = {"{\"type\":\"REQUEST\",\"command\":\"activeThreadCount\",\"parameters\":{\"applicationName\":\"mobile_shopping_web\"}}",
            "{\"type\":\"REQUEST\",\"command\":\"activeThreadCount\",\"parameters\":{\"applicationName\":\"shoppingmall_image_web\"}}",
            "{\"type\":\"REQUEST\",\"command\":\"activeThreadCount\",\"parameters\":{\"applicationName\":\"mobile_store_web\"}}",

    };

    @Test
    public void websocket() {
        HttpClient client = Vertx.vertx().createHttpClient();
        MultiMap headers = new VertxHttpHeaders();
        headers.add("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
        ReadStream<WebSocket> readStream = client.websocketStreamAbs("ws://10.9.16.78/agent/activeThread.pinpointws", headers, WebsocketVersion.V13, "");

        readStream.handler(ws -> {
            ws.frameHandler(response -> {
                System.out.println(response.textData());
            });

            new Thread(new Write(ws)).start();

        });

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public final class Write implements Runnable {

        private WebSocket ws;

        public Write(WebSocket ws) {
            this.ws = ws;
        }

        @Override
        public void run() {
            int times = 0;
            while (times < 5) {

                ws.writeTextMessage(commands[times % commands.length]);
                times++;

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
