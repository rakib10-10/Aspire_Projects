import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import '../utils/app_colors.dart';
import '../widgets/product_card.dart';
import '../models/product_model.dart';
import 'product_details_screen.dart';
import 'filter_sheet.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  final DatabaseReference _productsRef = FirebaseDatabase.instance.ref().child('products');

  List<Product> _allProducts = [];
  List<Product> _displayedProducts = [];
  List<String> _activeCategoryFilters = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchAllProducts();
  }


  void _fetchAllProducts() async {
    try {
      final snapshot = await _productsRef.get();
      if (snapshot.exists) {
        Map<dynamic, dynamic> data = snapshot.value as Map<dynamic, dynamic>;
        List<Product> loadedProducts = [];

        data.forEach((key, value) {
          loadedProducts.add(Product.fromMap(key, value));
        });

        setState(() {
          _allProducts = loadedProducts;
          _displayedProducts = loadedProducts;
          _isLoading = false;
        });
      } else {
        setState(() => _isLoading = false);
      }
    } catch (e) {
      debugPrint("Error fetching products: $e");
      setState(() => _isLoading = false);
    }
  }


  void _updateList() {
    final String searchText = _searchController.text.toLowerCase();

    setState(() {
      _displayedProducts = _allProducts.where((product) {
        // Condition A: Does name match search text?
        final bool matchesSearch = product.name.toLowerCase().contains(searchText);

        // Condition B: Is category selected? (If no filters, assume true)
        final bool matchesCategory = _activeCategoryFilters.isEmpty ||
            _activeCategoryFilters.contains(product.category);

        return matchesSearch && matchesCategory;
      }).toList();
    });
  }

  // 3. Open Filter Sheet
  void _openFilterSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => FilterSheet(
        allProducts: _allProducts,
        initialSelectedCategories: _activeCategoryFilters,
        onApply: (selectedCategories) {

          setState(() {
            _activeCategoryFilters = selectedCategories;
            _updateList();
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios, color: Colors.black),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text("Search", style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold)),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 25.0),
        child: Column(
          children: [
            const SizedBox(height: 20),


            Row(
              children: [
                Expanded(
                  child: Container(
                    decoration: BoxDecoration(
                      color: const Color(0xFFF2F3F2),
                      borderRadius: BorderRadius.circular(15),
                    ),
                    child: TextField(
                      controller: _searchController,
                      onChanged: (val) => _updateList(),
                      autofocus: false,
                      decoration: InputDecoration(
                        hintText: "Egg",
                        hintStyle: const TextStyle(color: Color(0xFF7C7C7C)),
                        prefixIcon: const Icon(Icons.search, color: Colors.black),
                        suffixIcon: _searchController.text.isNotEmpty
                            ? IconButton(
                          icon: const Icon(Icons.close, color: Color(0xFF7C7C7C)),
                          onPressed: () {
                            _searchController.clear();
                            _updateList();
                          },
                        )
                            : null,
                        border: InputBorder.none,
                        contentPadding: const EdgeInsets.symmetric(vertical: 15),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 15),


                GestureDetector(
                  onTap: _openFilterSheet,
                  child: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(

                        color: _activeCategoryFilters.isNotEmpty ? AppColors.primaryGreen.withOpacity(0.1) : Colors.transparent,
                        borderRadius: BorderRadius.circular(15)
                    ),
                    child: Icon(
                        Icons.tune,
                        color: _activeCategoryFilters.isNotEmpty ? AppColors.primaryGreen : Colors.black,
                        size: 28
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),


            Expanded(
              child: _isLoading
                  ? const Center(child: CircularProgressIndicator(color: AppColors.primaryGreen))
                  : _displayedProducts.isNotEmpty
                  ? GridView.builder(
                itemCount: _displayedProducts.length,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  childAspectRatio: 0.7,
                  crossAxisSpacing: 15,
                  mainAxisSpacing: 15,
                ),
                itemBuilder: (context, index) {
                  final product = _displayedProducts[index];
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

                        Navigator.push(
                            context,
                            MaterialPageRoute(builder: (c) => ProductDetailsScreen(product: product))
                        );
                      },
                    ),
                  );
                },
              )
                  : Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.search_off, size: 60, color: Colors.grey),
                    const SizedBox(height: 10),
                    Text(
                      "No products found",
                      style: TextStyle(fontSize: 18, color: Colors.grey.shade600),
                    ),
                    if (_activeCategoryFilters.isNotEmpty)
                      TextButton(
                          onPressed: () {
                            setState(() {
                              _activeCategoryFilters.clear();
                              _updateList();
                            });
                          },
                          child: const Text("Clear Filters", style: TextStyle(color: AppColors.primaryGreen))
                      )
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}