#!/usr/bin/env bash
set -euo pipefail

# Скрипт для тестирования установщика .dmg
echo "🔍 Тестирование установщика BlackJack .dmg..."

DMG_FILE="$(pwd)/dist/BlackJack-1.0.0.dmg"

if [[ ! -f "$DMG_FILE" ]]; then
    echo "❌ Файл .dmg не найден: $DMG_FILE"
    echo "Запустите сначала build-macos.sh для создания установщика"
    exit 1
fi

echo "✅ Найден файл: $DMG_FILE"
echo "📦 Размер файла: $(du -h "$DMG_FILE" | cut -f1)"

# Проверяем тип файла
FILE_TYPE=$(file "$DMG_FILE")
echo "🔍 Тип файла: $FILE_TYPE"

# Монтируем .dmg
echo "📱 Монтирование .dmg..."
MOUNT_RESULT=$(hdiutil attach "$DMG_FILE" 2>&1)
echo "$MOUNT_RESULT"

# Извлекаем путь к монтированному тому
MOUNT_POINT=$(echo "$MOUNT_RESULT" | grep "/Volumes/" | tail -1 | sed 's/.*\(\/Volumes\/[^[:space:]]*\).*/\1/')
echo "📂 Том смонтирован в: $MOUNT_POINT"

# Проверяем содержимое
echo "📋 Содержимое установщика:"
ls -la "$MOUNT_POINT/"

# Проверяем наличие .app
APP_PATH="$MOUNT_POINT/BlackJack.app"
if [[ -d "$APP_PATH" ]]; then
    echo "✅ Приложение найдено: $APP_PATH"
    
    # Проверяем структуру .app
    echo "📱 Структура приложения:"
    ls -la "$APP_PATH/Contents/"
    
    # Проверяем Info.plist
    echo "ℹ️ Информация о приложении:"
    plutil -p "$APP_PATH/Contents/Info.plist" | head -10
    
    # Тестовая установка (копирование в Applications)
    echo "🧪 Тестовая установка в /Applications..."
    if [[ -w "/Applications" ]]; then
        sudo cp -R "$APP_PATH" "/Applications/"
        echo "✅ Приложение установлено в /Applications/BlackJack.app"
        
        # Проверяем, что приложение можно запустить
        echo "🚀 Тестирование запуска приложения..."
        if open "/Applications/BlackJack.app"; then
            echo "✅ Приложение успешно запустилось!"
            sleep 2
            echo "⚠️ Закройте приложение вручную для завершения теста"
        else
            echo "❌ Ошибка при запуске приложения"
        fi
        
        # Удаляем тестовую установку
        echo "🧹 Удаление тестовой установки..."
        sudo rm -rf "/Applications/BlackJack.app"
        echo "✅ Тестовая установка удалена"
    else
        echo "⚠️ Нет прав записи в /Applications, пропускаем тестовую установку"
    fi
else
    echo "❌ Приложение не найдено в .dmg"
fi

# Размонтируем .dmg
echo "📱 Размонтирование .dmg..."
hdiutil detach "$MOUNT_POINT"
echo "✅ .dmg размонтирован"

echo ""
echo "🎉 Тестирование завершено!"
echo ""
echo "📋 Инструкция для пользователей:"
echo "1. Скачайте файл BlackJack-1.0.0.dmg"
echo "2. Дважды кликните по файлу .dmg"
echo "3. Перетащите BlackJack.app в папку Applications"
echo "4. Запустите BlackJack из папки Applications или Launchpad"