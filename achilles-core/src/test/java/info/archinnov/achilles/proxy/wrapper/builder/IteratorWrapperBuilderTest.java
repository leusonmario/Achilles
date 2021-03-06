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
package info.archinnov.achilles.proxy.wrapper.builder;

import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.context.PersistenceContext;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.operations.EntityProxifier;
import info.archinnov.achilles.proxy.wrapper.IteratorWrapper;
import info.archinnov.achilles.test.mapping.entity.CompleteBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IteratorWrapperBuilderTest {
	@Mock
	private Map<Method, PropertyMeta> dirtyMap;

	private Method setter;

	@Mock
	private EntityProxifier<PersistenceContext> proxifier;

	@Mock
	private PropertyMeta propertyMeta;

	@Mock
	private PersistenceContext context;

	@Before
	public void setUp() throws Exception {
		setter = CompleteBean.class.getDeclaredMethod("setFriends", List.class);
	}

	@Test
	public void should_build() throws Exception {
		List<Object> target = new ArrayList<Object>();
		target.add("a");

		Iterator<Object> iterator = target.iterator();
		IteratorWrapper wrapper = IteratorWrapperBuilder
				//
				.builder(context, iterator).dirtyMap(dirtyMap).setter(setter).propertyMeta(propertyMeta)
				.proxifier(proxifier).build();

		assertThat(Whitebox.getInternalState(wrapper, "target")).isSameAs(iterator);
		assertThat(wrapper.getDirtyMap()).isSameAs(dirtyMap);
		assertThat(Whitebox.getInternalState(wrapper, "setter")).isSameAs(setter);
		assertThat(Whitebox.getInternalState(wrapper, "propertyMeta")).isSameAs(propertyMeta);
		assertThat(Whitebox.getInternalState(wrapper, "proxifier")).isSameAs(proxifier);
		assertThat(Whitebox.getInternalState(wrapper, "context")).isSameAs(context);

	}
}
