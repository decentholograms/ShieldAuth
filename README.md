<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.16.5--1.21-brightgreen" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java Version">
  <img src="https://img.shields.io/badge/License-All%20Rights%20Reserved-red" alt="License">
  <img src="https://img.shields.io/badge/API-Spigot%20%7C%20Paper-blue" alt="API">
</p>

# 🛡️ ShieldAuth

**Advanced authentication plugin for Minecraft servers** with multi-layer security, GUI-based PIN system, temporary IP blocking, configurable timeouts, update checker and extensive customization options.

Compatible with **Spigot**, **Paper** and forks from version **1.16.5 to 1.21**.

---

## ✨ Features

### 🔐 Authentication
- Password-based authentication (`/register` and `/login`)
- Configurable password requirements (min/max: 6-32 by default)
- Multiple encryption algorithms: **ARGON2** (recommended), **BCRYPT**, **SHA256**, **SHA512**
- Configurable session system (remember login) - Disabled by default

### 🔢 PIN System
- GUI-based PIN entry with clickable number heads
- Configurable PIN length (default: 4 digits)
- Extra security layer after password authentication
- Brute force protection with rate limiting

### 🚫 Rate Limiting & Temporary IP Blocking
- **Per-account** login attempt limiting
- **Per-account** PIN attempt limiting
- **Per-IP** login attempt limiting (protects against distributed attacks)
- **Per-IP** PIN attempt limiting
- **Temporary IP blocking** - When limits are exceeded, the IP is blocked
- Configurable block durations
- Automatic unlock after timeout
- **Configurable kick on block** - Enable/disable kicking when blocked (enabled by default)

### ⏰ Authentication Timeouts
- **Auto-kick** if player doesn't register in time
- **Auto-kick** if player doesn't login in time
- **Auto-kick** if player doesn't enter PIN in time
- Fully configurable times
- Customizable kick messages

### 🔄 Update Checker
- Automatically checks for new versions on GitHub
- Displays console message if update is available
- Configurable (enable/disable)
- Customizable GitHub URL

### 💾 Database
- **SQLite** (local) - Zero configuration required
- **MySQL** (cloud) - For networks and multiple servers
- HikariCP connection pooling for optimal performance

### 🔔 Notifications
- **Discord Webhooks** - Get notified of logins/registrations
- Customizable embed colors (RGB)
- IP spoiler protection in Discord

### 📧 Email Verification
- SMTP email support (Gmail, custom SMTP)
- Verification codes
- Recovery options

### 🎨 Customization
- **ALL messages configurable** in config.yml
- Title messages with fade effects
- Custom command aliases
- Fully configurable permissions

---

## 📋 Commands

### User Commands

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/register <pass> <confirm>` | `/reg`, `/r`, `/signup`, `/crear`, `/registrar` | Register new account | None |
| `/login <password>` | `/l`, `/log`, `/signin`, `/entrar`, `/iniciar` | Login to account | None |
| `/changepassword <current> <new>` | `/changepass`, `/cp`, `/cambiarpass`, `/newpass`, `/chpass` | Change password | `shieldauth.changepassword` |
| `/unregister <password>` | `/unreg`, `/deleteaccount`, `/borrar` | Delete your account | `shieldauth.unregister` |
| `/setpin <pin>` | `/pin`, `/addpin`, `/createpin` | Set security PIN | `shieldauth.pin` |
| `/removepin <pin>` | `/delpin`, `/deletepin`, `/rmpin` | Remove your PIN | `shieldauth.pin` |
| `/unsetpin <pin>` | `/nopin`, `/clearpin` | Remove your PIN (alias) | `shieldauth.pin` |
| `/setemail <email>` | `/email`, `/mail`, `/addemail` | Set recovery email | `shieldauth.email` |
| `/verifyemail <code>` | `/verify`, `/confirmemail`, `/vmail` | Verify your email | None |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/shieldauth reload` | Reload configuration | `shieldauth.admin` |
| `/shieldauth addadmin <player>` | Add player to admin list | `shieldauth.admin` |
| `/shieldauth removeadmin <player>` | Remove player from admin list | `shieldauth.admin` |
| `/shieldauth list` | List all admins | `shieldauth.admin` |
| `/shieldauth info <player>` | View player authentication info | `shieldauth.admin` |
| `/shieldauth forcelogin <player>` | Force login a player | `shieldauth.forcelogin` |
| `/shieldauth forceunregister <player>` | Force unregister a player | `shieldauth.forceunregister` |
| `/shieldauth forcesetpin <player> <pin>` | Force set PIN for a player | `shieldauth.forcesetpin` |
| `/shieldauth forceremovepin <player>` | Force remove PIN from a player | `shieldauth.forceremovepin` |
| `/shieldauth help` | Show help menu | `shieldauth.admin` |

**Admin command aliases:** `/sa`, `/sh`, `/auth`, `/shield`

---

## 🔑 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `shieldauth.admin` | Access to all admin commands | OP |
| `shieldauth.forcelogin` | Force login players | OP |
| `shieldauth.forceunregister` | Force unregister players | OP |
| `shieldauth.forcesetpin` | Force set PIN on players | OP |
| `shieldauth.forceremovepin` | Force remove PIN from players | OP |
| `shieldauth.changepassword` | Change own password | true |
| `shieldauth.unregister` | Unregister own account | true |
| `shieldauth.pin` | Set/remove own PIN | true |
| `shieldauth.email` | Set own email | true |

---

## ⚙️ Configuration

### Database

```yaml
database:
  type: "sqlite"
  host: "localhost"
  port: 3306
  database: "shieldauth"
  username: "root"
  password: ""
  pool-size: 10
```

### Update Checker

```yaml
update-checker:
  enabled: true
  github-url: "https://github.com/yourusername/ShieldAuth"
  github-api-url: "https://api.github.com/repos/yourusername/ShieldAuth/releases/latest"
```

### Security

```yaml
security:
  encryption: "ARGON2"
  password-min-length: 6
  password-max-length: 32
  pin-length: 4
  max-login-attempts: 5
  lock-duration: 300
  max-pin-attempts: 3
  pin-lock-duration: 300
  max-ip-login-attempts: 10
  ip-lock-duration: 600
  max-ip-pin-attempts: 6
  ip-pin-lock-duration: 600
  ip-block-kick-enabled: true
  session-timeout: 1800
  session-enabled: false
  auth-timeout-enabled: true
  register-timeout: 60
  login-timeout: 60
  pin-timeout: 30
  allowed-commands:
    - "/login"
    - "/register"
    - "/l"
    - "/reg"
```

| Option | Description | Default |
|--------|-------------|---------|
| `encryption` | Algorithm: ARGON2, BCRYPT, SHA256, SHA512 | ARGON2 |
| `password-min-length` | Minimum password length | 6 |
| `password-max-length` | Maximum password length | 32 |
| `pin-length` | PIN length | 4 |
| `max-login-attempts` | Login attempts per account | 5 |
| `lock-duration` | Account lock duration (seconds) | 300 |
| `max-pin-attempts` | PIN attempts per account | 3 |
| `pin-lock-duration` | PIN lock duration (seconds) | 300 |
| `max-ip-login-attempts` | Login attempts per IP | 10 |
| `ip-lock-duration` | IP block duration for login (seconds) | 600 |
| `max-ip-pin-attempts` | PIN attempts per IP | 6 |
| `ip-pin-lock-duration` | IP block duration for PIN (seconds) | 600 |
| `ip-block-kick-enabled` | Kick player when IP is blocked | **true** |
| `session-timeout` | Session timeout (seconds) | 1800 |
| `session-enabled` | Remember login | **false** |
| `auth-timeout-enabled` | Enable timeouts | true |
| `register-timeout` | Seconds to register | 60 |
| `login-timeout` | Seconds to login | 60 |
| `pin-timeout` | Seconds to enter PIN | 30 |

### IP Block Behavior

**When `ip-block-kick-enabled: true` (default):**
- Player is kicked when IP block limit is reached
- Player cannot reconnect until block expires
- Shows block message with remaining time

**When `ip-block-kick-enabled: false`:**
- Player is NOT kicked
- Player stays connected but cannot use login/PIN commands
- Shows warning message in chat
- Commands are blocked for that IP until timeout expires

### Command Aliases

```yaml
aliases:
  register:
    - "reg"
    - "r"
    - "signup"
    - "crear"
    - "registrar"
  login:
    - "l"
    - "log"
    - "signin"
    - "entrar"
    - "iniciar"
```

### Discord Webhook

```yaml
discord:
  enabled: false
  webhook-url: "https://discord.com/api/webhooks/..."
  embed-color-red: 255
  embed-color-green: 0
  embed-color-blue: 0
```

### Email

```yaml
email:
  enabled: false
  smtp-host: "smtp.gmail.com"
  smtp-port: 587
  smtp-username: "your-email@gmail.com"
  smtp-password: "your-app-password"
  smtp-ssl: true
  from-address: "noreply@yourserver.com"
```

### Title Messages

```yaml
titles:
  register:
    title: "&c&lShieldAuth"
    subtitle: "&7Please register using &e/register <pass> <pass>"
    fade-in: 10
    stay: 70
    fade-out: 20
  login:
    title: "&c&lShieldAuth"
    subtitle: "&7Please login using &e/login <pass>"
  pin:
    title: "&6&lPIN Required"
    subtitle: "&7Enter your PIN to continue"
  success:
    title: "&a&lAuthenticated"
    subtitle: "&7Welcome back!"
```

### Block Messages

```yaml
messages:
  block-ip-locked: "&c&lTemporarily Blocked\n\n&7Your IP has been blocked for &c{time}&7.\n&7Reason: &cToo many failed login attempts."
  block-ip-pin-locked: "&c&lTemporarily Blocked\n\n&7Your IP has been blocked for &c{time}&7.\n&7Reason: &cToo many failed PIN attempts."
  block-ip-login-denied: "&c&lTemporarily Blocked\n\n&7Your IP is temporarily blocked for &c{time}&7.\n&7Reason: &cToo many failed login attempts."
  block-ip-pin-denied: "&c&lTemporarily Blocked\n\n&7Your IP is temporarily blocked for &c{time}&7.\n&7Reason: &cToo many failed PIN attempts."
  ip-locked: "&cYour IP has been temporarily blocked for {time} seconds due to too many failed attempts."
  ip-pin-locked: "&cYour IP has been blocked from PIN attempts for {time} seconds."
  session-restored: "&aSession restored! Welcome back."
  update-available: "&8[&c&lShieldAuth&8] &eNew version available! &7Current: &c{current} &7Latest: &a{latest}\n&8[&c&lShieldAuth&8] &7Download: &b{url}"
```

---

## 🔒 Security Features

### Multi-Layer Protection

1. **Password Encryption**: Industry-standard algorithms (ARGON2 recommended)
2. **Salted Hashes**: All passwords and PINs use unique random salts
3. **Timing Attack Prevention**: Constant-time comparison for all sensitive operations
4. **Rate Limiting**: Prevents brute force attacks at account and IP level
5. **Session Management**: Secure session tokens with IP validation
6. **Temporary IP Blocking**: Blocked IPs cannot authenticate until block expires
7. **Authentication Timeouts**: Auto-kick if not authenticated in time

### Rate Limiting & Blocking

| Type | Purpose | Default | Action |
|------|---------|---------|--------|
| Per-Account Login | Protects individual accounts | 5 attempts, 5 min lock | Account lock |
| Per-Account PIN | Protects individual PINs | 3 attempts, 5 min lock | PIN lock |
| Per-IP Login | Prevents distributed attacks | 10 attempts, 10 min block | **IP Block** |
| Per-IP PIN | Prevents IP-based PIN attacks | 6 attempts, 10 min block | **IP Block** |

**When an IP is blocked:**
- If `ip-block-kick-enabled: true`: Player is kicked and cannot reconnect
- If `ip-block-kick-enabled: false`: Player stays but commands are blocked
- Shows configurable block message with remaining time
- Time displayed in readable format: `5m 30s` or `1h 15m`

### Timeouts

| Type | Purpose | Default | Action |
|------|---------|---------|--------|
| Register Timeout | Prevents AFK players without registering | 60 seconds | Kick |
| Login Timeout | Prevents AFK players without login | 60 seconds | Kick |
| PIN Timeout | Prevents AFK players at PIN screen | 30 seconds | Kick |

---

## 🔄 Authentication Flow

```
Player tries to connect
         │
         ▼
┌─────────────────┐
│ IP Blocked?     │──Yes──► (if kick enabled) Connection Refused
│                 │         (if kick disabled) Allow but block commands
└────────┬────────┘
         │ No
         ▼
    Player Joins
         │
         ▼
┌─────────────────┐
│ Valid Session?  │──Yes──► Session restored (if session-enabled: true)
│                 │        │
└────────┬────────┘        │ Has PIN?
         │ No              │    │
         ▼                 │    ▼
┌─────────────────┐        │  Verify PIN
│ Is Registered?  │        │    │
└────────┬────────┘        │    ▼
         │                 │  ✓ Authenticated
    Yes  │  No             │
         │    │            │
         ▼    ▼            │
       Login  Register ◄───┘
         │      │
         ▼      ▼
┌─────────────────┐
│ Has PIN?        │──No──► ✓ Authenticated
└────────┬────────┘
         │ Yes
         ▼
    Verify PIN
         │
         ▼
    ✓ Authenticated
```

### Failed Attempts Flow

```
Failed Login/PIN Attempt
         │
         ▼
┌─────────────────────┐
│ Increment counters  │
│ (account + IP)      │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│ Attempts >= max?    │──Yes──► IP BLOCK
└─────────┬───────────┘         │
          │ No                  ▼
          ▼              ┌──────────────┐
    Show remaining       │ Kick enabled?│
    attempts             └──────┬───────┘
                           Yes  │  No
                                │
                           ▼    ▼
                         Kick  Show message
                               (block commands)
```

---

## 📦 Installation

1. Download `ShieldAuth-1.0.0.jar`
2. Place it in your server's `plugins` folder
3. Start/restart the server
4. Configure `plugins/ShieldAuth/config.yml` as needed
5. Use `/shieldauth reload` to apply changes

---

## 🔨 Building from Source

### Requirements
- Java 21 JDK
- Maven 3.6+

### Commands

```bash
git clone https://github.com/yourusername/ShieldAuth.git
cd ShieldAuth
mvn clean package
```

The compiled JAR will be in the `target` folder.

---

## 📊 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Spigot API | 1.16.5 | Minecraft server API |
| HikariCP | 4.0.3 | Database connection pooling |
| Argon2-jvm | 2.11 | Password hashing (recommended) |
| BouncyCastle | 1.70 | BCrypt implementation |
| Jakarta Mail | 2.0.1 | Email verification |
| Gson | 2.10.1 | JSON for Discord webhooks |

---

## 📝 All Configurable Messages

The plugin includes **65+ configurable messages** including:

- Registration messages (success, already registered, password mismatch, etc.)
- Login messages (success, wrong password, locked, etc.)
- PIN messages (set, removed, wrong, locked, etc.)
- Email messages (set, verified, invalid, etc.)
- Admin messages (all admin command responses)
- IP block messages (connection refused, commands blocked)
- Timeout messages (kick for taking too long)
- Session messages (session restored)
- Update available message
- Title messages (register, login, PIN, success)
- GUI messages (PIN interface)

All messages support color codes (`&a`, `&c`, `&l`, etc.) and placeholders (`{time}`, `{player}`, `{attempts}`, `{current}`, `{latest}`, `{url}`, etc.)

---

## 📜 License

All Rights Reserved © 2026

---

<p align="center">
  <b>ShieldAuth</b> - Secure your Minecraft server with confidence
</p>
