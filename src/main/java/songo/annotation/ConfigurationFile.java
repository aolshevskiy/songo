/*
 * Created by IntelliJ IDEA.
 * User: siasia
 * Date: 24.10.12
 * Time: 0:06
 */
package songo.annotation;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@BindingAnnotation
public @interface ConfigurationFile {
}
