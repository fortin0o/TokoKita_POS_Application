# TokoKita POS Application

TokoKita is a modern Point of Sale (POS) and Inventory Management system designed for multi-branch retail operations. Built with Kotlin and powered by Firebase, it provides a seamless experience for managing sales, stock, and staff across different locations.

## 🚀 Features

### 🛒 Point of Sale (POS)
- Easy-to-use transaction interface with category filtering and search.
- Interactive shopping cart with stock validation.
- Quick payment options for cash transactions.
- Support for multiple payment methods (Tunai, Transfer, QRIS).

### 📦 Inventory & Product Management
- Comprehensive product database with barcode support.
- Profit margin calculator (Nominal or Percentage).
- Automated stock tracking and "unlimited stock" option.
- Categorization for easier navigation.

### 🏢 Multi-Branch Support
- Manage multiple store locations from a single app.
- Assign products and staff to specific branches.
- Branch-specific revenue tracking.

### 🖨️ Thermal Printing
- Integrated Bluetooth thermal printer support.
- Custom receipt headers and footers.
- Optional store logo printing on receipts.

### 👥 Staff & Customer Management
- Manage employee roles and access.
- Track customer database and loyalty.

### 📊 Reporting
- Daily revenue summaries.
- Detailed transaction history.

## 📸 Screenshots

| Main Dashboard | POS Interface | Checkout |
|:---:|:---:|:---:|
| ![Main Dashboard](https://via.placeholder.com/300x600?text=Main+Dashboard) | ![POS Interface](https://via.placeholder.com/300x600?text=POS+Interface) | ![Checkout](https://via.placeholder.com/300x600?text=Checkout) |

| Printer Settings | Receipt Preview | Product Management |
|:---:|:---:|:---:|
| ![Printer Settings](https://via.placeholder.com/300x600?text=Printer+Settings) | ![Receipt](https://via.placeholder.com/300x600?text=Receipt) | ![Product Management](https://via.placeholder.com/300x600?text=Product+Management) |

*> Note: Replace placeholders above with actual screenshots from the `docs/screenshots` folder.*

## 🛠️ Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **Backend:** [Firebase Realtime Database](https://firebase.google.com/docs/database)
- **UI Framework:** Material Components for Android (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Communication:** Bluetooth ESC/POS for Thermal Printing

## ⚙️ Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/username/TokoKita_POS_Application.git
   ```
2. Open the project in **Android Studio (Hedgehog or newer)**.
3. Connect your project to your **Firebase Console**.
4. Download `google-services.json` and place it in the `app/` directory.
5. Build and run the application.

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

---
*Developed with ❤️ by the TokoKita Team.*
