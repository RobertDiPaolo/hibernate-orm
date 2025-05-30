/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.event.collection.association.bidirectional.manytomany;
import java.util.Collection;
import java.util.HashSet;

import org.hibernate.orm.test.event.collection.ParentWithCollection;
import org.hibernate.orm.test.event.collection.association.AbstractAssociationCollectionEventTest;

/**
 *
 * @author Gail Badner
 */
public class BidirectionalManyToManySetToSetCollectionEventTest extends AbstractAssociationCollectionEventTest {
	@Override
	public String[] getMappings() {
		return new String[] { "event/collection/association/bidirectional/manytomany/BidirectionalManyToManySetToSetMapping.hbm.xml" };
	}

	@Override
	public ParentWithCollection createParent(String name) {
		return new ParentWithBidirectionalManyToMany( name );
	}

	@Override
	public Collection createCollection() {
		return new HashSet();
	}
}
