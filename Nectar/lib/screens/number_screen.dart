import 'package:flutter/material.dart';
import '../utils/app_colors.dart';
import '../services/auth_service.dart';
import 'verification_screen.dart'; // We will create this next

class NumberScreen extends StatefulWidget {
  const NumberScreen({super.key});

  @override
  State<NumberScreen> createState() => _NumberScreenState();
}

class _NumberScreenState extends State<NumberScreen> {
  final TextEditingController _phoneController = TextEditingController();
  final AuthService _authService = AuthService();
  bool _isLoading = false;

  void _sendCode() async {
    String phone = _phoneController.text.trim();

    // Basic validation
    if (phone.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Enter a phone number")));
      return;
    }

    // Ensure format is correct (e.g., must contain country code if not typed)
    // For this example, assuming user types full international format or you append it
    // Example: if (!phone.startsWith('+')) phone = '+880$phone';

    setState(() => _isLoading = true);

    await _authService.verifyPhoneNumber(
      phone,
      onCodeSent: (verificationId) {
        setState(() => _isLoading = false);
        // Navigate to Verification Screen, passing the ID
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => VerificationScreen(verificationId: verificationId),
          ),
        );
      },
      onFailed: (error) {
        setState(() => _isLoading = false);
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error)));
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
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
              "Enter your mobile number",
              style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold, color: Colors.black),
            ),
            const SizedBox(height: 30),
            const Text("Mobile Number", style: TextStyle(color: Color(0xFF7C7C7C), fontSize: 16)),
            const SizedBox(height: 10),

            // Phone Input
            Row(
              children: [
                Image.asset('assets/images/flag_bd.png', height: 24, width: 34),
                const SizedBox(width: 12),
                Expanded(
                  child: TextField(
                    controller: _phoneController,
                    keyboardType: TextInputType.phone,
                    autofocus: true,
                    style: const TextStyle(fontSize: 18, color: Colors.black),
                    decoration: const InputDecoration(
                      hintText: "+8801712345678", // Example format
                      hintStyle: TextStyle(color: Color(0xFFB1B1B1)),
                      border: InputBorder.none,
                    ),
                  ),
                ),
              ],
            ),
            const Divider(color: Color(0xFFE2E2E2), thickness: 1),

            const Spacer(),

            // Next Button
            Align(
              alignment: Alignment.centerRight,
              child: FloatingActionButton(
                onPressed: _isLoading ? null : _sendCode,
                backgroundColor: AppColors.primaryGreen,
                elevation: 0,
                child: _isLoading
                    ? const CircularProgressIndicator(color: Colors.white)
                    : const Icon(Icons.arrow_forward_ios, color: Colors.white),
              ),
            ),
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }
}