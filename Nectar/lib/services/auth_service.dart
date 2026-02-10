import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:flutter_facebook_auth/flutter_facebook_auth.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;

  // =========================================================
  // PHONE NUMBER (OTP) LOGIN
  // =========================================================

  // 1. Send OTP (Trigger the SMS)
  Future<void> verifyPhoneNumber(
      String phoneNumber, {
        required Function(String verificationId) onCodeSent,
        required Function(String error) onFailed,
      }) async {
    await _auth.verifyPhoneNumber(
      phoneNumber: phoneNumber,

      // Handle automatic verification (Android only)
      verificationCompleted: (PhoneAuthCredential credential) async {
        await _auth.signInWithCredential(credential);
      },

      // Handle failure (e.g., invalid format)
      verificationFailed: (FirebaseAuthException e) {
        onFailed(e.message ?? "Phone verification failed");
      },

      // Handle when code is sent successfully
      codeSent: (String verificationId, int? resendToken) {
        onCodeSent(verificationId);
      },

      // Handle timeout
      codeAutoRetrievalTimeout: (String verificationId) {
        // You can handle timeout logic here if needed
      },
    );
  }

  // 2. Verify OTP (Complete the Sign In)
  Future<User?> signInWithOTP(String verificationId, String smsCode) async {
    try {
      // Create credential from the ID and the code user typed
      PhoneAuthCredential credential = PhoneAuthProvider.credential(
        verificationId: verificationId,
        smsCode: smsCode,
      );

      // Sign in with that credential
      UserCredential userCredential = await _auth.signInWithCredential(credential);
      return userCredential.user;
    } catch (e) {
      rethrow;
    }
  }

  // =========================================================
  // SOCIAL LOGIN (Google & Facebook)
  // =========================================================

  // --- Google Sign In ---
  Future<User?> signInWithGoogle() async {
    try {
      final GoogleSignInAccount? googleUser = await GoogleSignIn().signIn();
      if (googleUser == null) return null; // User canceled

      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;

      final OAuthCredential credential = GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      UserCredential result = await _auth.signInWithCredential(credential);
      return result.user;
    } catch (e) {
      print("Google Sign In Error: $e");
      rethrow;
    }
  }

  // --- Facebook Sign In ---
  Future<User?> signInWithFacebook() async {
    try {
      final LoginResult result = await FacebookAuth.instance.login();

      if (result.status == LoginStatus.success) {
        final OAuthCredential credential = FacebookAuthProvider.credential(
          result.accessToken!.tokenString,
        );

        final UserCredential userCredential = await _auth.signInWithCredential(credential);
        return userCredential.user;
      } else {
        return null;
      }
    } catch (e) {
      print("Facebook Sign In Error: $e");
      rethrow;
    }
  }

  // =========================================================
  // EMAIL & PASSWORD LOGIN
  // =========================================================

  // --- Sign Up ---
  Future<User?> signUp(String email, String password, String username) async {
    try {
      UserCredential result = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );
      User? user = result.user;
      await user?.updateDisplayName(username);
      return user;
    } catch (e) {
      rethrow;
    }
  }

  // --- Log In ---
  Future<User?> signIn(String email, String password) async {
    try {
      UserCredential result = await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );
      return result.user;
    } catch (e) {
      rethrow;
    }
  }

  // =========================================================
  // UTILITIES
  // =========================================================

  // --- Sign Out ---
  Future<void> signOut() async {
    await GoogleSignIn().signOut();
    await FacebookAuth.instance.logOut();
    await _auth.signOut();
  }

  // --- Get Current User ---
  User? getCurrentUser() {
    return _auth.currentUser;
  }
}