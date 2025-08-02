import io
import numpy as np
import tensorflow as tf
from PIL import Image
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
import logging

# --- Konfigurasi Dasar ---
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- Inisialisasi Aplikasi FastAPI ---
app = FastAPI(
    title="API Deteksi Penyakit Tomat",
    description="API untuk menganalisis gambar daun tomat dan memberikan informasi lengkap mengenai penyakit, penyebab, dan solusinya.",
    version="1.0.0",
)

# --- KAMUS INFORMASI PENYAKIT ---
INFORMASI_PENYAKIT = {
    "Bacterial_spot": {
        "nama_penyakit": "Bercak Bakteri",
        "penyebab": "Bakteri genus Xanthomonas (X. vesicatoria, X. perforans, dll.).",
        "gejala": "Bercak kecil, basah, berwarna gelap pada daun, batang, dan buah. Bercak pada daun seringkali memiliki lingkaran kuning di sekelilingnya. Penyakit ini tidak bisa disembuhkan.",
        "solusi": "Gunakan benih dan bibit yang bebas penyakit. Lakukan rotasi tanaman 3–4 tahun. Pengendalian berfokus pada pencegahan. Semprotkan bakterisida berbasis tembaga atau kombinasi tembaga–mankozeb segera setelah tanam atau saat gejala pertama muncul. Aplikasikan kembali sesuai interval pada label produk.",
    },
     "Early_blight": { 
        "nama_penyakit": "Hawar Dini", 
        "penyebab": "Jamur Alternaria solani (juga dikenal sebagai A. tomatophila atau A. linariae).", 
        "gejala": "Munculnya bercak cokelat kering berbentuk konsentris seperti 'papan target' pada daun, batang, dan buah. Biasanya dimulai dari daun-daun bagian bawah.",
        "solusi": "Gunakan mulsa plastik untuk mencegah percikan spora dari tanah ke daun. Semprotkan fungisida kontak seperti klorotalonil, mankozeb, atau tembaga saat gejala pertama kali muncul, terutama saat cuaca lembap." 
    },
    "Healthy": {
        "nama_penyakit": "Sehat",
        "penyebab": "Tanaman dalam kondisi baik dan tidak terinfeksi patogen.",
        "gejala": "Daun berwarna hijau segar dan merata, tidak ada bercak, lubang, atau perubahan warna yang aneh. Pertumbuhan tanaman terlihat normal dan kuat.",
        "solusi": "Pertahankan! Lanjutkan praktik perawatan yang baik seperti penyiraman teratur, pemupukan seimbang, dan pemantauan rutin untuk deteksi dini masalah.",
    },
    "Late_blight": {
        "nama_penyakit": "Hawar Daun (Busuk Daun)",
        "penyebab": "Oomycete (organisme mirip jamur) Phytophthora infestans.",
        "gejala": "Bercak basah berwarna hijau gelap hingga keunguan pada daun yang menyebar dengan cepat. Seringkali terdapat lapisan jamur putih di bagian bawah daun. Pada buah, muncul bercak besar berwarna cokelat dan berkeropeng. Penyakit ini sangat destruktif pada suhu sejuk (15–24°C) dan kelembapan tinggi.",
        "solusi": "Lakukan penyemprotan fungisida preventif sebelum gejala muncul, terutama saat musim hujan. Gunakan fungisida kontak seperti klorotalonil atau mankozeb, atau fungisida sistemik seperti kombinasi azoksistrobin+difenokonazol. ",
    },
    "Leaf_Mold": { 
        "nama_penyakit": "Kapang Daun", 
        "penyebab": "Jamur Passalora fulva (sinonim Cladosporium fulvum).", 
        "gejala": "Umumnya terjadi di rumah kaca atau area dengan kelembapan tinggi. Gejala awal adalah bintik kuning pucat di permukaan atas daun, yang diikuti oleh lapisan jamur berwarna zaitun di bagian bawahnya. Daun yang terinfeksi parah akan menguning dan rontok.", 
        "solusi": "Tingkatkan sirkulasi udara dengan menjaga jarak tanam dan memangkas tunas air. Hindari membasahi daun dengan menggunakan irigasi tetes. Jika serangan parah, gunakan fungisida seperti klorotalonil atau azoksistrobin+difenokonazol." 
    },
     "Mosaic_virus": {
        "nama_penyakit": "Virus Mosaic Tomat",
        "penyebab": "Virus ToMV yang sangat mudah menular melalui kontak mekanis (tangan, alat potong) dan benih yang terinfeksi.",
        "gejala": "Pola belang hijau muda dan hijau tua (mosaik) pada daun. Daun bisa tampak keriput, melepuh, atau berbentuk seperti benang. Tanaman menjadi kerdil dan buah bisa mengalami bercak internal.",
        "solusi": "Tidak ada pengobatan yang efektif. Pencegahan terbaik adalah menggunakan benih bebas virus, mengendalikan kutu daun, dan menghindari kontak dengan tanaman terinfeksi. Pemangkasan dan penghancuran tanaman terinfeksi juga dianjurkan.",
    },
    "Septoria_leaf_spot": {
        "nama_penyakit": "Bercak Daun Septoria",
        "penyebab": "Jamur Septoria lycopersici.",
        "gejala": "Munculnya banyak bintik kecil (1-2 mm) berwarna cokelat dengan bagian tengah keabu-abuan dan pinggiran lebih gelap. Penyakit ini biasanya dimulai dari daun paling bawah dan merambat ke atas, menyebabkan daun rontok parah.",
        "solusi": " Semprot secara berkala dengan fungisida kontak seperti tembaga, klorotalonil, atau mankozeb, terutama saat cuaca lembap."
    },
    "Spider_mites": {
        "nama_penyakit": "Hama Tungau",
        "penyebab": "Tungau kecil (Tetranychus urticae) yang menghisap cairan dari daun.",
        "gejala": "Daun menjadi kuning, berdebu, dan mungkin terdapat jaring laba-laba halus di bawah daun. Serangan berat dapat menyebabkan daun mengering dan rontok.",
        "solusi": "Gunakan insektisida berbasis minyak neem atau insektisida sistemik. Jaga kelembapan udara yang cukup untuk mengurangi populasi tungau. Pemangkasan daun yang terinfeksi juga dapat membantu mengendalikan penyebaran.",
    },
    "Target_Spot": {
        "nama_penyakit": "Bercak Target",
        "penyebab": "Jamur Corynespora cassiicola.",
        "gejala": "Bercak cokelat dengan tepi kuning yang berkembang menjadi bercak besar dengan pola konsentris. Biasanya dimulai dari daun bawah dan menyebar ke atas.",
        "solusi": "Gunakan fungisida kontak seperti klorotalonil atau mankozeb. Rotasi tanaman dan menjaga jarak tanam yang baik untuk meningkatkan sirkulasi udara juga penting.",
    },
    "YellowLeaf__Curl_Virus": {
        "nama_penyakit": "Virus Keriting Daun Kuning",
        "penyebab": "Disebabkan oleh Tomato Yellow Leaf Curl Virus (TYLCV), ditularkan oleh serangga kutu kebul (whitefly).",
        "gejala": "Daun baru menjadi kerdil, menguning, dan melengkung ke atas (keriting). Pertumbuhan tanaman terhambat parah dan produksi buah menurun drastis.",
        "solusi": "Tidak ada obat. Fokus pada pengendalian vektornya, yaitu kutu kebul, menggunakan insektisida atau perangkap lengket. Gunakan varietas yang tahan virus.",
    },
}

# --- Variabel Global & Konfigurasi Model ---
model = None
# Nama kelas internal (Bahasa Inggris), harus sama persis dengan saat training
NAMA_KELAS = list(INFORMASI_PENYAKIT.keys())
# Sesuaikan ukuran gambar dengan yang Anda gunakan saat training (misal: 224, 256, dll.)
UKURAN_INPUT_MODEL = (224, 224)

# --- Fungsi yang Berjalan Saat API Dinyalakan ---
@app.on_event("startup")
def load_model_on_startup():
    """Memuat model machine learning ke memori saat server dimulai."""
    global model
    model_path = 'model_tomat.keras' # Pastikan nama file ini benar
    try:
        logger.info(f"Mencoba memuat model dari: {model_path}")
        model = tf.keras.models.load_model(model_path)
        logger.info("✅ Model berhasil dimuat.")
    except Exception as e:
        logger.error(f"❌ Gagal memuat model: {e}")
        raise RuntimeError(f"Tidak dapat memuat model dari {model_path}. Pastikan file ada dan valid.")

# --- Fungsi untuk Memproses Gambar ---
def preprocess_image(image_bytes: bytes) -> np.ndarray:
    """Mengubah gambar menjadi format yang diterima oleh model."""
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    image = image.resize(UKURAN_INPUT_MODEL)
    image_array = np.array(image) / 255.0
    return np.expand_dims(image_array, axis=0)

# --- ENDPOINT UTAMA UNTUK PREDIKSI ---
@app.post("/predict")
async def predict_disease(file: UploadFile = File(...)):
    """Menerima file gambar, menganalisis, dan mengembalikan hasil lengkap."""
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Tipe file tidak valid. Harap unggah file gambar (JPG, PNG).")

    try:
        # Baca dan proses gambar
        image_bytes = await file.read()
        processed_image = preprocess_image(image_bytes)
        
        # Lakukan prediksi
        prediction_scores = model.predict(processed_image)[0]
        predicted_index = np.argmax(prediction_scores)
        confidence = float(np.max(prediction_scores))
        
        # Dapatkan nama kelas internal (English)
        predicted_class_internal = NAMA_KELAS[predicted_index]
        
        # Ambil informasi detail dari kamus
        informasi_detail = INFORMASI_PENYAKIT.get(predicted_class_internal)

        logger.info(f"Prediksi berhasil: {predicted_class_internal} ({confidence:.2%})")

        # Kembalikan respons JSON yang akan diterima aplikasi Android
        return JSONResponse(
            status_code=200,
            content={
                "status": "success",
                "nama_penyakit": informasi_detail["nama_penyakit"],
                "tingkat_keyakinan": f"{confidence:.2%}",
                "gejala": informasi_detail["gejala"],
                "penyebab": informasi_detail["penyebab"],
                "solusi": informasi_detail["solusi"],
            }
        )
    except Exception as e:
        logger.error(f"Terjadi kesalahan saat prediksi: {e}")
        raise HTTPException(status_code=500, detail=f"Terjadi kesalahan pada server: {e}")

# --- Endpoint untuk mengecek status API ---
@app.get("/")
def read_root():
    """Endpoint dasar untuk memastikan API berjalan."""
    return {"status": "API Deteksi Penyakit Tomat aktif."}
