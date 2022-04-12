package com.socure.idplus

import android.Manifest
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.socure.idplus.devicerisk.androidsdk.Interfaces
import com.socure.idplus.devicerisk.androidsdk.logSDK
import com.socure.idplus.devicerisk.androidsdk.model.InformationRequest
import com.socure.idplus.devicerisk.androidsdk.model.InformationResponse
import com.socure.idplus.devicerisk.androidsdk.model.SocureSdkError
import com.socure.idplus.devicerisk.androidsdk.model.UploadResult
import com.socure.idplus.devicerisk.androidsdk.sensors.DeviceRiskManager
import com.socure.idplus.uploader.InformationUploader
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity(), MultiplePermissionsListener,
    DeviceRiskManager.DataUploadCallback, Interfaces.InformationUploadCallback {

    companion object {
        const val TAG = "MainActivity"
        private var PRIVATE_MODE = 0
        private val PREF_NAME = "user_preferences.xml"
    }

    private var sharedPref: SharedPreferences? = null
    private var deviceRiskManager: DeviceRiskManager? = null
    private var uploadResult: UploadResult? = null

    private var result: String? = null
    private var uuid: String? = null

    private var informationUploader: InformationUploader? = null

    private val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        Dexter.withActivity(this)
            .withPermissions(permissions)
            .withListener(this)
            .onSameThread()
            .check()

        riskButton.setOnClickListener {
            Snackbar.make(layout, "Sending data", Snackbar.LENGTH_LONG).show()

            deviceRiskManager?.sendData(DeviceRiskManager.Context.Profile)
        }

        loadDeviceRiskManager()
    }

    private fun loadDeviceRiskManager(){
        deviceRiskManager = DeviceRiskManager()
        val list = mutableListOf<DeviceRiskManager.DeviceRiskDataSourcesEnum>()
        //motion
        list.add(DeviceRiskManager.DeviceRiskDataSourcesEnum.Device)
        //location
        list.add(DeviceRiskManager.DeviceRiskDataSourcesEnum.Location)
        //Locale
        list.add(DeviceRiskManager.DeviceRiskDataSourcesEnum.Locale)
        list.add(DeviceRiskManager.DeviceRiskDataSourcesEnum.Network)

        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        uuid = sharedPref?.getString(getString(R.string.uuidKey), null)

        uuid?.let { logSDK(TAG, it) }

        deviceRiskManager?.setTracker(
            key = "89346a7b-eb0c-4344-8b7c-3abadf6e1e9c",
            trackers = list,
            userConsent = true,
            activity = this,
            callback = this
        )

//,
//            deviceRiskUrl = "https://dvnfo.com",

        /*
        deviceRiskManager?.passMotionData(
            accelerometerModel = AccelerometerModel(
                0.5F,
                0.5F,
                0.5F
            ),
            magnetometerModel = MagnetometerModel(0.5F, 0.5F, 0.5F),
            gyroscopeModel = GyroscopeModel(0.5F, 0.5F, 0.5F),
            gravityModel = GravityModel(0.5F, 0.5F, 0.5F),
            rotationModel = RotationModel(0.5F, 0.5F, 0.5F),
            proximity = "5.0"
        )

        val passBluetoothData = deviceRiskManager?.passBluetoothData(
            BluetoothModel(
                mutableListOf(
                    BluetoothDevice(
                        "device 0",
                        "device 0"
                    )
                )
            )
        )



        deviceRiskManager?.passLocationData(LocationModel("12.3456879"))

        deviceRiskManager?.passPedometerData(PedometerModel(stepsNumber = "0"))

        deviceRiskManager?.passDocumentVerificationData(mutableListOf(Pair("asd", "1234")))
        */
    }


    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {


    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
    }

    override fun dataUploadFinished(uploadResult: UploadResult) {
        this.uploadResult = uploadResult
        if (uuid == null) {
            this.uploadResult?.uuid?.let {
                logSDK(TAG, it)
                uuid = this.uploadResult?.uuid
                deviceRiskManager?.setUUID(uuid)
                with(sharedPref?.edit()) {
                    this?.putString(getString(R.string.uuidKey), it)
                    this?.commit()
                }
            }
        }

        informationButton.isEnabled = true
        resultView.text = uploadResult.toString()
    }

    override fun onError(errorType: DeviceRiskManager.SocureSDKErrorType, errorMessage: String?) {

    }

    override fun informationUploadFinished(informationResponse: InformationResponse?) {
        resultView.text = informationResponse.toString()
    }

    override fun informationUploadError(error: SocureSdkError?) {
        Snackbar.make(layout, "informationUploadError", Snackbar.LENGTH_LONG).show()
    }

    override fun onSocurePublicKeyError(error: SocureSdkError?) {
        Snackbar.make(layout, "onSocurePublicKeyError", Snackbar.LENGTH_LONG).show()
    }

}