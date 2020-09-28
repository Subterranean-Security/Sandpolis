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
package com.sandpolis.core.server.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import com.sandpolis.core.instance.State.ProtoDocument;
import com.sandpolis.core.instance.state.AbstractSTDocument;
import com.sandpolis.core.instance.state.STAttribute;
import com.sandpolis.core.instance.state.STCollection;
import com.sandpolis.core.instance.state.STDocument;

/**
 * {@link HibernateDocument} allows documents to be persistent.
 *
 * @since 5.1.1
 */
@Entity
public class HibernateDocument extends AbstractSTDocument implements STDocument {

	@Id
	private String db_id;

	@Column
	public long getTag() {
		return tag;
	}

	@Column(nullable = true)
	private HibernateDocument parentDocument;

	@Column(nullable = true)
	private HibernateCollection parentCollection;

	@MapKeyColumn
	@OneToMany(cascade = CascadeType.ALL)
	private Map<Integer, HibernateDocument> documents;

	@MapKeyColumn
	@OneToMany(cascade = CascadeType.ALL)
	private Map<Integer, HibernateCollection> collections;

	@MapKeyColumn
	@OneToMany(cascade = CascadeType.ALL)
	private Map<Integer, HibernateAttribute<?>> attributes;

	public HibernateDocument(HibernateDocument parent) {
		this.parentDocument = parent;
		this.db_id = UUID.randomUUID().toString();

		documents = new HashMap<>();
		collections = new HashMap<>();
		attributes = new HashMap<>();
	}

	public HibernateDocument(HibernateDocument parent, ProtoDocument document) {
		this(parent);
		merge(document);
	}

	public HibernateDocument(HibernateCollection parent) {
		this.parentCollection = parent;
		this.db_id = UUID.randomUUID().toString();

		documents = new HashMap<>();
		collections = new HashMap<>();
		attributes = new HashMap<>();
	}

	public HibernateDocument(HibernateCollection parent, ProtoDocument document) {
		this(parent);
		merge(document);
	}

	protected HibernateDocument() {
		// JPA CONSTRUCTOR
	}

	@Override
	public STAttribute<?> newAttribute() {
		return new HibernateAttribute<>(this);
	}

	@Override
	public STDocument newDocument() {
		return new HibernateDocument(this);
	}

	@Override
	public STCollection newCollection() {
		return new HibernateCollection(this);
	}

}
