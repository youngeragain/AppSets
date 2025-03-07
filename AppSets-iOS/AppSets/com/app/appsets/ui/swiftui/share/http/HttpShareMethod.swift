//
//  HttpShareMethod.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/2.
//
import Foundation

class HttpShareMethod: ShareMethod {
    private static let TAG = "HttpShareMethod"
    public static var INSTANCE = HttpShareMethod(viewModel: ShareViewModel.INSTANCE)

    private var serverBootStrap: ServerBootStrap? = nil

    private var discovery: Discovery? = nil

    override init(viewModel: ShareViewModel) {
        super.init(viewModel: viewModel)
    }
    
    deinit{
        PurpleLogger.current.d(HttpShareMethod.TAG, "deinit")
    }

    override func initMethod() {
        updateDeviceName()
        open()
    }
    
    func open() {
        PurpleLogger.current.d(HttpShareMethod.TAG, "open")
        struct Listener: ServerBootStrap.ActionListener {
            let httpShareMethod: HttpShareMethod
            func onSuccess() {
                PurpleLogger.current.d(HttpShareMethod.TAG, "open, success")
                httpShareMethod.startDiscoveryService()
            }

            func onFailure(reason: String?) {
                PurpleLogger.current.d(HttpShareMethod.TAG, "open, failure")
            }
        }
        let actionListener = Listener(httpShareMethod: self)
        serverBootStrap = ServerBootStrap()
        serverBootStrap?.main(actionListener: actionListener)
    }

    func startDiscoveryService() {
        let discovery = BonjourDiscovery(httpShareMethod: self)
        self.discovery = discovery
        discovery.startService()
        discovery.startDiscovery()
    }

    override func destroy() {
        PurpleLogger.current.d(HttpShareMethod.TAG, "destroy, discovery:\(String(describing: discovery))")
        struct Listener: ServerBootStrap.ActionListener {
            func onSuccess() {
                PurpleLogger.current.d(HttpShareMethod.TAG, "destroy, success")
            }

            func onFailure(reason: String?) {
                PurpleLogger.current.d(HttpShareMethod.TAG, "destroy, failure, \(String(describing: reason))")
            }
        }
        discovery?.stopService()
        let actionListener = Listener()
        serverBootStrap?.close(actionListener: actionListener)
    }
    
    override func updateDeviceName() {
        super.updateDeviceName()
        let shareDevice = HttpShareDevice(
            deviceName: deviceName,
            deviceAddress: DevcieAddress.NONE,
            deviceType: ShareDevice.DEVICE_TYPE_PHONE
        )
        viewModel.updateShareDevice(shareDevice)
    }
    
    func notifyShareDeviceChangedOnDiscovery(_ shareDeviceList:[ShareDevice]){
        PurpleLogger.current.d(HttpShareMethod.TAG, "notifyShareDeviceChangedOnDiscovery")
        viewModel.updateShareDeviceList(shareDeviceList)
    }
}
