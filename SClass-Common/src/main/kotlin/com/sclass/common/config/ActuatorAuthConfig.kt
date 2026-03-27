package com.sclass.common.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ActuatorAuthProperties::class)
class ActuatorAuthConfig
