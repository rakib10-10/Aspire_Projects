import '../providers/cart_provider.dart';

class OrderModel {
  final String id;
  final String date;
  final double totalCost;
  final String
  status; // "Placed", "Processing", "On Way", "Delivered", "Failed"
  final String deliveryMethod; // "Home Delivery" or "Pick Up"
  final String paymentMethod; // "Card" or "COD"
  final List<CartItem> items;

  OrderModel({
    required this.id,
    required this.date,
    required this.totalCost,
    required this.status,
    required this.deliveryMethod,
    required this.paymentMethod,
    required this.items,
  });
}
