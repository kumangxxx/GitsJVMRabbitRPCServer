package id.gits.deps.rabbitrpcserver;

public interface RabbitRPCServerListener {
    public void onConnected();
    public void onError(Exception e, RabbitError type);
}
