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
package com.sandpolis.viewer.lifegem.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sandpolis.core.instance.State.ProtoCollection;
import com.sandpolis.core.instance.state.AbstractSTCollection;
import com.sandpolis.core.instance.state.STCollection;
import com.sandpolis.core.instance.state.STDocument;
import com.sandpolis.core.instance.state.VirtObject;
import com.sandpolis.core.instance.state.oid.RelativeOid;
import com.sandpolis.core.instance.store.StoreMetadata;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FxCollection<T extends VirtObject> extends AbstractSTCollection implements STCollection {

	private Function<STDocument, T> constructor;

	private ObservableList<T> container;

	private Map<Integer, Integer> indexMap;

	public FxCollection(Function<STDocument, T> constructor) {
		this.container = FXCollections.observableArrayList();
		this.indexMap = new HashMap<>();
		this.constructor = Objects.requireNonNull(constructor);
	}

	public FxCollection(STCollection base, Function<STDocument, T> constructor) {
		this(constructor);
		base.documents().map(constructor).forEach(container::add);
	}

	@Override
	public STDocument document(int tag) {
		var index = indexMap.get(tag);
		if (index != null)
			return container.get(index).document;

		var document = newDocument();
		setDocument(tag, document);

		return document;
	}

	@Override
	public Stream<STDocument> documents() {
		return container.stream().map(item -> item.document);
	}

	@Override
	public STDocument getDocument(int tag) {
		if (indexMap.containsKey(tag))
			return container.get(indexMap.get(tag)).document;

		return null;
	}

	@Override
	public StoreMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObservableList<T> getObservable() {
		return container;
	}

	@Override
	public boolean isEmpty() {
		return container.isEmpty();
	}

	@Override
	public void merge(ProtoCollection snapshot) {
		for (var entry : snapshot.getDocumentMap().entrySet()) {
			document(entry.getKey()).merge(entry.getValue());
		}

//		if (!snapshot.getPartial()) {
//			Platform.runLater(() -> {
//				// Remove anything that wasn't in the snapshot
//				container.removeIf(item -> !snapshot.containsDocument(item.tag()));
//			});
//		}
	}

	@Override
	public STDocument newDocument() {
		return new FxDocument<T>(this);
	}

	@Override
	public void remove(STDocument document) {
		for (int i = 0; i < container.size(); i++) {
			if (container.get(i).document.equals(document)) {
				final int index = i;
				indexMap.values().removeIf(j -> j == index);
				container.remove(index);
				return;
			}
		}
	}

	@Override
	public void setDocument(int tag, STDocument document) {
		Platform.runLater(() -> {
			var index = indexMap.get(tag);
			if (index == null) {
				indexMap.put(tag, container.size());
				container.add(constructor.apply(document));
			} else {
				container.set(index, constructor.apply(document));
			}
		});
	}

	@Override
	public int size() {
		return container.size();
	}

	@Override
	public ProtoCollection snapshot(RelativeOid<?>... oids) {
		throw new UnsupportedOperationException();
	}

}