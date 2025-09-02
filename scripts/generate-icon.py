#!/usr/bin/env python3
"""
BlackJack Application Icon Generator
Creates a custom icon featuring playing cards and poker chips
"""

import os
import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False

def create_blackjack_icon(size=512):
    """Create a BlackJack application icon with playing cards and poker chip"""
    
    # Create a new image with transparent background
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Colors
    background_color = (20, 70, 30, 255)  # Dark green like casino table
    card_color = (255, 255, 255, 255)     # White card
    black_color = (0, 0, 0, 255)          # Black
    red_color = (220, 20, 20, 255)        # Red
    chip_base = (40, 40, 40, 255)         # Dark chip base
    chip_edge = (200, 180, 40, 255)       # Gold chip edge
    
    # Draw rounded background
    margin = size // 20
    draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=size // 8,
        fill=background_color
    )
    
    # Card dimensions
    card_width = size // 3
    card_height = int(card_width * 1.4)
    
    # Draw first card (Ace of Spades) - slightly rotated
    card1_x = size // 6
    card1_y = size // 6
    
    # Card background
    draw.rounded_rectangle(
        [card1_x, card1_y, card1_x + card_width, card1_y + card_height],
        radius=size // 40,
        fill=card_color,
        outline=black_color,
        width=2
    )
    
    # Draw "A" for Ace
    font_size = card_width // 3
    try:
        # Try to use system font
        font = ImageFont.truetype("Arial", font_size)
    except:
        # Fallback to default font
        font = ImageFont.load_default()
    
    # Draw "A" in top-left
    draw.text(
        (card1_x + font_size // 4, card1_y + font_size // 6),
        "A",
        fill=black_color,
        font=font
    )
    
    # Draw spade symbol (simplified)
    spade_x = card1_x + card_width // 2
    spade_y = card1_y + card_height // 2
    spade_size = font_size // 2
    
    # Spade shape (simplified heart upside down)
    draw.ellipse(
        [spade_x - spade_size//2, spade_y - spade_size//3,
         spade_x + spade_size//2, spade_y + spade_size//3],
        fill=black_color
    )
    draw.polygon(
        [(spade_x, spade_y + spade_size//3),
         (spade_x - spade_size//4, spade_y + spade_size//2),
         (spade_x + spade_size//4, spade_y + spade_size//2)],
        fill=black_color
    )
    
    # Draw second card (King of Hearts) - overlapping
    card2_x = size // 2
    card2_y = size // 4
    
    # Card background
    draw.rounded_rectangle(
        [card2_x, card2_y, card2_x + card_width, card2_y + card_height],
        radius=size // 40,
        fill=card_color,
        outline=black_color,
        width=2
    )
    
    # Draw "K" for King
    draw.text(
        (card2_x + font_size // 4, card2_y + font_size // 6),
        "K",
        fill=red_color,
        font=font
    )
    
    # Draw heart symbol
    heart_x = card2_x + card_width // 2
    heart_y = card2_y + card_height // 2
    heart_size = font_size // 2
    
    # Heart shape (two circles and triangle)
    draw.ellipse(
        [heart_x - heart_size//2, heart_y - heart_size//4,
         heart_x, heart_y + heart_size//4],
        fill=red_color
    )
    draw.ellipse(
        [heart_x, heart_y - heart_size//4,
         heart_x + heart_size//2, heart_y + heart_size//4],
        fill=red_color
    )
    draw.polygon(
        [(heart_x - heart_size//2, heart_y),
         (heart_x + heart_size//2, heart_y),
         (heart_x, heart_y + heart_size//2)],
        fill=red_color
    )
    
    # Draw poker chip in bottom right
    chip_size = size // 4
    chip_x = size - chip_size - size // 10
    chip_y = size - chip_size - size // 10
    
    # Chip outer ring
    draw.ellipse(
        [chip_x, chip_y, chip_x + chip_size, chip_y + chip_size],
        fill=chip_edge
    )
    
    # Chip inner circle
    inner_margin = chip_size // 8
    draw.ellipse(
        [chip_x + inner_margin, chip_y + inner_margin,
         chip_x + chip_size - inner_margin, chip_y + chip_size - inner_margin],
        fill=chip_base
    )
    
    # Chip center circle
    center_margin = chip_size // 3
    draw.ellipse(
        [chip_x + center_margin, chip_y + center_margin,
         chip_x + chip_size - center_margin, chip_y + chip_size - center_margin],
        fill=chip_edge
    )
    
    # Draw "21" on the chip
    small_font_size = chip_size // 4
    try:
        small_font = ImageFont.truetype("Arial", small_font_size)
    except:
        small_font = ImageFont.load_default()
    
    text_bbox = draw.textbbox((0, 0), "21", font=small_font)
    text_width = text_bbox[2] - text_bbox[0]
    text_height = text_bbox[3] - text_bbox[1]
    
    draw.text(
        (chip_x + chip_size//2 - text_width//2,
         chip_y + chip_size//2 - text_height//2),
        "21",
        fill=black_color,
        font=small_font
    )
    
    return img

def generate_icon_sizes():
    """Generate icon in multiple sizes for different platforms"""
    
    if not PIL_AVAILABLE:
        print("❌ PIL (Pillow) not available. Installing...")
        os.system("pip3 install Pillow")
        try:
            from PIL import Image, ImageDraw, ImageFont
        except ImportError:
            print("❌ Failed to install PIL. Please install manually: pip3 install Pillow")
            return False
    
    # Create resources directory
    resources_dir = Path("src/main/resources")
    resources_dir.mkdir(parents=True, exist_ok=True)
    
    # Icon sizes for different platforms
    sizes = {
        'icon_16.png': 16,
        'icon_32.png': 32,
        'icon_48.png': 48,
        'icon_64.png': 64,
        'icon_128.png': 128,
        'icon_256.png': 256,
        'icon_512.png': 512,
        'icon_1024.png': 1024,
        'app_icon.png': 512,  # Main app icon
    }
    
    print("🎨 Generating BlackJack application icons...")
    
    for filename, size in sizes.items():
        print(f"📱 Creating {filename} ({size}x{size})")
        
        # Create icon
        icon = create_blackjack_icon(size)
        
        # Save icon
        icon_path = resources_dir / filename
        icon.save(icon_path, 'PNG')
        print(f"✅ Saved: {icon_path}")
    
    # Create .icns file for macOS (if iconutil is available)
    print("🍎 Creating .icns file for macOS...")
    create_icns_file()
    
    # Create .ico file for Windows
    print("🪟 Creating .ico file for Windows...")
    create_ico_file()
    
    return True

def create_icns_file():
    """Create .icns file for macOS using iconutil"""
    
    resources_dir = Path("src/main/resources")
    iconset_dir = resources_dir / "BlackJack.iconset"
    
    # Create iconset directory
    iconset_dir.mkdir(exist_ok=True)
    
    # Required sizes for .icns
    icns_sizes = {
        'icon_16x16.png': 16,
        'icon_16x16@2x.png': 32,
        'icon_32x32.png': 32,
        'icon_32x32@2x.png': 64,
        'icon_128x128.png': 128,
        'icon_128x128@2x.png': 256,
        'icon_256x256.png': 256,
        'icon_256x256@2x.png': 512,
        'icon_512x512.png': 512,
        'icon_512x512@2x.png': 1024,
    }
    
    # Generate iconset files
    for filename, size in icns_sizes.items():
        icon = create_blackjack_icon(size)
        icon_path = iconset_dir / filename
        icon.save(icon_path, 'PNG')
    
    # Convert to .icns using iconutil (macOS only)
    icns_path = resources_dir / "BlackJack.icns"
    result = os.system(f"iconutil -c icns '{iconset_dir}' -o '{icns_path}'")
    
    if result == 0:
        print(f"✅ Created: {icns_path}")
        # Clean up iconset directory
        os.system(f"rm -rf '{iconset_dir}'")
    else:
        print("⚠️ iconutil not available (only works on macOS)")

def create_ico_file():
    """Create .ico file for Windows"""
    try:
        resources_dir = Path("src/main/resources")
        
        # Create multiple sizes for .ico
        ico_sizes = [16, 32, 48, 64, 128, 256]
        images = []
        
        for size in ico_sizes:
            icon = create_blackjack_icon(size)
            images.append(icon)
        
        # Save as .ico
        ico_path = resources_dir / "BlackJack.ico"
        images[0].save(ico_path, format='ICO', sizes=[(img.width, img.height) for img in images])
        print(f"✅ Created: {ico_path}")
        
    except Exception as e:
        print(f"⚠️ Could not create .ico file: {e}")

if __name__ == "__main__":
    print("🎲 BlackJack Icon Generator")
    print("=" * 40)
    
    if generate_icon_sizes():
        print("\n🎉 Icon generation complete!")
        print("\n📋 Generated files:")
        print("   - PNG icons in various sizes")
        print("   - BlackJack.icns (macOS)")
        print("   - BlackJack.ico (Windows)")
        print("\n🔧 Next steps:")
        print("   1. Run: ./scripts/build-macos-with-icon.sh")
        print("   2. The custom icon will be included in your .dmg")
    else:
        print("\n❌ Icon generation failed")
        sys.exit(1)