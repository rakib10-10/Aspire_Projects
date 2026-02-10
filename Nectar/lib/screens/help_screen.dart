import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import '../utils/app_colors.dart';

class HelpScreen extends StatefulWidget {
  const HelpScreen({super.key});

  @override
  State<HelpScreen> createState() => _HelpScreenState();
}

class _HelpScreenState extends State<HelpScreen> {
  final _msgController = TextEditingController();
  bool _isLoading = false;

  void _submitHelp() async {
    final user = FirebaseAuth.instance.currentUser;
    if (user == null || _msgController.text.trim().isEmpty) return;

    setState(() => _isLoading = true);

    try {
      final ref = FirebaseDatabase.instance.ref().child('help_messages').push();
      await ref.set({
        'uid': user.uid,
        'name': user.displayName ?? "Unknown",
        'email': user.email,
        'message': _msgController.text.trim(),
        'timestamp': ServerValue.timestamp,
      });

      // Clear input and show success
      _msgController.clear();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Message Sent! We will contact you soon."), backgroundColor: AppColors.primaryGreen));
      Navigator.pop(context);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Error: $e")));
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text("Help & Support", style: TextStyle(color: Colors.black)),
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: Colors.black),
      ),
      body: Padding(
        padding: const EdgeInsets.all(25.0),
        child: Column(
          children: [
            const Text("How can we help you?", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 20),
            TextField(
              controller: _msgController,
              maxLines: 5,
              decoration: InputDecoration(
                hintText: "Type your message here...",
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(15)),
                focusedBorder: OutlineInputBorder(borderSide: const BorderSide(color: AppColors.primaryGreen), borderRadius: BorderRadius.circular(15)),
              ),
            ),
            const SizedBox(height: 40),
            SizedBox(
              width: double.infinity,
              height: 60,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _submitHelp,
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryGreen, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15))),
                child: _isLoading ? const CircularProgressIndicator(color: Colors.white) : const Text("Submit", style: TextStyle(color: Colors.white, fontSize: 18)),
              ),
            )
          ],
        ),
      ),
    );
  }
}