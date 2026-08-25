import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        SmartHomeKoinIosKt.startSmartHomeDependencies(logNetworkTraffic: false)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}