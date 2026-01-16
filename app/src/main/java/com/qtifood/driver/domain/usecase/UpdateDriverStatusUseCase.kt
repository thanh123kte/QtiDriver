package com.qtifood.driver.domain.usecase

import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository

class UpdateDriverStatusUseCase(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(driverId: String, status: String): Result<Driver> {
        return repository.updateDriverStatus(driverId, status)
    }
}
