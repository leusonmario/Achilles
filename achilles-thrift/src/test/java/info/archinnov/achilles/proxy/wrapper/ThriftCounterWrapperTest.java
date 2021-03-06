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
package info.archinnov.achilles.proxy.wrapper;

import static info.archinnov.achilles.type.ConsistencyLevel.EACH_QUORUM;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import info.archinnov.achilles.context.ThriftPersistenceContext;
import info.archinnov.achilles.context.execution.SafeExecutionContext;
import info.archinnov.achilles.dao.ThriftAbstractDao;
import info.archinnov.achilles.test.mapping.entity.CompleteBean;
import info.archinnov.achilles.type.ConsistencyLevel;
import me.prettyprint.hector.api.beans.Composite;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

@RunWith(MockitoJUnitRunner.class)
public class ThriftCounterWrapperTest {
	@InjectMocks
	private ThriftCounterWrapper wrapper;

	private Long key = RandomUtils.nextLong();

	@Mock
	private Composite columnName;

	@Mock
	private ThriftAbstractDao counterDao;

	@Mock
	private ThriftPersistenceContext context;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<Void>> voidExecCaptor;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<Long>> longExecCaptor;

	@Mock
	private SafeExecutionContext<Long> execContext;

	private ConsistencyLevel consistencyLevel = EACH_QUORUM;

	@Before
	public void setUp() {
		wrapper = new ThriftCounterWrapper(context);
		Whitebox.setInternalState(wrapper, "key", key);
		wrapper.setColumnName(columnName);
		wrapper.setCounterDao(counterDao);
		wrapper.setConsistencyLevel(consistencyLevel);
		when((Class<CompleteBean>) context.getEntityClass()).thenReturn(CompleteBean.class);
	}

	@Test
	public void should_get_counter() throws Exception {
		when(counterDao.getCounterValue(key, columnName)).thenReturn(10L);
		when(context.executeWithReadConsistencyLevel(longExecCaptor.capture(), eq(consistencyLevel))).thenReturn(10L);
		Long value = wrapper.get();

		assertThat(value).isEqualTo(10L);
		assertThat(longExecCaptor.getValue().execute()).isEqualTo(10L);
	}

	@Test
	public void should_get_counter_with_consistency_level() throws Exception {
		when(counterDao.getCounterValue(key, columnName)).thenReturn(10L);
		when(context.executeWithReadConsistencyLevel(longExecCaptor.capture(), eq(EACH_QUORUM))).thenReturn(10L);
		Long value = wrapper.get(EACH_QUORUM);

		assertThat(value).isEqualTo(10L);
		assertThat(longExecCaptor.getValue().execute()).isEqualTo(10L);
	}

	@Test
	public void should_incr() throws Exception {
		wrapper.incr();

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(consistencyLevel));
		voidExecCaptor.getValue().execute();

		verify(counterDao).incrementCounter(key, columnName, 1L);

	}

	@Test
	public void should_incr_with_consistency() throws Exception {
		wrapper.incr(EACH_QUORUM);

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(EACH_QUORUM));
		voidExecCaptor.getValue().execute();

		verify(counterDao).incrementCounter(key, columnName, 1L);
	}

	@Test
	public void should_incr_with_value() throws Exception {
		wrapper.incr(10L);

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(consistencyLevel));
		voidExecCaptor.getValue().execute();

		verify(counterDao).incrementCounter(key, columnName, 10L);
	}

	@Test
	public void should_incr_with_value_and_consistency() throws Exception {
		wrapper.incr(10L, EACH_QUORUM);
		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(EACH_QUORUM));
		voidExecCaptor.getValue().execute();

		verify(counterDao).incrementCounter(key, columnName, 10L);

	}

	@Test
	public void should_decr() throws Exception {
		wrapper.decr();

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(consistencyLevel));
		voidExecCaptor.getValue().execute();

		verify(counterDao).decrementCounter(key, columnName, 1L);
	}

	@Test
	public void should_decr_with_consistency() throws Exception {
		wrapper.decr(EACH_QUORUM);

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(EACH_QUORUM));
		voidExecCaptor.getValue().execute();

		verify(counterDao).decrementCounter(key, columnName, 1L);
	}

	@Test
	public void should_decr_with_value() throws Exception {
		wrapper.decr(10L);

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(consistencyLevel));
		voidExecCaptor.getValue().execute();

		verify(counterDao).decrementCounter(key, columnName, 10L);
	}

	@Test
	public void should_decr_with_value_and_consistency() throws Exception {
		wrapper.decr(10L, EACH_QUORUM);

		verify(context).executeWithWriteConsistencyLevel(voidExecCaptor.capture(), eq(EACH_QUORUM));
		voidExecCaptor.getValue().execute();

		verify(counterDao).decrementCounter(key, columnName, 10L);
	}
}
