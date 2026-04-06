package com.sclass.infrastructure.redis

/**
 * Redisson 분산 락 어노테이션.
 *
 * 메서드 실행 전 Redis 분산 락을 획득하고, 완료 후 해제한다.
 * 트랜잭션 커밋 후 락이 해제되도록 AOP 순서를 @Transactional보다 앞에 둔다.
 *
 * @param key 락 키 (SpEL 지원). 예: "'payment:' + #orderId"
 * @param waitTime 락 획득 최대 대기 시간 (초)
 * @param leaseTime 락 자동 해제 시간 (초). 작업이 이보다 오래 걸리면 락이 풀림
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val waitTime: Long = 5,
    val leaseTime: Long = 10,
)
