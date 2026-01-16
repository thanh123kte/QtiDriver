package com.qtifood.driver.domain.usecase

import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository

class GetDriverByFirebaseUidUseCase(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(firebaseUid: String): Result<Driver?> {
        return repository.getDriverByFirebaseUid(firebaseUid)
    }
}
