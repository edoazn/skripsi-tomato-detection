package com.example.docmat.data.repository

import com.example.docmat.data.remote.api.NewsApiService
import com.example.docmat.data.remote.mapper.toNews
import com.example.docmat.domain.model.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsApiService: NewsApiService
) {
    private var cachedNews: List<News>? = null

    suspend fun getNews(): List<News> {
        return withContext(Dispatchers.IO) {
            cachedNews?.let { return@withContext it }
            try {
                val newsResponse = newsApiService.getNewsList()
                val newsList = newsResponse.data.map { it.toNews() }
                cachedNews = newsList
                newsList
            } catch (e: Exception) {
                getMockNews().also { cachedNews = it }
            }
        }
    }

    suspend fun refreshNews(): List<News> {
        return withContext(Dispatchers.IO) {
            try {
                val newsResponse = newsApiService.getNewsList()
                val newsList = newsResponse.data.map { it.toNews() }
                cachedNews = newsList
                newsList
            } catch (e: Exception) {
                // If API fails, return mock data
                getMockNews().also { cachedNews = it }
            }
        }
    }

    suspend fun getNewsDetail(id: Int): News {
        return withContext(Dispatchers.IO) {
            try {
                val response = newsApiService.getNewsDetail(id)
                response.data.toNews()
            } catch (e: Exception) {
                // If API fails, return mock detail based on id
                getMockNewsDetail(id)
            }
        }
    }

    suspend fun searchNews(keyword: String): List<News> {
        return withContext(Dispatchers.IO) {
            try {
                val response = newsApiService.searchNews(keyword)
                response.data.map { it.toNews() }
            } catch (e: Exception) {
                // If API fails, return filtered mock data
                getMockNews().filter { 
                    it.title.contains(keyword, ignoreCase = true) ||
                    it.description.contains(keyword, ignoreCase = true)
                }
            }
        }
    }

    suspend fun getRelatedNews(id: Int): List<News> {
        return withContext(Dispatchers.IO) {
            try {
                val response = newsApiService.getRelatedNews(id)
                response.data.map { it.toNews() }
            } catch (e: Exception) {
                // If API fails, return other mock news
                getMockNews().filter { it.id != id }.take(3)
            }
        }
    }

    private fun getMockNews(): List<News> {
        return listOf(
            News(
                id = 1,
                title = "Kembali Meroket! Harga Tomat Capai Rp 20‑30 rb/kg",
                description = "Distribusi terganggu dan cuaca menyebabkan harga tomat melonjak tajam.",
                imageUrl = "https://placehold.co/600x400/f87171/FFFFFF?text=Harga+Tomat",
                source = "Klik Pertanian",
                publishedAt = "2024-01-15T00:00:00Z"
            ),
            News(
                id = 2,
                title = "Tren dan Inovasi Budidaya Tomat di Indonesia 2025",
                description = "Teknologi dan peran pemerintah dorong produktivitas tomat nasional.",
                imageUrl = "https://placehold.co/600x400/22c55e/FFFFFF?text=Inovasi",
                source = "Estuary.co.id",
                publishedAt = "2024-01-12T00:00:00Z"
            ),
            News(
                id = 3,
                title = "Indonesia's Cherry Tomato Revolution",
                description = "Penelitian kustumisasi sistem tanam dan irigasi pada varietas tomat ceri.",
                imageUrl = "https://placehold.co/600x400/eab308/FFFFFF?text=Cherry+Tomat",
                source = "Agritech Insights",
                publishedAt = "2024-01-10T00:00:00Z"
            ),
            News(
                id = 11,
                title = "Cara Mengendalikan Penyakit Bercak Daun pada Tanaman Tomat",
                description = "Penyakit bercak daun merupakan salah satu penyakit yang sering menyerang tanaman tomat. Penyakit ini disebabkan oleh jamur Cercospora capsici.",
                imageUrl = "https://placehold.co/600x400/a16207/FFFFFF?text=Bercak+Daun",
                source = "Dinas Pertanian Buleleng",
                publishedAt = "2024-01-08T00:00:00Z"
            ),
            News(
                id = 5,
                title = "Penelitian Terbaru: Identifikasi Penyakit Tomat dengan Machine Learning",
                description = "Tim peneliti dari universitas terkemuka berhasil mengembangkan sistem identifikasi penyakit tomat dengan tingkat akurasi 95%.",
                imageUrl = "https://placehold.co/600x400/8b5cf6/FFFFFF?text=ML+Research",
                source = "Tech Agriculture",
                publishedAt = "2024-01-05T00:00:00Z"
            )
        )
    }

    private fun getMockNewsDetail(id: Int): News {
        return when (id) {
            1 -> News(
                id = 1,
                title = "Kembali Meroket! Harga Tomat Capai Rp 20‑30 rb/kg",
                description = "Distribusi terganggu dan cuaca menyebabkan harga tomat melonjak tajam.",
                content = "Harga tomat di berbagai daerah kembali mengalami kenaikan drastis mencapai Rp 20.000-30.000 per kilogram. Kenaikan ini dipicu oleh gangguan distribusi dan kondisi cuaca yang tidak menentu yang mempengaruhi produksi di sentra-sentra pertanian tomat. Petani menghadapi tantangan besar dalam mempertahankan kualitas dan kuantitas hasil panen.\n\nFaktor cuaca seperti hujan berlebihan dan kekeringan berkepanjangan menjadi penyebab utama menurunnya produksi tomat nasional. Hal ini diperparah dengan sistem distribusi yang belum optimal, terutama dari daerah penghasil ke konsumen akhir.",
                imageUrl = "https://placehold.co/600x400/f87171/FFFFFF?text=Harga+Tomat",
                source = "Klik Pertanian",
                publishedAt = "2024-01-15T00:00:00Z",
                category = "Ekonomi Pertanian",
                url = "https://klikpertanian.com/harga-tomat-meroket"
            )
            2 -> News(
                id = 2,
                title = "Tren dan Inovasi Budidaya Tomat di Indonesia 2025",
                description = "Teknologi dan peran pemerintah dorong produktivitas tomat nasional.",
                content = "Indonesia tengah mengalami revolusi dalam budidaya tomat dengan adopsi teknologi modern. Salah satu tren terbaru adalah penggunaan teknologi hidroponik dan sistem irigasi berbasis sensor yang dapat mengatur kebutuhan air dengan presisi.\n\nPemerintah melalui Kementerian Pertanian telah menyediakan program bantuan untuk petani yang ingin mengadopsi teknologi pertanian modern. Program ini mencakup pelatihan, subsidi peralatan, dan pendampingan teknis.",
                imageUrl = "https://placehold.co/600x400/22c55e/FFFFFF?text=Inovasi",
                source = "Estuary.co.id",
                publishedAt = "2024-01-12T00:00:00Z",
                category = "Teknologi & Inovasi",
                url = "https://estuary.co.id/budidaya-tomat-indonesia-2025"
            )
            3 -> News(
                id = 3,
                title = "Indonesia's Cherry Tomato Revolution",
                description = "Penelitian kustumisasi sistem tanam dan irigasi pada varietas tomat ceri.",
                content = "Varietas tomat ceri (cherry tomato) menunjukkan potensi besar untuk dikembangkan di Indonesia. Penelitian terbaru menunjukkan bahwa tomat ceri memiliki ketahanan yang lebih baik terhadap berbagai penyakit tanaman, terutama penyakit jamur.\n\nSistem tanam tomat ceri menggunakan metode hidroponik vertikal dapat meningkatkan produktivitas hingga 300% dibandingkan metode konvensional. Hal ini sangat cocok untuk lahan terbatas di perkotaan.",
                imageUrl = "https://placehold.co/600x400/eab308/FFFFFF?text=Cherry+Tomat",
                source = "Agritech Insights",
                publishedAt = "2024-01-10T00:00:00Z",
                category = "Penelitian & Inovasi",
                url = "https://agritech-insights.com/cherry-tomato-revolution"
            )
            11 -> News(
                id = 11,
                title = "Cara Mengendalikan Penyakit Bercak Daun pada Tanaman Tomat",
                description = "Penyakit bercak daun merupakan salah satu penyakit yang sering menyerang tanaman tomat. Penyakit ini disebabkan oleh jamur Cercospora capsici.",
                content = "Penyakit bercak daun pada tomat disebabkan oleh jamur Cercospora capsici dan Alternaria solani. Gejala awal berupa bercak-bercak kecil berwarna coklat pada daun yang lambat laun membesar dan dapat menyebabkan daun menguning lalu gugur.\n\nPencegahan dapat dilakukan dengan rotasi tanaman, sanitasi kebun, dan penggunaan varietas tahan penyakit. Pengendalian dapat menggunakan fungisida organik seperti ekstrak daun mimba atau fungisida kimia sesuai anjuran.",
                imageUrl = "https://placehold.co/600x400/a16207/FFFFFF?text=Bercak+Daun",
                source = "Dinas Pertanian Buleleng",
                publishedAt = "2024-01-08T00:00:00Z",
                category = "Pengendalian Hama & Penyakit",
                url = "https://pertanian.bulelengkab.go.id/bercak-daun-tomat"
            )
            5 -> News(
                id = 5,
                title = "Penelitian Terbaru: Identifikasi Penyakit Tomat dengan Machine Learning",
                description = "Tim peneliti dari universitas terkemuka berhasil mengembangkan sistem identifikasi penyakit tomat dengan tingkat akurasi 95%.",
                content = "Tim peneliti dari Institut Pertanian Bogor (IPB) berhasil mengembangkan aplikasi berbasis artificial intelligence untuk identifikasi penyakit tomat dengan akurasi mencapai 95%. Sistem ini menggunakan teknologi deep learning dan computer vision.\n\nAplikasi ini dapat mengidentifikasi 10 jenis penyakit tomat hanya dengan mengambil foto daun tanaman menggunakan smartphone. Teknologi ini diharapkan dapat membantu petani melakukan diagnosa dini dan pengobatan yang tepat sasaran.",
                imageUrl = "https://placehold.co/600x400/8b5cf6/FFFFFF?text=ML+Research",
                source = "Tech Agriculture",
                publishedAt = "2024-01-05T00:00:00Z",
                category = "Teknologi & Penelitian",
                url = "https://tech-agriculture.com/ai-tomato-disease-detection"
            )
            else -> {
                val baseNews = getMockNews().find { it.id == id } ?: getMockNews().first()
                baseNews.copy(
                    content = baseNews.description + "\n\nUntuk informasi lebih lanjut tentang topik ini, silakan konsultasikan dengan ahli pertanian setempat atau kunjungi situs web resmi Kementerian Pertanian Indonesia.",
                    category = "Informasi Umum"
                )
            }
        }
    }
}
