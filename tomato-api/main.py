from contextlib import asynccontextmanager
import io
import os
import numpy as np
import tensorflow as tf
from PIL import Image
from fastapi import FastAPI, File, UploadFile, HTTPException, Depends, Header
from fastapi.responses import JSONResponse
import logging
from dotenv import load_dotenv
import uuid
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, auth
from news_service import get_news_data  


# Load environment variables dari file .env
load_dotenv()

# --- Konfigurasi Dasar ---
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- Fungsi Startup: Load Model ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    global model
    model_path = os.getenv("MODEL_PATH", "model_siap_pakai.keras")
    try:
        logger.info(f"Mencoba memuat model dari: {model_path}")
        model = tf.keras.models.load_model(model_path, compile=False)
        logger.info("✅ Model berhasil dimuat.")
        yield
    except Exception as e:
        logger.error(f"❌ Gagal memuat model: {e}")
        raise RuntimeError(
            f"Tidak dapat memuat model dari {model_path}. Pastikan file ada dan valid."
        )

# --- Inisialisasi Aplikasi FastAPI ---
app = FastAPI(
    title="API Deteksi Penyakit & Berita Tomat",
    description="API untuk menganalisis gambar daun tomat dan menyediakan berita pertanian.",
    version="2.0.0",
    lifespan=lifespan,
)

# --- KAMUS INFORMASI PENYAKIT (Data Anda yang sudah sangat baik) ---
INFORMASI_PENYAKIT = {
    "Bacterial_spot": {
        "nama_penyakit": "Bercak Bakteri (Bacterial Spot)",
        "penyebab": "Bakteri genus Xanthomonas (X. vesicatoria, X. perforans, dll.).",
        "gejala": "Bercak kecil, basah, berwarna gelap pada daun, batang, dan buah. Bercak pada daun seringkali memiliki lingkaran kuning di sekelilingnya. Penyakit ini tidak bisa disembuhkan.",
        "solusi": "Gunakan benih dan bibit yang bebas penyakit. Lakukan rotasi tanaman 3–4 tahun. Pengendalian berfokus pada pencegahan. Semprotkan bakterisida berbasis tembaga atau kombinasi tembaga–mankozeb segera setelah tanam atau saat gejala pertama muncul. Aplikasikan kembali sesuai interval pada label produk.",
    },
    "Early_blight": {
        "nama_penyakit": "Hawar Dini (Early Blight)",
        "penyebab": "Jamur Alternaria solani (juga dikenal sebagai A. tomatophila atau A. linariae).",
        "gejala": "Munculnya bercak cokelat kering berbentuk konsentris seperti 'papan target' pada daun, batang, dan buah. Biasanya dimulai dari daun-daun bagian bawah.",
        "solusi": "Gunakan mulsa plastik untuk mencegah percikan spora dari tanah ke daun. Semprotkan fungisida kontak seperti klorotalonil, mankozeb, atau tembaga saat gejala pertama kali muncul, terutama saat cuaca lembap.",
    },
    "Healthy": {
        "nama_penyakit": "Sehat (Healthy)",
        "penyebab": "Tidak ada penyakit.",
        "gejala": "-",
        "solusi": "Pertahankan! Lanjutkan praktik perawatan yang baik seperti penyiraman teratur, pemupukan seimbang, dan pemantauan rutin untuk deteksi dini masalah.",
    },
    "Late_blight": {
        "nama_penyakit": "Hawar Daun, Busuk Daun (Late Blight)",
        "penyebab": "Oomycete (organisme mirip jamur) Phytophthora infestans.",
        "gejala": "Bercak basah berwarna hijau gelap hingga keunguan pada daun yang menyebar dengan cepat. Seringkali terdapat lapisan jamur putih di bagian bawah daun. Pada buah, muncul bercak besar berwarna cokelat dan berkeropeng. Penyakit ini sangat destruktif pada suhu sejuk (15–24°C) dan kelembapan tinggi.",
        "solusi": "Lakukan penyemprotan fungisida preventif sebelum gejala muncul, terutama saat musim hujan. Gunakan fungisida kontak seperti klorotalonil atau mankozeb, atau fungisida sistemik seperti kombinasi azoksistrobin+difenokonazol. ",
    },
    "Leaf_Mold": {
        "nama_penyakit": "Kapang Daun (Leaf Mold)",
        "penyebab": "Jamur Passalora fulva (sinonim Cladosporium fulvum).",
        "gejala": "Umumnya terjadi di rumah kaca atau area dengan kelembapan tinggi. Gejala awal adalah bintik kuning pucat di permukaan atas daun, yang diikuti oleh lapisan jamur berwarna zaitun di bagian bawahnya. Daun yang terinfeksi parah akan menguning dan rontok.",
        "solusi": "Tingkatkan sirkulasi udara dengan menjaga jarak tanam dan memangkas tunas air. Hindari membasahi daun dengan menggunakan irigasi tetes. Jika serangan parah, gunakan fungisida seperti klorotalonil atau azoksistrobin+difenokonazol.",
    },
    "Mosaic_virus": {
        "nama_penyakit": "Virus Mosaic Tomat (Tomato Mosaic Virus)",
        "penyebab": "Virus ToMV yang sangat mudah menular melalui kontak mekanis (tangan, alat potong) dan benih yang terinfeksi.",
        "gejala": "Pola belang hijau muda dan hijau tua (mosaik) pada daun. Daun bisa tampak keriput, melepuh, atau berbentuk seperti benang. Tanaman menjadi kerdil dan buah bisa mengalami bercak internal.",
        "solusi": "Tidak ada pengobatan yang efektif. Pencegahan terbaik adalah menggunakan benih bebas virus, mengendalikan kutu daun, dan menghindari kontak dengan tanaman terinfeksi. Pemangkasan dan penghancuran tanaman terinfeksi juga dianjurkan.",
    },
    "Septoria_leaf_spot": {
        "nama_penyakit": "Bercak Daun Septoria (Septoria Leaf Spot)",
        "penyebab": "Jamur Septoria lycopersici.",
        "gejala": "Munculnya banyak bintik kecil (1-2 mm) berwarna cokelat dengan bagian tengah keabu-abuan dan pinggiran lebih gelap. Penyakit ini biasanya dimulai dari daun paling bawah dan merambat ke atas, menyebabkan daun rontok parah.",
        "solusi": " Semprot secara berkala dengan fungisida kontak seperti tembaga, klorotalonil, atau mankozeb, terutama saat cuaca lembap.",
    },
    "Spider_mites": {
        "nama_penyakit": "Hama Tungau (Spider Mites)",
        "penyebab": "Tungau kecil (Tetranychus urticae) yang menghisap cairan dari daun.",
        "gejala": "Daun menjadi kuning, berdebu, dan mungkin terdapat jaring laba-laba halus di bawah daun. Serangan berat dapat menyebabkan daun mengering dan rontok.",
        "solusi": "Gunakan insektisida berbasis minyak neem atau insektisida sistemik. Jaga kelembapan udara yang cukup untuk mengurangi populasi tungau. Pemangkasan daun yang terinfeksi juga dapat membantu mengendalikan penyebaran.",
    },
    "Target_Spot": {
        "nama_penyakit": "Bercak Target (Target Spot)",
        "penyebab": "Jamur Corynespora cassiicola.",
        "gejala": "Bercak cokelat dengan tepi kuning yang berkembang menjadi bercak besar dengan pola konsentris. Biasanya dimulai dari daun bawah dan menyebar ke atas.",
        "solusi": "Gunakan fungisida kontak seperti klorotalonil atau mankozeb. Rotasi tanaman dan menjaga jarak tanam yang baik untuk meningkatkan sirkulasi udara juga penting.",
    },
    "YellowLeaf__Curl_Virus": {
        "nama_penyakit": "Keriting Daun Kuning (Yellow Leaf Curl Virus)",
        "penyebab": "Disebabkan oleh Tomato Yellow Leaf Curl Virus (TYLCV), ditularkan oleh serangga kutu kebul (whitefly).",
        "gejala": "Daun baru menjadi kerdil, menguning, dan melengkung ke atas (keriting). Pertumbuhan tanaman terhambat parah dan produksi buah menurun drastis.",
        "solusi": "Tidak ada obat. Fokus pada pengendalian vektornya, yaitu kutu kebul, menggunakan insektisida atau perangkap lengket. Gunakan varietas yang tahan virus.",
    },
}

# --- Variabel Global & Konfigurasi Model ---
model = None
NAMA_KELAS = list(INFORMASI_PENYAKIT.keys())
UKURAN_INPUT_MODEL = (224, 224)

# Inisialisasi Firebase Admin dengan file service account
cred = credentials.Certificate("docmat-app-firebase-adminsdk-key.json")
firebase_admin.initialize_app(cred)

# Fungsi untuk memverifikasi token
def verify_firebase_token(authorization: str = Header(...)):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Token otentikasi tidak ditemukan.")
    id_token = authorization.split(" ", 1)[1]
    try:
        decoded_token = auth.verify_id_token(id_token)
        return decoded_token
    except auth.InvalidIdTokenError:
        raise HTTPException(
            status_code=401,
            detail="Token Firebase tidak valid atau sudah expired."
        )


# --- Preprocessing Gambar ---
def preprocess_image(image_bytes: bytes) -> np.ndarray:
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    image = image.resize(UKURAN_INPUT_MODEL)
    image_array = np.array(image) / 255.0
    return np.expand_dims(image_array, axis=0)


# --- Endpoint Status ---
@app.get("/")
def read_root():
    return {"status": "API Deteksi Penyakit Tomat aktif."}


# --- ENDPOINT UTAMA UNTUK PREDIKSI ---
# Maksimal ukuran file adalah 2MB
MAX_FILE_SIZE = 2 * 1024 * 1024

@app.post("/predict")
async def predict_disease(
    file: UploadFile = File(...),
    user: dict = Depends(verify_firebase_token)
):
    # Cek ukuran file gambar
    if file.size > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=400,
            detail="Ukuran file gambar terlalu besar. Maksimal ukuran file adalah 2MB."
        )

    if not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="Tipe file tidak valid. Harap unggah file gambar (JPG, PNG).",
        )

    try:
        # Membaca dan memproses gambar
        image_bytes = await file.read()
        processed_image = preprocess_image(image_bytes)
        
        # Melakukan prediksi
        prediction_scores = model.predict(processed_image)[0]
        confidence = float(np.max(prediction_scores))
        predicted_index = np.argmax(prediction_scores)
        predicted_class_internal = NAMA_KELAS[predicted_index]
        informasi_detail = INFORMASI_PENYAKIT.get(predicted_class_internal)

        predict_id = str(uuid.uuid4())
        timestamp = datetime.utcnow().isoformat() + "Z"
        model_version = "2.0.0"

        MIN_CONFIDENCE = 0.60
        if confidence < MIN_CONFIDENCE:
            return JSONResponse(
                status_code=200,
                content={
                    "status": "unrecognized",
                    "predict_id": predict_id,
                    "timestamp": timestamp,
                    "model_version": model_version,
                    "data": {
                        "disease_id": None,
                        "nama_penyakit": "Gambar Tidak Dapat Diidentifikasi",
                        "confidence": confidence,
                        "confidence_str": f"{confidence:.2%}",
                        "gejala": ["Model tidak cukup yakin untuk membuat diagnosis."],
                        "penyebab": "Ini bisa terjadi jika gambar buram, pencahayaan kurang, atau objek bukan daun tomat.",
                        "solusi": [
                            "Silakan coba ambil foto ulang. Pastikan fokus pada daun yang bergejala dengan pencahayaan yang baik."
                        ],
                    },
                },
            )

        def to_list(text):
            if isinstance(text, list):
                return text
            return [
                t.strip() for t in text.replace("\n", ". ").split(". ") if t.strip()
            ]

        return JSONResponse(
            status_code=200,
            content={
                "status": "success",
                "predict_id": predict_id,
                "timestamp": timestamp,
                "model_version": model_version,
                "data": {
                    "disease_id": predicted_class_internal,
                    "nama_penyakit": informasi_detail["nama_penyakit"],
                    "confidence": confidence,
                    "confidence_str": f"{confidence:.2%}",
                    "gejala": to_list(informasi_detail["gejala"]),
                    "penyebab": informasi_detail["penyebab"],
                    "solusi": to_list(informasi_detail["solusi"]),
                    "image_url": f"https://appku.com/ilustrasi/{predicted_class_internal}.jpg",
                },
            },
        )
    except Exception as e:
        logger.error(f"Terjadi kesalahan saat prediksi: {e}")
        raise HTTPException(
            status_code=500, detail=f"Terjadi kesalahan pada server: {e}"
        )


# --- ENDPOINT BERITA ---
@app.get("/api/news")
async def get_news_list():
    all_news = get_news_data()
    summary_list = [
        {
            "id": news["id"],
            "title": news["title"],
            "description": news["description"],
            "imageUrl": news["imageUrl"],
            "source": news["source"],
        }
        for news in all_news
    ]
    return {"status": "success", "data": summary_list}


@app.get("/api/news/{news_id}")
async def get_news_detail(news_id: int):
    all_news = get_news_data()
    target_news = next((news for news in all_news if news["id"] == news_id), None)
    if not target_news:
        raise HTTPException(
            status_code=404, detail="Berita dengan ID tersebut tidak ditemukan."
        )
    return {"status": "success", "data": target_news}
