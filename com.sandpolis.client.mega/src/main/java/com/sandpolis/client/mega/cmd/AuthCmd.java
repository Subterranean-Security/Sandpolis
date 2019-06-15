/******************************************************************************
 *                                                                            *
 *                    Copyright 2017 Subterranean Security                    *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *      http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 *                                                                            *
 *****************************************************************************/
package com.sandpolis.client.mega.cmd;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sandpolis.core.net.command.Cmdlet;
import com.sandpolis.core.net.future.ResponseFuture;
import com.sandpolis.core.net.handler.Sand5Handler;
import com.sandpolis.core.proto.net.MCAuth.RQ_KeyAuth;
import com.sandpolis.core.proto.net.MCAuth.RQ_NoAuth;
import com.sandpolis.core.proto.net.MCAuth.RQ_PasswordAuth;
import com.sandpolis.core.proto.util.Result.Outcome;
import com.sandpolis.core.util.CryptoUtil.SAND5.ReciprocalKeyPair;

/**
 * Contains authentication commands for client instances.
 * 
 * @author cilki
 * @since 5.0.0
 */
public final class AuthCmd extends Cmdlet<AuthCmd> {

	/**
	 * Attempt to authenticate without providing any form of identification.
	 * 
	 * @return A response future
	 */
	public ResponseFuture<Outcome> none() {
		return route(RQ_NoAuth.newBuilder());
	}

	/**
	 * Attempt to authenticate with a password.
	 * 
	 * @return A response future
	 */
	public ResponseFuture<Outcome> password(String password) {
		checkNotNull(password);

		return route(RQ_PasswordAuth.newBuilder().setPassword(password));
	}

	/**
	 * Attempt to authenticate with a SAND5 keypair.
	 * 
	 * @param group The group ID
	 * @param mech  The key mechanism ID
	 * @param key   The client keypair
	 * @return A response future
	 */
	public ResponseFuture<Outcome> key(String group, long mech, ReciprocalKeyPair key) {
		checkNotNull(group);
		checkNotNull(key);

		Sand5Handler.registerResponseHandler(sock.channel(), key);
		return route(RQ_KeyAuth.newBuilder().setGroupId(group).setMechId(mech));
	}

	/**
	 * Prepare for an asynchronous command.
	 * 
	 * @return A configurable object from which all asynchronous (nonstatic)
	 *         commands in {@link AuthCmd} can be invoked
	 */
	public static AuthCmd async() {
		return new AuthCmd();
	}

	private AuthCmd() {
	}
}
