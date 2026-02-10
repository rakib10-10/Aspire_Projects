// File: lib/firebase_options.dart
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart' show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for ios - '
              'you can reconfigure this by running the FlutterFire CLI again.',
        );
      case TargetPlatform.macOS:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for macos - '
              'you can reconfigure this by running the FlutterFire CLI again.',
        );
      case TargetPlatform.windows:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for windows - '
              'you can reconfigure this by running the FlutterFire CLI again.',
        );
      case TargetPlatform.linux:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for linux - '
              'you can reconfigure this by running the FlutterFire CLI again.',
        );
      default:
        throw UnsupportedError(
          'DefaultFirebaseOptions are not supported for this platform.',
        );
    }
  }

  // --- ANDROID CONFIGURATION (Extracted from your JSON) ---
  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyCPeiaoRm3YcKrPRWF8WyVJx6qHae3BGl8',
    appId: '1:711164649106:android:665ddcd96ae1c0d7a3904b',
    messagingSenderId: '711164649106',
    projectId: 'nectar-grocery-app-f34db',
    databaseURL: 'https://nectar-grocery-app-f34db-default-rtdb.firebaseio.com',
    storageBucket: 'nectar-grocery-app-f34db.firebasestorage.app',
  );

  // --- WEB CONFIGURATION ---
  // Note: google-services.json is mainly for Android.
  // I have re-used the keys that are shared (Database, Project ID).
  // If Web Login fails, check the "Web App ID" in Firebase Console.
  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyCPeiaoRm3YcKrPRWF8WyVJx6qHae3BGl8',
    appId: '1:711164649106:android:665ddcd96ae1c0d7a3904b', // Using Android ID as fallback. Ideally, create a Web App in console.
    messagingSenderId: '711164649106',
    projectId: 'nectar-grocery-app-f34db',
    authDomain: 'nectar-grocery-app-f34db.firebaseapp.com',
    storageBucket: 'nectar-grocery-app-f34db.firebasestorage.app',
    databaseURL: 'https://nectar-grocery-app-f34db-default-rtdb.firebaseio.com',
  );
}