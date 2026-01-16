package com.qtifood.driver.domain.usecase

import com.qtifood.driver.domain.model.Driver
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository

class CreateDriverUseCase(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(driver: Driver): Result<Driver> {
        return repository.createDriver(driver)
    }
}
