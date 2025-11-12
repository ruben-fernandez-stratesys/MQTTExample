package cat.tmb.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        // Defaults matching your MQTT Explorer setup
        final String uri = "wss://mqtt.tmbapi.tmb.cat:443/"; // basepath "/"
        final String username = "7b1d51c7";
        final String password = "1d3b01b5ab0d47f508873a9add265540";
        final String[] topics = new String[] {
                "sanbox_david/#",
                "alerts/metro/megafonia_imp"
        };
        
        final int[] qos = new int[topics.length];
        for (int i = 0; i < qos.length; i++) {
        	qos[i] = 0; // QoS 0 for all
        }

        final String clientId = "java-cli-" + UUID.randomUUID();

        MqttClient client = null;
        try {
            System.out.println("MQTT URI       : " + uri);
            System.out.println("Client ID      : " + clientId);
            System.out.println("Username       : " + username);
            System.out.println("Topics (QoS 0) :");
            for (String t : topics) {
            	System.out.println("  - " + t);
            }

            client = new MqttClient(uri, clientId, new MemoryPersistence());

            // Callback to handle messages and connection events
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println(ts() + " Connection lost: " + (cause != null ? cause.getMessage() : "unknown"));
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payloadPreview;
                    byte[] payload = message.getPayload();
                    if (payload == null) payload = new byte[0];
                    // Try to decode as UTF-8 for readable logs
                    payloadPreview = new String(payload, StandardCharsets.UTF_8);
                    System.out.printf(
                            "%s << topic=%s qos=%d retained=%s payload=%s%n",
                            ts(), topic, message.getQos(), message.isRetained(), payloadPreview
                    );
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not publishing in this sample; left for completeness.
                }
            });

            // Connect options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true); // robust against network blips
            // You can tune timeouts if needed:
            options.setConnectionTimeout(10); // seconds
            options.setKeepAliveInterval(30); // seconds

            System.out.println(ts() + " Connecting...");
            client.connect(options);
            System.out.println(ts() + " Connected.");

            // Subscribe to topics
            if (topics.length == 1) {
                client.subscribe(topics[0], qos[0]);
            } else {
                client.subscribe(topics, qos);
            }
            System.out.println(ts() + " Subscribed.");

            // Keep process alive; add a shutdown hook for graceful disconnect
            MqttClient finalClient = client;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (finalClient.isConnected()) {
                        System.out.println(ts() + " Disconnecting...");
                        finalClient.disconnect();
                        System.out.println(ts() + " Disconnected.");
                    }
                } catch (Exception e) {
                    // ignore
                }
            }));

            // Block main thread
            System.out.println(ts() + " Waiting for messages. Press Ctrl+C to exit.");
            //noinspection InfiniteLoopStatement
            while (true) {
                Thread.sleep(1000L);
            }

        } catch (MqttException | InterruptedException e) {
            System.err.println(ts() + " Error: " + e.getMessage());
            if (e instanceof MqttException) {
                MqttException me = (MqttException) e;
                System.err.println("  reason  : " + me.getReasonCode());
                System.err.println("  cause   : " + me.getCause());
                System.err.println("  details : " + me);
            }
            System.exit(1);
        }
    }

    private static String ts() {
        return "[" + ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "]";
    }
}