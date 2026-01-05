## JUpgradableEconomy

**Ядро:** Paper / Spigot / Folia  
**Версия:** 1.16.5 - 1.21.11  
**Java:** 16+  
**Зависимости:** `Vault`, `PlaceholderAPI`  

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

| Команда                    | Описание                                                                  | Право                       | По умолчанию  |
|----------------------------|---------------------------------------------------------------------------|-----------------------------|---------------|
| `/ecoupgrade`              | Прокачать лимит баланса                                                   | `jupgradableeconomy.player` | всем          |
| `/ecoupgrade info <игрок>` | Посмотреть информацию о лимитах и прокачке другого игрока                 | `jupgradableeconomy.player` | всем          |
| `/ecoupgrade gift <игрок>` | Оплатить один уровень прокачки другому игроку (списание с вашего баланса) | `jupgradableeconomy.player` | всем          |
| `/ecoreload`               | Перезагрузить конфиг и сохранить данные                                   | `jupgradableeconomy.admin`  | op            |
| `/ecoreset <игрок\|*>`     | Сбросить прокачку лимита (у игрока или у всех)                            | `jupgradableeconomy.admin`  | op            |
| `/ecolimits`               | Показать текущий лимит и прогресс прокачки                                | `jupgradableeconomy.player` | всем          |

#### Примеры использования
- `/ecoupgrade` — обычная прокачка своего лимита
- `/ecoupgrade info Steve` — посмотреть лимиты Steve
- `/ecoupgrade gift Steve` — оплатить один уровень прокачки для Steve
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

# Кулдаун для команды /ecoupgrade gift (в секундах)
gift-cooldown: 300 # 5 минут

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
  usage-main: "&cИспользование: &e/ecoupgrade &7[info <игрок> | gift <игрок>]"
  usage-info: "&cИспользование: &e/ecoupgrade info <игрок>"
  usage-gift: "&cИспользование: &e/ecoupgrade gift <игрок>"
  unknown-subcommand: "&cНеизвестная подкоманда. Доступно: &einfo&7, &egift"
  gift-self-forbidden: "&cВы не можете подарить уровень прокачки себе!"
  gift-invalid-player: "&cИгрок &e{player} &cне найден или никогда не заходил."
  gift-max-level: "&cИгрок &e{player} &cуже достиг максимального уровня прокачки!"
  gift-on-cooldown: "&cВы сможете снова оплатить прокачку через &e{time} &cсекунд."
  gift-success-sender: "&aВы успешно оплатили прокачку лимита игроку &e{player}&a!"
  gift-success-receiver: "&aИгрок &e{sender} &aоплатил вам один уровень прокачки баланса!"
  info-player-never-joined: "&cИгрок &e{player} &cникогда не заходил на сервер."

  limits:
    - ""
    - "  &a≫ Ваши лимиты баланса ≪"
    - "  &7Баланс: &a%jue_balance%"
    - "  &7Текущий лимит: &a%jue_limit%"
    - "  &7Уровень прокачки: &e%jue_level%"
    - ""
    - "  &7Следующий уровень:"
    - "  &a%jue_next_limit% &7за &a%jue_next_cost%"
    - ""

  limits-max:
    - ""
    - "  &a≫ Ваши лимиты баланса ≪"
    - "  &7Баланс: &a%jue_balance%"
    - "  &7Текущий лимит: &a%jue_limit%"
    - "  &7Уровень прокачки: &e%jue_level%"
    - ""
    - "  &cВы достигли максимального"
    - "  &cлимита прокачки баланса!"
    - ""

  info-limits:
    - ""
    - "  &a≫ Лимиты игрока &e{player} ≪"
    - "  &7Баланс: &a{balance}"
    - "  &7Текущий лимит: &a{current_limit}"
    - "  &7Уровень прокачки: &e{level}"
    - ""
    - "  &7Следующий уровень (&e{next_level}&7):"
    - "  &a{next_limit} &7за &a{cost}"
    - ""

  info-limits-max:
    - ""
    - "  &a≫ Лимиты игрока &e{player} ≪"
    - "  &7Баланс: &a{balance}"
    - "  &7Текущий лимит: &a{current_limit}"
    - "  &7Уровень прокачки: &e{level}"
    - ""
    - "  &cУже достиг максимального"
    - "  &cлимита прокачки баланса!"
    - ""
```

### Кастомные плейсхолдеры
| Плейсхолдер            | Описание                                                                   | Пример вывода<br>(зависит от настроек формата)  |
|------------------------|----------------------------------------------------------------------------|-------------------------------------------------|
| `%jue_balance%`        | Текущий баланс игрока                                                      | `$1.5k` или `1500 $` или `1,500`                |
| `%jue_current_limit%`  | Текущий максимальный лимит баланса                                         | `$5k`                                           |
| `%jue_level%`          | Текущий уровень прокачки лимита (0 — базовый)                              | `3`                                             |
| `%jue_next_cost%`      | Стоимость следующего уровня апгрейда<br>(если максимум достигнут — `0`)    | `$4k` или `0`                                   |
| `%jue_next_limit%`     | Лимит баланса на следующем уровне<br>(если максимум — показывает текущий)  | `$10k` или `$50k` (при максимуме)               |

### Где их можно использовать?

- `TAB` (таб-лист, над головой)
- `DeluxeMenus` / `DeluxeHub` (в GUI, лоре предметов)
- Chat (форматирование чата: `EssentialsChat`, `ChatControl` и др.)
- Scoreboard (`FeatherBoard`, `AnimatedScoreboard` и т.д.)
- `BossBar`, `ActionBar`, `HolographicDisplays`, `CMI` и любые другие плагины с поддержкой `PlaceholderAPI`

### Установка

- Положите `JUpgradableEconomy.jar` в папку /plugins
- Перезапустите сервер
- При первом запуске плагин импортирует балансы из предыдущей экономики (Essentials, etc.) и отключит автоимпорт
- Готово! Игроки могут прокачивать лимит через `/ecoupgrade`

<img width="940" height="195" alt="зображення" src="https://github.com/user-attachments/assets/850f4f08-b491-488f-99b2-dc581fbe6934" />
<img width="1020" height="256" alt="Знімок_20251230_153014" src="https://github.com/user-attachments/assets/3d1458e4-a630-4e89-afe1-01d13bab28c2" />
<img width="1021" height="287" alt="Знімок_20251230_153126" src="https://github.com/user-attachments/assets/4298bd25-c89f-456a-83c0-3f1833f6f6d9" />
<img width="366" height="306" alt="Знімок_20251230_154802" src="https://github.com/user-attachments/assets/f7a82ed2-825b-42ba-8379-051fd1b0be95" />
<img width="1019" height="266" alt="Знімок_20260105_211713" src="https://github.com/user-attachments/assets/f3c76a4c-08db-4cdb-8cbf-8493732305e0" />
<img width="972" height="121" alt="Знімок_20260105_212422" src="https://github.com/user-attachments/assets/53899dd5-1316-4533-b95f-bab62ca3a4f0" />
<img width="1021" height="126" alt="Знімок_20260105_212435" src="https://github.com/user-attachments/assets/81bd66b8-30f0-47ac-94ae-c4a47608d604" />
