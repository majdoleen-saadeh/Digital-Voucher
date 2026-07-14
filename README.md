# Digital Voucher Application

**Digital Voucher** is a native Android application developed using **Kotlin**, designed to streamline the process of discovering, purchasing, and managing digital vouchers. The application features a seamless user experience, real-time API integrations, and robust security through OTP verification.

---

## 🚀 Key Features

*   **Home Dashboard (Dual-Tab Interface):**
    *   **Buy New Tab:** Explore a comprehensive list of voucher providers.
    *   **Search & Filter:** Easily find specific vouchers and filter by country using integrated country-data APIs.
    *   **Saved Vouchers Tab:** A dedicated space to view and manage all previously purchased vouchers.
*   **Seamless Navigation:**
    *   Click on any **Provider** to view their available digital vouchers.
    *   Click on any **Voucher** to access comprehensive details and pricing information.
*   **Transaction Workflow:**
    *   **Proceed to Checkout:** One-tap purchase initiation.
    *   **OTP Verification:** Secure transaction processing. The app includes an automatic SMS-retrieval feature for seamless OTP validation.
*   **Voucher Management:**
    *   Upon successful verification, purchased vouchers are instantly moved to the "Saved Vouchers" tab.
*   **Social Sharing:**
    *   Share your voucher details directly with others by capturing and sharing a screenshot of the voucher details page.

---

## 🛠 Technical Stack

*   **Language:** Kotlin
*   **Platform:** Android SDK
*   **Networking:** API Integration (Retrofit/OkHttp)
*   **Architecture:** Native Android Architecture
*   **Authentication:** Automated SMS-based OTP verification

---

## 📂 Project Structure

*   **/ui**: Contains all Kotlin code responsible for the user interface, including Fragments, Activities, and screen logic.
*   **/api**: Manages all API interface definitions and network request logic.
*   **/data**: Contains all data classes, models, and response structures used for API data mapping and local storage.

---

## 📱 User Flow

1.  **Browse:** Open the app to the Main Screen and toggle between *Buy New* and *Saved Vouchers*.
2.  **Filter:** Use the "All Countries" button to fetch and filter providers by region via API.
3.  **Purchase:** Select a Provider -> Select a Voucher -> Click **Proceed**.
4.  **Verify:** Enter the OTP manually or allow the app to detect the incoming SMS automatically.
5.  **Success:** Once verified, the voucher is saved to your account and displayed in the *Saved Vouchers* tab.
6.  **Share:** View any purchased voucher and share the details with your friends.

---

*Built with passion using Kotlin for a smooth and secure digital voucher experience.*
