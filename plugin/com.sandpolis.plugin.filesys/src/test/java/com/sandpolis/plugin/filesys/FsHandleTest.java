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
package com.sandpolis.plugin.filesys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sandpolis.core.proto.net.MCFsHandle.FileListlet;
import com.sandpolis.core.proto.net.MCFsHandle.FileListlet.UpdateType;

class FsHandleTest {

	@Test
	@DisplayName("Check that the handle can descend into directories")
	void down_1(@TempDir Path temp) throws IOException {
		Files.createDirectories(temp.resolve("test1/test2/test3"));

		try (FsHandle fs = new FsHandle(temp.toString())) {
			assertTrue(fs.down("test1"));
			assertTrue(fs.down("test2"));
			assertTrue(fs.down("test3"));
		}
	}

	@Test
	@DisplayName("Check that the handle cannot descend into files")
	void down_2(@TempDir Path temp) throws IOException {
		Files.createFile(temp.resolve("test.txt"));

		try (FsHandle fs = new FsHandle(temp.toString())) {
			assertFalse(fs.down("test.txt"));
		}
	}

	@Test
	@DisplayName("Check that the handle can move out of directories")
	void up_1(@TempDir Path temp) throws IOException {
		Files.createDirectories(temp.resolve("test1/test2/test3"));

		try (FsHandle fs = new FsHandle(temp.resolve("test1/test2/test3").toString())) {
			assertTrue(fs.up());
			assertTrue(fs.up());
			assertTrue(fs.up());
			assertEquals(temp.toString(), fs.pwd());
		}
	}

	@Test
	@DisplayName("Check that the handle cannot move higher than the root")
	void up_2() {
		try (FsHandle fs = new FsHandle("/")) {
			assertFalse(fs.up());
			assertFalse(fs.up());
			assertFalse(fs.up());
			assertEquals(Paths.get("/").toString(), fs.pwd());
		}
	}

	@Test
	@DisplayName("Check that the handle lists directory contents")
	void list_1(@TempDir Path temp) throws IOException {
		Files.createDirectory(temp.resolve("test1"));
		Files.createFile(temp.resolve("test1.txt"));

		try (FsHandle fs = new FsHandle(temp.toString())) {

			assertTrue(fs.list().stream().anyMatch(listlet -> {
				return "test1".equals(listlet.getName()) && listlet.getDirectory() == true;
			}));

			assertTrue(fs.list().stream().anyMatch(listlet -> {
				return "test1.txt".equals(listlet.getName()) && listlet.getDirectory() == false;
			}));

			assertEquals(2, fs.list().size());
		}
	}

	@Test
	@DisplayName("Check that the add event listener is notified")
	void add_callback_1(@TempDir Path temp) throws IOException, InterruptedException {
		BlockingQueue<FileListlet> eventQueue = new ArrayBlockingQueue<>(5);

		try (FsHandle fs = new FsHandle(temp.toString())) {
			fs.addUpdateCallback(ev -> {
				ev.getListingList().stream().forEachOrdered(eventQueue::add);
			});

			// Add a file
			Files.createFile(temp.resolve("test.txt"));

			FileListlet fileCreated = eventQueue.poll(1000, TimeUnit.MILLISECONDS);
			assertEquals("test.txt", fileCreated.getName());
			assertEquals(UpdateType.ENTRY_CREATE, fileCreated.getUpdateType());
			assertEquals(0, eventQueue.size());
		}
	}

	@Test
	@DisplayName("Check that the delete event listener is notified")
	void delete_callback_1(@TempDir Path temp) throws IOException, InterruptedException {
		BlockingQueue<FileListlet> eventQueue = new ArrayBlockingQueue<>(5);
		Files.createFile(temp.resolve("test.txt"));

		try (FsHandle fs = new FsHandle(temp.toString())) {
			fs.addUpdateCallback(ev -> {
				ev.getListingList().stream().forEachOrdered(eventQueue::add);
			});

			// Delete a file
			Files.delete(temp.resolve("test.txt"));

			FileListlet fileDeleted = eventQueue.poll(1000, TimeUnit.MILLISECONDS);
			assertEquals("test.txt", fileDeleted.getName());
			assertEquals(UpdateType.ENTRY_DELETE, fileDeleted.getUpdateType());
			assertEquals(0, eventQueue.size());
		}
	}

	@Test
	@DisplayName("Check that the modify event listener is notified")
	void modify_callback_1(@TempDir Path temp) throws IOException, InterruptedException {
		BlockingQueue<FileListlet> eventQueue = new ArrayBlockingQueue<>(5);
		Files.createFile(temp.resolve("test.txt"));

		try (FsHandle fs = new FsHandle(temp.toString())) {
			fs.addUpdateCallback(ev -> {
				ev.getListingList().stream().forEachOrdered(eventQueue::add);
			});

			// Modify file
			try (PrintWriter pw = new PrintWriter(temp.resolve("test.txt").toFile())) {
				pw.println("1234");
			}

			FileListlet fileModified = eventQueue.poll(1000, TimeUnit.MILLISECONDS);
			assertEquals("test.txt", fileModified.getName());
			assertEquals(UpdateType.ENTRY_MODIFY, fileModified.getUpdateType());
			assertEquals(0, eventQueue.size());
		}
	}
}