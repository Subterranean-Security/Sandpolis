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
package com.sandpolis.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.UnknownTaskException

/**
 * This plugin packages a Sandpolis plugin into an installable archive.
 *
 * @author cilki
 */
public class PluginPackager implements Plugin<Project> {

	void apply(Project project) {
	
    	project.subprojects {
    		afterEvaluate {
    			try {
					project.tasks.getByName('jar')
						.from(tasks.getByName('jar').outputs.files.getFiles()[0].getParent())
    			} catch (UnknownTaskException e) {
    				// Skip
    			}
    		}
    	}
	}
}