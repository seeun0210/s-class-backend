package com.sclass.infrastructure.redis

/**
 * Redisson 분산 락 어노테이션.
 *
 * 메서드 실행 전 Redis 분산 락을 획득하고, 완료 후 해제한다.
 * 트랜잭션 커밋 후 락이 해제되도록 AOP 순서를 @Transactional보다 앞에 둔다.
 *
 * 락 키는 "lock:{prefix}:{파라미터 값}" 형태로 생성된다.
 * - key를 지정하면 해당 이름의 파라미터 값을 사용
 * - key를 생략하면 첫 번째 파라미터 값을 사용
 *
 * 예시:
 *   @DistributedLock(prefix = "coin")                      → lock:coin:{첫 번째 파라미터}
 *   @DistributedLock(prefix = "payment", key = "orderId")  → lock:payment:{orderId 파라미터}
 *
 * @param prefix 락 키 프리픽스 (도메인 구분)
 * @param key 락 키로 사용할 파라미터 이름. 생략하면 첫 번째 파라미터 사용
 * @param waitTime 락 획득 최대 대기 시간 (초)
 * @param leaseTime 락 자동 해제 시간 (초)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val prefix: String,
    val key: String = "",
    val waitTime: Long = 5,
    val leaseTime: Long = 10,
)
