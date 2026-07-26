# Issue #47 audit: no class keep rules required. App code uses no reflection
# (no Class.forName/getDeclaredMethod) and no JSON/Java serialization
# (SharedPreferences stores primitives only). Custom views in layouts and
# manifest components are kept by AGP-generated rules. threetenabp reads
# TZDB.dat from assets (exempt from resource shrinking) and registers its
# ZoneRulesProvider directly, no ServiceLoader.

# Keep file/line info so release stack traces stay debuggable
-keepattributes SourceFile,LineNumberTable
# Collapse source file names since line numbers above are retained
-renamesourcefileattribute ''
