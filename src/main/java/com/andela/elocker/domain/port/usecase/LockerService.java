package com.andela.elocker.domain.port.usecase;

import com.andela.elocker.domain.port.usecase.locker.LockEntityUseCase;
import com.andela.elocker.domain.port.usecase.locker.UnlockEntityUseCase;

public interface LockerService<T> extends LockEntityUseCase<T>, UnlockEntityUseCase<T> {
}
