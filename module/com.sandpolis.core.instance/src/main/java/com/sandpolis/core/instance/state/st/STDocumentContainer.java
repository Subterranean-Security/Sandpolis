package com.sandpolis.core.instance.state.st;

import static com.sandpolis.core.foundation.util.OidUtil.OTYPE_COLLECTION;
import static com.sandpolis.core.foundation.util.OidUtil.OTYPE_DOCUMENT;

import java.util.Collection;
import java.util.function.Consumer;

import com.sandpolis.core.foundation.util.OidUtil;
import com.sandpolis.core.instance.state.oid.GenericOidException;
import com.sandpolis.core.instance.state.oid.RelativeOid;

public interface STDocumentContainer {

	/**
	 * Get a subdocument by its tag. This method never returns {@code null}.
	 *
	 * @param tag The subdocument tag
	 * @return The subdocument associated with the tag
	 */
	public STDocument document(long tag);

	public default STDocument document(RelativeOid oid) {
		if (!oid.isConcrete())
			throw new GenericOidException(oid);

		if (oid.size() == 1) {
			switch (OidUtil.getOidType(oid.first())) {
			case OTYPE_DOCUMENT:
				return document(oid.first());
			}
			throw new IllegalArgumentException("Unacceptable document tag: " + oid.first());
		} else {
			switch (OidUtil.getOidType(oid.first())) {
			case OTYPE_DOCUMENT:
				return document(oid.first()).document(oid.tail());
			case OTYPE_COLLECTION:
				if (this instanceof STCollectionContainer) {
					return ((STCollectionContainer) this).collection(oid.first()).document(oid.tail());
				}
				break;
			}
			throw new IllegalArgumentException("OID: " + oid);
		}
	}

	/**
	 * Get all subdocuments.
	 *
	 * @return A collection of all subdocuments
	 */
	public Collection<STDocument> documents();

	public void forEachDocument(Consumer<STDocument> consumer);

	public default STDocument get(RelativeOid.STDocumentOid oid) {
		return document(oid);
	}

	/**
	 * Get a subdocument by its tag. This method returns {@code null} if the
	 * subdocument doesn't exist.
	 *
	 * @param tag The subdocument tag
	 * @return The subdocument associated with the tag or {@code null}
	 */
	public STDocument getDocument(long tag);

	public void remove(STDocument document);
}
