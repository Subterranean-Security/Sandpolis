//============================================================================//
//                                                                            //
//                Copyright © 2015 - 2020 Subterranean Security               //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation at:                                //
//                                                                            //
//    https://mozilla.org/MPL/2.0                                             //
//                                                                            //
//=========================================================S A N D P O L I S==//
package com.sandpolis.installer;

/**
 * Represents an installable Sandpolis component.
 *
 * @author cilki
 * @since 6.0.1
 */
public enum InstallComponent {

	/**
	 * com.sandpolis.server.vanilla
	 */
	SERVER_VANILLA("com.sandpolis:sandpolis-server-vanilla:"),

	/**
	 * com.sandpolis.viewer.lifegem
	 */
	VIEWER_LIFEGEM("com.sandpolis:sandpolis-viewer-lifegem:"),

	/**
	 * com.sandpolis.viewer.ascetic
	 */
	VIEWER_ASCETIC("com.sandpolis:sandpolis-viewer-ascetic:"),

	/**
	 * com.sandpolis.client.mega
	 */
	CLIENT_MEGA("com.sandpolis:sandpolis-client-mega:");

	public final String coordinate;
	public final String id;
	public final String fileBase;

	private InstallComponent(String coordinate) {
		this.coordinate = coordinate;
		this.id = "com." + coordinate.split(":")[1].replaceAll("-", ".");
		this.fileBase = coordinate.split(":")[1];
	}
}
