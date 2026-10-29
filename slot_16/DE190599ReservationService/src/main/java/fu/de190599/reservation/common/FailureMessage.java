package fu.de190599.reservation.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Message returned (with HTTP 500, status 0) when an unexpected error occurs in the annotated endpoint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FailureMessage {
    String value();
}
