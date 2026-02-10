import 'package:flutter/material.dart';
import 'package:nectar_grocery_app/screens/login_screen.dart';
import 'package:provider/provider.dart';
import '../utils/app_colors.dart';
import '../providers/location_provider.dart';
import 'home_screen.dart';

class LocationScreen extends StatefulWidget {
  const LocationScreen({super.key});

  @override
  State<LocationScreen> createState() => _LocationScreenState();
}

class _LocationScreenState extends State<LocationScreen> {
  String? selectedZone;
  String? selectedArea;

  final List<String> zones = ['Dhaka', 'Chittagong', 'Sylhet', 'Khulna'];
  final List<String> areas = [
    'Banasree',
    'Gulshan',
    'Dhanmondi',
    'Bashundhara',
    'Oxyzen',
    'Muradpur',
    'Raozan',
  ];

  void _submitLocation() {
    if (selectedZone == null || selectedArea == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please select both Zone and Area")),
      );
      return;
    }

    Provider.of<LocationProvider>(
      context,
      listen: false,
    ).updateLocation(selectedZone!, selectedArea!);

    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => const LoginScreen()),
    );
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
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 25.0),
          child: Column(
            children: [
              const SizedBox(height: 20),

              Image.asset(
                'assets/images/illustration.png',
                height: 170,
                errorBuilder: (c, o, s) => const Icon(
                  Icons.map,
                  size: 100,
                  color: AppColors.primaryGreen,
                ),
              ),
              const SizedBox(height: 40),

              const Text(
                "Select Your Location",
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.bold,
                  color: Colors.black,
                ),
              ),
              const SizedBox(height: 15),
              const Text(
                "Switch on your location to stay in tune with\nwhat’s happening in your area",
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Color(0xFF7C7C7C),
                  fontSize: 16,
                  height: 1.2,
                ),
              ),
              const SizedBox(height: 40),

              _buildDropdown(
                label: "Your Zone",
                hint: "Select your zone",
                value: selectedZone,
                items: zones,
                onChanged: (value) {
                  setState(() => selectedZone = value);
                },
              ),
              const SizedBox(height: 30),

              _buildDropdown(
                label: "Your Area",
                hint: "Types of your area",
                value: selectedArea,
                items: areas,
                onChanged: (value) {
                  setState(() => selectedArea = value);
                },
              ),
              const SizedBox(height: 40),

              SizedBox(
                width: double.infinity,
                height: 67,
                child: ElevatedButton(
                  onPressed: _submitLocation,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryGreen,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(19),
                    ),
                    elevation: 0,
                  ),
                  child: const Text(
                    "Submit",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 30),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDropdown({
    required String label,
    required String hint,
    required String? value,
    required List<String> items,
    required Function(String?) onChanged,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            color: Color(0xFF7C7C7C),
            fontSize: 16,
            fontWeight: FontWeight.w500,
          ),
        ),
        DropdownButtonFormField<String>(
          initialValue: value,
          icon: const Icon(Icons.keyboard_arrow_down, color: Color(0xFF7C7C7C)),
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: const TextStyle(color: Color(0xFFB1B1B1)),
            enabledBorder: const UnderlineInputBorder(
              borderSide: BorderSide(color: Color(0xFFE2E2E2)),
            ),
            focusedBorder: const UnderlineInputBorder(
              borderSide: BorderSide(color: AppColors.primaryGreen),
            ),
            contentPadding: const EdgeInsets.symmetric(vertical: 15),
            isDense: true,
          ),
          items: items.map((String item) {
            return DropdownMenuItem<String>(
              value: item,
              child: Text(
                item,
                style: const TextStyle(color: Colors.black, fontSize: 18),
              ),
            );
          }).toList(),
          onChanged: onChanged,
        ),
      ],
    );
  }
}
