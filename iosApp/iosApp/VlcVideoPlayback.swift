import Foundation
import UIKit
import VLCKit
import Shared

final class VlcVideoPlayback: NSObject, NativeVideoPlayback, VLCMediaPlayerDelegate {

    private let player = VLCMediaPlayer()
    private let surface = UIView()
    private var listener: NativeVideoPlaybackListener?
    private var reportedState: VLCMediaPlayerState?
    private var watcher: Timer?

    override init() {
        super.init()
        surface.backgroundColor = .black
        player.delegate = self
        player.drawable = surface
    }

    func view() -> UIView {
        surface
    }

    func start(url: String, listener: any NativeVideoPlaybackListener) {
        self.listener = listener
        reportedState = nil

        guard let streamUrl = URL(string: url) else {
            listener.onFailed()
            return
        }

        player.media = VLCMedia(url: streamUrl)
        player.play()
        watchState()
    }

    func stop() {
        watcher?.invalidate()
        watcher = nil
        listener = nil
        reportedState = nil
        player.stop()
    }

    func mediaPlayerStateChanged(_ newState: VLCMediaPlayerState) {
        report(newState)
    }

    private func watchState() {
        watcher?.invalidate()
        watcher = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] _ in
            guard let self else { return }
            self.report(self.player.state)
        }
    }

    private func report(_ state: VLCMediaPlayerState) {
        guard state != reportedState, let listener else { return }
        reportedState = state

        switch state {
        case .opening:
            listener.onBuffering()
        case .playing:
            listener.onPlaying()
        case .stopped:
            listener.onEnded()
        case .error:
            listener.onFailed()
        default:
            break
        }
    }
}
