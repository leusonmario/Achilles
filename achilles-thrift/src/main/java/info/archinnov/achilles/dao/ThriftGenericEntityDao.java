/**
 *
 * Copyright (C) 2012-2013 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.archinnov.achilles.dao;

import static info.archinnov.achilles.serializer.ThriftSerializerUtils.*;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;

import org.apache.cassandra.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftGenericEntityDao extends ThriftAbstractDao {
	private static final Logger log = LoggerFactory.getLogger(ThriftGenericEntityDao.class);

	protected static final byte[] START_EAGER = new byte[] { 0 };
	protected static final byte[] END_EAGER = new byte[] { 20 };

	private Composite startCompositeForEagerFetch;
	private Composite endCompositeForEagerFetch;

	protected ThriftGenericEntityDao() {
		this.initComposites();
	}

	protected <K, V> ThriftGenericEntityDao(Pair<K, V> rowkeyAndValueClasses) {
		this.initComposites();
		super.rowkeyAndValueClasses = rowkeyAndValueClasses;
	}

	public <K, V> ThriftGenericEntityDao(Cluster cluster, Keyspace keyspace, String cf,
			AchillesConsistencyLevelPolicy consistencyPolicy, Pair<K, V> rowkeyAndValueClasses) {
		super(cluster, keyspace, cf, consistencyPolicy, rowkeyAndValueClasses);
		this.initComposites();
		columnNameSerializer = COMPOSITE_SRZ;
		log.debug(
				"Initializing GenericEntityDao for key serializer '{}', composite comparator and value serializer '{}'",
				this.rowSrz().getComparatorType().getTypeName(), STRING_SRZ.getComparatorType().getTypeName());

	}

	public <K> List<Pair<Composite, String>> eagerFetchEntity(K key) {
		log.trace("Eager fetching properties for column family {} ", columnFamily);

		return this.findColumnsRange(key, startCompositeForEagerFetch, endCompositeForEagerFetch, false,
				Integer.MAX_VALUE);
	}

	public <K> Map<K, List<Pair<Composite, String>>> eagerFetchEntities(List<K> keys) {
		log.trace("Eager fetching properties for multiple entities in column family {} ", columnFamily);

		Map<K, List<Pair<Composite, String>>> map = new HashMap<K, List<Pair<Composite, String>>>();

		Rows<K, Composite, String> rows = this.multiGetSliceRange(keys, startCompositeForEagerFetch,
				endCompositeForEagerFetch, false, Integer.MAX_VALUE);

		for (Row<K, Composite, String> row : rows) {
			List<Pair<Composite, String>> columns = new ArrayList<Pair<Composite, String>>();
			for (HColumn<Composite, String> column : row.getColumnSlice().getColumns()) {
				columns.add(Pair.create(column.getName(), column.getValue()));
			}

			map.put(row.getKey(), columns);
		}

		return map;
	}

	private void initComposites() {
		startCompositeForEagerFetch = new Composite();
		startCompositeForEagerFetch.addComponent(0, START_EAGER, ComponentEquality.EQUAL);

		endCompositeForEagerFetch = new Composite();
		endCompositeForEagerFetch.addComponent(0, END_EAGER, ComponentEquality.GREATER_THAN_EQUAL);
	}
}
