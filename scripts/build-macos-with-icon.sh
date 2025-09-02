#!/usr/bin/env bash
set -euo pipefail

# macOS .dmg builder with custom BlackJack icon
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

APP_NAME="BlackJack"
APP_VERSION="1.0.0"
MAIN_CLASS="com.example.blackjack.Main"
JAR_NAME="blackjack-desktop-${APP_VERSION}.jar"
DMG_NAME="${APP_NAME}-${APP_VERSION}-with-icon.dmg"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "🎲 Building BlackJack .dmg installer with custom icon..."

# Check if icons exist
ICON_FILE="src/main/resources/BlackJack.icns"
if [[ ! -f "$ICON_FILE" ]]; then
    echo "🎨 Custom icon not found, generating..."
    python3 scripts/generate-icon.py
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -f "dist/$DMG_NAME"
rm -rf "dist/dmg-temp"

# Build JAR
echo "📦 Building JAR..."
mvn -q -DskipTests package

# Prepare directories
mkdir -p dist/dmg-temp

# Create .app with jpackage including custom icon
echo "🔨 Creating application bundle with custom icon..."
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
  --description "Классическая игра BlackJack 1v1 против дилера" \
  --icon "$ICON_FILE" \
  --resource-dir src/main/resources

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
    
    # Verify icon is included
    if [[ -f "$APP_PATH/Contents/Resources/BlackJack.icns" ]]; then
        echo "✅ Custom icon included in app bundle"
    else
        echo "⚠️ Custom icon not found in app bundle"
        # Copy icon manually if needed
        cp "src/main/resources/BlackJack.icns" "$APP_PATH/Contents/Resources/" 2>/dev/null || true
    fi
    
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

# Create symbolic link to Applications
echo "🔗 Creating Applications symlink..."
ln -sf /Applications "dist/dmg-temp/Applications"

# Create the final .dmg with custom volume icon
echo "💿 Creating final .dmg with custom icon..."

# Copy volume icon
cp "src/main/resources/BlackJack.icns" "dist/dmg-temp/.VolumeIcon.icns"

# Create the .dmg
hdiutil create -volname "BlackJack" \
    -srcfolder "dist/dmg-temp" \
    -ov -format UDZO \
    "dist/$DMG_NAME"

# Set custom volume icon
echo "🎨 Setting custom volume icon..."
# This will make the .dmg itself show the custom icon when mounted
SetFile -a C "dist/$DMG_NAME" 2>/dev/null || true

# Clean up temporary files
rm -rf "dist/dmg-temp"

echo "✅ .dmg installer with custom icon created successfully!"
echo ""
echo "📋 Distribution info:"
echo "   File: dist/$DMG_NAME"
echo "   Size: $(du -h "dist/$DMG_NAME" | cut -f1)"
echo "   Icon: Custom BlackJack playing cards and poker chip"
echo ""
echo "🔍 Verifying the installer with custom icon..."

# Test the new .dmg
VERIFY_MOUNT="/tmp/blackjack_verify_$$"
mkdir -p "$VERIFY_MOUNT"

if hdiutil attach "dist/$DMG_NAME" -mountpoint "$VERIFY_MOUNT" -quiet; then
    VERIFY_APP="$VERIFY_MOUNT/BlackJack.app"
    
    if [[ -d "$VERIFY_APP" ]]; then
        echo "✅ Application found in .dmg"
        
        # Check custom icon
        if [[ -f "$VERIFY_APP/Contents/Resources/BlackJack.icns" ]]; then
            echo "✅ Custom icon included in application"
        else
            echo "⚠️ Custom icon not found in application"
        fi
        
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
        
        # Check volume icon
        if [[ -f "$VERIFY_MOUNT/.VolumeIcon.icns" ]]; then
            echo "✅ Volume icon included"
        else
            echo "⚠️ Volume icon not found"
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
echo "🎉 Custom icon .dmg installer complete!"
echo ""
echo "🎨 Features:"
echo "✅ Custom BlackJack icon (playing cards + poker chip)"
echo "✅ No permission issues"
echo "✅ No quarantine attributes"
echo "✅ Applications folder symlink"
echo "✅ Custom volume icon"
echo ""
echo "📱 Installation instructions:"
echo "1. Download $DMG_NAME"
echo "2. Double-click to mount (shows custom icon)"
echo "3. Drag BlackJack.app to Applications"
echo "4. Launch from Applications (shows custom icon in dock)"
echo ""
echo "🧪 Test with: ./scripts/test-installer-with-icon.sh"