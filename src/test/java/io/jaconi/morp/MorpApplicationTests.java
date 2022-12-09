package io.jaconi.morp;

import io.jaconi.morp.tenant.TenantGatewayTagsProvider;
import io.jaconi.morp.tenant.TenantWebFluxObservationConvention;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class MorpApplicationTests {

    @Nested
    @TestPropertySource(properties = "morp.metrics.tenantdimension.enabled=false")
    class NoTenantMetricContributors {

        @Autowired
        private ApplicationContext appContext;

        @Test
        void test() {
            assertThrows(NoSuchBeanDefinitionException.class, () -> appContext.getBean(TenantWebFluxObservationConvention.class));
            assertThrows(NoSuchBeanDefinitionException.class, () -> appContext.getBean(TenantGatewayTagsProvider.class));
        }
    }

    @Nested
    @TestPropertySource(properties = "morp.metrics.tenantdimension.enabled=true")
    class TenantMetricContributorsExist {

        @Autowired
        private ApplicationContext appContext;

        @Test
        void test() {
            assertThat(appContext.getBean(TenantWebFluxObservationConvention.class)).isNotNull();
            assertThat(appContext.getBean(TenantGatewayTagsProvider.class)).isNotNull();
        }

    }
}
