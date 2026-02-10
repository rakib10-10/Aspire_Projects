import 'dart:io';
import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:image_picker/image_picker.dart';
import '../utils/app_colors.dart';

class MyDetailsScreen extends StatefulWidget {
  const MyDetailsScreen({super.key});

  @override
  State<MyDetailsScreen> createState() => _MyDetailsScreenState();
}

class _MyDetailsScreenState extends State<MyDetailsScreen> {
  final _formKey = GlobalKey<FormState>();

  // Controllers
  final TextEditingController _firstNameController = TextEditingController();
  final TextEditingController _lastNameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();

  File? _imageFile;
  String? _networkImageUrl;
  bool _isLoading = false;

  final FirebaseAuth _auth = FirebaseAuth.instance;

  final DatabaseReference _userRef = FirebaseDatabase.instance.ref().child('users');

  @override
  void initState() {
    super.initState();
    _loadUserData();
  }


  void _loadUserData() async {
    User? user = _auth.currentUser;
    if (user != null) {
      setState(() => _isLoading = true);


      final snapshot = await _userRef.child(user.uid).get();

      if (snapshot.exists) {

        Map<dynamic, dynamic> data = snapshot.value as Map<dynamic, dynamic>;
        _firstNameController.text = data['firstName'] ?? "";
        _lastNameController.text = data['lastName'] ?? "";
        _emailController.text = data['email'] ?? (user.email ?? "");
        _phoneController.text = data['phone'] ?? (user.phoneNumber ?? "");
        _networkImageUrl = data['photoUrl'] ?? user.photoURL;
      } else {

        if (user.displayName != null && user.displayName!.contains(" ")) {
          var names = user.displayName!.split(" ");
          _firstNameController.text = names[0];
          _lastNameController.text = names.sublist(1).join(" ");
        } else {
          _firstNameController.text = user.displayName ?? "";
        }
        _emailController.text = user.email ?? "";
        _phoneController.text = user.phoneNumber ?? "";
        _networkImageUrl = user.photoURL;
      }

      setState(() => _isLoading = false);
    }
  }

  Future<void> _pickImage() async {
    final pickedFile = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile != null) {
      setState(() {
        _imageFile = File(pickedFile.path);
      });
    }
  }


  Future<void> _saveChanges() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      User? user = _auth.currentUser;
      if (user == null) return;

      String photoURL = _networkImageUrl ?? "";


      if (_imageFile != null) {
        final ref = FirebaseStorage.instance
            .ref()
            .child('user_images')
            .child('${user.uid}.jpg');

        await ref.putFile(_imageFile!);
        photoURL = await ref.getDownloadURL();
      }

      // B. Prepare Data
      Map<String, String> userData = {
        "firstName": _firstNameController.text.trim(),
        "lastName": _lastNameController.text.trim(),
        "email": _emailController.text.trim(),
        "phone": _phoneController.text.trim(),
        "photoUrl": photoURL,
      };


      await _userRef.child(user.uid).set(userData);


      String fullName = "${_firstNameController.text.trim()} ${_lastNameController.text.trim()}";
      await user.updateDisplayName(fullName);
      if (photoURL.isNotEmpty) {
        await user.updatePhotoURL(photoURL);
      }
      await user.reload();

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Profile Saved Successfully!"), backgroundColor: AppColors.primaryGreen),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Error: $e"), backgroundColor: Colors.red),
      );
    } finally {
      setState(() => _isLoading = false);
    }
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
        title: const Text("My Details", style: TextStyle(color: AppColors.darkText, fontWeight: FontWeight.bold)),
        centerTitle: true,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: AppColors.primaryGreen))
          : SingleChildScrollView(
        padding: const EdgeInsets.all(25.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [

              Center(
                child: Stack(
                  children: [
                    Container(
                      width: 100, height: 100,
                      decoration: BoxDecoration(
                        color: Colors.grey.shade200,
                        shape: BoxShape.circle,
                        image: _imageFile != null
                            ? DecorationImage(image: FileImage(_imageFile!), fit: BoxFit.cover)
                            : (_networkImageUrl != null && _networkImageUrl!.isNotEmpty)
                            ? DecorationImage(image: NetworkImage(_networkImageUrl!), fit: BoxFit.cover)
                            : null,
                      ),
                      child: (_imageFile == null && (_networkImageUrl == null || _networkImageUrl!.isEmpty))
                          ? const Icon(Icons.person, size: 50, color: Colors.grey)
                          : null,
                    ),
                    Positioned(
                      bottom: 0, right: 0,
                      child: GestureDetector(
                        onTap: _pickImage,
                        child: Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: AppColors.primaryGreen,
                            shape: BoxShape.circle,
                            border: Border.all(color: Colors.white, width: 2),
                          ),
                          child: const Icon(Icons.camera_alt, color: Colors.white, size: 18),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 30),

              _buildLabel("First Name"),
              _buildTextField(_firstNameController, "Enter first name"),
              const SizedBox(height: 20),

              _buildLabel("Last Name"),
              _buildTextField(_lastNameController, "Enter last name"),
              const SizedBox(height: 20),

              _buildLabel("Email"),
              _buildTextField(_emailController, "Enter email"),
              const SizedBox(height: 20),

              _buildLabel("Phone Number"),
              _buildTextField(_phoneController, "Enter phone number"),
              const SizedBox(height: 40),

              SizedBox(
                width: double.infinity,
                height: 67,
                child: ElevatedButton(
                  onPressed: _saveChanges,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryGreen,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(19)),
                    elevation: 0,
                  ),
                  child: const Text("Save Changes", style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildLabel(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(text, style: const TextStyle(color: AppColors.greyText, fontSize: 16, fontWeight: FontWeight.w600)),
    );
  }

  Widget _buildTextField(TextEditingController controller, String hint) {
    return TextFormField(
      controller: controller,

      decoration: InputDecoration(
        hintText: hint,
        hintStyle: TextStyle(color: Colors.grey.shade400),
        contentPadding: const EdgeInsets.symmetric(vertical: 15, horizontal: 10),
        enabledBorder: const UnderlineInputBorder(borderSide: BorderSide(color: Color(0xFFE2E2E2))),
        focusedBorder: const UnderlineInputBorder(borderSide: BorderSide(color: AppColors.primaryGreen)),
      ),
      validator: (value) => value!.isEmpty ? "This field is required" : null,
    );
  }
}