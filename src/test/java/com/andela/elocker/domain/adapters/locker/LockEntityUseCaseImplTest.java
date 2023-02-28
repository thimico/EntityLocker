package com.andela.elocker.domain.adapters.locker;

import com.andela.elocker.domain.adapters.LockerServiceImpl;
import lombok.Data;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link LockerServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LockEntityUseCaseImplTest {

    public static final int TIMEOUT_NANOS = 100000;
    private static int increments = 2;
    private static List<Thread> threads;
    private static EasyRandom easyRandom;
    private ExecutorService executor;
    @InjectMocks
    private LockerServiceImpl<String> unitUnderTest;

    @BeforeAll
    public void beforeTests() {
        easyRandom = new EasyRandom();
        executor = Executors.newFixedThreadPool(2);
        threads = new ArrayList<>();
    }

    @BeforeEach
    void setUp() {
        LockEntityUseCaseImpl<String> lockEntityUseCaseImpl = new LockEntityUseCaseImpl<>();
        UnlockEntityUseCaseImpl<String> unlockEntityUseCase = new UnlockEntityUseCaseImpl<>();
        unitUnderTest = new LockerServiceImpl<>(lockEntityUseCaseImpl, unlockEntityUseCase);
    }

    @AfterAll
    public void tearDown() {
        executor.shutdown();
    }

    @Test
    @DisplayName("Should throw an exception when the timeout is reached")
    public void lockWhenTimeoutIsReachedThenThrowException() throws Exception {
        MyClass myObject = easyRandom.nextObject(MyClass.class);

        Thread thread = new Thread(() -> {
            try {
                unitUnderTest.lock(myObject.getId());
                Thread.sleep(1000);
                myObject.plusOne();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                unitUnderTest.unlock(myObject.getId());
            }
        });
        thread.start();
        threads.add(thread);

        Thread anotherThread = new Thread(() -> {
            try {
                assertThrows(Exception.class, () -> unitUnderTest.lock(myObject.getId(), 1L, TimeUnit.MILLISECONDS));
                try {
                    myObject.plusOne();
                } finally {
                    unitUnderTest.unlock(myObject.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // time is out - do nothing
            }
        });
        anotherThread.start();
        threads.add(anotherThread);

    }

    @Test
    @DisplayName("Should throw an exception when the entityid is null")
    void lockWhenEntityIdIsNullThenThrowException() throws Exception {

        String entityId = null;

        assertThrows(Exception.class, () -> unitUnderTest.lock(entityId));

    }

    @Test
    @DisplayName("Should lock the entity when the entityid is not null")
    void lockWhenEntityIdIsNotNullThenLockTheEntity() throws Exception {

        String entityId = easyRandom.nextObject(String.class);

        unitUnderTest.lock(entityId);

        assertTrue(unitUnderTest.isLocked());
    }

    @Test
    public void lockWithTimeoutExpires() throws Exception {

        String entityId = easyRandom.nextObject(String.class);

        unitUnderTest.lock(entityId);

        Future<Boolean> task1 = executor.submit(() -> {
            boolean locked = unitUnderTest.lock(entityId, TIMEOUT_NANOS);
            if (locked) unitUnderTest.unlock(entityId);
            return locked;
        });
        assertFalse(task1.get());
    }

    @Test
    public void lockWhenEntityIdIsNotNullThenReentryLock() throws Exception {
        String entityId = easyRandom.nextObject(String.class);

        unitUnderTest.lock(entityId);
        unitUnderTest.unlock(entityId);

        unitUnderTest.lock(entityId);
        unitUnderTest.lock(entityId, 10);

        Future<Boolean> task1 = executor.submit(() -> {
            try {
                return unitUnderTest.lock(entityId, TIMEOUT_NANOS);
            } catch (InterruptedException e) {
                return true;
            }
        });
        assertFalse(task1.get());
        unitUnderTest.unlock(entityId);

    }

    @Test
    public void testGlobalLock() throws Exception {
        MyClass myObject = easyRandom.nextObject(MyClass.class);
        myObject.setValue(4);
        for (int i = 0; i < increments; ++i) {
            Thread thread = new Thread(() -> {
                try {
                    unitUnderTest.lock(myObject.getId());
                    myObject.plusOne();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unitUnderTest.unlock(myObject.getId());
                }
            });
            thread.start();
            threads.add(thread);
        }

        Thread thread = new Thread(() -> {
            unitUnderTest.globalLock();

            try {
                for (int i = 0; i < increments; i++) {
                    myObject.plusOne();
                }
            } finally {
                unitUnderTest.globalUnlock();
            }
        });
        thread.start();
        threads.add(thread);

        Assertions.assertEquals(increments * 2, (myObject.getValue() - 1));
    }

    @Test
    public void testLock() throws Exception {
        int value = 30;
        MyClass myObject = easyRandom.nextObject(MyClass.class);
        try {
            unitUnderTest.lock(myObject.getId());
            myObject.setValue(value);
        } finally {
            unitUnderTest.unlock(myObject.getId());
        }

        Assertions.assertEquals(value, myObject.getValue());
    }

    @Test
    public void testeConcurrentRequests() throws Exception {
        MyClass myObject = easyRandom.nextObject(MyClass.class);
        myObject.setValue(0);
        for (int i = 0; i < increments; ++i) {
            Thread thread = new Thread(() -> {
                try {
                    unitUnderTest.lock(myObject.getId());
                    myObject.plusOne();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unitUnderTest.unlock(myObject.getId());
                }
            });
            thread.start();
            threads.add(thread);
        }

        Assertions.assertNotEquals(increments, myObject.getValue());
    }

    @Data
    private static class MyClass {
        private String id;

        private int value;

        public void plusOne() {
            this.value = this.value + 1;
        }
    }
}