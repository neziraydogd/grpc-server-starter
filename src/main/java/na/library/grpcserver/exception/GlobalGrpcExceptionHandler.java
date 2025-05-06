package na.library.grpcserver.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalGrpcExceptionHandler.class);

    @GrpcExceptionHandler
    public StatusRuntimeException handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Handling IllegalArgumentException: {}", ex.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler
    public StatusRuntimeException handleRuntimeException(RuntimeException ex) {
        logger.error("Unexpected error", ex);
        return Status.INTERNAL.withDescription("Internal server error").asRuntimeException();
    }
}
