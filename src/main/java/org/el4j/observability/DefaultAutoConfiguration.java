package org.el4j.observability;

import org.el4j.observability.property.DefaultLoggingProperties;
import org.el4j.observability.property.ExtraLoggingProperties;
import org.el4j.observability.property.MetricsProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configures properties and create a property converter.
 */
@PropertySource({"classpath:el4j-observability-default-logging.properties", "classpath:el4j-observability-extra-logging.properties"})
@EnableConfigurationProperties({DefaultLoggingProperties.class, ExtraLoggingProperties.class, MetricsProperties.class})
@Configuration
public class DefaultAutoConfiguration {
	/**
	 * Creates bean of type {@link PropertyConverter}
	 * which converts Strings to {@link org.el4j.observability.property.PackageLevelLog}.
	 *
	 * @return {@link PropertyConverter}.
	 */
	@Bean
	public PropertyConverter propertyConverter() {
		return new PropertyConverter();
	}
}
