import 'package:flutter/material.dart';
import '../models/order_model.dart';

class OrderProvider extends ChangeNotifier {
  final List<OrderModel> _orders = [];

  List<OrderModel> get orders => _orders;

  void addOrder(OrderModel order) {
    _orders.insert(0, order); // Add new order to the top
    notifyListeners();
  }
}