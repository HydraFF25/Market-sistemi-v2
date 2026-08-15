# Bworld Market

Bworld SMP sunucusu için özel olarak geliştirilmiş, gelişmiş bir GUI market/shop eklentisi.
EconomyShopGUI mantığına benzer ama şu farklarla:

- **Kategorili, sayfalı GUI** (istediğiniz kadar kategori/ürün)
- **Arz-talep bazlı dinamik fiyatlandırma**: bir ürün çok alınırsa fiyatı yükselir, çok
  satılırsa düşer; zamanla taban fiyata geri döner (`config.yml` içinden açılıp kapatılabilir)
- **Sadece SMP için anlamlı ürünler** varsayılan olarak eklidir; **odun, spawner, netherite
  zırh/silah, elytra, totem** gibi kolay farm edilebilen veya "op" ürünler bilinçli olarak
  listeye eklenmemiştir
- `/market sellhand` — elindeki eşyayı satar
- `/market sellall` — envanterdeki tüm satılabilir eşyaları onay ekranıyla satar
- Sol tık = 1 adet satın al, Shift+Sol tık = 64 adet satın al
- Sağ tık = 1 adet sat, Shift+Sağ tık = envanterdeki tüm o eşyayı sat
- İşlem geçmişi `plugins/BworldMarket/transactions.log` dosyasına yazılır
- Tüm fiyatlar ve ürünler `items.yml` üzerinden veya oyun içi komutlarla yönetilebilir

## Gereksinimler

- Paper/Spigot 1.20.x sunucu
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- Vault destekli bir ekonomi eklentisi (örn. EssentialsX)

## Kurulum

1. `BworldMarket.jar` dosyasını `plugins/` klasörüne atın.
2. Vault ve bir ekonomi eklentisinin kurulu olduğundan emin olun.
3. Sunucuyu başlatın; `plugins/BworldMarket/` içinde `config.yml`, `items.yml` ve
   `messages.yml` otomatik oluşur.
4. `items.yml` dosyasını sunucunuza göre düzenleyin (kategoriler, fiyatlar, hangi ürünlerin
   alınıp/satılabileceği).

## Komutlar

| Komut | Açıklama | Yetki |
|---|---|---|
| `/market` | Market GUI'sini açar | `bworldmarket.use` (varsayılan: herkes) |
| `/market sellhand` | Elindeki eşyayı satar | `bworldmarket.use` |
| `/market sellall` | Envanterdeki satılabilir tüm eşyaları satar | `bworldmarket.use` |
| `/bworldmarket reload` | Ayarları yeniden yükler | `bworldmarket.admin` (varsayılan: op) |
| `/bworldmarket additem <kategori> <materyal> <alış> <satış>` | Ürün ekler (satış/alış için -1 = kapalı) | `bworldmarket.admin` |
| `/bworldmarket removeitem <kategori> <materyal>` | Ürün kaldırır | `bworldmarket.admin` |
| `/bworldmarket setbuyprice <kategori> <materyal> <fiyat>` | Alış fiyatını değiştirir | `bworldmarket.admin` |
| `/bworldmarket setsellprice <kategori> <materyal> <fiyat>` | Satış fiyatını değiştirir | `bworldmarket.admin` |
| `/bworldmarket list` | Kategorileri listeler | `bworldmarket.admin` |

## GitHub üzerinden derleme (GitHub Actions)

Bu proje `.github/workflows/build.yml` içinde hazır bir GitHub Actions iş akışı içerir.
Yapmanız gerekenler:

1. Bu klasörü kendi GitHub reponuza push edin (`main` veya `master` dalına).
2. GitHub reponuzda **Actions** sekmesine gidin, "Build BworldMarket" iş akışının
   otomatik çalıştığını göreceksiniz (push sonrası, ya da elle "Run workflow" ile).
3. İş akışı bitince, çalışan işin altındaki **Artifacts** bölümünden `BworldMarket`
   adlı zip dosyasını indirin — içinde derlenmiş `BworldMarket.jar` bulunur.
4. Bu jar dosyasını sunucunuzun `plugins/` klasörüne atmanız yeterli.

## Yerel derleme (isterseniz)

```bash
mvn clean package
```

Derlenen dosya `target/BworldMarket.jar` yolunda oluşur.

## Yapıyı genişletme fikirleri

- Kategori/ürün ikonlarını `custom-model-data` ile özelleştirebilirsiniz (resource pack ile).
- `sell-tax-percent` ile satışlardan sunucu ekonomisine vergi ekleyebilirsiniz.
- Dinamik fiyatlandırmayı kapatıp sabit fiyatlı klasik bir shop olarak da kullanabilirsiniz
  (`config.yml` → `dynamic-pricing.enabled: false`).
