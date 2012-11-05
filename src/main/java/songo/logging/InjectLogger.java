/*
 * Created by IntelliJ IDEA.
 * User: siasia
 * Date: 02.11.12
 * Time: 19:58
 */
package songo.logging;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@BindingAnnotation
public @interface InjectLogger {
}
