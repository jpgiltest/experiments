import UIKit
import Flutter
import GoogleMaps //Maps APIs


@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GMMServices.provideAPIKey("AIzaSyD-9tSrke72PouQMnMX-a7eZSW0jkFMBWY") //Maps APIs
    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
