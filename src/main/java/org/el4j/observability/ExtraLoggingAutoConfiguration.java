package org.el4j.observability;

import java.util.List;
import java.util.Objects;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.el4j.observability.aspect.EventLogAppender;
import org.el4j.observability.aspect.SystemCPUMonitoring;
import org.el4j.observability.aspect.SystemMemoryMonitoring;
import org.el4j.observability.property.ExtraLoggingProperties;
import org.el4j.observability.property.PackageLevelLog;
import org.el4j.observability.repository.redis.CpuEventLogRepository;
import org.el4j.observability.repository.redis.EventLogRepository;
import org.el4j.observability.repository.redis.MemoryEventRepository;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configures extra logging.
 * This includes:
 * <p>
 * 1. Cpu and memory monitoring.
 * <br>
 * 2. Package level logging.
 */
@AutoConfigureAfter({RedisRepositoryAutoConfiguration.class, AspectAutoConfiguration.class})
@ConditionalOnProperty({"elf4j.metrics.logging.enabled", "elf4j.metrics.logging.extra.enabled"})
@EnableScheduling
@Configuration
@Slf4j
public class ExtraLoggingAutoConfiguration {

	private final ExtraLoggingProperties properties;

	/**
	 * Constructor.
	 *
	 * @param properties Extra metrics properties.
	 */
	public ExtraLoggingAutoConfiguration(final @NonNull ExtraLoggingProperties properties) {
		this.properties = properties;
	}

	/**
	 * Creates bean for {@link org.el4j.observability.aspect.SystemMemoryMonitoring}.
	 *
	 * @param systemUtilizationRepo Memory event log persistence layer.
	 * @return Bean.
	 */
	@Bean
	public SystemMemoryMonitoring monitoringMemAspect(
			final @NonNull MemoryEventRepository systemUtilizationRepo) {
		return new SystemMemoryMonitoring(systemUtilizationRepo, properties.ttl());
	}

	/**
	 * Creates bean for {@link org.el4j.observability.aspect.SystemCPUMonitoring}.
	 *
	 * @param systemUtilizationRepo Cpu event log persistence layer.
	 * @return Bean.
	 */
	@Bean
	public SystemCPUMonitoring monitoringCpuAspect(
			final @NonNull CpuEventLogRepository systemUtilizationRepo) {
		return new SystemCPUMonitoring(systemUtilizationRepo, properties.ttl());
	}

	/**
	 * Creates bean for {@link org.el4j.observability.aspect.EventLogAppender}.
	 *
	 * @param logRepository Default event log persistence layer.
	 * @return Bean.
	 */
	@Bean
	public EventLogAppender el4jLogAppender(final @NonNull EventLogRepository logRepository) {
		final List<PackageLevelLog> packages = properties.packages();
		if (Objects.isNull(packages)) {
			return null;
		}

		log.info("Initializing logging via package level and @Slf4j");

		final EventLogAppender eventLogAppender = new EventLogAppender(logRepository, properties.ttl());
		eventLogAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
		eventLogAppender.start();

		packages
				.forEach(packageFolder -> {
					final Logger logger = (Logger) LoggerFactory.getLogger(packageFolder.name());
					logger.setLevel(Level.toLevel(packageFolder.level()));
					logger.addAppender(eventLogAppender);
				});

		return eventLogAppender;
	}
}
