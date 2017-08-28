package id.gits.deps.rabbitrpcserver;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;

public class GITSRabbitRPCConsumerHandler {

    private static HashMap<String, RPCHandler> handlers = new HashMap<>();
    public static void Handle(String routingKey, RPCHandler handler) {
        handlers.put(routingKey, handler);
    }

    public interface ConsumerListener {
        public void onSuccess(String result, Envelope envelope, AMQP.BasicProperties properties);
        public void onError(Exception e, RabbitError err);
    }

    static Consumer Consumer(Channel channel, ConsumerListener listener) {
        return new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                String routingKey = envelope.getRoutingKey();
                RPCHandler handler = handlers.get(routingKey);
                if (handler != null) {
                    try {
                        handler.messageReceived(new String(body), new RPCHandlerCallback() {
                            @Override
                            public void callback(String result) {
                                listener.onSuccess(result, envelope, properties);
                                try { channel.basicAck(envelope.getDeliveryTag(), false); } catch(Exception e) { listener.onError(e, RabbitError.OnSendReplyError); }
                            }
                        });
                    } catch (Exception e) {
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                } else {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }

            }
        };
    }

}
