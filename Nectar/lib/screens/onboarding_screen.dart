import 'package:flutter/material.dart';
import 'signin_screen.dart';

class OnboardingScreen extends StatelessWidget {
  const OnboardingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [

          Container(
            decoration: const BoxDecoration(
              image: DecorationImage(
                image: AssetImage("assets/images/welcome2.jpg"),
                fit: BoxFit.cover,
              ),
            ),
          ),

          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.transparent,
                  Colors.black.withValues(alpha: 0.9)
                ],
              ),
            ),
          ),

          SafeArea(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                Center(child: Image.asset('assets/images/carrot_image_splashscreen.png', height: 60)),
                const SizedBox(height: 20),
                const Text(
                  'Welcome\nto our store',
                  textAlign: TextAlign.center,
                  style: TextStyle(color: Colors.white, fontSize: 48, fontWeight: FontWeight.bold),
                ),
                const Text(
                  'Get your groceries in as fast as one hour',
                  style: TextStyle(color: Colors.white70, fontSize: 16),
                ),
                const SizedBox(height: 40),

                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 30),
                  child: SizedBox(
                    width: double.infinity,
                    height: 60,
                    child: ElevatedButton(
                        onPressed: () {
                        Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const SignInScreen())
                        );
                        },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF53B175),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
                      ),
                      child: const Text('Get Started', style: TextStyle(color: Colors.white, fontSize: 18)),
                    ),
                  ),
                ),
                const SizedBox(height: 90),
              ],
            ),
          ),
        ],
      ),
    );
  }
}