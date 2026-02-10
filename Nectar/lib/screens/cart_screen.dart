import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../utils/app_colors.dart';
import '../providers/cart_provider.dart';
import 'checkout_bottom_sheet.dart';

class CartScreen extends StatelessWidget {
  const CartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Use Consumer to listen to changes in CartProvider
    return Consumer<CartProvider>(
      builder: (context, cart, child) {
        return Scaffold(
          backgroundColor: Colors.white,
          appBar: AppBar(
            backgroundColor: Colors.white,
            elevation: 0,
            centerTitle: true,
            title: const Text(
              "My Cart",
              style: TextStyle(color: AppColors.darkText, fontWeight: FontWeight.bold, fontSize: 20),
            ),
            bottom: PreferredSize(
              preferredSize: const Size.fromHeight(1.0),
              child: Container(color: const Color(0xFFE2E2E2), height: 1.0),
            ),
          ),

          body: cart.items.isEmpty
              ? const Center(child: Text("Your cart is empty", style: TextStyle(fontSize: 18, color: Colors.grey)))
              : Column(
            children: [
              // 1. List of Cart Items
              Expanded(
                child: ListView.separated(
                  padding: const EdgeInsets.symmetric(vertical: 20),
                  itemCount: cart.items.length,
                  separatorBuilder: (context, index) => const Divider(color: Color(0xFFE2E2E2), indent: 25, endIndent: 25),
                  itemBuilder: (context, index) {
                    return _buildCartItem(context, cart.items[index], cart);
                  },
                ),
              ),

              // 2. Checkout Button
              Padding(
                padding: const EdgeInsets.all(25.0),
                child: SizedBox(
                  width: double.infinity,
                  height: 67,
                  child: ElevatedButton(
                    onPressed: () {
                      showModalBottomSheet(
                        context: context,
                        isScrollControlled: true,
                        backgroundColor: Colors.transparent,
                        builder: (context) => CheckoutBottomSheet(
                          totalCost: cart.totalPrice,
                        ),
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primaryGreen,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                      elevation: 0,
                    ),
                    child: Stack(
                      children: [
                        const Center(
                          child: Text(
                            "Go to Checkout",
                            style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
                          ),
                        ),
                        Positioned(
                          right: 10,
                          top: 0,
                          bottom: 0,
                          child: Center(
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
                              decoration: BoxDecoration(
                                color: const Color(0xFF489E67),
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                "\$${cart.totalPrice.toStringAsFixed(2)}",
                                style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                              ),
                            ),
                          ),
                        )
                      ],
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

  // Widget for a single row in the cart
  Widget _buildCartItem(BuildContext context, CartItem item, CartProvider cart) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 10),
      child: Row(
        children: [
          // Image
          SizedBox(
            width: 70, height: 70,
            child: item.product.imageUrl.startsWith('http')
                ? Image.network(item.product.imageUrl, fit: BoxFit.contain)
                : Image.asset(item.product.imageUrl, fit: BoxFit.contain),
          ),
          const SizedBox(width: 20),

          // Details
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      item.product.name,
                      style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.darkText),
                    ),
                    GestureDetector(
                      onTap: () => cart.removeFromCart(item),
                      child: const Icon(Icons.close, color: Color(0xFFB3B3B3)),
                    )
                  ],
                ),
                const SizedBox(height: 5),
                Text(
                  item.product.unit,
                  style: const TextStyle(color: AppColors.greyText, fontSize: 14),
                ),
                const SizedBox(height: 15),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        _buildQtyBtn(Icons.remove, () => cart.decreaseQty(item)),
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 15.0),
                          child: Text(
                            "${item.quantity}",
                            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                          ),
                        ),
                        _buildQtyBtn(Icons.add, () => cart.increaseQty(item)),
                      ],
                    ),
                    Text(
                      "\$${(item.product.price * item.quantity).toStringAsFixed(2)}",
                      style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.darkText),
                    ),
                  ],
                )
              ],
            ),
          )
        ],
      ),
    );
  }

  Widget _buildQtyBtn(IconData icon, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(17),
      child: Container(
        height: 45, width: 45,
        decoration: BoxDecoration(
          border: Border.all(color: const Color(0xFFF0F0F0)),
          borderRadius: BorderRadius.circular(17),
        ),
        child: Icon(icon, color: icon == Icons.add ? AppColors.primaryGreen : const Color(0xFFB3B3B3)),
      ),
    );
  }
}