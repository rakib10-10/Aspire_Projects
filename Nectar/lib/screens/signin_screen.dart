import 'package:flutter/material.dart';
import '../utils/app_colors.dart';
import 'number_screen.dart';
import '../services/auth_service.dart';
import 'home_screen.dart';

class SignInScreen extends StatefulWidget {
  const SignInScreen({super.key});

  @override
  State<SignInScreen> createState() => _SignInScreenState();
}

class _SignInScreenState extends State<SignInScreen> {
  final AuthService _authService = AuthService();
  bool _isLoading = false;


  void _handleSocialLogin(Future<dynamic> Function() signInMethod) async {
    setState(() => _isLoading = true);
    try {
      final user = await signInMethod();
      if (user != null && mounted) {

        Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (context) => const HomeScreen()),
              (route) => false,
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Login Failed: ${e.toString()}")),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final ButtonStyle socialButtonStyle = ElevatedButton.styleFrom(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
      minimumSize: const Size(double.infinity, 67),
      elevation: 0,
      padding: const EdgeInsets.symmetric(horizontal: 20),
    );

    return Scaffold(
      backgroundColor: Colors.white,
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: AppColors.primaryGreen))
          : SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [

            SizedBox(
              height: 400,
              width: double.infinity,
              child: Stack(
                children: [
                  Positioned(
                    top: -50,
                    right: -40,
                    left: -20,
                    bottom: 0,
                    child: Transform.rotate(
                      angle: 0.1,
                      child: Image.asset(
                        "assets/images/signup2.jpg",
                        fit: BoxFit.contain,
                        alignment: Alignment.topCenter,
                        errorBuilder: (c, o, s) => Container(color: Colors.grey[100]),
                      ),
                    ),
                  ),
                  Positioned(
                    bottom: 0,
                    left: 0,
                    right: 0,
                    height: 100,
                    child: Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [
                            Colors.white.withOpacity(0.0),
                            Colors.white
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),


            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 25.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Get your groceries\nwith nectar',
                    style: TextStyle(
                      fontSize: 26,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF030303),
                      height: 1.2,
                    ),
                  ),
                  const SizedBox(height: 30),


                  const Text(
                      'Phone Number',
                      style: TextStyle(color: Color(0xFF7C7C7C), fontSize: 16)
                  ),
                  const SizedBox(height: 10),

                  Row(
                    children: [
                      Image.asset(
                        'assets/images/flag_bd.png',
                        height: 24,
                        width: 34,
                        errorBuilder: (c,o,s) => const Text('🇧🇩', style: TextStyle(fontSize: 24)),
                      ),
                      const SizedBox(width: 12),
                      const Text(
                        '+880',
                        style: TextStyle(fontSize: 18, color: Colors.black),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: TextField(
                          readOnly: true,
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(builder: (context) => const NumberScreen()),
                            );
                          },
                          decoration: const InputDecoration(
                            border: InputBorder.none,
                            hintText: 'Enter your number',
                            hintStyle: TextStyle(color: Color(0xFF7C7C7C)),
                            contentPadding: EdgeInsets.zero,
                            isDense: true,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const Divider(color: Color(0xFFE2E2E2), thickness: 1),

                  const SizedBox(height: 40),

                  const Center(
                    child: Text(
                      'Or connect with social media',
                      style: TextStyle(
                          color: Color(0xFF828282),
                          fontSize: 14,
                          fontWeight: FontWeight.w600
                      ),
                    ),
                  ),
                  const SizedBox(height: 40),


                  ElevatedButton(
                    onPressed: () => _handleSocialLogin(_authService.signInWithGoogle),
                    style: socialButtonStyle.copyWith(
                      backgroundColor: const WidgetStatePropertyAll(Color(0xFF5383EC)),
                    ),
                    child: Row(
                      children: [
                        const SizedBox(width: 10),


                        Image.asset(
                          'assets/images/google_logo.png',
                          height: 24,
                          width: 24,
                          fit: BoxFit.contain,
                        ),

                        const Expanded(
                          child: Text(
                            'Continue with Google',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                color: Colors.white,
                                fontSize: 18,
                                fontWeight: FontWeight.w600
                            ),
                          ),
                        ),
                        const SizedBox(width: 34),
                      ],
                    ),
                  ),

                  const SizedBox(height: 20),


                  ElevatedButton(
                    onPressed: () => _handleSocialLogin(_authService.signInWithFacebook),
                    style: socialButtonStyle.copyWith(
                      backgroundColor: const WidgetStatePropertyAll(Color(0xFF4A66AC)),
                    ),
                    child: const Row(
                      children: [
                        SizedBox(width: 10),
                        Icon(Icons.facebook, color: Colors.white, size: 30),
                        Expanded(
                          child: Text(
                            'Continue with Facebook',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                color: Colors.white,
                                fontSize: 18,
                                fontWeight: FontWeight.w600
                            ),
                          ),
                        ),
                        SizedBox(width: 40),
                      ],
                    ),
                  ),
                  const SizedBox(height: 50),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}