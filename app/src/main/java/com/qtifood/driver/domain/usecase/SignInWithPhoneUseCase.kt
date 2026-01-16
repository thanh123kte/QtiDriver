package com.qtifood.driver.domain.usecase

import com.google.firebase.auth.PhoneAuthCredential
import com.qtifood.driver.domain.model.Result
import com.qtifood.driver.domain.repository.DriverRepository

class SignInWithPhoneUseCase(
    private val repository: DriverRepository
) {
    suspend operator fun invoke(credential: PhoneAuthCredential): Result<String> {
        return repository.signInWithPhoneCredential(credential)
    }
}
