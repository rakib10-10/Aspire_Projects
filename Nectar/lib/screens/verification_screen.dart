import 'package:flutter/material.dart';
import '../utils/app_colors.dart';
import '../services/auth_service.dart';
import 'location_screen.dart';

class VerificationScreen extends StatefulWidget {
  final String verificationId;

  const VerificationScreen({super.key, required this.verificationId});

  @override
  State<VerificationScreen> createState() => _VerificationScreenState();
}

class _VerificationScreenState extends State<VerificationScreen> {
  final TextEditingController _codeController = TextEditingController();
  final AuthService _authService = AuthService();
  bool _isLoading = false;

  void _verifyCode() async {
    String smsCode = _codeController.text.trim();

    if (smsCode.length < 6) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text("Enter a 6-digit code")));
      return;
    }

    setState(() => _isLoading = true);

    try {
      final user = await _authService.signInWithOTP(
        widget.verificationId,
        smsCode,
      );

      if (user != null && mounted) {
        Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (context) => const LocationScreen()),
          (route) => false,
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Invalid Code: ${e.toString()}")),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios, color: Colors.black),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 25.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 40),
            const Text(
              "Enter your 4-digit code",
              style: TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.bold,
                color: Colors.black,
              ),
            ),
            const SizedBox(height: 30),
            const Text(
              "Code",
              style: TextStyle(color: Color(0xFF7C7C7C), fontSize: 16),
            ),
            const SizedBox(height: 10),

            TextField(
              controller: _codeController,
              keyboardType: TextInputType.number,
              maxLength: 6,
              autofocus: true,
              style: const TextStyle(
                fontSize: 18,
                color: Colors.black,
                letterSpacing: 5,
              ),
              decoration: const InputDecoration(
                hintText: "- - - - - -",
                hintStyle: TextStyle(color: Colors.grey),
                counterText: "",
              ),
            ),

            const Spacer(),

            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                TextButton(
                  onPressed: () {
                    Navigator.pop(context);
                  },
                  child: const Text(
                    "Resend Code",
                    style: TextStyle(
                      color: AppColors.primaryGreen,
                      fontSize: 18,
                    ),
                  ),
                ),

                FloatingActionButton(
                  onPressed: _isLoading ? null : _verifyCode,
                  backgroundColor: AppColors.primaryGreen,
                  elevation: 0,
                  child: _isLoading
                      ? const CircularProgressIndicator(color: Colors.white)
                      : const Icon(
                          Icons.arrow_forward_ios,
                          color: Colors.white,
                        ),
                ),
              ],
            ),
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }
}
