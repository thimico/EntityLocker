package com.andela.elocker.domain.adapters.locker;

import com.andela.elocker.domain.port.usecase.locker.UnlockEntityUseCase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
@NoArgsConstructor
public class UnlockEntityUseCaseImpl<T> implements UnlockEntityUseCase<T> {

    ReentrantLock globalReentrantLock = new ReentrantLock(true);
    boolean isGlobalLock;
    private Map<T, Lock> entityLocks = new ConcurrentHashMap<>();

    public UnlockEntityUseCaseImpl(HashMap<T, Lock> entityLocks) {
    }

    public UnlockEntityUseCaseImpl(Map<String, Lock> entityLocks) {
    }


    @Override
    public void unlock(T entityId) {
        ReentrantLock lock = (ReentrantLock) entityLocks.get(entityId);
        if (lock != null) {
            if (!lock.isHeldByCurrentThread()) {
                throw new IllegalMonitorStateException();
            }
            if (!lock.hasQueuedThreads()) {
                entityLocks.remove(entityId);
            }
            lock.unlock();
        }

    }

    @Override
    public void globalUnlock() {
        if (globalReentrantLock.isHeldByCurrentThread()) {
            this.isGlobalLock = false;
            for (Lock lock : entityLocks.values()) {
                if (((ReentrantLock) lock).isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            globalReentrantLock.unlock();
        }
    }
}
