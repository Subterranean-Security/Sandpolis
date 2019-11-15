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
package com.sandpolis.plugin.shell.client.mega.stream;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import com.sandpolis.core.stream.store.StreamSink;
import com.sandpolis.plugin.shell.net.MsgShell.EV_ShellStream;

public class ShellStreamSink extends StreamSink<EV_ShellStream> {

	private Process process;

	public ShellStreamSink(Process process) {
		checkArgument(process.isAlive());
		this.process = process;
	}

	@Override
	public void onNext(EV_ShellStream item) {
		try {
			process.getOutputStream().write(item.getData().toByteArray());
			process.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
