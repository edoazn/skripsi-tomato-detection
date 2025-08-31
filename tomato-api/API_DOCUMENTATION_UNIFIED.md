# API Documentation - Unified Content Service

## Overview
API ini menyediakan layanan deteksi penyakit tomat dan unified content service yang menggabungkan berita dan tips pertanian dalam satu endpoint.

**Base URL**: `http://localhost:8000`

---

## Endpoints

### 1. Health Check
**GET** `/`

**Response:**
```json
{
  "status": "API Deteksi Penyakit Tomat aktif."
}
```

### 2. Test Unified Content Service
**GET** `/test/content`

Endpoint untuk testing apakah unified content service berfungsi dengan baik.

**Response:**
```json
{
  "status": "success",
  "message": "Unified content service berfungsi dengan baik!",
  "total_content": 19,
  "berita_count": 1,
  "tip_count": 18,
  "categories": [
    "Pertanian",
    "Pencegahan", 
    "Pengobatan",
    "Perawatan"
  ]
}
```

### 3. Disease Prediction
**POST** `/predict`

Upload gambar daun tomat untuk analisis penyakit.

**Headers:**
- `Authorization: Bearer <firebase_token>`
- `Content-Type: multipart/form-data`

**Body:**
- `file`: Image file (JPG/PNG, max 2MB)

**Response:**
```json
{
  "status": "success",
  "predict_id": "uuid-string",
  "timestamp": "2024-01-01T00:00:00Z",
  "model_version": "2.0.0",
  "data": {
    "disease_id": "Early_blight",
    "nama_penyakit": "Hawar Dini (Early Blight)",
    "confidence": 0.85,
    "confidence_str": "85.00%",
    "gejala": ["Munculnya bercak cokelat kering..."],
    "penyebab": "Jamur Alternaria solani...",
    "solusi": ["Gunakan mulsa plastik..."],
    "image_url": "https://appku.com/ilustrasi/Early_blight.jpg"
  }
}
```

---

## Unified Content API

### 4. Browse/List Content with Basic Filters
**GET** `/api/content`

Mendapatkan konten untuk browsing/listing dengan filter dasar (ideal untuk tab navigation).

**Query Parameters:**
- `type` (optional): Filter berdasarkan tipe - `"berita"` atau `"tip"`
- `category` (optional): Filter berdasarkan kategori

**Examples:**

#### Get all content
```
GET /api/content
```

#### Get only tips (untuk tab "Tips")
```
GET /api/content?type=tip
```

#### Get only news (untuk tab "Berita")
```
GET /api/content?type=berita
```

#### Get tips from specific category (untuk badge/filter kategori)
```
GET /api/content?type=tip&category=Pencegahan
```

**Response:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "title": "Cara Memilih Bibit Tomat Berkualitas",
      "description": "Tips memilih bibit tomat yang sehat...",
      "type": "tip",
      "category": "Pencegahan",
      "imageUrl": "https://example.com/bibit-tomat.jpg",
      "source": "Cybext Kementerian Pertanian"
    }
  ],
  "total": 1,
  "filters_applied": {
    "type": "tip",
    "category": "Pencegahan"
  }
}
```

### 5. Search Content by Keyword
**GET** `/api/content/search`

Endpoint dedicated untuk search konten berdasarkan keyword (ideal untuk search bar).

**Query Parameters:**
- `q` (required): Kata kunci pencarian (min 2 karakter)
- `type` (optional): Filter berdasarkan tipe - `"berita"` atau `"tip"`
- `category` (optional): Filter berdasarkan kategori

**Examples:**

#### Search all content
```
GET /api/content/search?q=pupuk
```

#### Search only in tips
```
GET /api/content/search?q=pupuk&type=tip
```

#### Search with category filter
```
GET /api/content/search?q=penyiraman&type=tip&category=Perawatan
```

**Response:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 5,
      "title": "Jadwal Pemupukan Tomat yang Optimal",
      "description": "Panduan lengkap jadwal pemupukan...",
      "type": "tip",
      "category": "Perawatan",
      "imageUrl": "https://example.com/pupuk.jpg",
      "source": "Cybext Kementerian Pertanian"
    }
  ],
  "total": 1,
  "search_query": "pupuk",
  "filters_applied": {
    "type": "tip",
    "category": "Perawatan"
  }
}
```

### 6. Get Content Detail by ID and Type
**GET** `/api/content/{content_type}/{content_id}`

Mendapatkan detail lengkap konten berdasarkan tipe dan ID.

**Path Parameters:**
- `content_type`: `"berita"` atau `"tip"`
- `content_id`: Integer ID konten

**Examples:**
```
GET /api/content/tip/1
GET /api/content/berita/1
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "title": "Cara Memilih Bibit Tomat Berkualitas",
    "description": "Tips memilih bibit tomat yang sehat dan produktif",
    "content": "Bibit tomat berkualitas adalah kunci keberhasilan...",
    "type": "tip",
    "category": "Pencegahan",
    "imageUrl": "https://example.com/bibit-tomat.jpg",
    "source": "Cybext Kementerian Pertanian",
    "date": "2024-01-15"
  }
}
```

### 7. Get Content Statistics
**GET** `/api/content/stats`

Mendapatkan statistik konten yang tersedia.

**Response:**
```json
{
  "status": "success",
  "data": {
    "total_content": 19,
    "berita_count": 1,
    "tip_count": 18,
    "categories": [
      "Pertanian",
      "Pencegahan",
      "Pengobatan", 
      "Perawatan"
    ]
  }
}
```

---

## Testing Examples

### Using cURL

#### 1. Test Content Service
```bash
curl -X GET "http://localhost:8000/test/content"
```

#### 2. Get All Tips
```bash
curl -X GET "http://localhost:8000/api/content?type=tip"
```

#### 3. Get Prevention Tips
```bash
curl -X GET "http://localhost:8000/api/content?type=tip&category=Pencegahan"
```

#### 4. Search Content  
```bash
curl -X GET "http://localhost:8000/api/content/search?q=pupuk"
```

#### 5. Get Specific Tip Detail
```bash
curl -X GET "http://localhost:8000/api/content/tip/1"
```

#### 6. Get Content Statistics
```bash
curl -X GET "http://localhost:8000/api/content/stats"
```

### Using Postman

#### Collection Setup
1. Create new collection "Tomato API - Unified Content"
2. Set base URL variable: `{{base_url}}` = `http://localhost:8000`

#### Request Examples

1. **Get All Content**
   - Method: GET
   - URL: `{{base_url}}/api/content`

2. **Filter by Type**
   - Method: GET
   - URL: `{{base_url}}/api/content`
   - Params: `type=tip`

3. **Filter by Category**
   - Method: GET
   - URL: `{{base_url}}/api/content`
   - Params: `type=tip&category=Pencegahan`

4. **Search Content**
   - Method: GET
   - URL: `{{base_url}}/api/content/search`
   - Params: `q=pupuk`

5. **Get Content Detail**
   - Method: GET
   - URL: `{{base_url}}/api/content/tip/{{content_id}}`

6. **Get Statistics**
   - Method: GET
   - URL: `{{base_url}}/api/content/stats`

---

## Error Responses

### 400 - Bad Request
```json
{
  "detail": "Parameter 'type' harus berupa 'berita' atau 'tip'"
}
```

### 404 - Not Found
```json
{
  "detail": "Konten tip dengan ID 999 tidak ditemukan"
}
```

### 500 - Internal Server Error
```json
{
  "detail": "Terjadi kesalahan server: [error message]"
}
```

---

## Migration Notes

### Legacy Endpoints (Deprecated)
Endpoint lama masih tersedia untuk backward compatibility:
- `/api/news/*` - News endpoints
- `/api/tips/*` - Tips endpoints

### Recommended Usage
Gunakan endpoint baru `/api/content/*` untuk:
- Konsistensi dalam response format
- Fleksibilitas filtering
- Performance yang lebih baik
- Struktur yang lebih maintainable

---

## Content Categories

### Tips Categories:
- **Pencegahan**: Tips untuk mencegah penyakit dan masalah
- **Pengobatan**: Tips untuk mengatasi penyakit yang sudah terjadi
- **Perawatan**: Tips untuk perawatan rutin tanaman tomat

### News Categories:
- **Pertanian**: Berita umum seputar pertanian

---

## Mobile UI Integration

### Recommended Mobile App Structure
```
📱 Content Screen
├── 🔍 Search Bar ────────────► GET /api/content/search?q=...
├── 📑 Tab: "Semua" ─────────► GET /api/content
├── 📰 Tab: "Berita" ────────► GET /api/content?type=berita  
├── 💡 Tab: "Tips" ──────────► GET /api/content?type=tip
└── 🏷️ Badge Filters:
    ├── "Pencegahan" ────────► GET /api/content?type=tip&category=Pencegahan
    ├── "Pengobatan" ────────► GET /api/content?type=tip&category=Pengobatan
    └── "Perawatan" ─────────► GET /api/content?type=tip&category=Perawatan
```

### UI/UX Benefits
- **Clear separation**: Browsing vs Search functionality
- **Tab-friendly**: Perfect untuk tab navigation dengan badge filters
- **Search-optimized**: Dedicated endpoint untuk search bar
- **Fast listing**: Quick loading untuk tab switching

---

## Technical Notes

- Semua response menggunakan format JSON
- Content service menggabungkan data dari `news_service.py` dan `tip_service.py`
- Field `type` ditambahkan untuk membedakan konten berita dan tips
- **Listing endpoint** (`/api/content`) optimized untuk browsing dengan filter dasar
- **Search endpoint** (`/api/content/search`) optimized untuk full-text search
- Pencarian dilakukan di field `title`, `description`, dan `content`
- Case-insensitive search pada semua field text
