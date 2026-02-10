import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:math';
import '../utils/app_colors.dart';
import '../providers/cart_provider.dart';
import '../providers/order_provider.dart';
import '../models/order_model.dart';
import 'order_accepted_screen.dart';
import 'order_failed_screen.dart';

class CheckoutBottomSheet extends StatefulWidget {
  final double totalCost;

  const CheckoutBottomSheet({super.key, required this.totalCost});

  @override
  State<CheckoutBottomSheet> createState() => _CheckoutBottomSheetState();
}

class _CheckoutBottomSheetState extends State<CheckoutBottomSheet> {
  String _deliveryMethod = "Select Method";
  String _paymentMethod = "Select Payment";
  bool _promoApplied = false;
  double _discount = 0.0;


  String _cardNumber = "";
  String _cvv = "";

  double get _finalCost => widget.totalCost - _discount;


  void _selectDelivery() {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return Container(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text("Choose Delivery Method", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 20),
              ListTile(
                title: const Text("Home Delivery"),
                leading: const Icon(Icons.home_outlined),
                onTap: () {
                  setState(() => _deliveryMethod = "Home Delivery");
                  Navigator.pop(context);
                },
              ),
              ListTile(
                title: const Text("Pick up from Store"),
                leading: const Icon(Icons.storefront),
                onTap: () {
                  setState(() => _deliveryMethod = "Pick Up");
                  Navigator.pop(context);
                },
              ),
            ],
          ),
        );
      },
    );
  }


  void _selectPayment() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (context) {
        return Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
          child: Container(
            padding: const EdgeInsets.all(20),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text("Payment Method", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                const SizedBox(height: 20),
                ListTile(
                  title: const Text("Cash on Delivery (COD)"),
                  leading: const Icon(Icons.money),
                  onTap: () {
                    setState(() {
                      _paymentMethod = "COD";
                      _cardNumber = "";
                      _cvv = "";
                    });
                    Navigator.pop(context);
                  },
                ),
                ListTile(
                  title: const Text("Credit/Debit Card"),
                  leading: const Icon(Icons.credit_card),
                  onTap: () {
                    Navigator.pop(context);
                    _showCardDialog(); // Open Input Dialog
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  void _showCardDialog() {
    final cardController = TextEditingController();
    final cvvController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text("Enter Card Details"),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: cardController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(hintText: "Card Number (16 digits)", labelText: "Card Number"),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: cvvController,
                keyboardType: TextInputType.number,
                maxLength: 3,
                decoration: const InputDecoration(hintText: "CVV (3 digits)", labelText: "CVV"),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Cancel"),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryGreen),
              onPressed: () {
                setState(() {
                  _paymentMethod = "Card";
                  _cardNumber = cardController.text;
                  _cvv = cvvController.text;
                });
                Navigator.pop(context);
              },
              child: const Text("Save", style: TextStyle(color: Colors.white)),
            ),
          ],
        );
      },
    );
  }


  void _applyPromo() {
    final promoController = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Enter Promo Code"),
        content: TextField(
          controller: promoController,
          decoration: const InputDecoration(hintText: "e.g. CUET20"),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text("Cancel")),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryGreen),
            onPressed: () {
              if (promoController.text.toUpperCase() == "RAKIB10") {
                setState(() {
                  _promoApplied = true;
                  _discount = 5.0;
                });
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Promo Applied!")));
              } else {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Invalid Code"), backgroundColor: Colors.red));
              }
            },
            child: const Text("Apply", style: TextStyle(color: Colors.white)),
          )
        ],
      ),
    );
  }


  void _placeOrder() {
    // A. Validation
    if (_deliveryMethod == "Select Method") {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Please select a delivery method")));
      return;
    }
    if (_paymentMethod == "Select Payment") {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Please select a payment method")));
      return;
    }


    if (_paymentMethod == "Card") {
      if (_cardNumber.length < 13 || _cvv.length != 3) {
        // FAIL Logic
        Navigator.push(context, MaterialPageRoute(builder: (context) => const OrderFailedScreen()));
        return;
      }
    }


    final cart = Provider.of<CartProvider>(context, listen: false);
    final orderProvider = Provider.of<OrderProvider>(context, listen: false);

    final newOrder = OrderModel(

      id: "#${Random().nextInt(9000) + 1000}",
      date: "${DateTime.now().day}/${DateTime.now().month}/${DateTime.now().year}",
      totalCost: _finalCost,
      status: "Placed",
      deliveryMethod: _deliveryMethod,
      paymentMethod: _paymentMethod,
      items: List.from(cart.items),
    );

    orderProvider.addOrder(newOrder);
    cart.clearCart();

    // Navigate to Success
    Navigator.pop(context);
    Navigator.push(context, MaterialPageRoute(builder: (context) => const OrderAcceptedScreen()));
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 30),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(30)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text("Checkout", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: AppColors.darkText)),
              IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.pop(context)),
            ],
          ),
          const Divider(color: Color(0xFFE2E2E2)),


          _buildOptionRow("Delivery", _deliveryMethod, onTap: _selectDelivery),
          _buildDivider(),
          _buildOptionRow("Payment", _paymentMethod, isIcon: _paymentMethod == "Card", onTap: _selectPayment),
          _buildDivider(),
          _buildOptionRow("Promo Code", _promoApplied ? "-\$${_discount.toStringAsFixed(2)}" : "Pick discount", onTap: _applyPromo),
          _buildDivider(),
          _buildOptionRow("Total Cost", "\$${_finalCost.toStringAsFixed(2)}", isTotal: true),
          _buildDivider(),

          const SizedBox(height: 20),
          const Text("By placing an order you agree to our Terms And Conditions", style: TextStyle(color: AppColors.greyText, fontSize: 14)),
          const SizedBox(height: 30),


          SizedBox(
            width: double.infinity,
            height: 67,
            child: ElevatedButton(
              onPressed: _placeOrder,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryGreen,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
              ),
              child: const Text("Place Order", style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600)),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOptionRow(String label, String value, {bool isIcon = false, bool isTotal = false, VoidCallback? onTap}) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 15.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: const TextStyle(fontSize: 18, color: Color(0xFF7C7C7C), fontWeight: FontWeight.w600)),
            Row(
              children: [
                if (isIcon) const Icon(Icons.credit_card, color: Colors.blue)
                else Text(value, style: TextStyle(fontSize: 16, color: AppColors.darkText, fontWeight: isTotal ? FontWeight.bold : FontWeight.w600)),

                if (!isTotal) ...[
                  const SizedBox(width: 15),
                  const Icon(Icons.arrow_forward_ios, size: 14, color: AppColors.darkText),
                ]
              ],
            )
          ],
        ),
      ),
    );
  }
  Widget _buildDivider() => const Divider(color: Color(0xFFE2E2E2));
}