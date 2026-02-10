import 'package:flutter/material.dart';
import 'dart:async';

// Replace with your main home screen
import 'onboarding_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  // Inside your _SplashScreenState:
  @override
  void initState() {
    super.initState();
    Timer(const Duration(seconds: 3), () {
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const OnboardingScreen()),
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // The color from the image is a specific shade of green
    const Color nectarGreen = Color(0xFF53B175);

    return Scaffold(
      // Set the background color of the entire screen
      backgroundColor: nectarGreen,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset('assets/images/carrot_image_splashscreen.png'),
            const SizedBox(height: 16),
            // "nectar" Text
            const Text(
              'nectar',
              style: TextStyle(
                fontSize: 50,
                fontWeight: FontWeight.bold,
                color: Colors.white,
                letterSpacing: 1.2,
              ),
            ),
            // "online grocery" Text
            Text(
              'online grocery',
              style: TextStyle(
                fontSize: 18,
                color: Colors.white.withValues(alpha: 0.9),
                letterSpacing: 2.5,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// A dummy HomePage for navigation purposes
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Home")),
      body: const Center(child: Text("Welcome to Nectar!")),
    );
  }
}