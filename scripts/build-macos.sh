#!/usr/bin/env bash
set -euo pipefail

# Путь к JDK 17 с jpackage (Homebrew)
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

APP_NAME="BlackJack"
APP_VERSION="1.0.0"
MAIN_CLASS="com.example.blackjack.Main"
JAR_NAME="blackjack-desktop-${APP_VERSION}.jar"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# Сборка jar
mvn -q -DskipTests package

# Подготовка директорий
mkdir -p dist

# Создание .dmg
echo "🔨 Создание .dmg с jpackage..."
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

# Исправление прав доступа и удаление атрибутов карантина
echo "🔧 Исправление прав доступа..."
DMG_FILE="dist/${APP_NAME}-${APP_VERSION}.dmg"

if [[ -f "$DMG_FILE" ]]; then
    # Временное монтирование для исправления прав
    TEMP_MOUNT="/tmp/blackjack_fix_$$"
    mkdir -p "$TEMP_MOUNT"
    
    echo "📱 Монтирование .dmg для исправления прав..."
    if hdiutil attach "$DMG_FILE" -mountpoint "$TEMP_MOUNT" -quiet; then
        APP_PATH="$TEMP_MOUNT/BlackJack.app"
        
        if [[ -d "$APP_PATH" ]]; then
            echo "🛠 Исправление прав доступа и удаление атрибутов карантина..."
            
            # Удаляем атрибуты карантина
            xattr -cr "$APP_PATH" 2>/dev/null || true
            
            # Устанавливаем правильные права доступа
            chmod -R 755 "$APP_PATH"
            chmod +x "$APP_PATH/Contents/MacOS/BlackJack"
            
            # Проверяем права
            if [[ -x "$APP_PATH/Contents/MacOS/BlackJack" ]]; then
                echo "✅ Права доступа исправлены"
            else
                echo "⚠️ Предупреждение: не удалось исправить права доступа"
            fi
        fi
        
        # Размонтируем
        hdiutil detach "$TEMP_MOUNT" -quiet
        rmdir "$TEMP_MOUNT"
    else
        echo "⚠️ Не удалось смонтировать .dmg для исправления прав"
    fi
fi

echo "✅ Создан дистрибутив: $DMG_FILE"
echo "📊 Размер файла: $(du -h "$DMG_FILE" | cut -f1)"
