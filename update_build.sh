sed -i 's/alias(libs.plugins.google.devtools.ksp)/alias(libs.plugins.google.devtools.ksp)\n  alias(libs.plugins.secrets)/' app/build.gradle.kts
sed -i 's/compose = true/compose = true\n    buildConfig = true/' app/build.gradle.kts
echo "secrets { propertiesFileName = \".env\"; defaultPropertiesFileName = \".env.example\" }" >> app/build.gradle.kts
sed -i 's/# GEMINI_API_KEY/GEMINI_API_KEY/' .env.example
