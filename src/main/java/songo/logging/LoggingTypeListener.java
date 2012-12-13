package songo.logging;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class LoggingTypeListener implements TypeListener {
	@Override
	public <I> void hear(TypeLiteral<I> aTypeLiteral, TypeEncounter<I> aTypeEncounter) {
		for(Field field : aTypeLiteral.getRawType().getDeclaredFields()) {
			if(field.getType() == Logger.class && field.isAnnotationPresent(InjectLogger.class)) {
				aTypeEncounter.register(new LoggingMembersInjector<I>(field));
			}
		}
	}
}
