import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Section keys for screens supporting custom backgrounds
enum AppSection {
  home,
  study,
  askDelulu,
  planner,
  focus,
  progress,
  settings;

  /// String identifier key matching the section name
  String get key {
    switch (this) {
      case AppSection.home:
        return 'home';
      case AppSection.study:
        return 'study';
      case AppSection.askDelulu:
        return 'ask_delulu';
      case AppSection.planner:
        return 'planner';
      case AppSection.focus:
        return 'focus';
      case AppSection.progress:
        return 'progress';
      case AppSection.settings:
        return 'settings';
    }
  }

  /// Resolve from a string section key (case-insensitive)
  static AppSection fromKey(String key) {
    switch (key.toLowerCase().trim()) {
      case 'home':
        return AppSection.home;
      case 'study':
        return AppSection.study;
      case 'ask':
      case 'ask_delulu':
      case 'askdelulu':
        return AppSection.askDelulu;
      case 'planner':
        return AppSection.planner;
      case 'focus':
      case 'timer':
        return AppSection.focus;
      case 'progress':
      case 'scoreboard':
        return AppSection.progress;
      case 'settings':
      case 'profile':
        return AppSection.settings;
      default:
        return AppSection.home;
    }
  }
}

/// Centralized helper that maps section keys (e.g., 'home', 'study') to their corresponding
/// files in 'assets/backgrounds/'. Provides fallback background colors, gradients, and decoration
/// logic if an image asset is missing or fails to load.
class AppBackgroundConfig {
  AppBackgroundConfig._();

  /// Root directory for custom background assets
  static const String basePath = 'assets/backgrounds/';

  /// Explicit mapping of section keys (e.g. 'home', 'study') to file paths in 'assets/backgrounds/'
  static const Map<String, String> sectionFiles = {
    'home': '${basePath}home.jpg',
    'study': '${basePath}study.jpg',
    'ask_delulu': '${basePath}ask_delulu.jpg',
    'ask': '${basePath}ask_delulu.jpg',
    'planner': '${basePath}planner.jpg',
    'focus': '${basePath}focus.jpg',
    'progress': '${basePath}progress.jpg',
    'settings': '${basePath}settings.jpg',
  };

  /// Retrieves the asset file path for any section key (e.g. 'home', 'study').
  /// Defaults to 'assets/backgrounds/home.jpg' if the key is unrecognized.
  static String getAssetPath(String sectionKey) {
    final normalizedKey = sectionKey.toLowerCase().trim();
    return sectionFiles[normalizedKey] ?? '${basePath}home.jpg';
  }

  /// Convenience getter for typed [AppSection] enum
  static String getAssetPathForSection(AppSection section) {
    return getAssetPath(section.key);
  }

  /// Fallback solid background color if an asset image is missing or disabled.
  static Color getFallbackColor(String sectionKey, {bool isDark = false}) {
    final section = AppSection.fromKey(sectionKey);
    if (isDark) {
      switch (section) {
        case AppSection.home:
          return const Color(0xFF0F172A);
        case AppSection.study:
          return const Color(0xFF064E3B);
        case AppSection.askDelulu:
          return const Color(0xFF1E1B4B);
        case AppSection.planner:
          return const Color(0xFF1E293B);
        case AppSection.focus:
          return const Color(0xFF1A103C);
        case AppSection.progress:
          return const Color(0xFF172554);
        case AppSection.settings:
          return const Color(0xFF0F172A);
      }
    } else {
      switch (section) {
        case AppSection.home:
          return const Color(0xFFEEF2FF);
        case AppSection.study:
          return const Color(0xFFECFDF5);
        case AppSection.askDelulu:
          return const Color(0xFFF5F3FF);
        case AppSection.planner:
          return const Color(0xFFF1F5F9);
        case AppSection.focus:
          return const Color(0xFFFAF5FF);
        case AppSection.progress:
          return const Color(0xFFEFF6FF);
        case AppSection.settings:
          return const Color(0xFFF8FAFC);
      }
    }
  }

  /// Fallback multi-stop gradient for high visual fidelity when custom photos are absent.
  static LinearGradient getFallbackGradient(String sectionKey, {bool isDark = false}) {
    final section = AppSection.fromKey(sectionKey);
    if (isDark) {
      switch (section) {
        case AppSection.home:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0B0F19)],
          );
        case AppSection.study:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF022C22)],
          );
        case AppSection.askDelulu:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A)],
          );
        case AppSection.planner:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF0B0F19)],
          );
        case AppSection.focus:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF1A103C), Color(0xFF0F172A), Color(0xFF050510)],
          );
        case AppSection.progress:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF111827)],
          );
        case AppSection.settings:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0B0F19)],
          );
      }
    } else {
      switch (section) {
        case AppSection.home:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFEEF2FF), Color(0xFFF8FAFC), Color(0xFFF1F5F9)],
          );
        case AppSection.study:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFECFDF5), Color(0xFFF0FDF4), Color(0xFFF8FAFC)],
          );
        case AppSection.askDelulu:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFEDE9FE), Color(0xFFF5F3FF), Color(0xFFF8FAFC)],
          );
        case AppSection.planner:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFF1F5F9), Color(0xFFF8FAFC), Color(0xFFE2E8F0)],
          );
        case AppSection.focus:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFFAF5FF), Color(0xFFF3E8FF), Color(0xFFF8FAFC)],
          );
        case AppSection.progress:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFEEF2FF), Color(0xFFF1F5F9), Color(0xFFFFFFFF)],
          );
        case AppSection.settings:
          return const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFFFFFFF)],
          );
      }
    }
  }

  /// Scrim overlay color to ensure foreground text and interactive cards
  /// maintain WCAG AA/AAA legibility over arbitrary background photos.
  static Color getScrimColor({bool isDark = false}) {
    return isDark
        ? const Color(0xDD0B0F19) // 87% dark slate scrim
        : const Color(0xCCF8FAFC); // 80% light slate scrim
  }

  /// Builds a [BoxDecoration] that incorporates either the background image (with scrim)
  /// or cleanly falls back to the gradient/solid color logic.
  static BoxDecoration buildDecoration(
    String sectionKey, {
    required bool isDark,
    ImageProvider? customImage,
  }) {
    if (customImage != null) {
      return BoxDecoration(
        color: getFallbackColor(sectionKey, isDark: isDark),
        image: DecorationImage(
          image: customImage,
          fit: BoxFit.cover,
          colorFilter: ColorFilter.mode(
            getScrimColor(isDark: isDark),
            BlendMode.srcOver,
          ),
        ),
      );
    }
    return BoxDecoration(
      color: getFallbackColor(sectionKey, isDark: isDark),
      gradient: getFallbackGradient(sectionKey, isDark: isDark),
    );
  }
}

/// Backwards compatibility alias for AppBackgroundConfig
typedef AppBackgrounds = AppBackgroundConfig;

/// Semantic color tokens extension
@immutable
class AppColorsExtension extends ThemeExtension<AppColorsExtension> {
  final Color emerald;
  final Color emeraldContainer;
  final Color onEmeraldContainer;
  final Color amber;
  final Color amberContainer;
  final Color onAmberContainer;
  final Color rose;
  final Color roseContainer;
  final Color onRoseContainer;
  final Color cyan;
  final Color cardBackground;
  final Color cardBorder;
  final Color textMuted;

  const AppColorsExtension({
    required this.emerald,
    required this.emeraldContainer,
    required this.onEmeraldContainer,
    required this.amber,
    required this.amberContainer,
    required this.onAmberContainer,
    required this.rose,
    required this.roseContainer,
    required this.onRoseContainer,
    required this.cyan,
    required this.cardBackground,
    required this.cardBorder,
    required this.textMuted,
  });

  static const light = AppColorsExtension(
    emerald: Color(0xFF10B981),
    emeraldContainer: Color(0xFFD1FAE5),
    onEmeraldContainer: Color(0xFF065F46),
    amber: Color(0xFFF59E0B),
    amberContainer: Color(0xFFFEF3C7),
    onAmberContainer: Color(0xFF92400E),
    rose: Color(0xFFEF4444),
    roseContainer: Color(0xFFFEE2E2),
    onRoseContainer: Color(0xFF991B1B),
    cyan: Color(0xFF06B6D4),
    cardBackground: Colors.white,
    cardBorder: Color(0xFFE2E8F0),
    textMuted: Color(0xFF64748B),
  );

  static const dark = AppColorsExtension(
    emerald: Color(0xFF34D399),
    emeraldContainer: Color(0xFF064E3B),
    onEmeraldContainer: Color(0xFFA7F3D0),
    amber: Color(0xFFFBBF24),
    amberContainer: Color(0xFF78350F),
    onAmberContainer: Color(0xFFFDE68A),
    rose: Color(0xFFF87171),
    roseContainer: Color(0xFF7F1D1D),
    onRoseContainer: Color(0xFFFECACA),
    cyan: Color(0xFF22D3EE),
    cardBackground: Color(0xFF161F30),
    cardBorder: Color(0xFF27354A),
    textMuted: Color(0xFF94A3B8),
  );

  @override
  AppColorsExtension copyWith({
    Color? emerald,
    Color? emeraldContainer,
    Color? onEmeraldContainer,
    Color? amber,
    Color? amberContainer,
    Color? onAmberContainer,
    Color? rose,
    Color? roseContainer,
    Color? onRoseContainer,
    Color? cyan,
    Color? cardBackground,
    Color? cardBorder,
    Color? textMuted,
  }) {
    return AppColorsExtension(
      emerald: emerald ?? this.emerald,
      emeraldContainer: emeraldContainer ?? this.emeraldContainer,
      onEmeraldContainer: onEmeraldContainer ?? this.onEmeraldContainer,
      amber: amber ?? this.amber,
      amberContainer: amberContainer ?? this.amberContainer,
      onAmberContainer: onAmberContainer ?? this.onAmberContainer,
      rose: rose ?? this.rose,
      roseContainer: roseContainer ?? this.roseContainer,
      onRoseContainer: onRoseContainer ?? this.onRoseContainer,
      cyan: cyan ?? this.cyan,
      cardBackground: cardBackground ?? this.cardBackground,
      cardBorder: cardBorder ?? this.cardBorder,
      textMuted: textMuted ?? this.textMuted,
    );
  }

  @override
  AppColorsExtension lerp(ThemeExtension<AppColorsExtension>? other, double t) {
    if (other is! AppColorsExtension) return this;
    return AppColorsExtension(
      emerald: Color.lerp(emerald, other.emerald, t)!,
      emeraldContainer: Color.lerp(emeraldContainer, other.emeraldContainer, t)!,
      onEmeraldContainer: Color.lerp(onEmeraldContainer, other.onEmeraldContainer, t)!,
      amber: Color.lerp(amber, other.amber, t)!,
      amberContainer: Color.lerp(amberContainer, other.amberContainer, t)!,
      onAmberContainer: Color.lerp(onAmberContainer, other.onAmberContainer, t)!,
      rose: Color.lerp(rose, other.rose, t)!,
      roseContainer: Color.lerp(roseContainer, other.roseContainer, t)!,
      onRoseContainer: Color.lerp(onRoseContainer, other.onRoseContainer, t)!,
      cyan: Color.lerp(cyan, other.cyan, t)!,
      cardBackground: Color.lerp(cardBackground, other.cardBackground, t)!,
      cardBorder: Color.lerp(cardBorder, other.cardBorder, t)!,
      textMuted: Color.lerp(textMuted, other.textMuted, t)!,
    );
  }
}

/// Material 3 App Theme configuration system
class AppTheme {
  AppTheme._();

  // Primary brand palette (Indigo / Violet)
  static const Color primaryIndigo = Color(0xFF4F46E5);
  static const Color primaryIndigoLight = Color(0xFF818CF8);
  static const Color primaryIndigoDark = Color(0xFF3730A3);

  // Secondary brand palette (Purple)
  static const Color secondaryPurple = Color(0xFF7C3AED);
  static const Color secondaryPurpleLight = Color(0xFFA78BFA);

  // Tertiary brand palette (Cyan)
  static const Color tertiaryCyan = Color(0xFF06B6D4);
  static const Color tertiaryCyanLight = Color(0xFF67E8F9);

  // Neutral tones - Light Mode
  static const Color lightBackground = Color(0xFFF8FAFC);
  static const Color lightSurface = Colors.white;
  static const Color lightSurfaceVariant = Color(0xFFF1F5F9);
  static const Color lightTextPrimary = Color(0xFF0F172A);
  static const Color lightTextSecondary = Color(0xFF475569);
  static const Color lightOutline = Color(0xFFE2E8F0);

  // Neutral tones - Dark Mode
  static const Color darkBackground = Color(0xFF0B0F19);
  static const Color darkSurface = Color(0xFF161F30);
  static const Color darkSurfaceVariant = Color(0xFF1E293B);
  static const Color darkTextPrimary = Color(0xFFF8FAFC);
  static const Color darkTextSecondary = Color(0xFF94A3B8);
  static const Color darkOutline = Color(0xFF27354A);

  /// Material 3 Light Color Scheme
  static const ColorScheme lightColorScheme = ColorScheme(
    brightness: Brightness.light,
    primary: primaryIndigo,
    onPrimary: Colors.white,
    primaryContainer: Color(0xFFEEF2FF),
    onPrimaryContainer: Color(0xFF312E81),
    secondary: secondaryPurple,
    onSecondary: Colors.white,
    secondaryContainer: Color(0xFFF5F3FF),
    onSecondaryContainer: Color(0xFF4C1D95),
    tertiary: tertiaryCyan,
    onTertiary: Colors.white,
    tertiaryContainer: Color(0xFFECFEFF),
    onTertiaryContainer: Color(0xFF164E63),
    error: Color(0xFFEF4444),
    onError: Colors.white,
    errorContainer: Color(0xFFFEE2E2),
    onErrorContainer: Color(0xFF991B1B),
    surface: lightSurface,
    onSurface: lightTextPrimary,
    surfaceContainerHighest: lightSurfaceVariant,
    onSurfaceVariant: lightTextSecondary,
    outline: lightOutline,
    outlineVariant: Color(0xFFCBD5E1),
    shadow: Color(0x1A000000),
  );

  /// Material 3 Dark Color Scheme
  static const ColorScheme darkColorScheme = ColorScheme(
    brightness: Brightness.dark,
    primary: primaryIndigoLight,
    onPrimary: Color(0xFF0B0F19),
    primaryContainer: Color(0xFF312E81),
    onPrimaryContainer: Color(0xFFE0E7FF),
    secondary: secondaryPurpleLight,
    onSecondary: Color(0xFF0B0F19),
    secondaryContainer: Color(0xFF4C1D95),
    onSecondaryContainer: Color(0xFFEDE9FE),
    tertiary: tertiaryCyanLight,
    onTertiary: Color(0xFF0B0F19),
    tertiaryContainer: Color(0xFF164E63),
    onTertiaryContainer: Color(0xFFCFFAFE),
    error: Color(0xFFF87171),
    onError: Color(0xFF0B0F19),
    errorContainer: Color(0xFF7F1D1D),
    onErrorContainer: Color(0xFFFECACA),
    surface: darkSurface,
    onSurface: darkTextPrimary,
    surfaceContainerHighest: darkSurfaceVariant,
    onSurfaceVariant: darkTextSecondary,
    outline: darkOutline,
    outlineVariant: Color(0xFF334155),
    shadow: Colors.black,
  );

  /// Material 3 Light ThemeData
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      colorScheme: lightColorScheme,
      scaffoldBackgroundColor: Colors.transparent,
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        systemOverlayStyle: SystemUiOverlayStyle.dark,
        titleTextStyle: TextStyle(
          color: lightTextPrimary,
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      cardTheme: CardTheme(
        color: lightSurface.withValues(alpha: 0.92),
        elevation: 0,
        shape: RoundedRectangleBorder(
          side: const BorderSide(color: lightOutline, width: 1),
          borderRadius: BorderRadius.circular(16),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: lightSurfaceVariant.withValues(alpha: 0.8),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: lightOutline),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: lightOutline),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: primaryIndigo, width: 2),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: lightSurface.withValues(alpha: 0.95),
        elevation: 8,
        indicatorColor: lightColorScheme.primaryContainer,
      ),
      extensions: const [
        AppColorsExtension.light,
      ],
    );
  }

  /// Material 3 Dark ThemeData
  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: darkColorScheme,
      scaffoldBackgroundColor: Colors.transparent,
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        systemOverlayStyle: SystemUiOverlayStyle.light,
        titleTextStyle: TextStyle(
          color: darkTextPrimary,
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      cardTheme: CardTheme(
        color: darkSurface.withValues(alpha: 0.92),
        elevation: 0,
        shape: RoundedRectangleBorder(
          side: const BorderSide(color: darkOutline, width: 1),
          borderRadius: BorderRadius.circular(16),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: darkSurfaceVariant.withValues(alpha: 0.8),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: darkOutline),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: darkOutline),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: primaryIndigoLight, width: 2),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: darkSurface.withValues(alpha: 0.95),
        elevation: 8,
        indicatorColor: darkColorScheme.primaryContainer,
      ),
      extensions: const [
        AppColorsExtension.dark,
      ],
    );
  }
}

/// Context extensions for easy access to theme attributes and background helpers
extension AppThemeContextX on BuildContext {
  ThemeData get theme => Theme.of(this);
  ColorScheme get colorScheme => Theme.of(this).colorScheme;
  bool get isDarkMode => Theme.of(this).brightness == Brightness.dark;

  AppColorsExtension get appColors =>
      Theme.of(this).extension<AppColorsExtension>() ??
      (isDarkMode ? AppColorsExtension.dark : AppColorsExtension.light);
}

/// A ready-to-use widget wrapper that maps section keys ('home', 'study', etc.)
/// to background images in 'assets/backgrounds/' with fallback colors/gradients and scrims.
class CustomBackgroundWrapper extends StatelessWidget {
  /// The section key (e.g., 'home', 'study', 'ask_delulu', 'planner', 'focus', 'progress', 'settings')
  final String sectionKey;
  final Widget child;
  final bool forceFallback;

  const CustomBackgroundWrapper({
    super.key,
    required this.sectionKey,
    required this.child,
    this.forceFallback = false,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = context.isDarkMode;
    final assetPath = AppBackgroundConfig.getAssetPath(sectionKey);

    return Stack(
      fit: StackFit.expand,
      children: [
        // Base fallback color/gradient layer
        DecoratedBox(
          decoration: BoxDecoration(
            color: AppBackgroundConfig.getFallbackColor(sectionKey, isDark: isDark),
            gradient: AppBackgroundConfig.getFallbackGradient(sectionKey, isDark: isDark),
          ),
        ),

        // Asset image layer with error fallback logic
        if (!forceFallback)
          Image.asset(
            assetPath,
            fit: BoxFit.cover,
            errorBuilder: (context, error, stackTrace) {
              // Graceful fallback to the gradient if the image file is missing
              return const SizedBox.shrink();
            },
          ),

        // Readability scrim overlay
        DecoratedBox(
          decoration: BoxDecoration(
            color: AppBackgroundConfig.getScrimColor(isDark: isDark),
          ),
        ),

        // Content
        child,
      ],
    );
  }
}
