import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.net.URI;

class RabbitRPCServer {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public  RabbitRPCServer(String uri) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);
        connection = factory.newConnection();
    }

    public RabbitRPCServer(String host, int port, String username, String password) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connection = connectionFactory.newConnection();
    }

}
