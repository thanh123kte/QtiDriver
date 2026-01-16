package com.qtifood.driver.domain.usecase

import com.qtifood.driver.domain.model.DriverLocation
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository

class UpdateDriverLocationUseCase(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(location: DriverLocation): Result<Unit> {
        return repository.updateDriverLocation(location)
    }
}
