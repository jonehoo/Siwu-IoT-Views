// mqttClient.js
import mqtt from 'mqtt';

class MqttClient {
  constructor(brokerUrl, options) {
    this.brokerUrl = brokerUrl || '';
    this.clientId = options.clientId || "SW-VIEWS" + new Date().getTime();
    this.username = options.username || 'admin';
    this.password = options.password || '123456';
    this.client = null;
  }

  // 连接 MQTT broker
  connect() {
    this.client = mqtt.connect(this.brokerUrl, {
      clientId: this.clientId,
      username: this.username,
      password: this.password
    });

    this.client.on('connect', () => {
    });

    this.client.on('reconnect', () => {
    });

    this.client.on('offline', () => {
    });

    this.client.on('error', (error) => {
    });
  }

  // 订阅某个主题
  subscribe(topic, callback) {
    if (this.client) {
      this.client.subscribe(topic, (err) => {
        if (!err) {
        } else {
        }
      });

      this.client.on('message', (topic, message) => {
        callback(topic, message);
      });
    } else {
    }
  }

  // 断开连接
  disconnect() {
    if (this.client) {
      this.client.end();
    }
  }
}

export default MqttClient;
