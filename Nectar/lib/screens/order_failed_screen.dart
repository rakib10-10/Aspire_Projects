import 'package:flutter/material.dart';
import 'package:nectar_grocery_app/screens/cart_screen.dart';
import '../utils/app_colors.dart';

class OrderFailedScreen extends StatelessWidget {
  const OrderFailedScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 25.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [

              Image.asset(
                'assets/images/order_failed.png',
                height: 250,
                fit: BoxFit.contain,
              ),
              const SizedBox(height: 40),


              const Text(
                "Order Failed",
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: AppColors.darkText,
                ),
              ),
              const SizedBox(height: 20),
              const Text(
                "Something went wrong. Please try again\nto continue your order.",
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  color: AppColors.greyText,
                  height: 1.2,
                ),
              ),
              const SizedBox(height: 60),


              SizedBox(
                width: double.infinity,
                height: 67,
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(builder: (context) => const CartScreen()),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryGreen,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                    elevation: 0,
                  ),
                  child: const Text(
                    "Please Try Again",
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              TextButton(
                onPressed: () {
                  // Navigate back to Home
                  // Navigator.pushAndRemoveUntil(
                  //   context,
                  //   MaterialPageRoute(builder: (context) => const HomeScreen()),
                  //   (route) => false,
                  // );
                  Navigator.pop(context);
                },
                child: const Text(
                  "Back to home",
                  style: TextStyle(color: AppColors.darkText, fontSize: 18, fontWeight: FontWeight.w600),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}