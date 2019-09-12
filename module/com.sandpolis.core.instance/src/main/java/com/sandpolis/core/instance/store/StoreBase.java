/*******************************************************************************
 *                                                                             *
 *                Copyright © 2015 - 2019 Subterranean Security                *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *      http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *                                                                             *
 ******************************************************************************/
package com.sandpolis.core.instance.store;

import static com.sandpolis.core.instance.store.thread.ThreadStore.ThreadStore;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.sandpolis.core.instance.event.Event;
import com.sandpolis.core.instance.event.ParameterizedEvent;
import com.sandpolis.core.instance.storage.database.Database;
import com.sandpolis.core.instance.store.StoreBase.StoreConfig;

/**
 * A Store is designed to provide extremely convenient access to a collection of
 * objects.
 */
public abstract class StoreBase<E extends StoreConfig> {

	private Logger log;

	protected StoreBase(Logger log) {
		this.log = log;
	}

	/**
	 * A bus that is used to deliver events to the users of the store.
	 */
	private final EventBus bus = new EventBus((Throwable exception, SubscriberExceptionContext context) -> {
		log.error("Exception occurred in event handler", exception);
	});

	/**
	 * Add the given subscriber from the store bus.
	 * 
	 * @param object The subscriber to add
	 */
	public final void register(Object object) {
		bus.register(object);
	}

	/**
	 * Remove the given subscriber from the store bus.
	 * 
	 * @param object The subscriber to remove
	 */
	public final void unregister(Object object) {
		bus.unregister(object);
	}

	/**
	 * Broadcast the given event asynchronously to the store's bus.
	 *
	 * @param constructor The event constructor
	 */
	public final void postAsync(Supplier<? extends Event> constructor) {
		ThreadStore.get("store.event_bus").submit(() -> {
			post(constructor);
		});
	}

	/**
	 * Broadcast the given event asynchronously to the store's bus.
	 * 
	 * @param <P>         The event parameter's type
	 * @param constructor The event constructor
	 * @param parameter   The event parameter
	 */
	public final <P> void postAsync(Supplier<? extends ParameterizedEvent<P>> constructor, P parameter) {
		ThreadStore.get("store.event_bus").submit(() -> {
			post(constructor, parameter);
		});
	}

	/**
	 * Broadcast the given event to the store's bus. This method blocks until every
	 * event handler completes.
	 *
	 * @param constructor The event constructor
	 */
	public final void post(Supplier<? extends Event> constructor) {
		Event event = constructor.get();

		log.debug("Event fired: " + event);// TODO toString
		bus.post(event);
	}

	/**
	 * Broadcast the given event to the store's bus. This method blocks until every
	 * event handler completes.
	 * 
	 * @param <P>         The event parameter's type
	 * @param constructor The event constructor
	 * @param parameter   The event parameter
	 */
	public final <P> void post(Supplier<? extends ParameterizedEvent<P>> constructor, P parameter) {
		ParameterizedEvent<P> event = constructor.get();

		try {
			// Set the parameter with reflection
			Field field = event.getClass().getSuperclass().getDeclaredField("object");
			field.setAccessible(true);
			field.set(event, parameter);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		log.debug("Event fired: " + event);// TODO toString
		bus.post(event);
	}

	/**
	 * Uninitialize and release the resources in the store.
	 *
	 * @throws Exception
	 */
	public void close() throws Exception {
	}

	/**
	 * Initialize the store.
	 * 
	 * @param configurator The configuration block
	 * @return {@code this}
	 */
	public StoreBase<E> init(Consumer<E> configurator) {
		return this;
	}

	/**
	 * A superclass for all store configurations.
	 */
	public abstract static class StoreConfig {

		/**
		 * Indicates that the store's data should not survive the closing of the store.
		 */
		public void ephemeral() {
			throw new UnsupportedOperationException("Store does not support ephemeral providers");
		}

		/**
		 * Indicates that the store's data should be persisted to the given database.
		 * 
		 * @param database The database handle
		 */
		public void persistent(Database database) {
			throw new UnsupportedOperationException("Store does not support persistent providers");
		}
	}
}