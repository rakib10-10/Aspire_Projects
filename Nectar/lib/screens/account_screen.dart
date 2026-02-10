import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:url_launcher/url_launcher.dart';
import '../utils/app_colors.dart';
import 'orders_screen.dart';
import 'my_details_screen.dart';
import 'login_screen.dart';
// New Imports
import 'delivery_address_screen.dart';
import 'payment_methods_screen.dart';

class AccountScreen extends StatelessWidget {
  const AccountScreen({super.key});

  void _logout(BuildContext context) async {
    await FirebaseAuth.instance.signOut();
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (context) => const LoginScreen()),
      (route) => false,
    );
  }

  void _showInfoDialog(BuildContext context, String title, String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text(
              "OK",
              style: TextStyle(color: AppColors.primaryGreen),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final User? currentUser = FirebaseAuth.instance.currentUser;
    final DatabaseReference userRef = FirebaseDatabase.instance
        .ref()
        .child('users')
        .child(currentUser?.uid ?? 'unknown');

    final List<Map<String, dynamic>> menuItems = [
      {"icon": Icons.shopping_bag_outlined, "title": "Orders"},
      {"icon": Icons.assignment_ind_outlined, "title": "My Details"},
      {"icon": Icons.location_on_outlined, "title": "Delivery Address"},
      {"icon": Icons.credit_card, "title": "Payment Methods"},
      {"icon": Icons.local_activity_outlined, "title": "Promo Code"},
      {"icon": Icons.notifications_none, "title": "Notifications"},
      {"icon": Icons.help_outline, "title": "Help"},
      {"icon": Icons.info_outline, "title": "About"},
    ];

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        automaticallyImplyLeading: false,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1.0),
          child: Container(color: const Color(0xFFE2E2E2), height: 1.0),
        ),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            StreamBuilder(
              stream: userRef.onValue,
              builder: (context, AsyncSnapshot<DatabaseEvent> snapshot) {
                String name = currentUser?.displayName ?? "User";
                String email = currentUser?.email ?? "";
                String? photoUrl = currentUser?.photoURL;

                if (snapshot.hasData && snapshot.data!.snapshot.value != null) {
                  final data =
                      snapshot.data!.snapshot.value as Map<dynamic, dynamic>;
                  final String fName = data['firstName'] ?? "";
                  final String lName = data['lastName'] ?? "";

                  if (fName.isNotEmpty) name = "$fName $lName";
                  if (data['photoUrl'] != null &&
                      data['photoUrl'].toString().isNotEmpty) {
                    photoUrl = data['photoUrl'];
                  }
                }

                bool isInvalidUrl =
                    photoUrl == null ||
                    photoUrl.contains(
                      "googleusercontent.com/profile/picture/0",
                    );

                return Padding(
                  padding: const EdgeInsets.all(25.0),
                  child: Row(
                    children: [
                      Container(
                        width: 65,
                        height: 65,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: Colors.grey.shade200,
                        ),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(65),
                          child: isInvalidUrl
                              ? Image.asset(
                                  'assets/images/profile_pic.png',
                                  fit: BoxFit.cover,
                                )
                              : Image.network(
                                  photoUrl,
                                  fit: BoxFit.cover,
                                  errorBuilder: (context, error, stackTrace) {
                                    return Image.asset(
                                      'assets/images/profile_pic.png',
                                      fit: BoxFit.cover,
                                    );
                                  },
                                ),
                        ),
                      ),
                      const SizedBox(width: 20),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Flexible(
                                  child: Text(
                                    name,
                                    style: const TextStyle(
                                      fontSize: 20,
                                      fontWeight: FontWeight.bold,
                                      color: AppColors.darkText,
                                    ),
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                const SizedBox(width: 8),
                                const Icon(
                                  Icons.edit_outlined,
                                  color: AppColors.primaryGreen,
                                  size: 18,
                                ),
                              ],
                            ),
                            const SizedBox(height: 5),
                            Text(
                              email,
                              style: const TextStyle(
                                color: AppColors.greyText,
                                fontSize: 16,
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),

            const Divider(height: 1, color: Color(0xFFE2E2E2)),

            ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: menuItems.length,
              separatorBuilder: (context, index) =>
                  const Divider(height: 1, color: Color(0xFFE2E2E2)),
              itemBuilder: (context, index) {
                final item = menuItems[index];
                return ListTile(
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 25,
                    vertical: 5,
                  ),
                  leading: Icon(
                    item['icon'],
                    color: AppColors.darkText,
                    size: 24,
                  ),
                  title: Text(
                    item['title'],
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                      color: AppColors.darkText,
                    ),
                  ),
                  trailing: const Icon(
                    Icons.arrow_forward_ios,
                    size: 16,
                    color: AppColors.darkText,
                  ),
                  onTap: () async {
                    String title = item['title'];

                    if (title == "Orders") {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => const OrdersScreen(),
                        ),
                      );
                    } else if (title == "My Details") {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => const MyDetailsScreen(),
                        ),
                      );
                    } else if (title == "Delivery Address") {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => const DeliveryAddressScreen(),
                        ),
                      );
                    } else if (title == "Payment Methods") {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => const PaymentMethodsScreen(),
                        ),
                      );
                    } else if (title == "Promo Code") {
                      _showInfoDialog(
                        context,
                        "Promo Code",
                        "Look on our Facebook, website, and buy products to receive promo codes!",
                      );
                    } else if (title == "Notifications") {
                      _showInfoDialog(
                        context,
                        "Notifications",
                        "You will be notified when needed.",
                      );
                    } else if (title == "Help") {
                      final Uri url = Uri.parse(
                        'https://thenexgenix.com/contact-us',
                      );
                      if (!await launchUrl(
                        url,
                        mode: LaunchMode.externalApplication,
                      )) {
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text("Could not open website"),
                            ),
                          );
                        }
                      }
                    } else if (title == "About") {
                      final Uri url = Uri.parse('https://thenexgenix.com');
                      if (!await launchUrl(
                        url,
                        mode: LaunchMode.externalApplication,
                      )) {
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text("Could not open website"),
                            ),
                          );
                        }
                      }
                    }
                  },
                );
              },
            ),

            const Divider(height: 1, color: Color(0xFFE2E2E2)),
            const SizedBox(height: 40),

            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 25.0),
              child: SizedBox(
                width: double.infinity,
                height: 67,
                child: ElevatedButton(
                  onPressed: () => _logout(context),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFF2F3F2),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(19),
                    ),
                    elevation: 0,
                  ),
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.logout, color: AppColors.primaryGreen),
                      SizedBox(width: 20),
                      Text(
                        "Log Out",
                        style: TextStyle(
                          color: AppColors.primaryGreen,
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }
}
