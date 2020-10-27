//============================================================================//
//                                                                            //
//                Copyright © 2015 - 2020 Subterranean Security               //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation at:                                //
//                                                                            //
//    https://mozilla.org/MPL/2.0                                             //
//                                                                            //
//=========================================================S A N D P O L I S==//
package com.sandpolis.core.instance.state.st;

import static com.sandpolis.core.instance.state.STStore.STStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.protobuf.Message;
import com.sandpolis.core.instance.state.oid.AbsoluteOid;
import com.sandpolis.core.instance.state.oid.Oid;

public abstract class AbstractSTObject<E extends Message> implements STObject<E> {

	private static final Logger log = LoggerFactory.getLogger(AbstractSTObject.class);

	/**
	 * The event bus that delivers change events. It is only initialized when a
	 * listener is attached. If the bus does not exist, events will not be
	 * generated.
	 */
	private EventBus bus;

	/**
	 * The number of listeners registered to the {@link #bus}.
	 */
	private int listeners;

	protected final Oid oid;

	protected final AbstractSTObject<?> parent;

	public AbstractSTObject(STObject<?> parent, long id) {
		this.parent = (AbstractSTObject<?>) parent;
		if (parent != null) {
			this.oid = parent.oid().child(id);
		} else {
			this.oid = AbsoluteOid.newOid(id);
		}
	}

	@Override
	public synchronized void addListener(Object listener) {
		if (bus == null) {
			bus = new EventBus();
		}
		bus.register(listener);
		listeners++;
	}

	public Oid oid() {
		return oid;
	}

	@Override
	public AbstractSTObject<?> parent() {
		return parent;
	}

	@Override
	public synchronized void removeListener(Object listener) {
		if (bus != null) {
			bus.unregister(listener);
			listeners--;
		}
		if (listeners == 0) {
			bus = null;
		}
	}

	protected synchronized <T> void fireAttributeValueChangedEvent(STAttribute<T> attribute,
			STAttributeValue<T> oldValue, STAttributeValue<T> newValue) {

		if (log.isTraceEnabled() && attribute == this) {
			log.trace("Attribute ({}) changed value from \"{}\" to \"{}\"", attribute.oid(), oldValue, newValue);
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STAttribute.ChangeEvent<T>(attribute, oldValue, newValue));
			});
		}

		if (parent() != null)
			parent().fireAttributeValueChangedEvent(attribute, oldValue, newValue);
	}

	protected synchronized void fireCollectionAddedEvent(STDocument document, STCollection newCollection) {

		if (log.isTraceEnabled() && document == this) {
			log.trace("Collection ({}) added to document ({})", newCollection.oid().last(), document.oid());
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STDocument.CollectionAddedEvent(document, newCollection));
			});
		}

		if (parent() != null)
			parent().fireCollectionAddedEvent(document, newCollection);
	}

	protected synchronized void fireCollectionRemovedEvent(STDocument document, STCollection oldCollection) {

		if (log.isTraceEnabled() && document == this) {
			log.trace("Collection ({}) removed from document ({})", oldCollection.oid().last(), document.oid());
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STDocument.CollectionRemovedEvent(document, oldCollection));
			});
		}

		if (parent() != null)
			parent().fireCollectionRemovedEvent(document, oldCollection);
	}

	protected synchronized void fireDocumentAddedEvent(STCollection collection, STDocument newDocument) {

		if (log.isTraceEnabled() && collection == this) {
			log.trace("Document ({}) added to collection ({})", newDocument.oid().last(), collection.oid());
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STCollection.DocumentAddedEvent(collection, newDocument));
			});
		}

		if (parent() != null)
			parent().fireDocumentAddedEvent(collection, newDocument);
	}

	protected synchronized void fireDocumentAddedEvent(STDocument document, STDocument newDocument) {

		if (log.isTraceEnabled() && document == this) {
			log.trace("Document ({}) added to document ({})", newDocument.oid().last(), document.oid());
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STDocument.DocumentAddedEvent(document, newDocument));
			});
		}

		if (parent() != null)
			parent().fireDocumentAddedEvent(document, newDocument);
	}

	protected synchronized void fireDocumentRemovedEvent(STCollection collection, STDocument oldDocument) {

		if (log.isTraceEnabled() && collection == this) {
			log.trace("Document ({}) removed from collection ({})", oldDocument.oid().last(), collection.oid());
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STCollection.DocumentRemovedEvent(collection, oldDocument));
			});
		}

		if (parent() != null)
			parent().fireDocumentRemovedEvent(collection, oldDocument);
	}

	protected synchronized void fireDocumentRemovedEvent(STDocument document, STDocument oldDocument) {

		if (log.isTraceEnabled() && document == this) {
			log.trace("Document ({}) removed from document ({})", oldDocument.oid().last(), document.oid());
		}

		if (bus != null) {
			STStore.pool().submit(() -> {
				bus.post(new STDocument.DocumentRemovedEvent(document, oldDocument));
			});
		}

		if (parent() != null)
			parent().fireDocumentRemovedEvent(document, oldDocument);
	}
}