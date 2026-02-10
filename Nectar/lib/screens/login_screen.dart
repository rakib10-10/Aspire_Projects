import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import '../utils/app_colors.dart';
import '../services/auth_service.dart'; // Import AuthService
import 'home_screen.dart';
import 'signup_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final AuthService _authService = AuthService();

  // Text Controllers
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  bool _isPasswordVisible = false;
  bool _isLoading = false;

  void _handleLogin() async {
    if (_emailController.text.isEmpty || _passwordController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter email and password")),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      await _authService.signIn(
        _emailController.text.trim(),
        _passwordController.text.trim(),
      );

      if (mounted) {
        // Navigate to Home on success
        Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (context) => const HomeScreen()),
              (route) => false,
        );
      }
    } on FirebaseAuthException catch (e) {
      String message = "Login Failed";
      if (e.code == 'user-not-found') message = "No user found for that email.";
      if (e.code == 'wrong-password') message = "Wrong password provided.";

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 25.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 60),
              Center(
                child: Image.asset(
                  'assets/images/logo_orange.png',
                  height: 50,
                  errorBuilder: (c, o, s) => const Icon(Icons.eco, color: AppColors.primaryGreen, size: 50),
                ),
              ),
              const SizedBox(height: 60),

              const Text(
                "Log In",
                style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold, color: Colors.black),
              ),
              const SizedBox(height: 10),
              const Text(
                "Enter your email and password",
                style: TextStyle(color: Color(0xFF7C7C7C), fontSize: 16),
              ),
              const SizedBox(height: 40),

              const Text("Email", style: TextStyle(color: Color(0xFF7C7C7C), fontSize: 16)),
              TextField(
                controller: _emailController, // Connect Controller
                keyboardType: TextInputType.emailAddress,
                style: const TextStyle(fontSize: 18, color: Colors.black),
                decoration: const InputDecoration(
                  hintText: "imshuvo97@gmail.com",
                  hintStyle: TextStyle(color: Color(0xFFB1B1B1)),
                  enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFFE2E2E2))),
                  focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.primaryGreen)),
                ),
              ),
              const SizedBox(height: 30),

              const Text("Password", style: TextStyle(color: Color(0xFF7C7C7C), fontSize: 16)),
              TextField(
                controller: _passwordController, // Connect Controller
                obscureText: !_isPasswordVisible,
                style: const TextStyle(fontSize: 18, color: Colors.black),
                decoration: InputDecoration(
                  hintText: "********",
                  hintStyle: const TextStyle(color: Color(0xFFB1B1B1)),
                  enabledBorder: const UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFFE2E2E2))),
                  focusedBorder: const UnderlineInputBorder(borderSide: BorderSide(color: AppColors.primaryGreen)),
                  suffixIcon: IconButton(
                    icon: Icon(
                      _isPasswordVisible ? Icons.visibility : Icons.visibility_off,
                      color: const Color(0xFF7C7C7C),
                    ),
                    onPressed: () {
                      setState(() {
                        _isPasswordVisible = !_isPasswordVisible;
                      });
                    },
                  ),
                ),
              ),

              Align(
                alignment: Alignment.centerRight,
                child: TextButton(
                  onPressed: () {},
                  child: const Text("Forgot Password?", style: TextStyle(color: Colors.black, fontSize: 14)),
                ),
              ),
              const SizedBox(height: 30),

              SizedBox(
                width: double.infinity,
                height: 67,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _handleLogin, // Disable while loading
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryGreen,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                    elevation: 0,
                  ),
                  child: _isLoading
                      ? const CircularProgressIndicator(color: Colors.white)
                      : const Text(
                    "Log In",
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
              const SizedBox(height: 25),

              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text("Don't have an account? ", style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
                  GestureDetector(
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const SignupScreen()),
                      );
                    },
                    child: const Text(
                      "Sign Up",
                      style: TextStyle(color: AppColors.primaryGreen, fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 30),
            ],
          ),
        ),
      ),
    );
  }
}