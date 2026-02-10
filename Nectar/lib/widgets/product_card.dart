import 'package:flutter/material.dart';
import '../utils/app_colors.dart';

class ProductCard extends StatelessWidget {
  final String imagePath;
  final String name;
  final String description;
  final String price;
  final VoidCallback? onAddTap;

  const ProductCard({
    super.key,
    required this.imagePath,
    required this.name,
    required this.description,
    required this.price,
    this.onAddTap,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 173, // Fixed width to match design
      decoration: BoxDecoration(
        border: Border.all(color: AppColors.borderColor),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Padding(
        padding: const EdgeInsets.all(15.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 1. Product Image
            Center(
              child: Image.asset(
                imagePath,
                height: 80,
                fit: BoxFit.contain,
                errorBuilder: (c, o, s) => const Icon(Icons.image_not_supported, size: 80, color: Colors.grey),
              ),
            ),
            const SizedBox(height: 20),

            // 2. Product Name
            Text(
              name,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: AppColors.darkText,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 5),

            // 3. Product Description (e.g., "7pcs, Priceg")
            Text(
              description,
              style: const TextStyle(
                fontSize: 14,
                color: AppColors.greyText,
              ),
            ),
            const SizedBox(height: 20),

            // 4. Price and Add Button Row
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  price,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.darkText,
                  ),
                ),
                // The Green "+" Button
                InkWell(
                  onTap: onAddTap,
                  borderRadius: BorderRadius.circular(17),
                  child: Container(
                    height: 45,
                    width: 45,
                    decoration: BoxDecoration(
                      color: AppColors.primaryGreen,
                      borderRadius: BorderRadius.circular(17),
                    ),
                    child: const Icon(Icons.add, color: Colors.white),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}