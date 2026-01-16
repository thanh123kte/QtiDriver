package com.qtifood.driver.domain.usecase

import com.qtifood.driver.domain.model.Order
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository

class GetCurrentOrderUseCase(
    private val repository: DriverRepository
) {

}
