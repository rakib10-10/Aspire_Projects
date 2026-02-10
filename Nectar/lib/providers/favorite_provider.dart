import 'package:flutter/material.dart';
import '../models/product_model.dart';

class FavoriteProvider extends ChangeNotifier {
  final List<Product> _favorites = [];

  List<Product> get favorites => _favorites;

  // Check if a product is already favorited
  bool isFavorite(Product product) {
    // Compare by ID if available, or name as fallback
    return _favorites.any((item) => item.name == product.name);
  }

  // Toggle Favorite (Add or Remove)
  void toggleFavorite(Product product) {
    if (isFavorite(product)) {
      _favorites.removeWhere((item) => item.name == product.name);
    } else {
      _favorites.add(product);
    }
    notifyListeners();
  }
}