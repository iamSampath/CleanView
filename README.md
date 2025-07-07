# 📘 CleanView - Minimalist PDF Viewer

**CleanView** is a modern, lightweight, and fast PDF viewer built using JavaFX and Apache PDFBox. Designed with clean aesthetics and high usability in mind, it provides essential PDF features without the clutter of bloated applications.

---
<p align="center">
  <img src="https://github.com/iamSampath/CleanView/blob/c47ac8db29b009c7eeff7620ff2e515d046e07f1/src/main/resources/icon.png" alt="CleanView Logo" width="300"/>
</p>

## ✨ Features

### 📄 Core PDF Functionality
- Open and view standard PDF documents
- Navigate by page number or with Next / Previous buttons
- Display total number of pages and current page

### 🔍 Search and Highlights
- Search for keywords across the document
- Highlights matching words on the current page

### 🔎 Zoom & Layout
- Zoom in and out with adjustable DPI rendering
- Fit-to-width functionality to optimize layout on resize

### 🌗 Theme Support
- Toggle between **Light** and **Dark** themes
- Styled headers, toolbar, and status bar to match the theme

### 📤 Export & Print
- Print the current page with DPI-scaled clarity
- Export the current page as a high-resolution PNG image

### 🎨 UI/UX Design
- Animated header with app icon and title
- Elegant, theme-aware toolbar and status bar
- Centered labels with clean fonts and minimal style
- Sleek modern color palette inspired by macOS aesthetics

### 📦 Application Integration
- Can be launched from terminal or by double-clicking `.exe`
- Supports setting CleanView as the default PDF viewer
- Drag-and-drop support (in roadmap)
- 
## 🚀 Installation (End Users)

### ✅ Option 1: Download from [Releases](https://github.com/iamSampath/CleanView/releases)

1. Go to the [**Releases**](https://github.com/iamSampath/CleanView/releases) page.
2. Download and extract the latest `CleanView-1.0.exe` file from SourceCode.Zip.
3. Double-click to launch the installer.
4. Follow the installation prompts:
   - Choose install location
   - (Optional) Create a desktop shortcut
   - (Optional) Set **CleanView** as the default PDF viewer

> 💡 Once installed, you can open PDF files by double-clicking them or using “Open With → CleanView” from the file menu.

---

### 🛠 Option 2: Manual Build (For Developers)

If you'd like to build the application yourself please read the below instructions:

## 🧪 How to Build

```bash
git clone https://github.com/iamSampath/CleanView.git
cd CleanView
mvn clean package
```

To run the app with JavaFX modules:

```bash
java --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.swing -jar target/CleanView-1.0.jar
```

---

## 🛠 Packaging with `jpackage`

Make sure to run:

```bash
jlink ^
  --module-path "%JAVA_HOME%/jmods;C:/javafx-jmods-21.0.7" ^
  --add-modules java.base,java.desktop,javafx.controls,javafx.graphics,javafx.swing ^
  --output target/runtime ^
  --strip-debug ^
  --compress=2 ^
  --no-header-files ^
  --no-man-pages

mvn jpackage:jpackage
```

Output will be in: `target/installer`

---

## 🖥️ Supported Platforms

CleanView currently supports the following operating systems:

| Platform     | Status        | Details                                      |
|--------------|---------------|----------------------------------------------|
| 🪟 Windows    | ✅ Supported   | Installer (.exe) available in [Releases](#)  |
| 🍎 macOS      | 🔜 Coming Soon | `.dmg` version under active development       |
| 🐧 Linux      | ❌ Not Supported | May be considered in future                 |

> ℹ️ **Note:** CleanView is fully functional on Windows. macOS support is in progress and expected in a future update.


## 🧠 Author

**Sampath Kumar Medarametla**  
📌 Program Manager  
🔗 [linkedin.com/in/smedarametla](https://www.linkedin.com/in/smedarametla/)

