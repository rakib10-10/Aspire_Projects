import 'package:flutter/material.dart';
import '../models/product_model.dart';

class CartItem {
  final Product product;
  int quantity;

  CartItem({required this.product, required this.quantity});
}

class CartProvider extends ChangeNotifier {
  // Internal list of cart items
  final List<CartItem> _items = [];

  // Getter to access items from outside
  List<CartItem> get items => _items;

  // Calculate Total Price
  double get totalPrice {
    double total = 0;
    for (var item in _items) {
      total += item.product.price * item.quantity;
    }
    return total;
  }

  // Add Item to Cart
  void addToCart(Product product, int quantity) {
    // Check if item already exists
    int index = _items.indexWhere((item) => item.product.name == product.name);

    if (index >= 0) {
      // If exists, just update quantity
      _items[index].quantity += quantity;
    } else {
      // If new, add to list
      _items.add(CartItem(product: product, quantity: quantity));
    }

    notifyListeners(); // Update UI
  }

  // Remove Item
  void removeFromCart(CartItem item) {
    _items.remove(item);
    notifyListeners();
  }

  // Increase Quantity inside Cart
  void increaseQty(CartItem item) {
    item.quantity++;
    notifyListeners();
  }

  // Decrease Quantity inside Cart
  void decreaseQty(CartItem item) {
    if (item.quantity > 1) {
      item.quantity--;
    } else {
      _items.remove(item);
    }
    notifyListeners();
  }

  // Clear Cart (optional, for checkout)
  void clearCart() {
    _items.clear();
    notifyListeners();
  }
}