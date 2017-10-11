package org.effiandeggie.jfall;

import com.twitter.util.Future;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;


public class FutureUtil {

    public static <T> CompletableFuture<T> toJavaFuture(Future<T> finagleFuture) {
        final CompletableFuture<T> javaFuture = new CompletableFuture<>();
        finagleFuture.onSuccess(new AbstractFunction1<T, BoxedUnit>() {
            @Override
            public BoxedUnit apply(final T value) {
                javaFuture.complete(value);
                return null;
            }
        });

        finagleFuture.onFailure(new AbstractFunction1<Throwable, BoxedUnit>() {
            @Override
            public BoxedUnit apply(final Throwable exception) {
                javaFuture.completeExceptionally(exception);
                return null;
            }
        });

        return javaFuture;
    }
}
