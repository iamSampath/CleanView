# 📘 CleanView - Privacy first PDF Viewer

**CleanView** is a modern, lightweight, and fast PDF viewer built using JavaFX and Apache PDFBox. Designed with clean aesthetics and high usability in mind, it provides essential PDF features without the clutter of bloated applications.

---
<p align="center">
  <img src="https://github.com/iamSampath/CleanView/blob/3c703f470f0f26b7c8aa86710dfd7b7f25afac3c/src/main/resources/icon.png" alt="CleanView Logo" width="300"/>
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

## 🚀 Installation (End Users)

### ✅ Option 1: Download from [Releases](https://github.com/iamSampath/CleanView/releases)

1. Go to the [**Releases**](https://github.com/iamSampath/CleanView/releases) page.
2. Download the installer for your operating system:
   - **Windows**:  `CleanView-1.0.exe` 
   - **macOS (Intel)**:  `CleanView-Intel-1.0.dmg`
   - **macOS (Apple Silicon)**:  `CleanView-AppleSilicon-1.0.dmg`
3. Launch the installer:
   - On **Windows**, double-click the `.exe` file
   - On **macOS**, open the `.dmg` file and drag **CleanView** to the Applications folder
4. Follow the installation prompts:
   - Choose install location (Windows)
   - (Optional) Create a desktop shortcut (Windows)
   - (Optional) Set **CleanView** as the default PDF viewer

> 💡 Once installed, you can open PDF files by double-clicking them or using “Open With → CleanView” from the file menu.

---

### 🛠 Option 2: Manual Build (For Developers)

If you'd like to build the application yourself please read the below instructions:

## 🧪 Building installer locally

```bash
git clone https://github.com/iamSampath/CleanView.git
cd CleanView
mvn clean package
```

Output will be in: `target/installer`

---

## 🖥️ Supported Platforms

CleanView currently supports the following operating systems:

| Platform     | Status        | Details                                          |
|--------------|---------------|--------------------------------------------------|
| 🪟 Windows    | ✅ Supported   | Installer (`.exe`) available in [Releases](#)    |
| 🍎 macOS      | ✅ Supported   | Installer (`.dmg`) available in [Releases](#)    |
| 🐧 Linux      | ❌ Not Supported | May be considered in the future                 |


> ℹ️ **Note:** CleanView is fully functional on both **Windows** and **macOS**.  
> Linux support is currently not available but may be considered in the future.

## 🧠 Author

**Sampath Kumar Medarametla**  
📌 Program Manager  
🔗 [linkedin.com/in/smedarametla](https://www.linkedin.com/in/smedarametla/)

