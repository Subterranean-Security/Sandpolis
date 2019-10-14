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
package com.sandpolis.server.vanilla.store.group;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sandpolis.core.instance.storage.MemoryMapStoreProvider;
import com.sandpolis.core.instance.storage.database.Database;
import com.sandpolis.core.instance.store.MapStore;
import com.sandpolis.core.instance.util.ConfigUtil;
import com.sandpolis.core.proto.pojo.Group.GroupConfig;
import com.sandpolis.core.proto.pojo.Group.ProtoGroup;
import com.sandpolis.core.proto.util.Result.ErrorCode;
import com.sandpolis.server.vanilla.store.group.GroupStore.GroupStoreConfig;
import com.sandpolis.server.vanilla.store.user.User;

/**
 * The {@link GroupStore} manages groups and authentication mechanisms.
 *
 * @author cilki
 * @since 5.0.0
 */
public final class GroupStore extends MapStore<String, Group, GroupStoreConfig> {

	private static final Logger log = LoggerFactory.getLogger(GroupStore.class);

	public GroupStore() {
		super(log);
	}

	/**
	 * Create a new group from the given configuration and add it to the store.
	 *
	 * @param config The group configuration
	 */
	public void add(GroupConfig.Builder config) {
		add(config.build());
	}

	/**
	 * Create a new group from the given configuration and add it to the store.
	 *
	 * @param config The group configuration
	 */
	public void add(GroupConfig config) {
		Objects.requireNonNull(config);
		checkArgument(ConfigUtil.valid(config) == ErrorCode.OK, "Invalid configuration");
		checkArgument(ConfigUtil.complete(config) == ErrorCode.OK, "Incomplete configuration");

		add(new Group(config));
	}

	/**
	 * Add a group to the store.
	 *
	 * @param group The group to add
	 */
	public void add(Group group) {
		checkArgument(get(group.getGroupId()).isEmpty(), "ID conflict");

		log.debug("Adding new group: {}", group.getGroupId());
		provider.add(group);
	}

	/**
	 * Get all groups that the given user owns or is a member of.
	 *
	 * @param user A user
	 * @return A list of the user's groups
	 */
	public List<Group> getMembership(User user) {
		return stream().filter(group -> user.equals(group.getOwner()) || group.getMembers().contains(user))
				.collect(Collectors.toList());
	}

	/**
	 * Get a list of groups without an auth mechanism.
	 *
	 * @return A list of unauth groups
	 */
	public List<Group> getUnauthGroups() {
		return stream().filter(group -> group.getKeys().size() == 0 && group.getPasswords().size() == 0)
				.collect(Collectors.toList());
	}

	/**
	 * Get a list of groups with the given password.
	 *
	 * @param password The password
	 * @return A list of groups with the password
	 */
	public List<Group> getByPassword(String password) {
		return stream()
				.filter(group -> group.getPasswords().stream().anyMatch(mech -> mech.getPassword().equals(password)))
				.collect(Collectors.toList());
	}

	/**
	 * Change a group's configuration or statistics.
	 *
	 * @param id    The ID of the group to modify
	 * @param delta The changes
	 * @return The outcome of the action
	 */
	public ErrorCode delta(String id, ProtoGroup delta) {
		Group group = get(id).orElse(null);
		if (group == null)
			return ErrorCode.UNKNOWN_GROUP;

		return group.merge(delta);
	}

	@Override
	public GroupStore init(Consumer<GroupStoreConfig> configurator) {
		var config = new GroupStoreConfig();
		configurator.accept(config);

		config.defaults.forEach(this::add);

		return (GroupStore) super.init(null);
	}

	public final class GroupStoreConfig extends StoreConfig {

		public final List<GroupConfig> defaults = new ArrayList<>();

		@Override
		public void ephemeral() {
			provider = new MemoryMapStoreProvider<String, Group>(Group.class, Group::getGroupId);
		}

		@Override
		public void persistent(Database database) {
			provider = database.getConnection().provider(Group.class, "id");
		}
	}

	public static final GroupStore GroupStore = new GroupStore();
}
