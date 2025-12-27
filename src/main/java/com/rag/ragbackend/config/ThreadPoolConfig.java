package com.rag.ragbackend.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description SpringBoot线程池配置类，支持通过配置文件自定义线程池参数，优雅关闭，拒绝策略可控
 * @Version 1.0.0
 * @Date 2025-12-27 14:48
 * @Author by zjh
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {
    // 核心线程数（默认CPU核心数）
    @Value("${thread.pool.core-size:${runtime.availableProcessors}}")
    private int corePoolSize;

    // 最大线程数（默认CPU核心数*2）
    @Value("${thread.pool.max-size:${runtime.availableProcessors}*2}")
    private int maxPoolSize;

    // 队列容量（默认1000）
    @Value("${thread.pool.queue-capacity:1000}")
    private int queueCapacity;

    // 线程空闲时间（默认60秒）
    @Value("${thread.pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    // 线程名称前缀
    @Value("${thread.pool.name-prefix:biz-task-}")
    private String threadNamePrefix;

    // 拒绝策略（默认CallerRunsPolicy：由调用线程执行）
    private static final ThreadPoolExecutor.CallerRunsPolicy REJECT_POLICY = new ThreadPoolExecutor.CallerRunsPolicy();

    /**
     * 业务线程池Bean（可通过@Qualifier("bizExecutor")注入使用）
     */
    @Bean(name = "bizExecutor")
    public Executor bizExecutor() {
        ThreadPoolTaskExecutor executor = new VisibleThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程名称前缀（便于日志排查）
        executor.setThreadNamePrefix(threadNamePrefix);
        // 拒绝策略（避免任务丢失，优先让调用线程执行）
        executor.setRejectedExecutionHandler(REJECT_POLICY);
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 关闭时的等待超时时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        log.info("业务线程池初始化完成 | 核心线程数：{} | 最大线程数：{} | 队列容量：{}",
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    /**
     * 优雅关闭线程池（Spring销毁前执行）
     */
    @PreDestroy
    public void destroy() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) bizExecutor();
        if (executor != null) {
            executor.shutdown();
            try {
                // 等待线程池关闭，超时则强制关闭
                if (!executor.getThreadPoolExecutor().awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdown();
                    log.warn("业务线程池强制关闭，仍有未完成任务");
                } else {
                    log.info("业务线程池优雅关闭完成");
                }
            } catch (InterruptedException e) {
                executor.shutdown();
                Thread.currentThread().interrupt();
                log.error("线程池关闭被中断", e);
            }
        }
    }

    /**
     * 可视化线程池（打印线程池状态，便于监控）
     */
    static class VisibleThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        private void logThreadPoolStatus() {
            ThreadPoolExecutor executor = getThreadPoolExecutor();
            if (executor == null) {
                return;
            }
            log.info("==========线程池状态监控==========");
            log.info("核心线程数：{}", executor.getCorePoolSize());
            log.info("活跃线程数：{}", executor.getActiveCount());
            log.info("最大线程数：{}", executor.getMaximumPoolSize());
            log.info("线程池当前线程数：{}", executor.getPoolSize());
            log.info("队列等待任务数：{}", executor.getQueue().size());
            log.info("已完成任务数：{}", executor.getCompletedTaskCount());
            log.info("==================================");
        }

        @Override
        public void execute(Runnable task) {
            logThreadPoolStatus();
            super.execute(task);
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) {
            logThreadPoolStatus();
            return super.submit(task);
        }

        @Override
        public java.util.concurrent.Future<?> submit(Runnable task) {
            logThreadPoolStatus();
            return super.submit(task);
        }
    }
}
