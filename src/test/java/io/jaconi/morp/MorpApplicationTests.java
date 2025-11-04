package io.jaconi.morp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;

import io.jaconi.morp.tenant.TenantObservationConvention;

@SpringBootTest
@DisabledInAotMode
class MorpApplicationTests {

	@Nested
	@TestPropertySource(properties = "morp.metrics.tenantdimension.enabled=false")
	class NoTenantMetricContributors {

		@Autowired
		private ApplicationContext appContext;

		@Test
		void test() {
			assertThrows(NoSuchBeanDefinitionException.class, () -> appContext.getBean(TenantObservationConvention.class));
		}
	}

	@Nested
	@TestPropertySource(properties = "morp.metrics.tenantdimension.enabled=true")
	class TenantMetricContributorsExist {

		@Autowired
		private ApplicationContext appContext;

		@Test
		void test() {
			assertThat(appContext.getBean(TenantObservationConvention.class)).isNotNull();
		}

	}
}
