package com.andela.elocker.domain.port.usecase.locker;

import java.util.concurrent.TimeUnit;

public interface LockEntityUseCase<T> {

    void lock(T entityId) throws Exception;

    void lock(T entityId, long timeout, TimeUnit timeUnit) throws Exception;

    boolean lock(T entityT, long timeoutNanos) throws Exception;

    boolean isLocked();

    void globalLock();

}
