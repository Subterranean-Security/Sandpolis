/******************************************************************************
 *                                                                            *
 *                    Copyright 2018 Subterranean Security                    *
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
package com.sandpolis.server.vanilla.exe;

import static com.sandpolis.core.instance.Environment.EnvPath.LIB;
import static com.sandpolis.core.instance.store.plugin.PluginStore.PluginStore;
import static com.sandpolis.core.util.ArtifactUtil.ParsedCoordinate.fromCoordinate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.sandpolis.core.instance.Config;
import com.sandpolis.core.instance.ConfigConstant.plugin;
import com.sandpolis.core.instance.Environment;
import com.sandpolis.core.net.command.Exelet;
import com.sandpolis.core.proto.net.MCPlugin.RQ_PluginInstall;
import com.sandpolis.core.proto.net.MCPlugin.RQ_PluginList;
import com.sandpolis.core.proto.net.MCPlugin.RS_ArtifactDownload;
import com.sandpolis.core.proto.net.MCPlugin.RS_PluginList;
import com.sandpolis.core.proto.net.MSG;
import com.sandpolis.core.proto.util.Platform.InstanceFlavor;
import com.sandpolis.core.proto.util.Result.Outcome;
import com.sandpolis.core.util.ArtifactUtil;
import com.sandpolis.core.util.ArtifactUtil.ParsedCoordinate;
import com.sandpolis.core.util.CertUtil;
import com.sandpolis.core.util.JarUtil;
import com.sandpolis.core.util.NetUtil;
import com.sandpolis.server.vanilla.store.trust.TrustStore;

/**
 * Message handlers for plugin requests.
 * 
 * @author cilki
 * @since 5.0.0
 */
public class PluginExe extends Exelet {

	private static final Logger log = LoggerFactory.getLogger(PluginExe.class);

	@Auth
	@Handler(tag = MSG.Message.RQ_ARTIFACT_DOWNLOAD_FIELD_NUMBER)
	public void rq_artifact_download(MSG.Message m) {
		var rq = Objects.requireNonNull(m.getRqArtifactDownload());
		var rs = RS_ArtifactDownload.newBuilder();

		ParsedCoordinate coordinate = fromCoordinate(rq.getCoordinates());
		log.debug("Received artifact request: " + coordinate.coordinate);

		PluginStore.getPlugin(coordinate.artifactId).ifPresentOrElse(plugin -> {
			if (!PluginStore.findComponentTypes(plugin).contains(connector.getRemoteInstanceFlavor()))
				reply(m, Outcome.newBuilder().setResult(false));// TODO message
			else if (rq.getLocation()) {
				reply(m, rs.setCoordinates(String.format(":%s:%s", plugin.getId(), plugin.getVersion())));
			} else {
				// Send binary for correct component
				ByteSource component = PluginStore.getPluginComponent(plugin, connector.getRemoteInstance(),
						// TODO hardcoded subtype
						InstanceFlavor.MEGA);

				try (var in = component.openStream()) {
					reply(m, rs.setBinary(ByteString.readFrom(in)));
				} catch (IOException e) {
					// Failed to read plugin
					reply(m, Outcome.newBuilder().setResult(false));// TODO message
				}
			}
		}, () -> {
			// Check regular artifacts
			Path artifact = ArtifactUtil.getArtifactFile(Environment.get(LIB), coordinate.coordinate);

			if (!Files.exists(artifact)) {
				// Try to find a suitable artifact
				try (Stream<Path> artifacts = ArtifactUtil.findArtifactFile(Environment.get(LIB),
						coordinate.artifactId)) {
					artifact = artifacts.findAny().orElse(null);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			if (artifact != null) {
				if (rq.getLocation()) {
					reply(m, rs.setCoordinates("TODO"));
				} else {
					try (var in = Files.newInputStream(artifact)) {
						reply(m, rs.setBinary(ByteString.readFrom(in)));
					} catch (IOException e) {
						// Failed to read artifact
						reply(m, Outcome.newBuilder().setResult(false));// TODO message
					}
				}
			} else if (rq.getLocation()) {
				reply(m, rs.setCoordinates("TODO"));
			} else {
				// No artifact could be found or located
				reply(m, Outcome.newBuilder().setResult(false));// TODO message
			}
		});
	}

	@Auth
	@Handler(tag = MSG.Message.RQ_PLUGIN_LIST_FIELD_NUMBER)
	public Message.Builder rq_plugin_list(RQ_PluginList rq) {
		if (!Config.getBoolean(plugin.enabled))
			return failure(begin());

		return RS_PluginList.newBuilder().addAllPlugin(() -> PluginStore.getPluginDescriptors().iterator());
	}

	@Auth
	@Handler(tag = MSG.Message.RQ_PLUGIN_INSTALL_FIELD_NUMBER)
	public Message.Builder rq_plugin_install(RQ_PluginInstall rq) throws Exception {
		var outcome = begin();
		if (!Config.getBoolean(plugin.enabled))
			return failure(outcome);

		Path binary = Files.createTempFile("", ".jar");
		switch (rq.getSourceCase()) {
		case PLUGIN_BINARY:
			Files.write(binary, rq.getPluginBinary().toByteArray());
			break;
		case PLUGIN_URL:
			Files.write(binary, NetUtil.download(rq.getPluginUrl()));
			break;
		case PLUGIN_COORDINATES:
			ArtifactUtil.download(binary.getParent(), rq.getPluginCoordinates());

			binary = binary.resolveSibling("TODO");// TODO
			break;
		default:
			return failure(outcome);
		}

		var manifest = JarUtil.getManifest(binary);

		// Read plugin name
		String id = manifest.getValue("Plugin-Id");

		// Read certificate
		var cert = CertUtil.parse(manifest.getValue("Plugin-Cert"));

		// Verify certificate
		if (!TrustStore.verifyPluginCertificate(cert))
			return failure(outcome);

		// Move into library directory
		Files.move(binary, Environment.get(LIB).resolve(id + ".jar"));
		return success(outcome);
	}

}
