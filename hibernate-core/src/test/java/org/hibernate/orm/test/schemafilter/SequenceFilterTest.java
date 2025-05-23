/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.schemafilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.internal.DefaultSchemaFilter;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.SchemaDropperImpl;
import org.hibernate.tool.schema.spi.SchemaFilter;

import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.ServiceRegistryBuilder;
import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Andrea Boriero
 */
@JiraKey(value = "HHH-10937")
@RequiresDialect(H2Dialect.class)
public class SequenceFilterTest extends BaseUnitTestCase {
	private StandardServiceRegistryImpl serviceRegistry;
	private Metadata metadata;

	@Before
	public void setUp() {
		Map settings = new HashMap();
		settings.putAll( Environment.getProperties() );
		settings.put( AvailableSettings.DIALECT, H2Dialect.class.getName() );
		settings.put( "hibernate.temp.use_jdbc_metadata_defaults", "false" );
		settings.put( AvailableSettings.FORMAT_SQL, false );

		this.serviceRegistry = ServiceRegistryBuilder.buildServiceRegistry( settings );

		MetadataSources ms = new MetadataSources( serviceRegistry );
		ms.addAnnotatedClass( Schema1Entity1.class );
		ms.addAnnotatedClass( Schema2Entity2.class );
		this.metadata = ms.buildMetadata();
	}

	@After
	public void tearDown() {
		serviceRegistry.destroy();
	}

	@Test
	public void createSchema_unfiltered() {
		RecordingTarget target = doCreation( new DefaultSchemaFilter() );

		Assert.assertThat( target.getActions( RecordingTarget.Category.SEQUENCE_CREATE ), containsExactly(
				"entity_1_seq_gen",
				"entity_2_seq_gen"
		) );
	}

	@Test
	public void createSchema_filtered() {
		RecordingTarget target = doCreation( new TestSchemaFilter() );

		Assert.assertThat( target.getActions( RecordingTarget.Category.SEQUENCE_CREATE ), containsExactly(
				"entity_1_seq_gen"
		) );
	}

	@Test
	public void dropSchema_unfiltered() {
		RecordingTarget target = doDrop( new DefaultSchemaFilter() );

		Assert.assertThat( target.getActions( RecordingTarget.Category.SEQUENCE_DROP ), containsExactly(
				"entity_1_seq_gen",
				"entity_2_seq_gen"
		) );
	}

	@Test
	public void dropSchema_filtered() {
		RecordingTarget target = doDrop( new TestSchemaFilter() );

		Assert.assertThat(
				target.getActions( RecordingTarget.Category.SEQUENCE_DROP ),
				containsExactly( "entity_1_seq_gen" )
		);
	}

	@Entity
	@SequenceGenerator(initialValue = 1, name = "idgen", sequenceName = "entity_1_seq_gen")
	@jakarta.persistence.Table(name = "the_entity_1", schema = "the_schema_1")
	public static class Schema1Entity1 {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgen")
		private long id;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}
	}

	@Entity
	@SequenceGenerator(initialValue = 1, name = "idgen2", sequenceName = "entity_2_seq_gen")
	@jakarta.persistence.Table(name = "the_entity_2", schema = "the_schema_2")
	public static class Schema2Entity2 {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgen2")
		private long id;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}
	}

	private static class TestSchemaFilter implements SchemaFilter {

		@Override
		public boolean includeNamespace(Namespace namespace) {
			return true;
		}

		@Override
		public boolean includeTable(Table table) {
			return true;
		}

		@Override
		public boolean includeSequence(Sequence sequence) {
			final String render = sequence.getName().render();
			return !"entity_2_seq_gen".endsWith( sequence.getName().render() );
		}
	}

	private RecordingTarget doCreation(SchemaFilter filter) {
		RecordingTarget target = new RecordingTarget();
		new SchemaCreatorImpl( serviceRegistry, filter ).doCreation( metadata, true, target );
		return target;
	}

	private RecordingTarget doDrop(SchemaFilter filter) {
		RecordingTarget target = new RecordingTarget();
		new SchemaDropperImpl( serviceRegistry, filter ).doDrop( metadata, true, target );
		return target;
	}

	private BaseMatcher<Set<String>> containsExactly(Object... expected) {
		return containsExactly( new HashSet( Arrays.asList( expected ) ) );
	}

	private BaseMatcher<Set<String>> containsExactly(final Set expected) {
		return new BaseMatcher<Set<String>>() {
			@Override
			public boolean matches(Object item) {
				Set set = (Set) item;
				return set.size() == expected.size()
						&& set.containsAll( expected );
			}

			@Override
			public void describeTo(Description description) {
				description.appendText( "Is set containing exactly " + expected );
			}
		};
	}

}
