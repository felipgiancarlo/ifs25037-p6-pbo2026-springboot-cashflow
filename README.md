# ifs25037-p6-pbo2026-springboot-cashflow

Aplikasi web **Cashflow (Catatan Keuangan)** berbasis **Spring Boot 4.1.2**, **Maven**, **Vaadin 25.3.4**, **JDBC (PostgreSQL)**, dan **integrasi AI (OpenAI-compatible)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework). Studi kasus Praktikum 6 PBO 2026 (Integrasi AI).

> Username: `ifs25037` (folder, artifact Maven, package `org.delcom.ifs25037springbootcashflow`).

## Fitur

- Register, login, logout; sesi bertahan setelah refresh (`AuthSession` + cookie + `LOGGED_OUT_KEY`); password SHA-256
- Role **ADMIN** / **USER**; admin di-seed dari `ROOT_USERNAME` / `ROOT_PASSWORD`
- Transaksi **Pemasukan (INCOME)** / **Pengeluaran (EXPENSE)** per pengguna, bukti gambar opsional (`proofPath`), klik bukti → modal zoom
- Cari keterangan, **filter tipe** (`TransactionTypeFilter`: semua / pemasukan / pengeluaran), **urutan** (`SortOption`, default **terbaru di atas**; juga terlama, jumlah naik/turun, pemasukan dulu, pengeluaran dulu)
- **Generate AI**: tulis catatan keuangan bebas → AI menyusun transaksi (`description`, `amount`, `type`) → preview → simpan; `ProgressBar` loading saat proses async
- Kartu ringkasan: total transaksi, pemasukan, pengeluaran, saldo (format `Rp 1,000,000.00`)
- Tambah / ubah (klik 2x baris) / hapus (dengan konfirmasi)
- Profil: nama, password, foto
- ADMIN: daftar pengguna + transaksi pengguna lain (read-only); admin juga punya cashflow sendiri

## Integrasi AI

| Lapisan | Class |
|---|---|
| Domain (port & model) | `IFinanceAiService`, `AiGeneratedTransaction` |
| Use case | `FinanceAiUseCase` (`generateTransactions`, `saveGeneratedTransactions`) |
| Adapter | `OpenAiFinanceService` (HTTP), `InMemoryFinanceAiService` (unit test) |
| Framework | `FinanceView` → tombol **Generate AI** (async + `UI.access`) |

Isi `OPENAI_ENDPOINT`, `OPENAI_KEY`, `OPENAI_MODEL` di `.env`. Unit test tidak memanggil server AI nyata.

## Rute

| Kategori | Route | View | Akses |
|---|---|---|---|
| Public | `/login`, `/register` | LoginView, RegisterView | Anonim (sudah login → `/`) |
| Public | `/uploads/**`, `/icons/**`, `styles.css` | static | Semua |
| Protected | `/` | FinanceView | USER & ADMIN |
| Protected | `/profile` | ProfileView | USER & ADMIN |
| Admin | `/users`, `/user-transactions/{id}` | UsersView, UserTransactionsView | ADMIN (USER → `/`) |

## Menjalankan

1. Buat database sebelum run:
   ```sql
   CREATE DATABASE pbo2026_cashflow;
   ```
2. Salin `.env.example` menjadi `.env` lalu sesuaikan `DB_*`, `ROOT_*`, `UPLOAD_DIR`, `OPENAI_*`.
3. Development:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Build JAR (wajib profil production Vaadin):
   ```powershell
   .\mvnw.cmd clean package -Pproduction
   java -jar target\ifs25037-p6-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
   ```
5. Uji + coverage Jacoco 100% (view/auth dikecualikan): `.\mvnw.cmd clean verify`

Login admin default: `admin` / `admin` (dari `.env`).

## Struktur

```
src/main/java/org/delcom/ifs25037springbootcashflow/
├── App.java, DotEnv.java
├── config/AppConfig.java               # composition root (@Bean)
├── domain/
│   ├── PasswordHasher.java
│   ├── entity/       Transaction, TransactionType, Balance, User, Role, SortOption
│   ├── exception/    DomainException, EntityNotFoundException, ValidationException
│   └── repository/   IRepository<T,ID>, ITransactionRepository, IUserRepository
├── usecase/          AuthUseCase, FinanceUseCase, UserUseCase
├── adapter/
│   ├── presenter/    FinancePresenter
│   ├── repository/   InMemory*, Jdbc*, SchemaInitializer
│   └── storage/      FileStorage
└── framework/
    ├── auth/         AuthSession
    └── view/         Login, Register, MainLayout, Finance, Profile, Users, UserTransactions, ...
```

Dependency rule: `domain ← usecase ← adapter / framework`.
