package songo.logging;

import com.google.inject.MembersInjector;
import com.google.inject.spi.InjectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class LoggingMembersInjector<I> implements MembersInjector<I> {
	private final Field field;
	private final Logger logger;

	LoggingMembersInjector(Field aField) {
		field = aField;
		logger = LoggerFactory.getLogger(field.getDeclaringClass());
		field.setAccessible(true);
	}

	public void injectMembers(I anArg0) {
		try {
			field.set(anArg0, logger);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
