import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import '../utils/app_colors.dart';

class DeliveryAddressScreen extends StatefulWidget {
  const DeliveryAddressScreen({super.key});

  @override
  State<DeliveryAddressScreen> createState() => _DeliveryAddressScreenState();
}

class _DeliveryAddressScreenState extends State<DeliveryAddressScreen> {
  final _addressController = TextEditingController();
  final _cityController = TextEditingController();
  final _zipController = TextEditingController();
  bool _isLoading = false;

  void _saveAddress() async {
    final user = FirebaseAuth.instance.currentUser;
    if (user == null) return;

    if (_addressController.text.isEmpty || _cityController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Please fill in Address and City")));
      return;
    }

    setState(() => _isLoading = true);

    try {
      final ref = FirebaseDatabase.instance.ref().child('users').child(user.uid).child('delivery_address');
      await ref.set({
        'address': _addressController.text.trim(),
        'city': _cityController.text.trim(),
        'zip': _zipController.text.trim(),
      });

      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Address Saved!"), backgroundColor: AppColors.primaryGreen));
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
        title: const Text("Delivery Address", style: TextStyle(color: Colors.black)),
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: Colors.black),
      ),
      body: Padding(
        padding: const EdgeInsets.all(25.0),
        child: Column(
          children: [
            TextField(controller: _addressController, decoration: const InputDecoration(labelText: "Address Line")),
            const SizedBox(height: 15),
            TextField(controller: _cityController, decoration: const InputDecoration(labelText: "City")),
            const SizedBox(height: 15),
            TextField(controller: _zipController, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: "Zip Code")),
            const SizedBox(height: 40),
            SizedBox(
              width: double.infinity,
              height: 60,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _saveAddress,
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryGreen, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15))),
                child: _isLoading ? const CircularProgressIndicator(color: Colors.white) : const Text("Save Address", style: TextStyle(color: Colors.white, fontSize: 18)),
              ),
            )
          ],
        ),
      ),
    );
  }
}