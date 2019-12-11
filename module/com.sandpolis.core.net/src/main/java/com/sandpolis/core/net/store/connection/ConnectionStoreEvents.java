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
package com.sandpolis.core.net.store.connection;

import com.sandpolis.core.instance.event.ParameterizedEvent;
import com.sandpolis.core.net.sock.Sock;

public final class ConnectionStoreEvents {

	public static final class SockLostEvent extends ParameterizedEvent<Sock> {
	}

	public static final class SockEstablishedEvent extends ParameterizedEvent<Sock> {
	}

	private ConnectionStoreEvents() {
	}
}
