package org.example.stockfishanalyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置
 * 启用 Spring 的 @Async 注解支持
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
