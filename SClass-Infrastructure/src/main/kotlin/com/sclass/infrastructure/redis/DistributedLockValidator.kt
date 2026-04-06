package com.sclass.infrastructure.redis

import org.springframework.aop.support.AopUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DistributedLockValidator(
    private val applicationContext: ApplicationContext,
) {
    @EventListener(ContextRefreshedEvent::class)
    fun validate() {
        applicationContext.beanDefinitionNames
            .mapNotNull { runCatching { applicationContext.getBean(it) }.getOrNull() }
            .forEach { bean ->
                AopUtils
                    .getTargetClass(bean)
                    .methods
                    .filter { it.isAnnotationPresent(DistributedLock::class.java) }
                    .forEach { method ->
                        val hasLockKey =
                            method.parameters.any {
                                it.isAnnotationPresent(LockKey::class.java)
                            }
                        require(hasLockKey) {
                            "@DistributedLock 메서드에 @LockKey 파라미터가 없습니다: " +
                                "${bean.javaClass.simpleName}.${method.name}()"
                        }
                    }
            }
    }
}
