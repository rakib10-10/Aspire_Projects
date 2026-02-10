import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:provider/provider.dart';
import '../models/product_model.dart';
import '../utils/app_colors.dart';
import 'product_details_screen.dart';
import '../providers/location_provider.dart';


import 'explore_screen.dart';
import 'cart_screen.dart';
import 'favorite_screen.dart';
import 'account_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;


  Widget _buildShopTab() {
    final DatabaseReference productsRef = FirebaseDatabase.instance.ref().child('products');

    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 20),


            Center(
              child: Consumer<LocationProvider>(
                builder: (context, locationProvider, child) {
                  return Column(
                    children: [
                      const Icon(Icons.location_on, color: Color(0xFF4C4F4D), size: 30),
                      const SizedBox(height: 5),
                      Text(
                        "${locationProvider.zone}, ${locationProvider.area}", // <--- Use Dynamic Data
                        style: const TextStyle(color: Color(0xFF4C4F4D), fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                    ],
                  );
                },
              ),
            ),

            const SizedBox(height: 20),


            Container(
              decoration: BoxDecoration(
                color: const Color(0xFFF2F3F2),
                borderRadius: BorderRadius.circular(15),
              ),
              child: const TextField(
                decoration: InputDecoration(
                  border: InputBorder.none,
                  prefixIcon: Icon(Icons.search, color: Colors.black),
                  hintText: "Search Store",
                  contentPadding: EdgeInsets.symmetric(vertical: 15),
                ),
              ),
            ),
            const SizedBox(height: 20),


            Container(
              height: 115,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(8),
                image: const DecorationImage(
                  image: AssetImage('assets/images/banner.png'),
                  fit: BoxFit.cover,
                ),
                color: Colors.green.withOpacity(0.2),
              ),
            ),
            const SizedBox(height: 35),


            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text("Exclusive Offer", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                TextButton(
                    onPressed: () {},
                    child: const Text("See all", style: TextStyle(color: AppColors.primaryGreen))
                ),
              ],
            ),
            const SizedBox(height: 15),


            SizedBox(
              height: 250,
              child: StreamBuilder(
                stream: productsRef.onValue,
                builder: (context, AsyncSnapshot<DatabaseEvent> snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Center(child: CircularProgressIndicator(color: AppColors.primaryGreen));
                  }
                  if (snapshot.hasError || !snapshot.hasData || snapshot.data!.snapshot.value == null) {
                    return const Center(child: Text("No products found"));
                  }

                  Map<dynamic, dynamic> data = snapshot.data!.snapshot.value as Map<dynamic, dynamic>;
                  List<Product> products = [];
                  data.forEach((key, value) {
                    products.add(Product.fromMap(key, value));
                  });

                  return ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: products.length,
                    separatorBuilder: (c, i) => const SizedBox(width: 15),
                    itemBuilder: (context, index) {
                      return _buildProductCard(context, products[index]);
                    },
                  );
                },
              ),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }


  Widget _buildProductCard(BuildContext context, Product product) {
    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => ProductDetailsScreen(product: product)),
        );
      },
      child: Container(
        width: 173,
        padding: const EdgeInsets.all(15),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: const Color(0xFFE2E2E2)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: SizedBox(
                height: 80,
                child: product.imageUrl.startsWith('http')
                    ? Image.network(product.imageUrl, fit: BoxFit.contain)
                    : Image.asset(product.imageUrl, fit: BoxFit.contain, errorBuilder: (c,o,s)=> const Icon(Icons.image)),
              ),
            ),
            const SizedBox(height: 20),
            Text(product.name, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold), maxLines: 1, overflow: TextOverflow.ellipsis),
            const SizedBox(height: 5),
            Text(product.unit, style: const TextStyle(fontSize: 14, color: Color(0xFF7C7C7C))),
            const Spacer(),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text("\$${product.price}", style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                Container(
                  height: 45, width: 45,
                  decoration: BoxDecoration(color: AppColors.primaryGreen, borderRadius: BorderRadius.circular(17)),
                  child: const Icon(Icons.add, color: Colors.white),
                )
              ],
            )
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {

    final List<Widget> pages = [
      _buildShopTab(),
      const ExploreScreen(),
      const CartScreen(),
      const FavouriteScreen(),
      const AccountScreen(),
    ];

    return Scaffold(
      backgroundColor: Colors.white,


      body: SafeArea(
        child: IndexedStack(
          index: _currentIndex,
          children: pages,
        ),
      ),


      bottomNavigationBar: Container(
        decoration: const BoxDecoration(
          borderRadius: BorderRadius.only(topLeft: Radius.circular(15), topRight: Radius.circular(15)),
          boxShadow: [BoxShadow(color: Colors.black12, spreadRadius: 0, blurRadius: 10)],
        ),
        child: ClipRRect(
          borderRadius: const BorderRadius.only(topLeft: Radius.circular(15), topRight: Radius.circular(15)),
          child: BottomNavigationBar(
            currentIndex: _currentIndex,
            onTap: (index) {
              setState(() => _currentIndex = index);
            },
            type: BottomNavigationBarType.fixed,
            backgroundColor: Colors.white,
            selectedItemColor: AppColors.primaryGreen,
            unselectedItemColor: Colors.black,
            showUnselectedLabels: true,
            items: const [
              BottomNavigationBarItem(icon: Icon(Icons.storefront_outlined), label: "Shop"),
              BottomNavigationBarItem(icon: Icon(Icons.explore_outlined), label: "Explore"),
              BottomNavigationBarItem(icon: Icon(Icons.shopping_cart_outlined), label: "Cart"),
              BottomNavigationBarItem(icon: Icon(Icons.favorite_border), label: "Favourite"),
              BottomNavigationBarItem(icon: Icon(Icons.person_outline), label: "Account"),
            ],
          ),
        ),
      ),
    );
  }
}