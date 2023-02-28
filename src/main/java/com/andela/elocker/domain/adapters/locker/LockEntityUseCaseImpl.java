package com.andela.elocker.domain.adapters.locker;

import com.andela.elocker.domain.port.usecase.locker.LockEntityUseCase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
@NoArgsConstructor
public class LockEntityUseCaseImpl<T> implements LockEntityUseCase<T> {

    private final Map<T, Lock> entityLocks = new ConcurrentHashMap<>();

    ReentrantLock globalReentrantLock = new ReentrantLock(true);

    boolean isGlobalLock;


    @Override
    public void lock(T entityId) throws Exception {
        if (entityId == null) {
            throw new Exception("Entity id null");
        }

        entityLocks.putIfAbsent(entityId, new ReentrantLock(true));
        Lock lock = entityLocks.get(entityId);

        if (isGlobalLock) {
            globalReentrantLock.lock();
            try {
                // any protected code
            } finally {
                globalReentrantLock.unlock();
            }
        }

        lock.lock();
    }


    @Override
    public void lock(T entityId, long timeout, TimeUnit timeUnit) throws Exception {
        if (entityId == null) {
            throw new Exception("Entity id null");
        }

        entityLocks.putIfAbsent(entityId, new ReentrantLock(true));
        Lock lock = entityLocks.get(entityId);

        if (isGlobalLock) {
            globalReentrantLock.lock();
            try {
                // any protected code
            } finally {
                globalReentrantLock.unlock();
            }
        }
        if (!lock.tryLock(timeout, timeUnit)) {
            throw new Exception("Timeout");
        }
    }

    @Override
    public boolean lock(T entityT, long timeoutNanos) throws Exception {
        if (entityT == null) {
            throw new AssertionError();
        }

        ReentrantLock reentrantLock = (ReentrantLock) entityLocks.computeIfAbsent(entityT, t -> new ReentrantLock());

        if (timeoutNanos >= 0) {
            return reentrantLock.tryLock(timeoutNanos, TimeUnit.NANOSECONDS);
        } else {
            reentrantLock.lockInterruptibly();
        }

        return true;
    }


    @Override
    public boolean isLocked() {
        return !isGlobalLock;
    }


    @Override
    public void globalLock() {
        globalReentrantLock.lock();
        isGlobalLock = true;
        for (T id : entityLocks.keySet()) {
            entityLocks.get(id).lock();
        }
    }
}
