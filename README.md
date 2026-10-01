# مدیر ساختمان (Building Manager)

اپلیکیشن آفلاین مدیریت ساختمان برای اندروید — ساخته‌شده با WebView + HTML

## ساخت APK روی گیت‌هاب (GitHub Actions)

1. این پروژه را داخل یک مخزن گیت‌هاب آپلود کن (همه فایل‌ها).
2. برو به تب **Actions**.
3. workflow به نام **Build APK** را انتخاب کن.
4. روی **Run workflow** بزن (یا صبر کن تا با Push خودکار اجرا شود).
5. وقتی سبز شد، روی اجرای موفق کلیک کن.
6. پایین صفحه در بخش **Artifacts** فایل `building-manager-apk` را دانلود کن.
7. فایل APK را از داخل zip خارج کن و روی گوشی نصب کن.

## ساخت با Android Studio

1. پروژه را Open کن.
2. Build → Build APK(s)

## ساختار

- `app/src/main/assets/index.html` → کل اپ
- `MainActivity.kt` → WebView
- کاملاً آفلاین
