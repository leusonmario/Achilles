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
package info.archinnov.achilles.entity.operations.impl;

import static com.google.common.collect.Collections2.*;
import static info.archinnov.achilles.entity.metadata.PropertyType.*;
import info.archinnov.achilles.context.CQLPersistenceContext;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.type.CounterBuilder.CounterImpl;
import info.archinnov.achilles.validation.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CQLPersisterImpl {

	public void persist(CQLPersistenceContext context) {
		context.pushInsertStatement();
	}

	public void persistClusteredCounter(CQLPersistenceContext context) {
		Object entity = context.getEntity();
		PropertyMeta counterMeta = context.getFirstMeta();
		Object counter = counterMeta.getValueFromField(entity);
		if (counter != null) {
			Validator.validateTrue(CounterImpl.class.isAssignableFrom(counter.getClass()),
					"Counter property '%s' value from entity class '%s'  should be of type '%s'",
					counterMeta.getPropertyName(), counterMeta.getEntityClassName(),
					CounterImpl.class.getCanonicalName());
			CounterImpl counterValue = (CounterImpl) counter;
			context.pushClusteredCounterIncrementStatement(counterMeta, counterValue.get());
		} else {
			throw new IllegalStateException("Cannot insert clustered counter entity '" + entity
					+ "' with null clustered counter value");
		}

	}

	public void persistCounters(CQLPersistenceContext context, Set<PropertyMeta> counterMetas) {
		Object entity = context.getEntity();
		for (PropertyMeta counterMeta : counterMetas) {
			Object counter = counterMeta.getValueFromField(entity);
			if (counter != null) {
				Validator.validateTrue(CounterImpl.class.isAssignableFrom(counter.getClass()),
						"Counter property '%s' value from entity class '%s'  should be of type '%s'",
						counterMeta.getPropertyName(), counterMeta.getEntityClassName(),
						CounterImpl.class.getCanonicalName());
				CounterImpl counterValue = (CounterImpl) counter;
				context.bindForSimpleCounterIncrement(counterMeta, counterValue.get());
			}
		}
	}

	public void remove(CQLPersistenceContext context) {
		EntityMeta entityMeta = context.getEntityMeta();
		if (entityMeta.isClusteredCounter()) {
			context.bindForClusteredCounterRemoval(entityMeta.getFirstMeta());
		} else {
			context.bindForRemoval(entityMeta.getTableName());
			removeLinkedCounters(context);
		}
	}

	protected void removeLinkedCounters(CQLPersistenceContext context) {
		EntityMeta entityMeta = context.getEntityMeta();

		List<PropertyMeta> allMetas = entityMeta.getAllMetasExceptIdMeta();
		Collection<PropertyMeta> proxyMetas = filter(allMetas, counterType);
		for (PropertyMeta pm : proxyMetas) {
			context.bindForSimpleCounterRemoval(pm);
		}
	}
}
