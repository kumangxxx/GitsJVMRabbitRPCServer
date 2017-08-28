package id.gits.deps.rabbitrpcserver;

public interface RPCHandler {
    public void messageReceived(String message, RPCHandlerCallback callback) throws Exception;
}
