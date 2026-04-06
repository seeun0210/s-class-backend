package com.sclass.infrastructure.redis

/**
 * 분산 락의 키로 사용할 파라미터를 지정하는 마커 어노테이션.
 *
 * @DistributedLock이 붙은 메서드에서 반드시 하나의 파라미터에 이 어노테이션을 붙여야 한다.
 * 해당 파라미터의 toString() 값이 락 키로 사용된다.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LockKey
