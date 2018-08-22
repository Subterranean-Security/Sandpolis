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
package com.sandpolis.server.store.user;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sandpolis.core.instance.ProtoType;
import com.sandpolis.core.proto.pojo.Listener.ListenerConfig;
import com.sandpolis.core.proto.pojo.Listener.ProtoListener;
import com.sandpolis.core.proto.pojo.User.ProtoUser;
import com.sandpolis.core.proto.pojo.User.UserConfig;
import com.sandpolis.core.proto.pojo.User.UserStats;
import com.sandpolis.core.proto.util.Result.ErrorCode;
import com.sandpolis.core.util.ValidationUtil;
import com.sandpolis.server.store.group.Group;
import com.sandpolis.server.store.listener.Listener;

/**
 * Represents a user account on the server.
 * 
 * @author cilki
 * @since 5.0.0
 */
@Entity
@Table(name = "Users")
public class User implements ProtoType<ProtoUser> {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int db_id;

	/**
	 * The user's unique ID.
	 */
	@Column(nullable = false, unique = true)
	private long id;

	/**
	 * The user's unique username.
	 */
	@Column(nullable = false, unique = true, length = 30)
	private String username;

	/**
	 * The user's optional email address.
	 */
	@Column(nullable = true)
	private String email;

	/**
	 * The user's password hash.
	 */
	@Column(nullable = false, length = 120)
	private String hash;

	/**
	 * The user creation timestamp.
	 */
	@Column(nullable = false)
	private long creation;

	/**
	 * An optional expiration timestamp.
	 */
	@Column(nullable = true)
	private long expiration;

	@ManyToMany(mappedBy = "members")
	private List<Group> groups;

	@Transient
	private int cvid;

	// JPA Constructor
	User() {
	}

	/**
	 * Construct a new {@link User} from a configuration.
	 * 
	 * @param config The configuration which should be prevalidated and complete
	 */
	public User(UserConfig config) {
		if (merge(ProtoUser.newBuilder().setConfig(config).build()) != ErrorCode.NONE)
			throw new IllegalArgumentException();
	}

	public long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public User setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public User setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getHash() {
		return hash;
	}

	public User setHash(String hash) {
		this.hash = hash;
		return this;
	}

	public long getCreation() {
		return creation;
	}

	public User setCreation(long creation) {
		this.creation = creation;
		return this;
	}

	public long getExpiration() {
		return expiration;
	}

	public User setExpiration(long expiration) {
		this.expiration = expiration;
		return this;
	}

	public int getCvid() {
		return cvid;
	}

	public User setCvid(int cvid) {
		this.cvid = cvid;
		return this;
	}

	@Override
	public ErrorCode merge(ProtoUser delta) {
		ErrorCode validity = ValidationUtil.validConfig(delta.getConfig());
		if (validity != ErrorCode.NONE)
			return validity;

		if (delta.hasConfig()) {
			UserConfig config = delta.getConfig();

			if (config.hasEmail())
				setEmail(config.getEmail());
			if (config.hasExpiration())
				setExpiration(config.getExpiration());
		}

		if (delta.hasStats()) {
			UserStats stats = delta.getStats();

			if (stats.hasCtime())
				setCreation(stats.getCtime());
		}

		return ErrorCode.NONE;
	}

	@Override
	public ProtoUser extract() {
		UserConfig.Builder config = UserConfig.newBuilder().setUsername(username).setEmail(email)
				.setExpiration(expiration);
		UserStats.Builder stats = UserStats.newBuilder().setCtime(creation);

		return ProtoUser.newBuilder().setConfig(config).setStats(stats).build();
	}
}