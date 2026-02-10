import 'package:flutter/material.dart';
import 'package:nectar_grocery_app/screens/home_screen.dart';
import 'package:nectar_grocery_app/screens/orders_screen.dart';
import '../utils/app_colors.dart';


class OrderAcceptedScreen extends StatelessWidget {
  const OrderAcceptedScreen({super.key});

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
              const Spacer(flex: 2),

              Image.asset(
                'assets/images/order_accepted.png',
                height: 250,
                fit: BoxFit.contain,
                errorBuilder: (c, o, s) => const Icon(Icons.check_circle_outline, size: 150, color: AppColors.primaryGreen),
              ),
              const SizedBox(height: 40),


              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 20.0),
                child: Text(
                  "Your Order Has Been Accepted",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.bold,
                    color: AppColors.darkText,
                  ),
                ),
              ),
              const SizedBox(height: 20),
              const Text(
                "Your items has been placed and is on\nit’s way to being processed",
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  color: AppColors.greyText,
                  height: 1.2,
                ),
              ),
              const Spacer(flex: 3),


              SizedBox(
                width: double.infinity,
                height: 67,
                child: ElevatedButton(
                  onPressed: () {
                    {
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(builder: (context) => const OrdersScreen()),
                      );
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryGreen,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                    elevation: 0,
                  ),
                  child: const Text(
                    "Track Order",
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              TextButton(
                onPressed: () {
                  {
                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(builder: (context) => const HomeScreen()),
                    );
                  }
                },
                child: const Text(
                  "Back to home",
                  style: TextStyle(color: AppColors.darkText, fontSize: 18, fontWeight: FontWeight.w600),
                ),
              ),
              const SizedBox(height: 30),
            ],
          ),
        ),
      ),
    );
  }
}