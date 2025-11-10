# Flip Cover Widgets - Samsung Official API

An Android app for Samsung Galaxy Z Flip that creates a customizable widget container for the cover screen using **Samsung's Official Cover Screen API**. Add ANY widget to your cover screen with automatic size detection!

## How It Works

### Official Samsung Integration
- Uses **Samsung's official cover screen API** (no accessibility permissions!)
- Appears natively in your cover screen widget picker
- Widget category set to `keyguard` for cover screen support
- Launches directly on cover screen (`launchDisplayId = 1`)

### Dynamic Widget Sizing
- **Automatically detects widget dimensions** (2x2, 3x1, 4x4, etc.)
- Converts widget dp sizes to grid cells (70dp per cell + 8dp gap)
- Supports widgets from **1x1 up to 6x6 cells**
- Your 2x2 weather widget takes exactly 2x2 space!

### Interactive Widget Container
- Opens when you tap the cover screen widget
- Uses `AppWidgetHost` to display **real, interactive widgets**
- Add ANY Android widget (including 3rd party!)
- Widgets update in real-time and are fully functional

## Features

- ✅ **Cover Screen Preview** - Quick view of your widgets on the cover screen
- ✅ **Interactive Container** - Full app with real, working widgets
- ✅ **Widget Management** - Add any Android widget to the container
- ✅ **4x4 Grid Layout** - Organize widgets in a grid
- ✅ **Persistent Storage** - Your widget arrangement is saved automatically

## Requirements

- Samsung Galaxy Z Flip 7 (or compatible device)
- Android 11 (API 30) or higher
- Android Studio Otter (for development)

## Setup for Android Studio

### Step 1: Import the Project

1. Open **Android Studio Otter**
2. Click **File** > **Open**
3. Navigate to this project folder and click **OK**
4. Wait for Gradle to sync (this may take a few minutes)

### Step 2: Build and Run

1. Click **Build** > **Make Project**
2. Connect your Samsung Galaxy Z Flip 7 via USB (or use emulator)
3. Click the **Run** button (green play icon)
4. The app will install and launch

## Using the App

### Step 1: Add the Cover Screen Widget

1. On your Flip 7, fold the device to show the cover screen
2. Long-press on the cover screen
3. Tap **Widgets**
4. Find "Flip Cover Widgets" in the list
5. Drag the widget to your cover screen

### Step 2: Add Widgets to the Container

**Option A: From the Main App**
1. Open "Flip Cover Widgets" app
2. Tap **"Open Widget Container (Interactive)"**
3. Tap the **+ (FAB button)** to add widgets
4. Select widgets from the system picker
5. Widgets appear in the grid and are fully interactive!

**Option B: From the Cover Screen**
1. Tap the widget on your cover screen
2. This opens the interactive widget container
3. Tap the **+ button** to add widgets
4. Widgets work just like they would on your home screen

### Step 3: Arrange Widgets

- Widgets automatically arrange in the 4x4 grid
- They update in real-time
- You can scroll to see more widgets if needed

## Project Structure

```
app/
├── src/main/
│   ├── java/com/flipcover/widgets/
│   │   ├── MainActivity.kt              # Main launcher screen
│   │   ├── WidgetHostActivity.kt        # Interactive widget container (NEW!)
│   │   ├── WidgetSelectorActivity.kt    # Widget picker
│   │   ├── GridEditorActivity.kt        # 4x4 grid editor
│   │   ├── CoverWidgetProvider.kt       # Cover screen widget
│   │   ├── WidgetContainerService.kt    # Widget preview service
│   │   ├── WidgetDataManager.kt         # Data persistence
│   │   └── [Adapters]                   # RecyclerView adapters
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_widget_host.xml # Interactive container layout
│   │   ├── values/
│   │   └── xml/widget_info.xml          # Widget configuration
│   └── AndroidManifest.xml
```

## Architecture: Hybrid Approach

### Cover Screen Layer
```
Cover Screen Widget (RemoteViews)
    ↓
Shows widget previews/icons
    ↓
Tap opens → WidgetHostActivity
```

### Interactive Layer
```
WidgetHostActivity
    ↓
AppWidgetHost (hosts real widgets)
    ↓
Widgets are fully functional & interactive
```

### Data Flow
```
User adds widget → AppWidgetHost.allocateAppWidgetId()
    → Widget picker → Configure widget
    → AppWidgetHost.createView()
    → Widget added to GridLayout
    → Saved to WidgetDataManager
    → Widget is live and updating!
```

## Key Technologies

- **Kotlin** - Primary language
- **AppWidgetHost** - Hosts real, interactive widgets (not just previews)
- **AppWidget** - Cover screen widget framework
- **RemoteViews** - For cover screen preview
- **Gson** - JSON serialization
- **SharedPreferences** - Persistent storage
- **Material Design 3** - UI components
- **GridLayout** - Widget arrangement

## Permissions

- `BIND_APPWIDGET` - To manage and bind app widgets
- `QUERY_ALL_PACKAGES` - To discover installed widgets

## Why Hybrid Approach?

### The Challenge
Android's `RemoteViews` (used for widgets) cannot embed other widgets' `RemoteViews`. This means a pure widget-based container can't show real widget content.

### The Solution
1. **Cover Screen Widget** - Shows previews, acts as a launcher
2. **Full Activity with AppWidgetHost** - Displays real, interactive widgets

### Benefits
- ✅ Real widgets that update and respond to touch
- ✅ Cover screen integration (tap to open)
- ✅ No Samsung-specific APIs required
- ✅ Works on any Android device with widget support
- ✅ Widgets are fully functional, not just static images

## Troubleshooting

### Widget doesn't appear on cover screen
- Make sure the app is installed
- Check that you added it from the widget picker
- Restart your device

### Widgets not interactive in cover screen widget
- **This is expected!** The cover screen shows previews only
- **Tap the cover widget** to open the interactive container
- Full widget functionality is available in the container app

### Widget won't add in container
- Grant all required permissions
- Make sure the widget supports configuration
- Try a different widget to test

## Development

### Building from Source

**Local Build:**
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

**GitHub Actions Build:**

This project includes automated APK builds via GitHub Actions! 

- Push your code to GitHub
- The workflow automatically builds the APK
- Download the APK from the Actions tab

See **[GITHUB_BUILD_GUIDE.md](GITHUB_BUILD_GUIDE.md)** for complete instructions.

### Testing

1. Connect Samsung Flip 7 via USB
2. Enable USB debugging in Developer Options
3. Run from Android Studio Otter
4. Test on cover screen and main screen

## What's Next

Future enhancements:
- Drag-and-drop widget positioning
- Widget resizing
- Custom grid sizes (beyond 4x4)
- Widget preview screenshots for cover screen
- Backup/restore functionality
- Widget categories and search

## License

This project is provided as-is for educational and personal use.

## Credits

Inspired by Samsung's Good Lock app and its Home Up module.
Built using Android's AppWidgetHost for real widget hosting.
