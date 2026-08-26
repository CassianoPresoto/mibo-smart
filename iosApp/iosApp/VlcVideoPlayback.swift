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
    private var pendingSnapshotPath: String?

    override init() {
        super.init()
        surface.backgroundColor = .black
        player.delegate = self
        player.drawable = surface
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(snapshotTaken),
            name: VLCMediaPlayer.snapshotTakenNotification,
            object: player
        )
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
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
        pendingSnapshotPath = nil
        player.stop()
    }

    func takeSnapshot(path: String) {
        pendingSnapshotPath = path
        player.saveVideoSnapshot(at: path, withWidth: 0, andHeight: 0)
    }

    func startRecording(directoryPath: String) {
        player.startRecording(atPath: directoryPath)
    }

    func stopRecording() {
        player.stopRecording()
    }

    func mediaPlayerStateChanged(_ newState: VLCMediaPlayerState) {
        report(newState)
    }

    func mediaPlayerStartedRecording(_ player: VLCMediaPlayer) {
        listener?.onRecordingStarted()
    }

    func mediaPlayer(_ player: VLCMediaPlayer, recordingStoppedAt url: URL?) {
        listener?.onRecordingStopped(path: url?.path)
    }

    @objc private func snapshotTaken() {
        let savedPath = pendingSnapshotPath
        pendingSnapshotPath = nil
        listener?.onSnapshotTaken(path: savedPath)
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
