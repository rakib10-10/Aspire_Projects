import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import '../utils/app_colors.dart';
import '../widgets/product_card.dart';
import '../models/product_model.dart';
import 'product_details_screen.dart';


class CategoryItemsScreen extends StatelessWidget {
  final String categoryTitle;

  const CategoryItemsScreen({super.key, required this.categoryTitle});

  @override
  Widget build(BuildContext context) {

    final DatabaseReference productsRef = FirebaseDatabase.instance.ref().child('products');

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios, color: Colors.black),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          categoryTitle,
          style: const TextStyle(color: Color(0xFF181725), fontWeight: FontWeight.bold, fontSize: 20),
        ),

      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 25.0),
        child: StreamBuilder(
          stream: productsRef.onValue,
          builder: (context, AsyncSnapshot<DatabaseEvent> snapshot) {

            if (snapshot.connectionState == ConnectionState.waiting) {
              return const Center(child: CircularProgressIndicator(color: AppColors.primaryGreen));
            }


            if (snapshot.hasError || !snapshot.hasData || snapshot.data!.snapshot.value == null) {
              return const Center(child: Text("No items found in this category"));
            }


            Map<dynamic, dynamic> data = snapshot.data!.snapshot.value as Map<dynamic, dynamic>;
            List<Product> categoryProducts = [];

            data.forEach((key, value) {
              final product = Product.fromMap(key, value);

              if (product.category == categoryTitle) {
                categoryProducts.add(product);
              }
            });

            if (categoryProducts.isEmpty) {
              return const Center(child: Text("No items found in this category"));
            }


            return GridView.builder(
              itemCount: categoryProducts.length,
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 0.7,
                crossAxisSpacing: 15,
                mainAxisSpacing: 15,
              ),
              itemBuilder: (context, index) {
                final product = categoryProducts[index];
                return GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ProductDetailsScreen(product: product),
                      ),
                    );
                  },
                  child: ProductCard(
                    imagePath: product.imageUrl,
                    name: product.name,
                    description: product.unit,
                    price: "\$${product.price}",
                    onAddTap: () {

                    },
                  ),
                );
              },
            );
          },
        ),
      ),
    );
  }
}