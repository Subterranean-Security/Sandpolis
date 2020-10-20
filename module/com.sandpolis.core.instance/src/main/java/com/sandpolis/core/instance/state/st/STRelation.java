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
package com.sandpolis.core.instance.state.st;

import java.util.stream.Stream;

import com.sandpolis.core.instance.state.vst.VirtObject;

/**
 * {@link STRelation} is similar to a collection.
 *
 * @param <T>
 * @since 7.0.0
 */
public interface STRelation<T extends VirtObject> {

	public void add(T element);

	public Stream<T> stream();

	public int size();

	public boolean contains(T element);

}
