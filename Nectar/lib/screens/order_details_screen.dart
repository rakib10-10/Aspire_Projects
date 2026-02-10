import 'package:flutter/material.dart';
import '../utils/app_colors.dart';
import '../models/order_model.dart'; // Import Order Model

class OrderDetailsScreen extends StatelessWidget {
  final OrderModel order; // Accept order data

  const OrderDetailsScreen({super.key, required this.order});

  @override
  Widget build(BuildContext context) {
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
        title: const Text(
          "Order Details",
          style: TextStyle(
            color: AppColors.darkText,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Dynamic Tracker based on status
            _buildTracker(order.status),
            const SizedBox(height: 30),

            const Text(
              "Delivery Details",
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            Text(
              "Method: ${order.deliveryMethod}\nPayment: ${order.paymentMethod}",
              style: const TextStyle(
                color: AppColors.greyText,
                height: 1.5,
                fontSize: 15,
              ),
            ),
            const Divider(height: 40, color: Color(0xFFE2E2E2)),

            const Text(
              "Items",
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 15),

            // Loop through real items
            ...order.items.map((cartItem) => _buildOrderItem(cartItem)),

            const Divider(height: 40, color: Color(0xFFE2E2E2)),

            _buildSummaryRow(
              "Total Cost",
              "\$${order.totalCost.toStringAsFixed(2)}",
              isTotal: true,
            ),
          ],
        ),
      ),
    );
  }

  // Helper Widgets
  Widget _buildTracker(String status) {
    // Basic logic to highlight steps based on status string
    bool placed = true;
    bool processing =
        status == "Processing" || status == "On Way" || status == "Delivered";
    bool onWay = status == "On Way" || status == "Delivered";
    bool delivered = status == "Delivered";

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        _buildStep("Placed", placed, placed),
        _buildLine(processing),
        _buildStep("Processing", processing, processing),
        _buildLine(onWay),
        _buildStep("On Way", onWay, onWay),
        _buildLine(delivered),
        _buildStep("Delivered", delivered, delivered),
      ],
    );
  }

  // (Include _buildStep, _buildLine, _buildOrderItem from your previous code,
  // just update _buildOrderItem to take CartItem object)
  Widget _buildStep(String label, bool isActive, bool isCompleted) {
    return Column(
      children: [
        Container(
          width: 30,
          height: 30,
          decoration: BoxDecoration(
            color: isCompleted
                ? AppColors.primaryGreen
                : (isActive ? Colors.white : const Color(0xFFE2E2E2)),
            shape: BoxShape.circle,
            border: Border.all(
              color: isActive
                  ? AppColors.primaryGreen
                  : const Color(0xFFE2E2E2),
              width: 2,
            ),
          ),
          child: isCompleted
              ? const Icon(Icons.check, color: Colors.white, size: 18)
              : null,
        ),
        const SizedBox(height: 8),
        Text(
          label,
          style: TextStyle(
            fontSize: 10,
            color: isActive ? AppColors.darkText : AppColors.greyText,
          ),
        ),
      ],
    );
  }

  Widget _buildLine(bool isActive) => Expanded(
    child: Container(
      height: 2,
      color: isActive ? AppColors.primaryGreen : const Color(0xFFE2E2E2),
      margin: const EdgeInsets.only(bottom: 20),
    ),
  );

  Widget _buildOrderItem(dynamic item) {
    // Assuming item is CartItem
    return Padding(
      padding: const EdgeInsets.only(bottom: 20.0),
      child: Row(
        children: [
          SizedBox(
            width: 60,
            height: 60,
            child: item.product.imageUrl.startsWith('http')
                ? Image.network(item.product.imageUrl)
                : Image.asset(item.product.imageUrl),
          ),
          const SizedBox(width: 15),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.product.name,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
                Text(
                  "${item.quantity} x ${item.product.unit}",
                  style: const TextStyle(
                    color: AppColors.greyText,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),
          Text(
            "\$${(item.product.price * item.quantity).toStringAsFixed(2)}",
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryRow(String label, String value, {bool isTotal = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: 16,
            color: isTotal ? AppColors.darkText : AppColors.greyText,
            fontWeight: isTotal ? FontWeight.bold : FontWeight.normal,
          ),
        ),
        Text(
          value,
          style: TextStyle(
            fontSize: 16,
            color: AppColors.darkText,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
