import 'package:flutter/material.dart';

const brandWhite = Color(0xFFFFFFFF);
const brandOrange = Color(0xFFFF8A00);
const brandBlack = Color(0xFF000000);
const textSecondary = Color(0x9E000000);
const surfacePressed = Color(0x47FFCC00);
const outline = Color(0x47000000);

ThemeData provisionerTheme() {
  return ThemeData(
    useMaterial3: true,
    scaffoldBackgroundColor: brandWhite,
    colorScheme: const ColorScheme.light(
      primary: brandOrange,
      onPrimary: brandWhite,
      surface: brandWhite,
      onSurface: brandBlack,
    ),
    fontFamily: 'sans-serif',
  );
}

class BrandButton extends StatelessWidget {
  const BrandButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.filled = true,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool filled;

  @override
  Widget build(BuildContext context) {
    final child = Text(label, textAlign: TextAlign.center);
    final shape = RoundedRectangleBorder(borderRadius: BorderRadius.circular(18));
    if (!filled) {
      return OutlinedButton(
        onPressed: onPressed,
        style: OutlinedButton.styleFrom(
          foregroundColor: brandOrange,
          side: const BorderSide(color: brandOrange),
          minimumSize: const Size.fromHeight(52),
          shape: shape,
        ),
        child: child,
      );
    }
    return FilledButton(
      onPressed: onPressed,
      style: FilledButton.styleFrom(
        backgroundColor: brandOrange,
        foregroundColor: brandWhite,
        disabledBackgroundColor: surfacePressed,
        minimumSize: const Size.fromHeight(52),
        shape: shape,
      ),
      child: child,
    );
  }
}
