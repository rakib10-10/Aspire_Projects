import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import '../utils/app_colors.dart';
import '../models/product_model.dart'; // Import your Product Model
import 'category_items_screen.dart';
import 'search_screen.dart';

class ExploreScreen extends StatefulWidget {
  const ExploreScreen({super.key});

  @override
  State<ExploreScreen> createState() => _ExploreScreenState();
}

class _ExploreScreenState extends State<ExploreScreen> {
  // Reference to Firebase
  final DatabaseReference _productsRef = FirebaseDatabase.instance.ref().child('products');

  // Hardcoded colors for visual appeal (mapped by index)
  final List<Map<String, Color>> _cardColors = [
    {"bg": AppColors.catGreenBg, "border": AppColors.catGreenBorder},
    {"bg": AppColors.catOrangeBg, "border": AppColors.catOrangeBorder},
    {"bg": AppColors.catPinkBg, "border": AppColors.catPinkBorder},
    {"bg": AppColors.catPurpleBg, "border": AppColors.catPurpleBorder},
    {"bg": AppColors.catYellowBg, "border": AppColors.catYellowBorder},
    {"bg": AppColors.catBlueBg, "border": AppColors.catBlueBorder},
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        automaticallyImplyLeading: false,
        title: const Text(
          "Find Products",
          style: TextStyle(color: Color(0xFF181725), fontSize: 20, fontWeight: FontWeight.bold),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 25.0),
        child: Column(
          children: [
            const SizedBox(height: 20),

            // 1. Search Bar
            GestureDetector(
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SearchScreen()),
                );
              },
              child: Container(
                decoration: BoxDecoration(
                  color: const Color(0xFFF2F3F2),
                  borderRadius: BorderRadius.circular(15),
                ),
                child: const TextField(
                  enabled: false,
                  decoration: InputDecoration(
                    hintText: "Search Store",
                    hintStyle: TextStyle(color: Color(0xFF7C7C7C)),
                    prefixIcon: Icon(Icons.search, color: Colors.black),
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.symmetric(vertical: 15),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 20),

            // 2. Realtime Category Grid
            Expanded(
              child: StreamBuilder(
                stream: _productsRef.onValue,
                builder: (context, AsyncSnapshot<DatabaseEvent> snapshot) {
                  // A. Loading
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Center(child: CircularProgressIndicator(color: AppColors.primaryGreen));
                  }

                  // B. No Data
                  if (snapshot.hasError || !snapshot.hasData || snapshot.data!.snapshot.value == null) {
                    return const Center(child: Text("No products found"));
                  }

                  // C. Process Data to find Categories
                  Map<dynamic, dynamic> data = snapshot.data!.snapshot.value as Map<dynamic, dynamic>;

                  // Use a Set to store unique category names
                  Set<String> uniqueCategories = {};
                  // Also map one image per category to show on the card
                  Map<String, String> categoryImages = {};

                  data.forEach((key, value) {
                    final product = Product.fromMap(key, value);
                    if (product.category.isNotEmpty) {
                      uniqueCategories.add(product.category);
                      // Save the first image found for this category
                      if (!categoryImages.containsKey(product.category)) {
                        categoryImages[product.category] = product.imageUrl;
                      }
                    }
                  });

                  final List<String> categoriesList = uniqueCategories.toList();

                  // D. Show Grid
                  return GridView.builder(
                    itemCount: categoriesList.length,
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      crossAxisSpacing: 15,
                      mainAxisSpacing: 15,
                      childAspectRatio: 0.83,
                    ),
                    itemBuilder: (context, index) {
                      final categoryName = categoriesList[index];
                      final imagePath = categoryImages[categoryName] ?? "";

                      // Cycle through colors
                      final colorSet = _cardColors[index % _cardColors.length];

                      return GestureDetector(
                        onTap: () {
                          // Navigate to Category Items Screen
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => CategoryItemsScreen(
                                categoryTitle: categoryName,
                              ),
                            ),
                          );
                        },
                        child: Container(
                          decoration: BoxDecoration(
                            color: colorSet['bg'],
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(color: colorSet['border']!, width: 1),
                          ),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              // Image
                              SizedBox(
                                height: 80,
                                width: 100,
                                child: imagePath.startsWith('http')
                                    ? Image.network(imagePath, fit: BoxFit.contain)
                                    : Image.asset(
                                  imagePath,
                                  fit: BoxFit.contain,
                                  errorBuilder: (c,o,s) => const Icon(Icons.category, size: 50, color: Colors.grey),
                                ),
                              ),
                              const SizedBox(height: 20),
                              // Name
                              Text(
                                categoryName,
                                textAlign: TextAlign.center,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                  color: Color(0xFF181725),
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}