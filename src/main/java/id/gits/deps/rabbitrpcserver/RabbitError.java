package id.gits.deps.rabbitrpcserver;

public enum RabbitError {
    OnConnectError,
    OnDeclareQueueError,
    OnOpenChannelError,
    OnDeclareExchangeError,
    OnRouteBindingError,
    OnSendReplyError,
    OnSetupConsumerError
}
