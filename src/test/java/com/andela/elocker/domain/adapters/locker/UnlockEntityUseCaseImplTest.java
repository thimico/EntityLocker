package com.andela.elocker.domain.adapters.locker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link UnlockEntityUseCaseImpl}
 */
@ExtendWith(MockitoExtension.class)
public class UnlockEntityUseCaseImplTest {

    @Test
    @DisplayName("Should not unlock any locks when the global lock is not held by current thread")
    void globalUnlockWhenGlobalLockIsNotHeldByCurrentThreadThenDoNothing() {
        Map<String, Lock> entityLocks = new HashMap<>();
        ReentrantLock globalReentrantLock = new ReentrantLock(true);
        UnlockEntityUseCaseImpl<String> unlockEntityUseCase =
                new UnlockEntityUseCaseImpl<>(entityLocks);
        unlockEntityUseCase.globalReentrantLock = globalReentrantLock;

        unlockEntityUseCase.globalUnlock();

        assertThat(globalReentrantLock.isLocked()).isFalse();
    }

    @Test
    @DisplayName("Should unlock all locks when the global lock is held by current thread")
    void globalUnlockWhenGlobalLockIsHeldByCurrentThreadThenUnlockAllLocks() {
        Map<String, Lock> entityLocks = new HashMap<>();
        entityLocks.put("1", new ReentrantLock());
        entityLocks.put("2", new ReentrantLock());
        entityLocks.put("3", new ReentrantLock());
        UnlockEntityUseCaseImpl<String> unlockEntityUseCase =
                new UnlockEntityUseCaseImpl<>(entityLocks);

        unlockEntityUseCase.globalUnlock();

        assertThat(unlockEntityUseCase.isGlobalLock).isFalse();
    }

    @Test
    @DisplayName("Should not unlock the entity when the entity is not locked by current thread")
    void unlockWhenEntityIsNotLockedByCurrentThread() {
        var entityId = "entityId";
        var entityLocks = new HashMap<String, Lock>();
        var reentrantLock = new ReentrantLock();
        entityLocks.put(entityId, reentrantLock);

        var unlockEntityUseCase = new UnlockEntityUseCaseImpl<String>(entityLocks);

        unlockEntityUseCase.unlock(entityId);

        assertThat(reentrantLock.isLocked()).isFalse();
    }

    @Test
    @DisplayName("Should unlock the entity when the entity is locked by current thread")
    void unlockWhenEntityIsLockedByCurrentThread() {
        String entityId = "entityId";
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        Map<String, Lock> entityLocks = new HashMap<>();
        entityLocks.put(entityId, reentrantLock);
        UnlockEntityUseCaseImpl<String> unlockEntityUseCase =
                new UnlockEntityUseCaseImpl<>(entityLocks);

        unlockEntityUseCase.unlock(entityId);

        assertThat(reentrantLock.isLocked()).isTrue();
    }
}
