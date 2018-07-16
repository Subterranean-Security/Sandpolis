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
package com.sandpolis.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sandpolis.core.proto.util.Platform.Instance;
import com.sandpolis.core.util.IDUtil.CVID;

public class IDUtilTest {

	@Test
	public void testCvidGenerator() {
		for (Instance instance : Instance.values()) {
			if (instance == Instance.UNRECOGNIZED)
				break;

			for (int i = 0; i < 1000; i++) {
				int cvid = CVID.cvid(instance);
				assertNotEquals(0, cvid);
				assertEquals(instance, CVID.extractInstance(cvid));
			}
		}
	}

	@Test
	public void testUuidGenerator() {
		String uuid = IDUtil.UUID.getUUID();

		assertNotNull(uuid);
		for (int i = 0; i < 100; i++) {
			assertEquals(uuid, IDUtil.UUID.getUUID());
		}
	}

	/**
	 * Maximum message ID.
	 */
	private static final int MSG_MAX = 64;

	@Test
	public void testMsgGenerator() {
		for (int i = 0; i < 10000; i++) {
			assertTrue(IDUtil.msg() <= MSG_MAX);
		}
	}

}