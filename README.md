# CAD to X Converter Android App

An Android-based CAD viewer and converter designed to handle DWG and DXF files. This project is built to assist in land record management and technical mapping, specifically optimized for mobile use.

## 🌟 Overview
This app allows users to open, view, and convert CAD files directly on an Android device. It is particularly useful for professionals like **Patwaris** and engineers who need to access technical drawings in the field.

The core conversion engine is based on the [cad2x-converter](https://github.com/orcastor/cad2x-converter) repository, leveraging a specialized build of LibreCAD and Qt.

## 🚀 Features
* **DWG/DXF Support:** View common CAD formats on the go.
* **Fast Conversion:** Convert complex drawings to viewable formats.
* **Lightweight:** Optimized for Android performance.
* **Offline Use:** No internet connection required for file processing.

## 🛠️ Built With
* **Android Studio & Kotlin/Java**
* **Qt 5.12.12** (Trimmed for Android)
* **LibreCAD Core** (GPLv2)
* **cad2x-converter** (Native C++ integration)

## 📜 License & Compliance
This project is licensed under the **GNU General Public License v2.0 (GPLv2)**.

### Why GPLv2?
As this app is a derivative work of [LibreCAD](https://librecad.org/) (via `cad2x-converter`), it is distributed as open-source software to comply with the "copyleft" requirements of the original authors. 

* You may view, modify, and redistribute the source code.
* Any derivative works must also be released under the GPLv2.
* See the `LICENSE` file for the full legal text.

## 📲 Installation
1. **Download:** Get the latest APK from the [Releases](https://github.com/dakshstdo-collab/cadwari2dconverter/releases) section or the Google Play Store.
2. **Permissions:** The app requires "Read External Storage" to access your CAD files.

## 🤝 Contributing
Contributions are welcome! If you are an Android developer or familiar with C++/Qt:
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---
*Developed with ❤️ by a self-taught Android Developer.*
