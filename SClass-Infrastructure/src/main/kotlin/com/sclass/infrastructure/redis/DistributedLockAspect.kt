package com.sclass.infrastructure.redis

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * @DistributedLock AOP Aspect.
 *
 * @Order(Ordered.HIGHEST_PRECEDENCE)로 @Transactional보다 먼저 실행되어,
 * 트랜잭션 커밋이 완료된 후에 락이 해제된다.
 *
 * 실행 순서:
 *   락 획득 → 트랜잭션 시작 → 비즈니스 로직 → 트랜잭션 커밋 → 락 해제
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class DistributedLockAspect(
    private val redissonClient: RedissonClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(distributedLock)")
    fun around(
        joinPoint: ProceedingJoinPoint,
        distributedLock: DistributedLock,
    ): Any? {
        val keyValue = resolveKeyValue(joinPoint, distributedLock.key)
        val lockKey = "lock:${distributedLock.prefix}:$keyValue"
        val lock = redissonClient.getLock(lockKey)

        val acquired =
            lock.tryLock(
                distributedLock.waitTime,
                distributedLock.leaseTime,
                TimeUnit.SECONDS,
            )

        if (!acquired) {
            log.warn("분산 락 획득 실패: key={}", lockKey)
            throw DistributedLockAcquisitionException(lockKey)
        }

        log.debug("분산 락 획득: key={}", lockKey)

        return try {
            joinPoint.proceed()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
                log.debug("분산 락 해제: key={}", lockKey)
            }
        }
    }

    private fun resolveKeyValue(
        joinPoint: ProceedingJoinPoint,
        key: String,
    ): String {
        val signature = joinPoint.signature as MethodSignature
        val parameterNames = signature.parameterNames
        val args = joinPoint.args

        if (parameterNames.isEmpty()) {
            throw IllegalArgumentException("분산 락 대상 메서드에 파라미터가 없습니다")
        }

        // key가 비어있으면 첫 번째 파라미터 사용
        if (key.isBlank()) {
            return args[0].toString()
        }

        // key에 해당하는 파라미터 이름 찾기
        val index = parameterNames.indexOf(key)
        if (index == -1) {
            throw IllegalArgumentException(
                "파라미터 '$key'를 찾을 수 없습니다. 사용 가능한 파라미터: ${parameterNames.toList()}",
            )
        }

        return args[index].toString()
    }
}
