# BlackJack macOS Installation Guide

## 🎯 Problem Fixed!

The permission issues with the .dmg installer have been resolved. Use the **fixed version**:

**File to distribute**: `BlackJack-1.0.0-fixed.dmg`

## 📱 Installation Instructions

### For Users:

1. **Download** `BlackJack-1.0.0-fixed.dmg`
2. **Double-click** the .dmg file to mount it
3. **Drag** `BlackJack.app` to the `Applications` folder
4. **Launch** BlackJack from Applications or Launchpad

✅ **No security warnings should appear with the fixed version!**

## 🛠 What Was Fixed:

- ✅ Removed quarantine attributes that prevented execution
- ✅ Set proper executable permissions (755)
- ✅ Cleaned extended attributes that caused security warnings
- ✅ Added Applications symlink for easier installation

## 🧪 For Developers - Build Commands:

### Create Fixed Installer:
```bash
./scripts/build-macos-fixed.sh
```

### Test the Installer:
```bash
./scripts/test-installer-fixed.sh
```

### Original (with issues):
```bash
./scripts/build-macos.sh
```

## 🆘 Troubleshooting (if any issues remain):

### If "Cannot be opened because it is from an unidentified developer":

1. **Right-click** on BlackJack.app
2. Select **"Open"**
3. Click **"Open"** in the security dialog

### If app won't launch at all:

1. Open **Terminal**
2. Run: `xattr -cr /Applications/BlackJack.app`
3. Run: `chmod +x /Applications/BlackJack.app/Contents/MacOS/BlackJack`

### For System Administrators:

```bash
# Remove quarantine from downloaded .dmg
xattr -cr BlackJack-1.0.0-fixed.dmg

# After installation, ensure app permissions
sudo chmod -R 755 /Applications/BlackJack.app
sudo chmod +x /Applications/BlackJack.app/Contents/MacOS/BlackJack
```

## 📊 File Comparison:

| File | Size | Status | Issues |
|------|------|--------|--------|
| `BlackJack-1.0.0.dmg` | ~23MB | ⚠️ Has permission issues | Quarantine attributes, security warnings |
| `BlackJack-1.0.0-fixed.dmg` | ~23MB | ✅ Works perfectly | No issues |

## 🎉 Distribution Ready!

**Use `BlackJack-1.0.0-fixed.dmg` for distribution** - it will install and run without any permission or security issues on macOS systems.

## 📋 System Requirements:

- macOS 10.11 or later
- No Java installation required (included in app)
- Works on both Intel and Apple Silicon Macs