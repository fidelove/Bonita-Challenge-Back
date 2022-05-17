package com.bonitasoft.challenge.email;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Define a pool of asynchronous task execution threads
 */
@Configuration
public class TaskPoolConfig {
	@Bean("taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		// Number of Core Threads 10: Number of threads initialized at thread pool
		// creation
		executor.setCorePoolSize(10);

		// Maximum number of threads 20: Maximum number of threads in the thread pool.
		// Threads that exceed the number of core threads are requested only after the
		// buffer queue is full
		executor.setMaxPoolSize(15);

		// Buffer queue 200: Queues used to buffer tasks
		executor.setQueueCapacity(200);

		// Allow threads to be idle for 60 seconds: Threads that exceed the number of
		// core threads will be destroyed when the idle time arrives
		executor.setKeepAliveSeconds(60);

		// Thread pool name prefix: Once set, it is easy to locate the thread pool where
		// the processing task is located.
		executor.setThreadNamePrefix("taskExecutor-");

		/*
		 * Thread pool's strategy for rejecting tasks: Caller Runs Policy is adopted
		 * here. When the thread pool has no processing power, the strategy will
		 * directly run the rejected task in the calling thread of the execute method.
		 * If the executor is closed, the task is discarded
		 */
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		// Set the thread pool to close and wait until all tasks are completed before
		// continuing to destroy other beans
		executor.setWaitForTasksToCompleteOnShutdown(true);

		// Set the waiting time of tasks in the thread pool, and force them to be
		// destroyed if they are not destroyed beyond that time to ensure that the
		// application can eventually be shut down rather than blocked.
		executor.setAwaitTerminationSeconds(600);
		return executor;
	}
}