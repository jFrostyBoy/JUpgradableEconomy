## JUpgradableEconomy

**Ядро:** Paper / Spigot / Folia  
**Версия:** 1.16.5 - 1.21.11  
**Java:** 16+  

Плагин полностью заменяет стандартную экономику Vault. Игроки начинают с небольшого лимита баланса, который можно увеличивать за деньги через команду `/ecoupgrade`.  
Лимит работает жёстко — при превышении лишние деньги просто не зачисляются (с уведомлением об этом игрока).

### Особенности
- Полная интеграция с Vault (совместим с ChestShop, Jobs, EssentialsX и др.)
- Гибкое форматирование валюты: `$1000`, `1000 $`, `1k`, `1.5M`, `1,000` и т.д.
- Все сообщения и настройки в `config.yml`
- Автоимпорт балансов из старой экономики при первом запуске
- Сохранение данных в `players.yml`
- Уставновка звукового сопровождения для сообщений

### Команды и права

| Команда                | Описание                                       | Право                       | По умолчанию |
|------------------------|------------------------------------------------|-----------------------------|--------------|
| `/ecoupgrade`          | Прокачать лимит баланса                        | `jupgradableeconomy.player` | всем         |
| `/ecoreload`           | Перезагрузить конфиг и сохранить данные        | `jupgradableeconomy.admin`  | op           |
| `/ecoreset <игрок\|*>` | Сбросить прокачку лимита (у игрока или у всех) | `jupgradableeconomy.admin`  | op           |

#### Примеры использования `/ecoreset`
- `/ecoreset Steve` — сбросить прокачку только у Steve
- `/ecoreset *` — сбросить прокачку у **всех** игроков (уровень → 0)

### Настройка (config.yml)

```yaml
# Стартовый баланс для новых игроков
starting-balance: 0.0

# Начальный максимальный лимит
default-max-balance: 1000.0

# Символ валюты
currency-symbol: "$"

# Формат отображения и сокращения суммы
currency-format:
  # Как показывать баланс: {amount} — форматированное число, {currency} — символ валюты
  display-format: "{currency}{amount}"  # Примеры: "$1000", "{amount} {currency}" → "1000 $"

  # Как сокращать большие числа
  # default — обычное число: 1000 → 1000
  # commas   — с разделителями: 1000 → 1,000 ; 1000000 → 1,000,000
  # formatted — сокращённо: 1000 → 1k ; 1500000 → 1.5M ; 1200000000 → 1.2B
  number-format: formatted   # варианты: default, commas, formatted

# Автоимпорт при первом запуске
auto-import: true

# Уровни апгрейда (можно добавлять новые)
upgrades:
  1:
    cost: 500.0
    max-balance: 2000.0
  2:
    cost: 1500.0
    max-balance: 5000.0
  3:
    cost: 4000.0
    max-balance: 10000.0
  4:
    cost: 10000.0
    max-balance: 25000.0
  5:
    cost: 25000.0
    max-balance: 50000.0

sounds:
  enabled: true                       # Включить/выключить все звуки плагина

  message:                            # Обычные сообщения (например, при частичном депозите)
    sound: "BLOCK_NOTE_BLOCK_PLING"
    pitch: 1.5
    volume: 1.0

  upgrade-success:                    # Успешная прокачка (/ecoupgrade)
    sound: "ENTITY_PLAYER_LEVELUP"
    pitch: 1.2
    volume: 1.0

  not-enough-money:                   # Недостаточно средств
    sound: "ENTITY_VILLAGER_NO"
    pitch: 1.0
    volume: 1.0

  max-level-reached:                  # Максимальный уровень уже достигнут
    sound: "ENTITY_ENDERMAN_SCREAM"
    pitch: 0.8
    volume: 0.8

  reset-success:                      # Сброс прокачки (/ecoreset)
    sound: "BLOCK_ANVIL_LAND"
    pitch: 0.7
    volume: 1.0

# Сообщения
messages:
  # Префикс можно остаить пустым (prefix: "") - для его отключения
  # Так же его можно прописать отдельно в сообщении
  # пример -> reload-success: "&7[&aJUE&7] &aПлагин перезагружен!"
  # Таким образом вы сами решаете возле каких сообщений будет отображён префикс
  prefix: "&7[&aJUE&7] "
  reload-success: "&aПлагин перезагружен!"
  reload-permission: "&cНет прав!"
  upgrade-success: "&aЛимит повышен!"
  upgrade-new-limit: "&7Новый лимит: &a{new_max}"
  upgrade-current-balance: "&7Баланс: &a{balance}"
  upgrade-no-more: "&cМаксимальный уровень достигнут!"
  upgrade-not-enough-money: "&cНедостаточно! Нужно: &a{cost}"
  deposit-limit-reached: "&eЛимит достигнут. Добавлена только часть суммы."
  player-only-command: "&cТолько для игроков!"
  import-complete: "&aИмпорт завершён (&3{players}&a игроков)."
  vault-not-found: "&cVault не найден!"
  reset-success: "&aПрокачка лимита успешно сброшена для &e{player}&a!"
  reset-all-success: "&aПрокачка лимита сброшена для &eвсех&a игроков!"
  reset-no-target: "&cУкажите игрока или * для всех."
  reset-player-not-found: "&cИгрок &e{player} &cне найден или никогда не заходил."
```

### Установка

- Положите `JUpgradableEconomy.jar` и `Vault.jar` в папку /plugins
- Перезапустите сервер
- При первом запуске плагин импортирует балансы из предыдущей экономики (Essentials, etc.) и отключит автоимпорт
- Готово! Игроки могут прокачивать лимит через `/ecoupgrade`

<img width="940" height="195" alt="зображення" src="https://github.com/user-attachments/assets/850f4f08-b491-488f-99b2-dc581fbe6934" />
