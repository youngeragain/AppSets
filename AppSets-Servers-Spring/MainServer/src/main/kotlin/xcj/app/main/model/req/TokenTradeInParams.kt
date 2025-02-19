package xcj.app.main.model.req

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull


data class TokenTradeInParams(
    @get: NotNull(message = "account is required")
    val account: String,
    @get:NotNull(message = "signInDeviceInfo is required")
    @Valid
    val signInDeviceInfo: DeviceInfo?,
    val signInLocation: String?
) {
    fun compareToLoginParams(loginParams: LoginParams): Boolean {
        return account == loginParams.account &&
                signInDeviceInfo?.platform == loginParams.signInDeviceInfo?.platform &&
                signInDeviceInfo?.screenResolution == loginParams.signInDeviceInfo?.screenResolution &&
                signInDeviceInfo?.screenSize == loginParams.signInDeviceInfo?.screenSize &&
                signInDeviceInfo?.version == loginParams.signInDeviceInfo?.version &&
                signInDeviceInfo?.vendor == loginParams.signInDeviceInfo?.vendor &&
                signInDeviceInfo?.model == loginParams.signInDeviceInfo?.model &&
                signInDeviceInfo?.modelCode == loginParams.signInDeviceInfo?.modelCode &&
                signInDeviceInfo?.ip == loginParams.signInDeviceInfo?.ip &&
                signInLocation == loginParams.signInLocation
    }
}
