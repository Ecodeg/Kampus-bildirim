# 📱 Kampüs Bildirim Sistemi 

Kampüs Bildirim Sistemi, üniversite kampüsü içerisinde meydana gelen sağlık, güvenlik, teknik ve çevresel olayların kullanıcılar tarafından bildirilmesini, takip edilmesini ve yöneticiler tarafından yönetilmesini sağlayan Android tabanlı bir mobil uygulamadır.

Uygulama Kotlin dili ile geliştirilmiş olup, Firebase altyapısı kullanılarak gerçek zamanlı veri yönetimi ve kullanıcı doğrulama işlemleri gerçekleştirilmiştir.<br><br>


## 🛠️ Teknik Altyapı
***Dil***: Kotlin

***SDK***: Android API 36 (Target), API 24 (Min)

***Backend***: Firebase Firestore (NoSQL), Firebase Authentication

***Harita***: Google Maps SDK & Google Play Services Location

***Görsel Yükleme***: Glide & Base64 Encoding<br><br>

## 🚀 Öne Çıkan Özellikler
***Rol Tabanlı Yetkilendirme***: Admin ve User rolleri ile özelleştirilmiş erişim kontrolü.

***Gerçek Zamanlı Senkronizasyon***: Firebase SnapshotListener ile sayfa yenilemeden anlık veri güncellemeleri.

***Coğrafi Takip***: Google Maps SDK entegrasyonu ile olayları harita üzerinde türlerine göre renkli pinlerle görselleştirme.

***Optimize Edilmiş Görsel İşleme***: Fotoğrafların Base64 formatına sıkıştırılarak Firestore dökümanları içinde saklanması.

***Acil Duyuru Sistemi***: Yöneticilerin tüm kullanıcılara anlık uyarı (broadcast) gönderebilmesi.<br><br>


## 📂 Proje Yapısı ve Kod Analizi
**Veri Modelleri**

***Notification.kt***: Olayların başlık, açıklama, koordinat ve takipçi verilerini tutan ana veri sınıfı.<br><br>

**Aktiviteler**

***LoginActivity.kt & SignUpActivity.kt***: Firebase Auth ile kullanıcı doğrulaması ve profil verilerinin Firestore'a kaydedilmesi.

***MainActivity.kt***: Bildirimlerin listelendiği, filtrelendiği ve asenkron olarak dinlendiği ana akış ekranı.

***AddNotificationActivity.kt***: GPS verisi ve sıkıştırılmış görsel (Base64) ile yeni rapor oluşturma modülü.

***MapActivity.kt***: Tüm olayların interaktif harita üzerinde kategorize edilmiş markerlar ile sunumu.

***AdminPanelActivity.kt***: İş akış yönetimi ve acil durum duyurularının yayınlandığı kontrol merkezi.<br><br>

## 🏗️ Kurulum ve Çalıştırma

- Depoyu klonlayın.

- Android Studio ile projeyi açın.

- Firebase konsolunda bir proje oluşturun ve google-services.json dosyasını app/ dizinine ekleyin.

- Google Cloud Console'dan Maps SDK API anahtarı alın ve AndroidManifest.xml dosyasındaki ilgili alana yapıştırın.

- Projeyi Build > Clean Project yaptıktan sonra çalıştırın.<br><br>

## 👩‍💻 Geliştiriciler
Ebrar GÜMÜŞ ve Betül Zehra İNCESU

🎓 Atatürk Üniversitesi

💻 Bilgisayar Mühendisliği (İngilizce)




