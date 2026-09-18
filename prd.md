# DoCue

**Tagline:** Save it. Get cued. Do it.

## 1. Product Overview

DoCue is a simple Android action-reminder app.

Unlike normal reminder apps, DoCue can connect a reminder to an **app or link**.

The core flow is:

**Reminder → Notification → Tap → Open the thing**

Examples:

* 🎮 Blood Strike → remind me to claim a reward
* 📱 MyGP → remind me to claim a daily reward
* 🎬 Instagram Reel → remind me to make the recipe later
* ▶️ YouTube → remind me to watch a tutorial
* 📝 Normal task → simply remind me

## 2. Target Users

People who frequently think:

> “I'll do this later, but I'll probably forget.”

Especially:

* Gamers
* People who use daily app rewards
* People who save Instagram/YouTube content
* People who have recurring tasks
* People who want lightweight reminders

## 3. Core Features

### Reminder types

1. **App Cue**

   * Select an installed app
   * Open it when the notification is tapped

2. **Link Cue**

   * Save a URL
   * Open it when the notification is tapped

3. **Simple Cue**

   * Normal reminder without an app/link

### Scheduling

* One-time
* Daily
* Weekly
* Weekdays
* Weekends
* Selected days
* Custom recurring interval
* Optional expiration/deadline

### Notification

Reminder notification contains:

* Title
* Description
* OPEN
* DONE
* SNOOZE

### Share to DoCue

Android Share Target.

Example:

**Instagram → Share → DoCue**

Show a compact Material 3 share UI instead of opening the full application.

User selects when to be reminded and saves.

### Reliability

* Survive phone restart
* Reschedule reminders
* Handle notification permission
* Handle exact-alarm requirements
* Provide battery optimization guidance
* Gracefully handle deleted target apps

## 4. UI

Use:

* Kotlin
* Jetpack Compose
* Material 3
* MVVM
* Room
* Dark/light/system theme
* Dynamic color where supported

Main navigation:

**Today · Reminders · Settings**

Floating Action Button:

**+**

Important UI surfaces:

* Create/Edit Cue
* App Picker
* Share popup
* Date Picker
* Time Picker
* Reminder details

## 5. Privacy

Local-first.

No:

* Account
* Cloud backend
* Tracking
* Analytics
* Advertising SDK
* Accessibility Service
* Automatic interaction with other apps

Reminder data stays on the device.

## 6. Android

Use modern Android APIs.

Relevant capabilities:

* Notifications
* AlarmManager/exact alarms where appropriate
* Boot receiver
* Android Intents
* Package visibility
* Android Share Target

Do not bypass Android security restrictions.

Do not automatically click or control third-party apps.

## 7. Permissions

Keep permissions minimal.

Potentially required:

* POST_NOTIFICATIONS
* Exact alarm capability where required
* RECEIVE_BOOT_COMPLETED
* Appropriate package visibility configuration

Do not request:

* Contacts
* SMS
* Phone
* Location
* Camera
* Microphone
* Accessibility
* Device Admin

unless a future feature genuinely requires it.

## 8. Build

The project must support GitHub Actions.

The user has a low-end PC, so builds should be performed through GitHub Actions.

Provide:

`.github/workflows/android.yml`

It should build:

* Debug APK
* Release APK when signing configuration is available

Upload APKs as GitHub Actions artifacts.

Use Gradle Wrapper.

## 9. MVP Success Criteria

DoCue is successful when these work reliably:

**Blood Strike**

Create reminder → notification → OPEN → Blood Strike launches.

**MyGP**

Create recurring reminder → notification → OPEN → MyGP launches.

**Instagram**

Share Reel → DoCue popup → choose time → save → notification → OPEN → Reel opens.

**Reboot**

Create reminder → restart phone → reminder remains scheduled.

**Snooze**

Notification → Snooze → reminder fires again.

## 10. Out of Scope

For v1:

* Automatic clicking
* Automatic reward claiming
* Accessibility automation
* AI
* Cloud sync
* Accounts
* Social features
* Advanced automation
* Cross-device synchronization
