import 'package:flutter/material.dart';
import '../models/product_model.dart'; // Ensure this import is correct
import '../utils/app_colors.dart';

class FilterSheet extends StatefulWidget {
  final List<Product> allProducts;
  final List<String> initialSelectedCategories;
  final Function(List<String>) onApply;

  const FilterSheet({
    super.key,
    required this.allProducts,
    required this.initialSelectedCategories,
    required this.onApply,
  });

  @override
  State<FilterSheet> createState() => _FilterSheetState();
}

class _FilterSheetState extends State<FilterSheet> {
  // Lists to hold data
  final List<String> _allCategories = [];
  final List<String> _selectedCategories = [];

  // Price Range (Optional addition for better filtering)
  RangeValues _currentPriceRange = const RangeValues(0, 100);

  @override
  void initState() {
    super.initState();
    _extractCategories();
    // Load previously selected filters
    _selectedCategories.addAll(widget.initialSelectedCategories);
  }

  // Logic: Look at all products and find the unique categories
  void _extractCategories() {
    final uniqueCategories = widget.allProducts
        .map((p) => p.category)
        .where((c) => c.isNotEmpty) // Filter out empty categories
        .toSet() // Removes duplicates
        .toList();

    uniqueCategories.sort(); // Sort alphabetically
    _allCategories.addAll(uniqueCategories);
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.85,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(30)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // --- Header ---
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              ),
              const Text("Filters", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
              TextButton(
                  onPressed: () {
                    setState(() {
                      _selectedCategories.clear();
                    });
                  },
                  child: const Text("Reset", style: TextStyle(color: AppColors.primaryGreen))
              )
            ],
          ),
          const SizedBox(height: 30),

          // --- Scrollable Content ---
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // 1. Categories Section (Dynamic)
                  const Text("Categories", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 20),

                  if (_allCategories.isEmpty)
                    const Text("No categories found", style: TextStyle(color: Colors.grey)),

                  ..._allCategories.map((category) => _buildCheckbox(category)),

                  const SizedBox(height: 40),

                  // 2. Price Range Section (Added for better utility)
                  const Text("Price Range", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 20),
                  RangeSlider(
                    values: _currentPriceRange,
                    min: 0,
                    max: 100, // You can make this dynamic based on max product price
                    activeColor: AppColors.primaryGreen,
                    inactiveColor: Colors.grey.shade300,
                    labels: RangeLabels(
                        "\$${_currentPriceRange.start.round()}",
                        "\$${_currentPriceRange.end.round()}"
                    ),
                    onChanged: (RangeValues values) {
                      setState(() {
                        _currentPriceRange = values;
                      });
                    },
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text("\$${_currentPriceRange.start.round()}", style: const TextStyle(fontSize: 16)),
                      Text("\$${_currentPriceRange.end.round()}", style: const TextStyle(fontSize: 16)),
                    ],
                  )
                ],
              ),
            ),
          ),

          // --- Apply Button ---
          SizedBox(
            width: double.infinity,
            height: 67,
            child: ElevatedButton(
              onPressed: () {
                // Pass data back to parent
                widget.onApply(_selectedCategories);
                Navigator.pop(context);
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryGreen,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                elevation: 0,
              ),
              child: const Text(
                "Apply Filter",
                style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCheckbox(String title) {
    final isSelected = _selectedCategories.contains(title);

    return Padding(
      padding: const EdgeInsets.only(bottom: 15.0),
      child: InkWell(
        onTap: () {
          setState(() {
            if (isSelected) {
              _selectedCategories.remove(title);
            } else {
              _selectedCategories.add(title);
            }
          });
        },
        child: Row(
          children: [
            Container(
              height: 24,
              width: 24,
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(8),
                  color: isSelected ? AppColors.primaryGreen : Colors.transparent,
                  border: Border.all(
                    color: isSelected ? AppColors.primaryGreen : const Color(0xFFB1B1B1),
                  )
              ),
              child: isSelected
                  ? const Icon(Icons.check, color: Colors.white, size: 18)
                  : null,
            ),
            const SizedBox(width: 15),
            Text(
              title,
              style: TextStyle(
                fontSize: 16,
                color: isSelected ? AppColors.primaryGreen : AppColors.darkText,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}