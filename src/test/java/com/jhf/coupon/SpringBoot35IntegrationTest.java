package com.jhf.coupon;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests specific to Spring Boot 3.5.9 upgrade.
 *
 * Verifies:
 * - Spring Boot and Spring Framework versions
 * - Auto-configuration works correctly
 * - HikariCP configuration
 * - Actuator endpoints
 * - Micrometer metrics integration
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.test.context.cache.maxSize=1"
})
class SpringBoot35IntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("Spring Boot version should be 3.5.9")
    void testSpringBootVersion() {
        String version = SpringBootVersion.getVersion();
        assertThat(version).isNotNull();
        assertThat(version).startsWith("3.5.9");
    }

    @Test
    @DisplayName("Spring Framework version should be 6.2.x (compatible with Spring Boot 3.5.9)")
    void testSpringFrameworkVersion() {
        String version = SpringVersion.getVersion();
        assertThat(version).isNotNull();
        assertThat(version).startsWith("6.2");
    }

    @Test
    @DisplayName("Application context should be loaded successfully")
    void testApplicationContextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("DataSource should be auto-configured")
    void testDataSourceAutoConfiguration() {
        assertThat(dataSource).isNotNull();
        assertThat(dataSource.getClass().getName()).contains("HikariDataSource");
    }

    @Test
    @DisplayName("HikariCP configuration should be loaded")
    void testHikariConfiguration() throws Exception {
        assertThat(dataSource).isNotNull();

        // Verify HikariCP is configured
        String dataSourceClass = dataSource.getClass().getName();
        assertThat(dataSourceClass).contains("Hikari");

        // Verify connection pool properties
        assertThat(dataSourceProperties.getUrl()).isNotNull();
        assertThat(dataSourceProperties.getUsername()).isNotNull();
    }

    @Test
    @DisplayName("Health actuator components should be available")
    void testHealthEndpointAvailable() {
        // In test context, health endpoint may not be fully configured
        // Check that actuator dependencies are on classpath
        assertThat(isClassPresent("org.springframework.boot.actuate.health.Health"))
                .withFailMessage("Spring Boot Actuator health classes should be available")
                .isTrue();

        // If HealthEndpoint is autowired, verify it works
        if (healthEndpoint != null) {
            var health = healthEndpoint.health();
            assertThat(health).isNotNull();
            assertThat(health.getStatus().getCode()).isIn("UP", "UNKNOWN");
        }
    }

    @Test
    @DisplayName("Micrometer MeterRegistry should be configured")
    void testMicrometerMetricsAvailable() {
        assertThat(meterRegistry).isNotNull();
        assertThat(meterRegistry.getMeters()).isNotEmpty();
    }

    @Test
    @DisplayName("HikariCP metrics should be registered in Micrometer")
    void testHikariCPMetricsRegistered() {
        assertThat(meterRegistry).isNotNull();

        // Check for HikariCP metrics
        boolean hasHikariMetrics = meterRegistry.getMeters().stream()
                .anyMatch(meter -> meter.getId().getName().startsWith("hikaricp"));

        assertThat(hasHikariMetrics)
                .withFailMessage("HikariCP metrics should be registered in MeterRegistry")
                .isTrue();
    }

    @Test
    @DisplayName("JVM metrics should be registered in Micrometer")
    void testJvmMetricsRegistered() {
        assertThat(meterRegistry).isNotNull();

        // Check for JVM metrics
        boolean hasJvmMetrics = meterRegistry.getMeters().stream()
                .anyMatch(meter -> meter.getId().getName().startsWith("jvm"));

        assertThat(hasJvmMetrics)
                .withFailMessage("JVM metrics should be registered in MeterRegistry")
                .isTrue();
    }

    @Test
    @DisplayName("Custom application metrics should be registered")
    void testCustomApplicationMetricsRegistered() {
        assertThat(meterRegistry).isNotNull();

        // Check for custom coupon system metrics
        boolean hasCustomMetrics = meterRegistry.getMeters().stream()
                .anyMatch(meter -> meter.getId().getName().startsWith("coupon_system"));

        assertThat(hasCustomMetrics)
                .withFailMessage("Custom application metrics should be registered")
                .isTrue();
    }

    @Test
    @DisplayName("Required Spring Boot starters should be on classpath")
    void testRequiredStartersOnClasspath() {
        // Verify key classes from required starters are available
        assertThat(isClassPresent("org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"))
                .withFailMessage("spring-boot-starter-web should be on classpath")
                .isTrue();

        assertThat(isClassPresent("org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"))
                .withFailMessage("spring-boot-starter-data-jdbc should be on classpath")
                .isTrue();

        assertThat(isClassPresent("org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"))
                .withFailMessage("spring-boot-starter-security should be on classpath")
                .isTrue();

        assertThat(isClassPresent("org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration"))
                .withFailMessage("spring-boot-starter-actuator should be on classpath")
                .isTrue();

        assertThat(isClassPresent("io.micrometer.core.instrument.MeterRegistry"))
                .withFailMessage("micrometer-core should be on classpath")
                .isTrue();
    }

    @Test
    @DisplayName("Spring Boot 3.5.9 specific auto-configurations should work")
    void testSpringBoot35AutoConfigurations() {
        // Verify key auto-configured beans exist
        assertThat(applicationContext.containsBean("dataSource")).isTrue();
        assertThat(applicationContext.containsBean("entityManagerFactory")).isFalse(); // We use JDBC, not JPA

        // Verify MeterRegistry is configured (bean name may vary)
        boolean hasMeterRegistry = applicationContext.containsBean("meterRegistry")
                || applicationContext.containsBean("simpleMeterRegistry")
                || applicationContext.getBeansOfType(MeterRegistry.class).size() > 0;
        assertThat(hasMeterRegistry).isTrue();
    }

    @Test
    @DisplayName("Database connection should be healthy")
    void testDatabaseConnection() throws Exception {
        assertThat(dataSource).isNotNull();

        // Test actual database connection
        try (var connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isClosed()).isFalse();
            assertThat(connection.isValid(5)).isTrue();
        }
    }

    @Test
    @DisplayName("Spring Boot DevTools should not be active in tests")
    void testDevToolsNotActive() {
        // DevTools should not be active in test context
        boolean hasDevTools = applicationContext.containsBean("restartingClassPathChangeChangedEventListener");
        assertThat(hasDevTools).isFalse();
    }

    @Test
    @DisplayName("Test property source should override defaults")
    void testPropertyOverrides() {
        // Verify test properties work
        Integer cacheMaxSize = applicationContext.getEnvironment()
                .getProperty("spring.test.context.cache.maxSize", Integer.class);

        assertThat(cacheMaxSize).isEqualTo(1);
    }

    // Helper method
    private boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
