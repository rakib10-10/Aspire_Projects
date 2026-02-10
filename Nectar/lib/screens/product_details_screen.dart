import 'package:flutter/material.dart';
import 'package:provider/provider.dart'; // <--- Required for State Management
import '../models/product_model.dart';
import '../providers/cart_provider.dart'; // <--- Required to access the Cart
import '../providers/favorite_provider.dart'; // <--- Required to access Favorites
import '../utils/app_colors.dart';

class ProductDetailsScreen extends StatefulWidget {
  final Product product;

  const ProductDetailsScreen({super.key, required this.product});

  @override
  State<ProductDetailsScreen> createState() => _ProductDetailsScreenState();
}

class _ProductDetailsScreenState extends State<ProductDetailsScreen> {
  int _quantity = 1;        // Local state for how many items to add
  bool _isExpanded = false; // Controls the "Product Detail" dropdown

  // --- Logic to Add to Cart ---
  void _addToBasket() {
    // 1. Access the CartProvider
    final cart = Provider.of<CartProvider>(context, listen: false);

    // 2. Add the product with the selected quantity
    cart.addToCart(widget.product, _quantity);

    // 3. Show feedback to the user
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text("${widget.product.name} added to cart!"),
        backgroundColor: AppColors.primaryGreen,
        duration: const Duration(seconds: 1),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // --- WATCH FAVORITE STATE ---
    // This listens to changes. If this product is favorited elsewhere, it updates here too.
    final favoriteProvider = Provider.of<FavoriteProvider>(context);
    final isFavorite = favoriteProvider.isFavorite(widget.product);

    return Scaffold(
      backgroundColor: Colors.white,

      // Transparent AppBar with Back and Share buttons
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios, color: Colors.black),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.ios_share, color: Colors.black),
            onPressed: () {
              // Share logic can go here later
            },
          )
        ],
      ),

      body: Column(
        children: [
          // --- SCROLLABLE CONTENT ---
          Expanded(
            child: SingleChildScrollView(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 25.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // 1. Product Image
                    Container(
                      height: 250,
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: const Color(0xFFF2F3F2),
                        borderRadius: BorderRadius.circular(25),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(25.0),
                        child: widget.product.imageUrl.startsWith('http')
                            ? Image.network(widget.product.imageUrl, fit: BoxFit.contain)
                            : Image.asset(
                          widget.product.imageUrl,
                          fit: BoxFit.contain,
                          errorBuilder: (c, o, s) => const Icon(Icons.image_not_supported, size: 80, color: Colors.grey),
                        ),
                      ),
                    ),
                    const SizedBox(height: 30),

                    // 2. Name and Favorite Icon (UPDATED LOGIC)
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Text(
                            widget.product.name,
                            style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        // --- HEART BUTTON ---
                        IconButton(
                          onPressed: () {
                            favoriteProvider.toggleFavorite(widget.product);
                          },
                          icon: Icon(
                            isFavorite ? Icons.favorite : Icons.favorite_border, // Filled or Outline
                            color: isFavorite ? const Color(0xFFFF5252) : const Color(0xFF7C7C7C), // Red or Grey
                            size: 30,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),

                    // 3. Unit / Weight
                    Text(
                      widget.product.unit,
                      style: const TextStyle(fontSize: 16, color: Color(0xFF7C7C7C), fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 30),

                    // 4. Quantity Selector and Price
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        // (-) Qty (+)
                        Row(
                          children: [
                            IconButton(
                              onPressed: () {
                                if (_quantity > 1) setState(() => _quantity--);
                              },
                              icon: const Icon(Icons.remove, color: Color(0xFFB3B3B3), size: 30),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
                              decoration: BoxDecoration(
                                border: Border.all(color: const Color(0xFFE2E2E2)),
                                borderRadius: BorderRadius.circular(17),
                              ),
                              child: Text(
                                "$_quantity",
                                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                              ),
                            ),
                            IconButton(
                              onPressed: () => setState(() => _quantity++),
                              icon: const Icon(Icons.add, color: AppColors.primaryGreen, size: 30),
                            ),
                          ],
                        ),

                        // Total Price for selected Quantity
                        Text(
                          "\$${(widget.product.price * _quantity).toStringAsFixed(2)}",
                          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    const SizedBox(height: 30),

                    // 5. Product Description Dropdown
                    const Divider(color: Color(0xFFE2E2E2)),
                    Theme(
                      data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
                      child: ExpansionTile(
                        title: const Text("Product Detail", style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: Colors.black)),
                        trailing: Icon(_isExpanded ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down, color: Colors.black),
                        tilePadding: EdgeInsets.zero,
                        childrenPadding: EdgeInsets.zero,
                        onExpansionChanged: (expanded) => setState(() => _isExpanded = expanded),
                        children: [
                          Text(
                            widget.product.description.isEmpty ? "No description available." : widget.product.description,
                            style: const TextStyle(color: Color(0xFF7C7C7C), height: 1.5, fontSize: 14),
                          ),
                        ],
                      ),
                    ),
                    const Divider(color: Color(0xFFE2E2E2)),

                    // Extra spacing at bottom
                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ),
          ),

          // --- ADD TO BASKET BUTTON ---
          Padding(
            padding: const EdgeInsets.all(25.0),
            child: SizedBox(
              width: double.infinity,
              height: 67,
              child: ElevatedButton(
                onPressed: _addToBasket, // Calls the function defined above
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryGreen,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                  elevation: 0,
                ),
                child: const Text(
                  "Add To Basket",
                  style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}