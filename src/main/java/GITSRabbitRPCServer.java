import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GITSRabbitRPCServer {

    enum RabbitError {
        OnConnectError,
        OnDeclareQueueError,
        OnOpenChannelError,
        OnDeclareExchangeError,
        OnRouteBindingError,
        OnSendReplyError,
        OnSetupConsumerError
    }


    interface RabbitRPCServerListener {
        public void onConnected();
        public void onError(Exception e, RabbitError type);
    }

    private static RabbitRPCServerListener rabbitRPCServerListener;

    static RabbitRPCServer server;
    static Channel globalChannel;

    private static void InitialSetup(RabbitRPCServerListener serverListener) {
        rabbitRPCServerListener = serverListener;
        if (rabbitRPCServerListener == null) {
            rabbitRPCServerListener = new RabbitRPCServerListener() {
                @Override
                public void onConnected() {
                    System.out.println("No listener registered for RabbitMQ on connect");
                }

                @Override
                public void onError(Exception e, RabbitError rabbitError) {
                    System.out.println("No listener registered for RabbitMQ on error connection");
                }
            };
        }
    }

    private static void PostSetup(RabbitRPCServer server, String queueName) {
        globalChannel = SetupChannel(server);
        SetupQueue(globalChannel, queueName);
        SetupConsumer(globalChannel, queueName);
    }

    private static void StartServer(RabbitRPCServer server, String queueName, RabbitRPCServerListener serverListener) {
        InitialSetup(serverListener);
        PostSetup(server, queueName);
    }

    public static void Start(String uri, String queueName, RabbitRPCServerListener serverListener) {
        try {
            server = new RabbitRPCServer(uri);
            StartServer(server, queueName, serverListener);
            rabbitRPCServerListener.onConnected();
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnConnectError);
        }
    }

    public static void Start(String host, int port, String username, String password, String queueName, RabbitRPCServerListener serverListener) {
        try {
            server = new RabbitRPCServer(host, port, username, password);
            StartServer(server, queueName, serverListener);
            rabbitRPCServerListener.onConnected();
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnConnectError);
        }
    }

    private static Channel SetupChannel(RabbitRPCServer server) {
        try {
            return server.getConnection().createChannel();
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnOpenChannelError);
            return null;
        }
    }

    private static void SetupQueue(Channel channel, String queueName) {
        try {
            channel.queueDeclare(queueName, true, false, false, null);
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnDeclareQueueError);
        }
    }

    private static void SetupConsumer(Channel channel, String queueName) {
        try {
            channel.basicConsume(queueName, false, GITSRabbitRPCConsumerHandler.Consumer(globalChannel, new GITSRabbitRPCConsumerHandler.ConsumerListener() {
                @Override
                public void onSuccess(String result, Envelope envelope, AMQP.BasicProperties properties) {
                    PublishReply(result, envelope, properties);
                }

                @Override
                public void onError(Exception e, RabbitError err) {
                    rabbitRPCServerListener.onError(e, err);
                }
            }));
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnSetupConsumerError);
        }
    }

    private static void PublishReply(String response, Envelope envelope, AMQP.BasicProperties properties) {
        String replyTo = properties.getReplyTo();
        String correlationId = properties.getCorrelationId();

        try {
            globalChannel.basicPublish("", replyTo, null, response.getBytes());
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnSendReplyError);
        }
    }

    public static void DeclareExchange(String exchangeName, String type, boolean durable) {
        try {
            globalChannel.exchangeDeclare(exchangeName, type, durable);
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnDeclareExchangeError);
        }
    }

    public  static  void BindingRoute(String routeKey, String exchangeName, String queueName) {
        try {
            globalChannel.queueBind(queueName, exchangeName, routeKey);
        } catch (Exception e) {
            rabbitRPCServerListener.onError(e, RabbitError.OnRouteBindingError);
        }
    }

}
