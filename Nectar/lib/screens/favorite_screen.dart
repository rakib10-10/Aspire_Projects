import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../utils/app_colors.dart';
import '../providers/favorite_provider.dart';
import '../providers/cart_provider.dart';
import 'product_details_screen.dart'; // To allow clicking on items

class FavouriteScreen extends StatelessWidget {
  const FavouriteScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<FavoriteProvider>(
      builder: (context, favProvider, child) {
        final favoriteItems = favProvider.favorites;

        return Scaffold(
          backgroundColor: Colors.white,
          appBar: AppBar(
            backgroundColor: Colors.white,
            elevation: 0,
            centerTitle: true,
            title: const Text(
              "Favorites",
              style: TextStyle(color: AppColors.darkText, fontWeight: FontWeight.bold, fontSize: 20),
            ),
            bottom: PreferredSize(
              preferredSize: const Size.fromHeight(1.0),
              child: Container(color: const Color(0xFFE2E2E2), height: 1.0),
            ),
          ),

          body: favoriteItems.isEmpty
              ? const Center(child: Text("No favorites yet", style: TextStyle(color: Colors.grey, fontSize: 18)))
              : Column(
            children: [
              // 1. List of Favorites
              Expanded(
                child: ListView.separated(
                  padding: const EdgeInsets.symmetric(vertical: 20),
                  itemCount: favoriteItems.length,
                  separatorBuilder: (context, index) => const Divider(color: Color(0xFFE2E2E2), indent: 25, endIndent: 25),
                  itemBuilder: (context, index) {
                    final product = favoriteItems[index];
                    return ListTile(
                      contentPadding: const EdgeInsets.symmetric(horizontal: 25, vertical: 10),
                      onTap: () {
                        // Navigate to details if user clicks the row
                        Navigator.push(
                            context,
                            MaterialPageRoute(builder: (c) => ProductDetailsScreen(product: product))
                        );
                      },
                      leading: SizedBox(
                        height: 60, width: 60,
                        child: product.imageUrl.startsWith('http')
                            ? Image.network(product.imageUrl, fit: BoxFit.contain)
                            : Image.asset(product.imageUrl, fit: BoxFit.contain),
                      ),
                      title: Text(
                        product.name,
                        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.darkText),
                      ),
                      subtitle: Text(
                        product.unit,
                        style: const TextStyle(fontSize: 14, color: AppColors.greyText),
                      ),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            "\$${product.price}",
                            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.darkText),
                          ),
                          const SizedBox(width: 10),
                          const Icon(Icons.arrow_forward_ios, size: 14, color: AppColors.darkText),
                        ],
                      ),
                    );
                  },
                ),
              ),

              // 2. Add All To Cart Button
              Padding(
                padding: const EdgeInsets.all(25.0),
                child: SizedBox(
                  width: double.infinity,
                  height: 67,
                  child: ElevatedButton(
                    onPressed: () {
                      if (favoriteItems.isEmpty) return;

                      // Get access to Cart Provider
                      final cartProvider = Provider.of<CartProvider>(context, listen: false);

                      // Loop through favorites and add 1 of each to cart
                      for (var product in favoriteItems) {
                        cartProvider.addToCart(product, 1);
                      }

                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text("All favorites added to cart!"),
                          backgroundColor: AppColors.primaryGreen,
                        ),
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primaryGreen,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                      elevation: 0,
                    ),
                    child: const Text(
                      "Add All To Cart",
                      style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}