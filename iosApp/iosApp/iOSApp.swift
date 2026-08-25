import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        SmartHomeKoinKt.startSmartHomeDependencies(logNetworkTraffic: false)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}