import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart'; // Import Firebase Core
import 'package:nectar_grocery_app/providers/order_provider.dart';
import 'package:nectar_grocery_app/screens/splash_screen.dart';
import 'package:provider/provider.dart'; // Import Provider
import 'providers/cart_provider.dart'; // Import your new CartProvider
// Your existing start screen
import 'providers/favorite_provider.dart';
import 'providers/location_provider.dart';

import 'firebase_options.dart';
// Ensure you have generated firebase_options.dart if using FlutterFire CLI,
// otherwise just use Firebase.initializeApp() for basic setup if manually configured.

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Firebase.initializeApp(
    options:
        DefaultFirebaseOptions.currentPlatform, // <--- Use the options here
  );

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => CartProvider()),
        ChangeNotifierProvider(create: (_) => FavoriteProvider()),
        ChangeNotifierProvider(create: (_) => OrderProvider()),
        ChangeNotifierProvider(create: (_) => LocationProvider()),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nectar Grocery',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF53B175),
          primary: const Color(0xFF53B175),
        ),
        useMaterial3: true,
        fontFamily: 'Gilroy',
      ),
      home: const SplashScreen(),
    );
  }
}
