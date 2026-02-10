import 'package:flutter/material.dart';

class LocationProvider extends ChangeNotifier {
  // Default values
  String _zone = "Dhaka";
  String _area = "Banasree";

  String get zone => _zone;
  String get area => _area;

  // Function to update location
  void updateLocation(String newZone, String newArea) {
    _zone = newZone;
    _area = newArea;
    notifyListeners(); // Tells HomeScreen to refresh
  }
}