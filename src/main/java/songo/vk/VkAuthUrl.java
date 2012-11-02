/*
 * Created by IntelliJ IDEA.
 * User: siasia
 * Date: 28.10.12
 * Time: 2:53
 */
package songo.vk;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
@BindingAnnotation
public @interface VkAuthUrl {
}
