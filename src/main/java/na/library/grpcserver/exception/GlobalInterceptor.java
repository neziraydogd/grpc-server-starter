package na.library.grpcserver.exception;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcGlobalServerInterceptor
public class GlobalInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GlobalInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        Context ctx = Context.current();

        ServerCall.Listener<ReqT> delegateListener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegateListener) {

            @Override
            public void onMessage(ReqT message) {
                if (ctx.isCancelled()) {
                    logger.warn("❌ Call to {} was already cancelled by the client", methodName);
                    call.close(Status.CANCELLED.withDescription("Cancelled by client"), new Metadata());
                    return;
                }

                if (ctx.getDeadline() != null && ctx.getDeadline().isExpired()) {
                    logger.warn("⏰ Call to {} exceeded deadline", methodName);
                    call.close(Status.DEADLINE_EXCEEDED.withDescription("Deadline exceeded"), new Metadata());
                    return;
                }

                super.onMessage(message);
            }

            @Override
            public void onCancel() {
                logger.warn("❌ Client cancelled the call to {}", methodName);
                super.onCancel();
            }
        };
    }
}
