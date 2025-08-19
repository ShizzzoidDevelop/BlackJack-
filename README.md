# BlackJack (Java Swing)

Десктопное приложение BlackJack 1 на 1 против дилера. Классические правила, ставки фишками, лидерборд.

## 🚀 Запуск

Требуется Java 17+ и Maven.

```bash
cd /Users/timataskin/Desktop/BlackJack-
mvn -q -DskipTests package
java -jar target/blackjack-desktop-1.0.0.jar
```

Или во время разработки:

```bash
mvn -q exec:java
```

## 🎮 Правила и возможности

- Классический блэкджек: дилер берёт до 17, стоит на 17
- Выплата за блэкджек игроку 3:2
- Действия: «Ещё», «Стоп», «Удвоить» (на первых двух картах)
- Ставки фишками (быстрые кнопки 10/25/50/100/500)
- Лидерборд: суммарная прибыль по имени игрока, сохраняется в файле в домашней директории

## 🧱 Структура

```
src/main/java/com/example/blackjack/
  Main.java
  engine/BlackjackGame.java
  model/... (Card, Deck, Hand, Player, Dealer, enums)
  ui/MainFrame.java
  leaderboard/... (LeaderboardEntry, LeaderboardRepository)
```

## ❗ Примечания

- Лидерборд хранится в `~/.blackjack_leaderboard.csv`
- Начальный банк игрока: 1000 фишек

