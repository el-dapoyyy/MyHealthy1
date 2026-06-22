# MyHealthy - Aplikasi Pelacak Gizi & Kesehatan

![Logo MyHealthy](assets/logo.png)

Aplikasi Android pelacak nutrisi dan gaya hidup sehat dengan fitur AI Scanner untuk analisis makanan berbasis Gemini, gamifikasi, dan dashboard kesehatan komprehensif.

---

## 📋 Daftar Isi

- [Fitur Utama](#-fitur-utama)
- [Showcase UI](#-showcase-ui)
- [Teknologi dan Library](#-teknologi-dan-library)
- [Cara Instalasi](#-cara-instalasi)
- [SCRUM Project](#-scrum-project)

---

## 🎯 Fitur Utama

### 1. **AI Food Scanner** (Gemini Vision)
Fitur andalan yang menggunakan Google Gemini Flash API untuk menganalisis foto makanan dan memberikan informasi gizi secara otomatis.

- 📸 **Ambil Foto** dari kamera atau pilih dari galeri
- 🤖 **Analisis AI** untuk mengenali makanan dan memperkirakan nilai gizi
- 📊 **Estimasi Nutrisi**: Kalori, Protein, Karbohidrat, Lemak
- 💾 **Simpan ke Diary** dengan sekali ketuk
- ⚡ **Powered by Gemini Flash Latest** - respons cepat dan akurat

### 2. **Food Diary (Catatan Makanan)**
Pencatatan makanan harian yang terhubung dengan Firebase Firestore.

- 📅 **Catatan Harian** per tanggal
- 🍽️ **Kategori Meal**: Sarapan, Makan Siang, Makan Malam, Snack, Minuman
- 🔥 **Tracking Kalori** dengan progress real-time
- 📈 **Target Kalori** dari kalkulator
- 🗑️ **Hapus Entry** dengan swipe atau tombol

### 3. **Kalkulator Nutrisi & BMI**
Kalkulator komprehensif untuk menghitung kebutuhan harian.

- 📐 **BMI Calculator** dengan kategori (Kurus/Normal/Gemuk/Obesitas)
- 🔥 **BMR & TDEE** menggunakan rumus Mifflin-St Jeor
- 🎯 **Target Kalori** berdasarkan tujuan:
  - **Cut** (Menurunkan berat) - defisit 500 kkal
  - **Maintain** (Menjaga berat) - sesuai TDEE
  - **Bulk** (Menambah berat) - surplus 300 kkal
- 📊 **Makronutrien** otomatis:
  - Protein: 1.6-1.8g per kg berat badan
  - Karbohidrat: sisa kalori
  - Lemak: 0.8-1g per kg berat badan
- 💾 **Simpan Data** ke SharedPreferences

### 4. **Dashboard Progress Harian**
Visualisasi lengkap aktivitas kesehatan harian.

- 👟 **Step Counter** dengan sensor bawaan HP
- 💧 **Water Intake Tracker** - target 8 gelas/hari
- 😴 **Sleep Log** -input jam tidur
- 😊 **Mood Tracker** - 5 emoji mood dengan XP reward
- 📈 **Grafik Kalori Mingguan** dengan custom view
- ⚖️ **BMI Bar** dengan needle indicator
- 🔥 **Streak Counter** - penghitungan login beruntun

### 5. **Meal Plan (Rencana Menu)**
Saran menu sehat untuk setiap waktu makan.

- 🌅 **Sarapan** (3 pilihan menu)
- ☀️ **Makan Siang** (3 pilihan menu)
- 🌙 **Makan Malam** (3 pilihan menu)
- 🍎 **Snack** (3 pilihan camilan sehat)
- 📊 **Info Kalori** per porsi

### 6. **Gamifikasi & Badges**
Sistem reward untuk memotivasi gaya hidup sehat.

#### Level System
| Level | Nama | XP Required |
|-------|------|-------------|
| 1 | Pemula | 0 |
| 2 | Starter | 100 |
| 3 | Explorer | 300 |
| 4 | Warrior | 600 |
| 5 | Master | 1000 |
| 6 | Legend | 1500 |

#### Badges
| Badge | Nama | Syarat |
|-------|------|--------|
| 💧 | Pejuang Air Putih | Minum 8 gelas/hari |
| 🏃 | Pejalan Kaki | 6.000 langkah/hari |
| ☀️ | Early Bird | Login 3 hari berturut-turut |
| 🔥 | Streak 7 | Login 7 hari berturut-turut |
| 🥗 | Diet Master | Catat makanan 7 hari |
| ⭐ | Konsisten | Log matrik 14 hari |

#### XP Rewards
| Aktivitas | XP |
|----------|-----|
| Logging Mood | +5 XP |
| Log Tidur | +10 XP |
| Update Berat | +15 XP |
| Minum Air Target | +20 XP |
| Unlock Badge | +25 XP |

### 7. **Profile & Stats**
Profil pengguna dengan statistik lengkap.

- 👤 **Info Akun** - Nama, Email, Provider (Google/Email)
- 📊 **Health Vitals** - Berat, Tinggi, BMI, Target Kalori
- 🏆 **Level & XP** dengan progress bar
- 🔥 **Streak** dan Badge count
- ✏️ **Edit Nama** tampilan
- 🚪 **Logout** dan **Hapus Akun**

### 8. **Database Makanan**
Koleksi 34+ makanan sehat dengan informasi nutrisi lengkap.

#### Protein Hewani
- Dada Ayam, Paha Ayam, Telur
- Ikan Tuna, Salmon Panggang, Ikan Nila
- Dada Kalkun, Daging Sapi Tanpa Lemak
- Yoghurt Yunani, Keju Cottage
- Susu Sapi Rendah Lemak

#### Protein Nabati
- Tahu, Tempe, Edamame
- Quinoa, Oatmeal, Beras Merah
- Gandum Utuh, Susu Kedelai
- Kedelai Hitam, Kacang Hijau

#### Sayuran & Buah
- Brokoli, Bayam
- Alpukat
- Ubi Jalar

#### Camilan Sehat
- Kacang Almond, Kacang Kenari
- Biji Chia

**Fitur Filter:**
- 🔍 **Search** nama makanan
- 🏷️ **Filter Kategori**: Semua, Hewani, Nabati
- ⭐ **Rating** pengguna

### 9. **Autentikasi**
Sistem login/register dengan Firebase Auth.

- 📧 **Email/Password** registration & login
- 🔐 **Password visibility** toggle
- ✅ **Validation** input
- 🔄 **Google Sign-In** (dengan Firebase Auth)

---

## 📱 Showcase UI

> **Catatan**: Screenshot UI akan ditambahkan oleh developer. Silakan replace placeholder di bawah ini dengan screenshot asli dari aplikasi.

### Screenshots

| Splash Screen | Login | Register |
|:---:|:---:|:---:|
| ![Splash](assets/screenshots/splash.png) | ![Login](assets/screenshots/login.png) | ![Register](assets/screenshots/register.png) |

| Home (Food Database) | Food Detail | AI Scanner |
|:---:|:---:|:---:|
| ![Home](assets/screenshots/home.png) | ![Detail](assets/screenshots/food_detail.png) | ![AI Scanner](assets/screenshots/ai_scanner.png) |

| Food Diary | Calculator | Daily Progress |
|:---:|:---:|:---:|
| ![Diary](assets/screenshots/diary.png) | ![Calculator](assets/screenshots/calculator.png) | ![Progress](assets/screenshots/progress.png) |

| Meal Plan | Profile | Bottom Navigation |
|:---:|:---:|:---:|
| ![Meal Plan](assets/screenshots/meal_plan.png) | ![Profile](assets/screenshots/profile.png) | ![Navigation](assets/screenshots/navigation.png) |

---

## 🛠️ Teknologi dan Library

### Bahasa & Framework
- **Java** - Bahasa pemrograman utama
- **Android SDK** 36 (Compile SDK)
- **Min SDK** 26 (Android 8.0 Oreo)
- **Target SDK** 36

### Dependencies

#### AndroidX & UI
```
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.10.0
androidx.activity:activity:1.8.0
androidx.constraintlayout:constraintlayout:2.1.4
androidx.recyclerview:recyclerview:1.2.1
```

#### Firebase
```
com.google.firebase:firebase-bom:33.1.0
com.google.firebase:firebase-auth
com.google.firebase:firebase-firestore
com.google.gms:google-services:4.4.2
```

#### Google Services
```
com.google.android.gms:play-services-auth:21.3.0
com.google.android.gms:play-services-location:21.0.1
```

#### AI & Image Processing
```
com.google.ai:generativelanguage-api (via REST)
```

#### Image Loading
```
com.github.bumptech.glide:glide:4.16.0
```

#### Testing
```
junit:junit:4.13.2
androidx.test.ext:junit:1.1.5
androidx.test.espresso:espresso-core:3.5.1
```

### Architecture & Patterns
- **MVVM-lite** - View + Fragment separation
- **SharedPreferences** - Local data persistence
- **Firestore** - Cloud database untuk diary & progress
- **Firebase Auth** - User authentication
- **REST API** - Gemini API integration

### Custom Views
- `CircularProgressView` - Progress lingkaran untuk step
- `WeeklyBarChartView` - Grafik batang kalori mingguan
- `WeightTrendView` - Trend berat badan

### API Integration
- **Google Gemini Flash Latest** - AI food analysis via REST
- **Firebase Firestore** - Real-time cloud database
- **Firebase Authentication** - Secure user auth

---

## 📥 Cara Instalasi

### Prerequisites
1. **Android Studio** Hedgehog (2023.1.1) atau lebih baru
2. **JDK** 11 atau lebih baru
3. **Android SDK** dengan platform API 36
4. **Firebase Project** dengan Firestore & Auth enabled
5. **Google AI API Key** (untuk Gemini)

### Langkah Instalasi

#### 1. Clone Repository
```bash
git clone https://github.com/username/MyHealthy.git
cd MyHealthy
```

#### 2. Buat Firebase Project
1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Klik **Add Project** → Beri nama `MyHealthy`
3. Ikuti wizard setup
4. Di **Project Settings**, download `google-services.json`
5. Tempatkan file di: `app/google-services.json`

#### 3. Enable Firebase Services
1. **Authentication** → Enable **Email/Password** dan **Google**
2. **Firestore Database** → Create database (start in **test mode**)
3. Buat index komposit untuk query diary:
   ```
   Collection: users/{userId}/diary
   Fields: date (Ascending)
   ```

#### 4. Setup Gemini API Key
1. Buka [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Generate API Key baru
3. Buat file `local.properties` di root project:
   ```properties
   GEMINI_API_KEY=YOUR_API_KEY_HERE
   ```

#### 5. Sync Gradle
```bash
./gradlew sync
```

#### 6. Build & Run
```bash
./gradlew assembleDebug
```
APK akan tersedia di: `app/build/outputs/apk/debug/app-debug.apk`

### Konfigurasi Tambahan

#### Google Sign-In (Opsional)
1. Setup OAuth di Firebase Console
2. Dapatkan `SHA-1` fingerprint:
   ```bash
   keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore
   ```
3. Tambahkan ke Firebase Project Settings

#### Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Troubleshooting

| Problem | Solution |
|---------|----------|
| `google-services.json` not found | Pastikan file ada di folder `app/` |
| Gemini API Error | Cek `GEMINI_API_KEY` di `local.properties` |
| Auth Error | Pastikan SHA-1 fingerprint terdaftar di Firebase |
| Firestore Permission Denied | Update security rules atau gunakan test mode |
| Step Counter not working | Cek permission `ACTIVITY_RECOGNITION` di Android 10+ |

---

## 📊 SCRUM Project

Project management menggunakan **ClickUp** untuk tracking sprint dan task.

### Link Project
🔗 [MyHealthy SCRUM Board - ClickUp](https://sharing.clickup.com/90181792360/b/h/2kzm1wk8-378/7fe21bafd1ddec5)

### Project Structure

#### Product Backlog
| ID | Task | Priority | Status |
|----|------|----------|--------|
| PB-001 | Setup Project Android Studio | High | Done |
| PB-002 | Firebase Authentication Setup | High | Done |
| PB-003 | UI/UX Design - Dark Theme | Medium | Done |
| PB-004 | Food Database & RecyclerView | High | Done |
| PB-005 | Food Diary Feature | High | Done |
| PB-006 | Calorie Calculator | High | Done |
| PB-007 | AI Scanner with Gemini | High | Done |
| PB-008 | Gamification System | Medium | Done |
| PB-009 | Daily Progress Dashboard | Medium | Done |
| PB-010 | Meal Plan Feature | Low | Done |
| PB-011 | Profile & Stats | Medium | Done |

#### Sprint Breakdown

**Sprint 1: Foundation**
- Project Setup
- Authentication (Login/Register)
- Navigation Structure
- Dark Theme UI

**Sprint 2: Core Features**
- Food Database with Search & Filter
- Food Detail View
- Calorie Calculator with BMI

**Sprint 3: Diary & Tracking**
- Food Diary CRUD
- Daily Progress Dashboard
- Step Counter Integration
- Water & Sleep Tracking

**Sprint 4: AI & Gamification**
- AI Food Scanner (Gemini API)
- Gamification System (XP, Levels, Badges)
- Weekly Charts
- Streak System

**Sprint 5: Polish**
- Profile Management
- Meal Plan Feature
- Bug Fixes
- Performance Optimization

### Task Management

| Status | Warna | Keterangan |
|--------|-------|------------|
| 🟢 To Do | Abu-abu | Belum dimulai |
| 🔵 In Progress | Biru | Sedang dikerjakan |
| 🟡 In Review | Kuning | Menunggu review |
| ✅ Done | Hijau | Selesai |
| ⚠️ Blocked | Merah | Ada hambatan |

### Team Roles
| Role | Responsibility |
|------|----------------|
| Product Owner | Prioritas fitur & backlog |
| Scrum Master | Sprint planning & tracking |
| Developer | Implementasi fitur |

---

## 📄 Lisensi

Project ini dibuat untuk keperluan akademis. Semua library pihak ketiga mengikuti lisensi masing-masing.

**MyHealthy** - © 2024

---

## 👤 Author

**Nama**: Daffa El Poy
**GitHub**: [el-dapoyyy](https://github.com/el-dapoyyy)

---

<div align="center">
  <strong>Made with ❤️ for a healthier lifestyle</strong>
</div>
