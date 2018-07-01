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
package com.sandpolis.core.net.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.sandpolis.core.proto.net.MSG.Message;
import com.sandpolis.core.util.RandUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class PeerPipelineInitTest {

	private final PeerPipelineInit init = new PeerPipelineInit(null);

	@Test
	public void testNioUdp() throws InterruptedException {
		Channel peer1 = new Bootstrap().group(new NioEventLoopGroup()).channel(NioDatagramChannel.class).handler(init)
				.bind(9000).sync().channel();

		Channel peer2 = new Bootstrap().group(new NioEventLoopGroup()).channel(NioDatagramChannel.class).handler(init)
				.connect("127.0.0.1", 9000).sync().channel();

		exchange(peer1, peer2);
		exchange(peer2, peer1);
	}

	@Test
	public void testNioTcp() throws InterruptedException {
		Channel server = new ServerBootstrap().group(new NioEventLoopGroup()).channel(NioServerSocketChannel.class)
				.childHandler(init).bind(9000).sync().channel();

		Channel client = new Bootstrap().group(new NioEventLoopGroup()).channel(NioSocketChannel.class).handler(init)
				.connect("127.0.0.1", 9000).sync().channel();

		exchange(server, client);
		exchange(client, server);
	}

	/**
	 * Exchange a random message between two peers and check for validity.
	 */
	private void exchange(Channel peer1, Channel peer2) {
		Message rand1 = Message.newBuilder().setId(RandUtil.nextInt()).build();
		Message rand2 = Message.newBuilder().setId(RandUtil.nextInt()).build();

		peer1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				assertEquals(rand2, msg);
				ctx.pipeline().remove(this);
			}
		});
		peer2.pipeline().addLast(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				assertEquals(rand1, msg);
				ctx.pipeline().remove(this);
			}
		});

		peer1.writeAndFlush(rand1);
		peer2.writeAndFlush(rand2);

		assertNull(peer1.pipeline().get("peer1"));
		assertNull(peer2.pipeline().get("peer2"));

	}

}