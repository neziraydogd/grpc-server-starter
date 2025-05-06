package na.library.grpcserver.exception;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@GrpcGlobalServerInterceptor
public class LoggingInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        logger.info("Received call: method = {}", call.getMethodDescriptor().getFullMethodName());
        Context ctx = Context.current();

        // Check if the client has canceled the call
        if (ctx.isCancelled()) {
            call.close(Status.CANCELLED.withDescription("Client canceled the call"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        // Handle the deadline
        long deadline = ctx.getDeadline() != null ? ctx.getDeadline().timeRemaining(TimeUnit.MILLISECONDS) : Long.MAX_VALUE;
        if (deadline <= 0) {
            call.close(Status.DEADLINE_EXCEEDED.withDescription("Deadline exceeded"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }

}
