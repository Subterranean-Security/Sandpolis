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
package com.sandpolis.core.ipc;

import static com.sandpolis.core.instance.store.thread.ThreadStore.ThreadStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sandpolis.core.instance.Core;
import com.sandpolis.core.instance.PoolConstant.net;
import com.sandpolis.core.instance.store.StoreBase;
import com.sandpolis.core.instance.store.pref.PrefStore;
import com.sandpolis.core.ipc.IPCStore.IPCStoreConfig;
import com.sandpolis.core.ipc.MCMetadata.RS_Metadata;
import com.sandpolis.core.ipc.MSG.Message;
import com.sandpolis.core.ipc.MSG.Message.MsgCase;
import com.sandpolis.core.proto.util.Platform.Instance;
import com.sandpolis.core.proto.util.Platform.InstanceFlavor;

/**
 * This store manages interprocess connections.
 * 
 * @author cilki
 * @since 5.0.0
 */
public final class IPCStore extends StoreBase<IPCStoreConfig> {

	public static final Logger log = LoggerFactory.getLogger(IPCStore.class);

	/**
	 * An IPC message handler.
	 */
	public static interface Handler {
		public void handle(Message message, OutputStream out) throws IOException;
	}

	/**
	 * A list of open incoming connections spawned from the listener.
	 */
	private static List<Receptor> receptors;

	/**
	 * A list of open outgoing connections.
	 */
	private static List<Connector> connectors;

	/**
	 * A list of running listeners.
	 */
	private static List<Listener> listeners;

	/**
	 * A list of registered message handlers.
	 */
	private static Map<MsgCase, Handler> handlers;

	public static void init() {
		receptors = Collections.synchronizedList(new LinkedList<>());
		connectors = Collections.synchronizedList(new LinkedList<>());
		listeners = Collections.synchronizedList(new LinkedList<>());
		handlers = Collections.synchronizedMap(new HashMap<>());

		register(MsgCase.RQ_METADATA, (Message message, OutputStream out) -> {
			Message.newBuilder()
					.setRsMetadata(RS_Metadata.newBuilder().setInstance(Core.INSTANCE.name())
							.setVersion(Core.SO_BUILD.getVersion()).setPid(ProcessHandle.current().pid()))
					.build().writeDelimitedTo(out);
		});
	}

	/**
	 * Register a new message handler for the given type.
	 * 
	 * @param type    The message type
	 * @param handler The message handler
	 */
	public static void register(MsgCase type, Handler handler) {
		handlers.put(type, handler);
	}

	/**
	 * Get an unmodifiable view of the {@link Receptor} list.
	 * 
	 * @return The store's receptor list
	 */
	public static List<Receptor> getReceptors() {
		return Collections.unmodifiableList(receptors);
	}

	static List<Receptor> getMutableReceptors() {
		return receptors;
	}

	/**
	 * Get an unmodifiable view of the {@link Connector} list.
	 * 
	 * @return The store's connector list
	 */
	public static List<Connector> getConnectors() {
		return Collections.unmodifiableList(connectors);
	}

	static List<Connector> getMutableConnectors() {
		return connectors;
	}

	/**
	 * Get an unmodifiable view of the {@link Listener} list.
	 * 
	 * @return The store's listener list
	 */
	public static List<Listener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}

	static List<Listener> getMutableListeners() {
		return listeners;
	}

	/**
	 * Get an unmodifiable view of the {@link Handler} list.
	 * 
	 * @return The store's message handlers
	 */
	public static Map<MsgCase, Handler> getHandlers() {
		return Collections.unmodifiableMap(handlers);
	}

	/**
	 * Attempt to get an instance's metadata over IPC.
	 * 
	 * @param instance The instance type to target
	 * @param flavor   The instance subtype
	 * @return The received metadata object or {@code null} if an error occurred.
	 */
	public static Optional<RS_Metadata> queryInstance(Instance instance, InstanceFlavor flavor) {
		log.debug("Performing IPC query for {}:{} instances", instance, flavor);

		try (Connector connector = connect(instance, flavor)) {
			return connector.rq_metadata();
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	/**
	 * Start a new listener for the given instance type.
	 * 
	 * @param instance The instance type
	 * @param flavor   The instance subtype
	 * @throws IOException
	 */
	public static void listen(Instance instance, InstanceFlavor flavor) throws IOException {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(flavor);

		Listener listener = new Listener();

		Preferences preferences = PrefStore.getPreferences(instance, flavor);
		preferences.putInt("ipc.port", listener.getPort());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			listener.close();
			throw new IOException(e);
		}

		listener.start(ThreadStore.get(net.ipc.listener), ThreadStore.get(net.ipc.receptor));
		listeners.add(listener);

		log.debug("Opened IPC listener on port: {}", listener.getPort());
	}

	/**
	 * Make a connection to the given instance type.
	 * 
	 * @param instance The instance type
	 * @param flavor   The instance subtype
	 * @return The established connection
	 * @throws IOException
	 */
	public static Connector connect(Instance instance, InstanceFlavor flavor) throws IOException {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(flavor);

		// Find the port
		int port = getPort(instance, flavor);
		if (port == 0)
			throw new IOException("Failed to find IPC port");

		log.debug("Attempting IPC connection on port: {}", port);
		return new Connector(port);
	}

	/**
	 * Get the IPC listening port for the specified {@link Instance}.
	 * 
	 * @param instance The instance type
	 * @param flavor   The instance subtype
	 * @return The IPC port or 0 for not found
	 */
	public static int getPort(Instance instance, InstanceFlavor flavor) {
		return PrefStore.getPreferences(instance, flavor).getInt("ipc.port", 0);
	}

	public static final class IPCStoreConfig {

	}

	public static final IPCStore IPCStore = new IPCStore();

	@Override
	public void init(Consumer<IPCStoreConfig> o) {
		// TODO Auto-generated method stub

	}
}
