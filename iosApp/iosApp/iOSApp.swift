import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        SmartHomeKoinIosKt.startSmartHomeDependencies(logNetworkTraffic: false)
        IosVideoPlayback.shared.provider = { VlcVideoPlayback() }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}