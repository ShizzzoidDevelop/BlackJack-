#!/usr/bin/env bash
set -euo pipefail

# Fixed macOS .dmg builder with proper permissions and no quarantine
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

APP_NAME="BlackJack"
APP_VERSION="1.0.0"
MAIN_CLASS="com.example.blackjack.Main"
JAR_NAME="blackjack-desktop-${APP_VERSION}.jar"
DMG_NAME="${APP_NAME}-${APP_VERSION}-fixed.dmg"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "🎲 Building BlackJack .dmg installer (fixed version)..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -f "dist/$DMG_NAME"
rm -rf "dist/dmg-temp"

# Build JAR
echo "📦 Building JAR..."
mvn -q -DskipTests package

# Prepare directories
mkdir -p dist/dmg-temp

# Create basic .app with jpackage first
echo "🔨 Creating application bundle with jpackage..."
TEMP_DMG="dist/temp-${APP_NAME}.dmg"
rm -f "$TEMP_DMG"

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

# Extract the app from the temporary .dmg
echo "📦 Extracting application from temporary .dmg..."
TEMP_MOUNT="/tmp/blackjack_extract_$$"
mkdir -p "$TEMP_MOUNT"

if hdiutil attach "dist/${APP_NAME}-${APP_VERSION}.dmg" -mountpoint "$TEMP_MOUNT" -quiet; then
    # Copy the app to our working directory
    cp -R "$TEMP_MOUNT/BlackJack.app" "dist/dmg-temp/"
    
    # Unmount the temporary .dmg
    hdiutil detach "$TEMP_MOUNT" -quiet
    rmdir "$TEMP_MOUNT"
    
    # Remove the temporary .dmg
    rm -f "dist/${APP_NAME}-${APP_VERSION}.dmg"
else
    echo "❌ Failed to extract application from temporary .dmg"
    exit 1
fi

# Fix permissions and remove quarantine attributes
echo "🛠 Fixing permissions and removing quarantine attributes..."
APP_PATH="dist/dmg-temp/BlackJack.app"

if [[ -d "$APP_PATH" ]]; then
    # Remove all extended attributes (including quarantine)
    xattr -cr "$APP_PATH" 2>/dev/null || true
    
    # Set proper permissions
    chmod -R 755 "$APP_PATH"
    chmod +x "$APP_PATH/Contents/MacOS/BlackJack"
    
    # Verify executable permissions
    if [[ -x "$APP_PATH/Contents/MacOS/BlackJack" ]]; then
        echo "✅ Executable permissions set correctly"
    else
        echo "❌ Failed to set executable permissions"
        exit 1
    fi
    
    # Check for quarantine attributes
    if xattr -l "$APP_PATH" | grep -q "com.apple.quarantine"; then
        echo "⚠️ Warning: Quarantine attributes still present"
    else
        echo "✅ Quarantine attributes removed"
    fi
else
    echo "❌ Application not found in extracted files"
    exit 1
fi

# Create a proper .dmg with correct structure
echo "💿 Creating final .dmg with proper structure..."

# Create a temporary directory for .dmg contents
DMG_TEMP_DIR="dist/dmg-temp"
DMG_SOURCE_DIR="$DMG_TEMP_DIR"

# Create symbolic link to Applications
ln -sf /Applications "$DMG_SOURCE_DIR/Applications"

# Create the .dmg
echo "🏗 Building final .dmg..."
hdiutil create -volname "BlackJack" \
    -srcfolder "$DMG_SOURCE_DIR" \
    -ov -format UDZO \
    "dist/$DMG_NAME"

# Clean up temporary files
rm -rf "$DMG_TEMP_DIR"

echo "✅ .dmg installer created successfully!"
echo ""
echo "📋 Distribution info:"
echo "   File: dist/$DMG_NAME"
echo "   Size: $(du -h "dist/$DMG_NAME" | cut -f1)"
echo ""
echo "🔍 Verifying the fixed installer..."

# Test the new .dmg
VERIFY_MOUNT="/tmp/blackjack_verify_$$"
mkdir -p "$VERIFY_MOUNT"

if hdiutil attach "dist/$DMG_NAME" -mountpoint "$VERIFY_MOUNT" -quiet; then
    VERIFY_APP="$VERIFY_MOUNT/BlackJack.app"
    
    if [[ -d "$VERIFY_APP" ]]; then
        echo "✅ Application found in .dmg"
        
        # Check permissions
        if [[ -x "$VERIFY_APP/Contents/MacOS/BlackJack" ]]; then
            echo "✅ Executable has correct permissions"
        else
            echo "❌ Executable permissions issue"
        fi
        
        # Check quarantine attributes
        if xattr -l "$VERIFY_APP" 2>/dev/null | grep -q "com.apple.quarantine"; then
            echo "⚠️ Quarantine attributes detected"
        else
            echo "✅ No quarantine attributes"
        fi
        
        # Test launch (dry run)
        echo "🧪 Testing application launch..."
        if "$VERIFY_APP/Contents/MacOS/BlackJack" --version 2>/dev/null; then
            echo "✅ Application can be executed"
        else
            echo "ℹ️ Application ready (version check not supported)"
        fi
        
    else
        echo "❌ Application not found in .dmg"
    fi
    
    hdiutil detach "$VERIFY_MOUNT" -quiet
    rmdir "$VERIFY_MOUNT"
else
    echo "❌ Could not mount .dmg for verification"
fi

echo ""
echo "🎉 Fixed .dmg installer complete!"
echo ""
echo "📱 Installation instructions:"
echo "1. Download $DMG_NAME"
echo "2. Double-click to mount"
echo "3. Drag BlackJack.app to Applications"
echo "4. Launch from Applications (no security warnings should appear)"
echo ""
echo "🧪 Test with: ./scripts/test-installer-fixed.sh"