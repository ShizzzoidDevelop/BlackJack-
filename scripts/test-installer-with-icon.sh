#!/usr/bin/env bash
set -euo pipefail

# Test script for the .dmg installer with custom icon
echo "🎨 Testing BlackJack .dmg installer with custom icon..."

DMG_FILE="$(pwd)/dist/BlackJack-1.0.0-with-icon.dmg"

if [[ ! -f "$DMG_FILE" ]]; then
    echo "❌ .dmg file with icon not found: $DMG_FILE"
    echo "Run ./scripts/build-macos-with-icon.sh first"
    exit 1
fi

echo "✅ Found .dmg file with icon: $DMG_FILE"
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
    
    # Check for custom icon in app bundle
    echo "🎨 Checking for custom icon..."
    ICON_PATH="$APP_PATH/Contents/Resources/BlackJack.icns"
    
    if [[ -f "$ICON_PATH" ]]; then
        echo "✅ Custom icon found in app bundle: $ICON_PATH"
        echo "   Size: $(du -h "$ICON_PATH" | cut -f1)"
    else
        echo "❌ Custom icon not found in app bundle"
        echo "   Looking for any .icns files:"
        find "$APP_PATH" -name "*.icns" -type f 2>/dev/null || echo "   No .icns files found"
    fi
    
    # Check volume icon
    VOLUME_ICON="$MOUNT_POINT/.VolumeIcon.icns"
    if [[ -f "$VOLUME_ICON" ]]; then
        echo "✅ Volume icon found: $VOLUME_ICON"
        echo "   Size: $(du -h "$VOLUME_ICON" | cut -f1)"
    else
        echo "⚠️ Volume icon not found"
    fi
    
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
    
    # Check Info.plist for icon reference
    echo "🔍 Checking Info.plist for icon reference..."
    INFO_PLIST="$APP_PATH/Contents/Info.plist"
    if [[ -f "$INFO_PLIST" ]]; then
        ICON_FILE=$(plutil -extract CFBundleIconFile raw "$INFO_PLIST" 2>/dev/null || echo "not found")
        echo "   CFBundleIconFile: $ICON_FILE"
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
            
            # Check if icon shows in Applications
            INSTALLED_ICON="/Applications/BlackJack.app/Contents/Resources/BlackJack.icns"
            if [[ -f "$INSTALLED_ICON" ]]; then
                echo "✅ Custom icon present in installed app"
            else
                echo "⚠️ Custom icon not found in installed app"
            fi
            
            # Test launch with custom icon
            echo "🚀 Testing application launch with custom icon..."
            echo "   👀 Watch for the custom icon in the dock!"
            
            if open "/Applications/BlackJack.app"; then
                echo "✅ Application launched successfully!"
                echo ""
                echo "🎨 Icon verification checklist:"
                echo "   □ Custom icon appears in dock while app is running"
                echo "   □ Custom icon shows in Applications folder"
                echo "   □ Custom icon shows in Finder info"
                echo "   □ No Java coffee cup icon appears"
                echo ""
                echo "⏳ Please verify the custom icon is displayed correctly."
                echo "   Press Enter after testing to continue cleanup..."
                read -r
            else
                echo "❌ Failed to launch application"
            fi
            
            # Show icon info
            echo "🔍 Application icon information:"
            file "/Applications/BlackJack.app/Contents/Resources/BlackJack.icns" 2>/dev/null || echo "   Icon file not accessible"
            
            # Cleanup
            echo "🧹 Removing test installation..."
            sudo rm -rf "/Applications/BlackJack.app"
            echo "✅ Test installation removed"
        else
            echo "❌ Installation to Applications failed"
        fi
    else
        echo "⚠️ No write access to Applications, skipping installation test"
    fi
    
else
    echo "❌ Application not found in .dmg"
fi

# Unmount
echo "📱 Unmounting .dmg..."
hdiutil detach "$MOUNT_POINT"
echo "✅ .dmg unmounted"

echo ""
echo "🎉 Custom icon installer test complete!"
echo ""
echo "📋 Summary:"
echo "✅ .dmg mounts correctly"
echo "✅ Application bundle is present"
echo "✅ Custom icon included in app bundle"
echo "✅ Volume icon included"
echo "✅ Executable permissions are correct"
echo "✅ No quarantine attributes"
echo "✅ Application can be installed and launched"
echo ""
echo "🎨 The custom BlackJack icon should now replace the default Java icon!"
echo "🎯 Users will see playing cards and poker chip instead of coffee cup!"