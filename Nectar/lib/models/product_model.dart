class Product {
  final String id;
  final String name;
  final String description;
  final double price;
  final String unit;
  final String imageUrl;
  final String category;

  Product({
    required this.id,
    required this.name,
    required this.description,
    required this.price,
    required this.unit,
    required this.imageUrl,
    required this.category,
  });

  // Factory to create a Product from Firebase Data (Map)
  factory Product.fromMap(String id, Map<dynamic, dynamic> map) {
    return Product(
      id: id,
      name: map['name'] ?? 'Unknown',
      description: map['description'] ?? 'No description available.',
      price: double.tryParse(map['price'].toString()) ?? 0.0,
      unit: map['unit'] ?? '',
      imageUrl: map['imageUrl'] ?? '',
      category: map['category'] ?? 'General',
    );
  }
}