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
package com.sandpolis.server.vanilla.net.init;

import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sandpolis.core.instance.Config;
import com.sandpolis.core.instance.ConfigConstant.net;
import com.sandpolis.core.net.command.Exelet;
import com.sandpolis.core.net.handler.CvidResponseHandler;
import com.sandpolis.core.net.init.ChannelConstant;
import com.sandpolis.core.net.init.PipelineInitializer;
import com.sandpolis.core.util.CertUtil;
import com.sandpolis.server.vanilla.exe.AuthExe;
import com.sandpolis.server.vanilla.exe.GenExe;
import com.sandpolis.server.vanilla.exe.GroupExe;
import com.sandpolis.server.vanilla.exe.ListenerExe;
import com.sandpolis.server.vanilla.exe.LoginExe;
import com.sandpolis.server.vanilla.exe.PluginExe;
import com.sandpolis.server.vanilla.exe.ServerExe;
import com.sandpolis.server.vanilla.exe.StreamExe;
import com.sandpolis.server.vanilla.exe.UserExe;
import com.sandpolis.server.vanilla.net.handler.ProxyHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultPromise;

/**
 * This {@link ChannelInitializer} configures a {@link ChannelPipeline} for use
 * as a server connection.
 *
 * @author cilki
 * @since 5.0.0
 */
public class ServerInitializer extends PipelineInitializer {

	private static final Logger log = LoggerFactory.getLogger(ServerInitializer.class);

	// This will cause problems if the server CVID is allowed to change because
	// CvidResponseHandler always uses the latest CVID while ServerInitializer is
	// stuck with the cvid field below.
	private static final CvidResponseHandler cvidHandler = new CvidResponseHandler();

	/**
	 * All server {@link Exelet} classes.
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends Exelet>[] exelets = new Class[] { AuthExe.class, GenExe.class, GroupExe.class,
			ListenerExe.class, LoginExe.class, ServerExe.class, UserExe.class, PluginExe.class, StreamExe.class };

	/**
	 * The certificate in PEM format.
	 */
	private byte[] cert;

	/**
	 * The private key in PEM format.
	 */
	private byte[] key;

	/**
	 * The cached {@link SslContext}.
	 */
	private SslContext sslCtx;

	/**
	 * The server's CVID.
	 */
	private int cvid;

	/**
	 * Construct a {@link ServerInitializer} with a self-signed certificate.
	 *
	 * @param cvid The server CVID
	 */
	public ServerInitializer(int cvid) {
		super(exelets);
		this.cvid = cvid;
	}

	/**
	 * Construct a {@link ServerInitializer} with the given certificate.
	 *
	 * @param cvid The server CVID
	 * @param cert The certificate
	 * @param key  The private key
	 */
	public ServerInitializer(int cvid, byte[] cert, byte[] key) {
		this(cvid);
		if (cert == null && key == null)
			return;
		if (cert == null || key == null)
			throw new IllegalArgumentException();

		this.cert = cert;
		this.key = key;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		super.initChannel(ch);

		if (Config.getBoolean(net.connection.tls)) {
			SslHandler ssl = getSslContext().newHandler(ch.alloc());
			ch.pipeline().addAfter("traffic", "ssl", ssl);
			ch.attr(ChannelConstant.HANDLER_SSL).set(ssl);
		}

		// Add CVID handler
		ch.pipeline().addBefore("exe", "cvid", cvidHandler);
		ch.attr(ChannelConstant.FUTURE_CVID).set(new DefaultPromise<>(ch.eventLoop()));

		// Add proxy handler
		ch.pipeline().addAfter("protobuf.frame_decoder", "proxy", new ProxyHandler(cvid));
	}

	public SslContext getSslContext() throws Exception {
		if (sslCtx == null && Config.getBoolean(net.connection.tls)) {
			sslCtx = buildSslContext();

			// No point in keeping these around anymore
			cert = null;
			key = null;
		}

		return sslCtx;
	}

	private SslContext buildSslContext() throws CertificateException, SSLException {
		if (cert != null && key != null)
			try {
				return SslContextBuilder.forServer(CertUtil.parseKey(key), CertUtil.parseCert(cert)).build();
			} catch (InvalidKeySpecException e) {
				throw new RuntimeException(e);
			}

		// Fallback certificate
		log.debug("Generating self-signed fallback certificate");
		SelfSignedCertificate ssc = new SelfSignedCertificate();
		return SslContextBuilder.forServer(ssc.key(), ssc.cert()).build();
	}

}
