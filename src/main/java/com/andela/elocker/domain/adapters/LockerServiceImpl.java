package com.andela.elocker.domain.adapters;

import com.andela.elocker.domain.port.usecase.LockerService;
import com.andela.elocker.domain.port.usecase.locker.LockEntityUseCase;
import com.andela.elocker.domain.port.usecase.locker.UnlockEntityUseCase;

import java.util.concurrent.TimeUnit;

public class LockerServiceImpl<T> implements LockerService<T> {

    private final LockEntityUseCase<T> lockEntityUseCase;
    private final UnlockEntityUseCase<T> unlockEntityUseCase;

    public LockerServiceImpl(LockEntityUseCase lockEntityUseCase, UnlockEntityUseCase unlockEntityUseCase) {
        this.lockEntityUseCase = lockEntityUseCase;
        this.unlockEntityUseCase = unlockEntityUseCase;
    }

    public void lock(T entityId) throws Exception {
        lockEntityUseCase.lock(entityId);
    }

    @Override
    public void lock(T entityId, long timeout, TimeUnit timeUnit) throws Exception {
        lockEntityUseCase.lock(entityId, timeout, timeUnit);
    }

    @Override
    public boolean lock(T entityT, long timeoutNanos) throws Exception {
        return lockEntityUseCase.lock(entityT, timeoutNanos);
    }

    @Override
    public boolean isLocked() {
        return lockEntityUseCase.isLocked();
    }

    @Override
    public void globalLock() {
        lockEntityUseCase.globalLock();
    }

    @Override
    public void unlock(T entityId) {
        unlockEntityUseCase.unlock(entityId);
    }

    @Override
    public void globalUnlock() {
        unlockEntityUseCase.globalUnlock();
    }
}
