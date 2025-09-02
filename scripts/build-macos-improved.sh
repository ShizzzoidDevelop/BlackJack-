#!/usr/bin/env bash
set -euo pipefail

# Improved macOS .dmg builder with custom layout
# Путь к JDK 17 с jpackage (Homebrew)
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

APP_NAME="BlackJack"
APP_VERSION="1.0.0"
MAIN_CLASS="com.example.blackjack.Main"
JAR_NAME="blackjack-desktop-${APP_VERSION}.jar"
DMG_NAME="${APP_NAME}-${APP_VERSION}.dmg"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "🎲 Building BlackJack .dmg installer..."

# Сборка jar
echo "📦 Building JAR..."
mvn -q -DskipTests package

# Подготовка директорий
mkdir -p dist
rm -f "dist/$DMG_NAME"

# Создание базового .dmg с jpackage
echo "🔨 Creating base .dmg with jpackage..."
jpackage \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --dest dist \
  --vendor "BlackJack Game Studio" \
  --input target \
  --main-jar "$JAR_NAME" \
  --main-class "$MAIN_CLASS" \
  --add-modules java.base,java.desktop \
  --mac-package-identifier com.example.blackjack \
  --mac-package-name "$APP_NAME" \
  --copyright "Copyright © 2025 BlackJack Game Studio" \
  --description "Классическая игра BlackJack 1v1 против дилера"

echo "✅ .dmg installer created: dist/$DMG_NAME"
echo "📊 File size: $(du -h "dist/$DMG_NAME" | cut -f1)"

# Проверяем созданный .dmg
echo "🔍 Verifying .dmg installer..."

# Временное монтирование для проверки
TEMP_MOUNT="/tmp/blackjack_verify_$$"
if hdiutil attach "dist/$DMG_NAME" -mountpoint "$TEMP_MOUNT" -quiet; then
    if [[ -d "$TEMP_MOUNT/BlackJack.app" ]]; then
        echo "✅ Application bundle found in .dmg"
        
        # Проверяем исполняемый файл
        if [[ -x "$TEMP_MOUNT/BlackJack.app/Contents/MacOS/BlackJack" ]]; then
            echo "✅ Executable is present and has correct permissions"
        else
            echo "⚠️ Warning: Executable may have incorrect permissions"
        fi
        
        # Проверяем Info.plist
        if [[ -f "$TEMP_MOUNT/BlackJack.app/Contents/Info.plist" ]]; then
            VERSION=$(plutil -extract CFBundleShortVersionString raw "$TEMP_MOUNT/BlackJack.app/Contents/Info.plist" 2>/dev/null || echo "unknown")
            IDENTIFIER=$(plutil -extract CFBundleIdentifier raw "$TEMP_MOUNT/BlackJack.app/Contents/Info.plist" 2>/dev/null || echo "unknown")
            echo "✅ App version: $VERSION"
            echo "✅ Bundle identifier: $IDENTIFIER"
        fi
    else
        echo "❌ Error: Application bundle not found in .dmg"
    fi
    
    # Размонтируем
    hdiutil detach "$TEMP_MOUNT" -quiet
else
    echo "❌ Error: Could not mount .dmg for verification"
fi

echo ""
echo "🎉 Build complete!"
echo ""
echo "📋 Distribution info:"
echo "   File: dist/$DMG_NAME"
echo "   Size: $(du -h "dist/$DMG_NAME" | cut -f1)"
echo "   Type: macOS Disk Image (.dmg)"
echo ""
echo "📱 Installation instructions for users:"
echo "1. Download $DMG_NAME"
echo "2. Double-click to mount the disk image"
echo "3. Drag BlackJack.app to Applications folder"
echo "4. Launch BlackJack from Applications or Launchpad"
echo ""
echo "🧪 To test the installer, run: ./scripts/test-installer.sh"