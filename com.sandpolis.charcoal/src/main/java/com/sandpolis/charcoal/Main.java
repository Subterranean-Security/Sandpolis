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
package com.sandpolis.charcoal;

import com.sandpolis.core.instance.MainDispatch;
import com.sandpolis.core.proto.util.Platform.Instance;
import com.sandpolis.core.proto.util.Platform.InstanceFlavor;

/**
 * This stub is the entry point for Charcoal instances. Control is given to
 * {@link MainDispatch} for initialization.
 *
 * @author cilki
 * @since 5.0.0
 */
public final class Main {
	private Main() {
	}

	public static void main(String[] args) {
		MainDispatch.dispatch(Charcoal.class, args, Instance.CHARCOAL, InstanceFlavor.NONE);
	}

}
