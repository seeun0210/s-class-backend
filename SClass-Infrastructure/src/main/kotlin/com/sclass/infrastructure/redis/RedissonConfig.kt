package com.sclass.infrastructure.redis

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedissonConfig(
    private val redisProperties: RedisProperties,
) {
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config
            .useSingleServer()
            .setAddress("redis://${redisProperties.host}:${redisProperties.port}")
            .setConnectionPoolSize(redisProperties.connectionPoolSize)
            .setConnectionMinimumIdleSize(redisProperties.connectionMinimumIdleSize)
            .setSubscriptionConnectionPoolSize(redisProperties.subscriptionConnectionPoolSize)
            .setSubscriptionConnectionMinimumIdleSize(redisProperties.subscriptionConnectionMinimumIdleSize)
            .apply {
                redisProperties.password?.takeIf { it.isNotEmpty() }?.let { password = it }
            }
        return Redisson.create(config)
    }
}
