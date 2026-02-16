package com.example.speak2do.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val isCodeSent: Boolean = false,
    val verificationId: String? = null,
    val phoneNumber: String = ""
)

class AuthViewModel : ViewModel() {

    companion object {
        private const val DEFAULT_COUNTRY_CODE = "+91"
    }

    private val auth = FirebaseAuth.getInstance()
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _authState = MutableStateFlow(
        AuthState(user = auth.currentUser)
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun sendOtp(activity: Activity, phoneNumber: String) {
        val raw = phoneNumber.trim()
        val digitsOnly = raw.filter { it.isDigit() }
        val normalizedPhone = when {
            raw.startsWith("+") -> raw
            digitsOnly.length == 10 -> "$DEFAULT_COUNTRY_CODE$digitsOnly"
            else -> raw
        }

        if (normalizedPhone.isBlank()) {
            _authState.value = _authState.value.copy(error = "Enter phone number")
            return
        }
        if (!normalizedPhone.startsWith("+91") || normalizedPhone.filter { it.isDigit() }.length != 12) {
            _authState.value = _authState.value.copy(
                error = "Enter valid 10-digit mobile number"
            )
            return
        }

        _authState.value = _authState.value.copy(
            isLoading = true,
            error = null,
            phoneNumber = normalizedPhone
        )

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(normalizedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        resendToken?.let { optionsBuilder.setForceResendingToken(it) }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyOtp(code: String) {
        val verificationId = _authState.value.verificationId
        val pin = code.trim()

        if (verificationId.isNullOrBlank()) {
            _authState.value = _authState.value.copy(error = "Request OTP first")
            return
        }
        if (pin.length < 6) {
            _authState.value = _authState.value.copy(error = "Enter valid 6-digit OTP")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, error = null)
        val credential = PhoneAuthProvider.getCredential(verificationId, pin)
        signInWithPhoneCredential(credential)
    }

    fun signOut() {
        auth.signOut()
        resendToken = null
        _authState.value = AuthState()
    }

    fun backToPhoneEntry() {
        _authState.value = _authState.value.copy(
            isCodeSent = false,
            verificationId = null,
            error = null
        )
    }

    fun updateDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _authState.value = _authState.value.copy(error = "Name cannot be empty")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = _authState.value.copy(error = "No signed in user")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, error = null)

        val updates = UserProfileChangeRequest.Builder()
            .setDisplayName(trimmed)
            .build()

        currentUser.updateProfile(updates)
            .addOnSuccessListener {
                currentUser.reload()
                    .addOnCompleteListener {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            user = auth.currentUser,
                            error = null
                        )
                    }
            }
            .addOnFailureListener { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to save name"
                )
            }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format"
                is FirebaseTooManyRequestsException -> "Too many attempts. Try again later."
                else -> e.localizedMessage ?: "Verification failed"
            }
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = message
            )
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            resendToken = token
            _authState.value = _authState.value.copy(
                isLoading = false,
                isCodeSent = true,
                verificationId = verificationId,
                error = null
            )
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    user = result.user,
                    error = null
                )
            }
            .addOnFailureListener { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Sign in failed"
                )
            }
    }
}
