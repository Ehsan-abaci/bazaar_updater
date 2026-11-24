# Bazaar Updater

**پلاگین Flutter برای یکپارچه‌سازی SDK رسمی BazaarUpdater بازار**

پکیج `bazaar_updater` یک پلاگین Flutter برای اتصال به SDK رسمی **BazaarUpdater** کافه‌بازار است:

`com.github.cafebazaar:bazaarUpdater`

با استفاده از این پلاگین می‌توانید مستقیماً از داخل برنامه، وضعیت انتشار نسخه جدید را بررسی کرده و فرآیند دانلود و نصب به‌روزرسانی را از طریق بازار آغاز کنید.

این پلاگین از SDK رسمی خود بازار استفاده می‌کند و نیازی به پیاده‌سازی سیستم اختصاصی بررسی نسخه یا ثبت دستی اطلاعات نسخه در بک‌اند برای Flavor بازار نخواهید داشت.

## قابلیت‌ها

- بررسی وجود نسخه جدید در بازار
- تشخیص وضعیت دانلود نسخه جدید
- شروع فرآیند به‌روزرسانی از داخل برنامه
- ادامه فرآیند نصب نسخه‌ای که قبلاً دانلود شده است
- دریافت وضعیت به‌روزرسانی به‌صورت غیرهمگام (Async)
- پشتیبانی از مدیریت خطاها
- استفاده از Platform Channel برای ارتباط Flutter و Android

## پیش‌نیازها (Requirements)

| مورد | نسخه / نیازمندی |
|---|---|
| Platform | Android |
| Flutter | نسخه سازگار با Android Plugin API |
| Dart | نسخه سازگار با Flutter مورد استفاده |
| Android SDK | نیازمند پروژه Android |
| Bazaar Android SDK | `1.2.0` |
| JitPack | مخزن دریافت SDK |
| Bazaar App | برای بررسی و دریافت آپدیت، نصب بودن بازار ضروری است |

**حداقل نسخه‌های Flutter، Dart، Android `minSdk`، Gradle و Kotlin هنوز به‌صورت رسمی برای این پلاگین تعیین نشده‌اند.** بنابراین نباید اعداد آزمایش‌نشده را به‌عنوان حداقل نسخه پشتیبانی‌شده در نظر گرفت.

### حداقل نسخه بازار برای قابلیت‌های مختلف

| قابلیت | حداقل نسخه بازار |
|---|---|
| بررسی وضعیت به‌روزرسانی | نسخه موردنیاز در مستندات SDK |
| تشخیص `NEW_VERSION_DOWNLOADED` | `30.1.0` |
| بررسی وضعیت Auto Update | `26.2.0` |
| فعال‌سازی Auto Update | `26.2.0` |

## نصب

در فایل `pubspec.yaml` پروژه Flutter:

```yaml
dependencies:
  bazaar_updater: ^1.0.0
```

سپس:

```bash
flutter pub get
```

> توجه: نسخه `1.0.0` در این مثال صرفاً نمونه است. نسخه واقعی پکیج را مطابق نسخه منتشرشده در pub.dev جایگزین کنید.

## نحوه استفاده

### ۱. بررسی وجود به‌روزرسانی

```dart
final state = await BazaarUpdater.checkForUpdate();

switch (state) {
  case BazaarUpdateState.newVersionAvailable:
    // نسخه جدید منتشر شده است.
    // نمایش پیام به‌روزرسانی به کاربر
    break;

  case BazaarUpdateState.newVersionDownloaded:
    // نسخه جدید قبلاً توسط بازار دانلود شده است.
    // امکان ادامه فرآیند نصب وجود دارد.
    break;

  case BazaarUpdateState.noNewVersion:
    // نسخه نصب‌شده آخرین نسخه موجود است.
    break;

  case BazaarUpdateState.unknown:
    // خطا در بررسی وضعیت یا عدم امکان تشخیص آن
    // در صورت نیاز، بررسی از طریق بک‌اند انجام شود.
    break;
}
```

### ۲. شروع فرآیند به‌روزرسانی

در صورت وجود نسخه جدید، پس از تأیید کاربر می‌توانید متد زیر را فراخوانی کنید:

```dart
await BazaarUpdater.updateApplication();
```

این متد فرآیند به‌روزرسانی را از طریق اپلیکیشن بازار آغاز می‌کند.

### ۳. نصب نسخه دانلودشده

در برخی دستگاه‌ها، به‌خصوص بعضی دستگاه‌های شیائومی، ممکن است دانلود نسخه جدید انجام شود اما نصب آن به دلایل محدودیت‌های سیستم‌عامل تکمیل نشود.

در چنین شرایطی، اگر وضعیت بررسی برابر با `newVersionDownloaded` بود، می‌توانید متد زیر را فراخوانی کنید:

```dart
await BazaarUpdater.installDownloadedUpdate();
```

این متد از فایل دانلودشده موجود استفاده می‌کند و فرآیند نصب را ادامه می‌دهد؛ بنابراین نیازی به دانلود مجدد نسخه جدید نیست.

**نکته مهم:** این متد را زمانی فراخوانی کنید که برنامه در حالت Foreground قرار دارد. از Android 10 به بعد، محدودیت‌هایی برای اجرای Activity از پس‌زمینه وجود دارد.

## مدیریت به‌روزرسانی اجباری (Mandatory Update)

SDK رسمی BazaarUpdater از قابلیت به‌روزرسانی اجباری پشتیبانی نمی‌کند.

اگر در بک‌اند خود فیلدی مانند `is_mandatory` دارید، همچنان باید منطق آن را در برنامه مدیریت کنید.

پیشنهاد می‌شود مسئولیت‌ها را به شکل زیر تفکیک کنید:

- **BazaarUpdater:** بررسی وجود نسخه جدید در بازار و شروع فرآیند به‌روزرسانی.
- **Backend:** مدیریت نسخه موردنیاز، حداقل نسخه مجاز و به‌روزرسانی اجباری.

در نتیجه، استفاده از BazaarUpdater به معنی حذف کامل سیستم بررسی نسخه بک‌اند نیست.

## نکات مهم

### ۱. تطابق Package Name

نام پکیج برنامه (Application ID) باید دقیقاً با نام پکیج نسخه منتشرشده در بازار یکسان باشد.

### ۲. تطابق Signature

برنامه باید با همان Keystore و Alias نسخه منتشرشده امضا شده باشد.

به همین دلیل، نسخه Debug یا نسخه‌ای که با کلید دیگری امضا شده است، ممکن است نتواند به‌روزرسانی منتشرشده در بازار را تشخیص دهد.

### ۳. نسخه بازار

برای دریافت وضعیت `newVersionDownloaded`، نسخه 30.1.0 یا بالاتر اپلیکیشن بازار موردنیاز است.

در نسخه‌های قدیمی‌تر، این وضعیت قابل تشخیص نیست و نتیجه بررسی به وضعیت‌های دیگر محدود می‌شود.

### ۴. وضعیت UNKNOWN

این وضعیت لزوماً به معنی نصب نبودن بازار نیست. ممکن است خطایی در فرآیند بررسی رخ داده باشد یا SDK نتواند وضعیت دریافتی را تشخیص دهد.

در این شرایط، بهتر است امکان بررسی نسخه از طریق بک‌اند نیز در نظر گرفته شود.

### ۵. محدودیت به‌روزرسانی اجباری

تصمیم نهایی برای نصب نسخه جدید در اختیار کاربر است. این SDK امکان اجبار کاربر به نصب نسخه جدید را فراهم نمی‌کند.

## مثال استفاده در پروژه

```dart
Future<void> checkBazaarUpdate() async {
  final state = await BazaarUpdater.checkForUpdate();

  switch (state) {
    case BazaarUpdateState.newVersionAvailable:
      // Show update dialog
      // On user confirmation:
      await BazaarUpdater.updateApplication();
      break;

    case BazaarUpdateState.newVersionDownloaded:
      // Optionally resume installation
      await BazaarUpdater.installDownloadedUpdate();
      break;

    case BazaarUpdateState.noNewVersion:
      // No update available
      break;

    case BazaarUpdateState.unknown:
      // Fallback to backend update check
      break;
  }
}
```

## مستندات و منابع

- [مستندات رسمی BazaarUpdater](https://github.com/cafebazaar/BazaarUpdater)
- [نسخه‌های منتشرشده SDK](https://github.com/cafebazaar/BazaarUpdater/releases)
- [Flutter Plugin Development](https://docs.flutter.dev/packages-and-plugins/developing-packages)

## License

این پروژه تحت مجوز مشخص‌شده در فایل `LICENSE` منتشر می‌شود.