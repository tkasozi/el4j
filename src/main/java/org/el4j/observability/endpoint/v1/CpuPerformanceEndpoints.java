package org.el4j.observability.endpoint.v1;

import lombok.NonNull;
import org.el4j.observability.data.Logs;
import org.el4j.observability.domain.redis.CpuEventLog;
import org.el4j.observability.repository.redis.CpuEventLogRepository;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * Handles requests for cpu performance metrics.
 */
@RequestMapping("/observer/v1/utilization/cpu")
@RestController
@ResponseBody
public class CpuPerformanceEndpoints {
	private final CpuEventLogRepository cpuEventLogRepository;

	/**
	 * Constructor.
	 * @param cpuEventLogRepository Persistence layer for cpu logs.
	 */
	public CpuPerformanceEndpoints(final @NonNull CpuEventLogRepository cpuEventLogRepository) {
		this.cpuEventLogRepository = cpuEventLogRepository;
	}

	/**
	 * GET.
	 *
	 * @return List of CPU Events.
	 */
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Logs<CpuEventLog> allCpuEndpoint() {
		return new Logs<>(cpuEventLogRepository.getAllSortedByDate());
	}

	/**
	 * GET by timeOffset.
	 *
	 * @param timeOffset  Time within which you want to know the events that happened
	 * @return List of CPU usage events
	 */
	@GetMapping(value = "/{timeOffset}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Logs<CpuEventLog> getCpuUsageByTimeOffset(@PathVariable("timeOffset") final Long timeOffset) {
		return new Logs<>(cpuEventLogRepository.getByTimeOffset(timeOffset));
	}
}
