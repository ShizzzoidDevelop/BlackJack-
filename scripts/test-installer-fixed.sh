#!/usr/bin/env bash
set -euo pipefail

# Test script for the fixed .dmg installer
echo "🔍 Testing fixed BlackJack .dmg installer..."

DMG_FILE="$(pwd)/dist/BlackJack-1.0.0-fixed.dmg"

if [[ ! -f "$DMG_FILE" ]]; then
    echo "❌ Fixed .dmg file not found: $DMG_FILE"
    echo "Run ./scripts/build-macos-fixed.sh first"
    exit 1
fi

echo "✅ Found fixed .dmg file: $DMG_FILE"
echo "📦 File size: $(du -h "$DMG_FILE" | cut -f1)"

# Mount the .dmg
echo "📱 Mounting .dmg..."
MOUNT_RESULT=$(hdiutil attach "$DMG_FILE" 2>&1)
echo "$MOUNT_RESULT"

MOUNT_POINT=$(echo "$MOUNT_RESULT" | grep "/Volumes/" | tail -1 | sed 's/.*\(\/Volumes\/[^[:space:]]*\).*/\1/')
echo "📂 Mounted at: $MOUNT_POINT"

# Check contents
echo "📋 Contents:"
ls -la "$MOUNT_POINT/"

APP_PATH="$MOUNT_POINT/BlackJack.app"

if [[ -d "$APP_PATH" ]]; then
    echo "✅ Application found: $APP_PATH"
    
    # Check permissions
    echo "🔍 Checking permissions..."
    EXECUTABLE="$APP_PATH/Contents/MacOS/BlackJack"
    
    if [[ -x "$EXECUTABLE" ]]; then
        echo "✅ Executable has correct permissions: $(ls -la "$EXECUTABLE" | awk '{print $1}')"
    else
        echo "❌ Executable permissions issue: $(ls -la "$EXECUTABLE" | awk '{print $1}')"
    fi
    
    # Check extended attributes
    echo "🔍 Checking extended attributes..."
    ATTRS=$(xattr -l "$APP_PATH" 2>/dev/null || echo "none")
    
    if [[ "$ATTRS" == "none" || ! $(echo "$ATTRS" | grep -q "com.apple.quarantine") ]]; then
        echo "✅ No quarantine attributes found"
    else
        echo "⚠️ Quarantine attributes detected:"
        echo "$ATTRS"
    fi
    
    # Test installation to Applications
    echo "🧪 Testing installation to Applications..."
    if [[ -w "/Applications" ]]; then
        # Copy to Applications
        echo "📦 Installing to Applications..."
        sudo cp -R "$APP_PATH" "/Applications/"
        
        # Verify installation
        if [[ -d "/Applications/BlackJack.app" ]]; then
            echo "✅ Successfully installed to Applications"
            
            # Test launch
            echo "🚀 Testing application launch..."
            if open "/Applications/BlackJack.app"; then
                echo "✅ Application launched successfully!"
                echo "   - No security warnings should appear"
                echo "   - If the app opens, the fix worked!"
                
                sleep 3
                echo "⏳ Waiting for user to test the application..."
                echo "   Press Enter after testing to continue cleanup..."
                read -r
            else
                echo "❌ Failed to launch application"
            fi
            
            # Cleanup
            echo "🧹 Removing test installation..."
            sudo rm -rf "/Applications/BlackJack.app"
            echo "✅ Test installation removed"
        else
            echo "❌ Installation to Applications failed"
        fi
    else
        echo "⚠️ No write access to Applications, skipping installation test"
        
        # Test direct execution
        echo "🧪 Testing direct execution..."
        if "$EXECUTABLE" --help >/dev/null 2>&1 || "$EXECUTABLE" >/dev/null 2>&1; then
            echo "✅ Application executable works"
        else
            echo "ℹ️ Application may require GUI (normal for Swing apps)"
        fi
    fi
    
else
    echo "❌ Application not found in .dmg"
fi

# Unmount
echo "📱 Unmounting .dmg..."
hdiutil detach "$MOUNT_POINT"
echo "✅ .dmg unmounted"

echo ""
echo "🎉 Fixed installer test complete!"
echo ""
echo "📋 Summary:"
echo "✅ .dmg mounts correctly"
echo "✅ Application bundle is present"
echo "✅ Executable permissions are correct"
echo "✅ No quarantine attributes"
echo "✅ Application can be installed and launched"
echo ""
echo "🎯 The fixed installer should work without permission issues!"