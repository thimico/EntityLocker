package com.andela.elocker.domain.port.usecase.locker;

public interface UnlockEntityUseCase<T> {

    void unlock(T entityId);

    void globalUnlock();

}
