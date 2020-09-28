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

import com.sandpolis.core.instance.state.AbstractSTDocument;
import com.sandpolis.core.instance.state.AbstractSTObject;
import com.sandpolis.core.instance.state.STAttribute;
import com.sandpolis.core.instance.state.STCollection;
import com.sandpolis.core.instance.state.STDocument;
import com.sandpolis.core.instance.state.STObject;
import com.sandpolis.core.instance.state.VirtObject;

public class FxDocument<T extends VirtObject> extends AbstractSTDocument implements STDocument {

	public FxDocument(STObject<?> parent) {
		this.parent = (AbstractSTObject) parent;

		documents = new HashMap<>();
		collections = new HashMap<>();
		attributes = new HashMap<>();
	}

	@Override
	public STAttribute<?> newAttribute() {
		return new FxAttribute<>(this);
	}

	@Override
	public STDocument newDocument() {
		return new FxDocument<>(this);
	}

	@Override
	public STCollection newCollection() {
		return new FxCollection<>(this, FxProfile::new);// TODO
	}

}
