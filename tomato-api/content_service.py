from news_service import get_news_data
from tip_service import get_tips_data

def get_unified_content_data():
    """
    Menggabungkan data berita dan tips menjadi satu unified content dengan field 'type'
    """
    # Ambil data berita dan tambahkan field 'type'
    news_data = get_news_data()
    unified_news = []
    for news in news_data:
        unified_news.append({
            **news,
            "type": "berita"
        })
    
    # Ambil data tips dan tambahkan field 'type'
    tips_data = get_tips_data()
    unified_tips = []
    for tip in tips_data:
        unified_tips.append({
            **tip,
            "type": "tip"
        })
    
    # Gabungkan semua content dan urutkan berdasarkan publishedAt (terbaru dulu)
    all_content = unified_news + unified_tips
    
    # Sort berdasarkan publishedAt descending (terbaru di atas)
    try:
        all_content.sort(key=lambda x: x.get("publishedAt", ""), reverse=True)
    except:
        # Jika ada error sorting, tetap return data tanpa sorting
        pass
    
    return all_content

def filter_content_by_type(content_type=None):
    """
    Filter content berdasarkan type (berita atau tip)
    """
    all_content = get_unified_content_data()
    
    if content_type:
        valid_types = ["berita", "tip"]
        if content_type.lower() not in valid_types:
            raise ValueError(f"Type tidak valid. Pilihan: {', '.join(valid_types)}")
        
        return [content for content in all_content if content["type"] == content_type.lower()]
    
    return all_content

def filter_content_by_category(category=None):
    """
    Filter content berdasarkan category
    """
    all_content = get_unified_content_data()
    
    if category:
        return [content for content in all_content if content.get("category", "").lower() == category.lower()]
    
    return all_content

def search_content(keyword):
    """
    Search content berdasarkan keyword di title, description, atau content
    """
    all_content = get_unified_content_data()
    keyword_lower = keyword.lower()
    
    filtered_content = [
        content for content in all_content
        if keyword_lower in content.get("title", "").lower() or
           keyword_lower in content.get("description", "").lower() or
           keyword_lower in content.get("content", "").lower()
    ]
    
    return filtered_content

def get_content_by_id(content_id, content_type):
    """
    Ambil content berdasarkan ID dan type
    """
    all_content = get_unified_content_data()
    
    # Filter by type first
    type_filtered = [content for content in all_content if content["type"] == content_type]
    
    # Then find by ID
    target_content = next((content for content in type_filtered if content["id"] == content_id), None)
    return target_content

def get_content_statistics():
    """
    Ambil statistik content untuk debugging/monitoring
    """
    all_content = get_unified_content_data()
    
    # Collect unique categories
    categories = set()
    for content in all_content:
        if content.get("category"):
            categories.add(content["category"])
    
    stats = {
        "total_content": len(all_content),
        "berita_count": len([c for c in all_content if c["type"] == "berita"]),
        "tip_count": len([c for c in all_content if c["type"] == "tip"]),
        "categories": sorted(list(categories))
    }
    
    return stats

def get_content_with_filters(content_type=None, category=None, search=None):
    """
    Filter content dengan kombinasi multiple filters
    """
    all_content = get_unified_content_data()
    
    # Filter by type
    if content_type:
        valid_types = ["berita", "tip"]
        if content_type.lower() not in valid_types:
            raise ValueError(f"Type tidak valid. Pilihan: {', '.join(valid_types)}")
        all_content = [content for content in all_content if content["type"] == content_type.lower()]
    
    # Filter by category
    if category:
        all_content = [content for content in all_content if content.get("category", "").lower() == category.lower()]
    
    # Filter by search keyword
    if search:
        search_lower = search.lower()
        all_content = [
            content for content in all_content
            if search_lower in content.get("title", "").lower() or
               search_lower in content.get("description", "").lower() or
               search_lower in content.get("content", "").lower()
        ]
    
    return all_content
