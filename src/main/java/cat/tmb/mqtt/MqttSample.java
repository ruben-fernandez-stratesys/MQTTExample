package cat.tmb.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSample {

	public static void main(String[] args) {
		String appId = "7b1d51c7";
		String appKey = "1d3b01b5ab0d47f508873a9add265540";
		String broker = "ws://mqtt.tmbapi.tmb.cat:443";
		String clientId = MqttClient.generateClientId();
		MemoryPersistence persistence = new MemoryPersistence();
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(appId);
		connOpts.setPassword(appKey.toCharArray());
		connOpts.setCleanSession(true);
		
		// ssl/tls config
        try {
            // broker = "ssl://broker.emqx.io:8883";

            // one-way ssl/tls
            // String caCrtFile = MqttSample.class.getResource("").getPath() + "./broker.emqx.io-ca.crt";
//             connOpts.setSocketFactory(SSLUtils.getSingleSocketFactory(caCrtFile));

            // two-way ssl/tls
            // String caCrtFile = MqttSample.class.getResource("").getPath() + "./server-ca.crt";
            // String crtFile = MqttSample.class.getResource("").getPath() + "./client.crt";
            // String keyFile = MqttSample.class.getResource("").getPath() + "./client.key";
            // connOpts.setSocketFactory(SSLUtils.getSocketFactory(caCrtFile, crtFile, keyFile, ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            MqttClient client = new MqttClient(broker, clientId, persistence);
            // callback
            client.setCallback(new SampleCallback());

            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected to broker: " + broker);
            
            client.disconnect();
            System.out.println("Disconnected");
            client.close();
            System.exit(0);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }

		
	}

}
