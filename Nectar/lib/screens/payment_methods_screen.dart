import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import '../utils/app_colors.dart';

class PaymentMethodsScreen extends StatefulWidget {
  const PaymentMethodsScreen({super.key});

  @override
  State<PaymentMethodsScreen> createState() => _PaymentMethodsScreenState();
}

class _PaymentMethodsScreenState extends State<PaymentMethodsScreen> {
  final _cardNumController = TextEditingController();
  final _expiryController = TextEditingController();
  final _cvvController = TextEditingController();
  bool _isLoading = false;

  void _saveCard() async {
    final user = FirebaseAuth.instance.currentUser;
    if (user == null) return;

    if (_cardNumController.text.length < 13 || _cvvController.text.length < 3) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Invalid Card Details")));
      return;
    }

    setState(() => _isLoading = true);

    try {
      final ref = FirebaseDatabase.instance.ref().child('users').child(user.uid).child('payment_method');
      await ref.set({
        'cardNumber': _cardNumController.text.trim(),
        'expiry': _expiryController.text.trim(),
        'cvv': _cvvController.text.trim(),
      });

      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Card Added!"), backgroundColor: AppColors.primaryGreen));
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
        title: const Text("Payment Methods", style: TextStyle(color: Colors.black)),
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: Colors.black),
      ),
      body: Padding(
        padding: const EdgeInsets.all(25.0),
        child: Column(
          children: [
            TextField(controller: _cardNumController, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: "Card Number", hintText: "XXXX XXXX XXXX XXXX")),
            const SizedBox(height: 15),
            Row(
              children: [
                Expanded(child: TextField(controller: _expiryController, decoration: const InputDecoration(labelText: "Expiry Date", hintText: "MM/YY"))),
                const SizedBox(width: 15),
                Expanded(child: TextField(controller: _cvvController, keyboardType: TextInputType.number, maxLength: 3, decoration: const InputDecoration(labelText: "CVV"))),
              ],
            ),
            const SizedBox(height: 40),
            SizedBox(
              width: double.infinity,
              height: 60,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _saveCard,
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryGreen, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15))),
                child: _isLoading ? const CircularProgressIndicator(color: Colors.white) : const Text("Save Card", style: TextStyle(color: Colors.white, fontSize: 18)),
              ),
            )
          ],
        ),
      ),
    );
  }
}