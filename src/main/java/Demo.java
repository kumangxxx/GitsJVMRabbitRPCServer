public class Demo {

    public static void main(String[] args) {

        GITSRabbitRPCConsumerHandler.Handle("java.test", new GITSRabbitRPCConsumerHandler.RPCHandler() {
            @Override
            public void messageReceived(String message, GITSRabbitRPCConsumerHandler.RPCHandlerCallback callback) throws Exception {
                System.out.println("JAVA.TEST");
                System.out.println("Message : " + message);
                callback.callback("OK");
            }
        });

        GITSRabbitRPCServer.Start(
                "amqps://gitsmicros:UNIKOM@portal-ssl1155-0.bmix-dal-yp-151044e0-030b-4406-8efc-84656da093b9.nancys-us-ibm-com.composedb.com:19324/bmix-dal-yp-151044e0-030b-4406-8efc-84656da093b9",
                "java_queue",
                new GITSRabbitRPCServer.RabbitRPCServerListener() {

            @Override
            public void onConnected() {
                System.out.println("Connected to Rabbit");
                GITSRabbitRPCServer.DeclareExchange("java_exc", "topic", true);
                GITSRabbitRPCServer.BindingRoute("java.*", "java_exc", "java_queue");
            }

            @Override
            public void onError(Exception e, GITSRabbitRPCServer.RabbitError type) {
                e.printStackTrace();
                System.out.println(type);
            }
        });

    }

}
