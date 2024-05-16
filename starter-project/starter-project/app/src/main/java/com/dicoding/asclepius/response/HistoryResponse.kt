package com.dicoding.asclepius.response

import com.google.gson.annotations.SerializedName

data class HistoryResponse(

	@field:SerializedName("articles")
	val articles: List<ArticlesItem?>? = null,
)

data class ArticlesItem(

	@field:SerializedName("urlToImage")
	val urlToImage: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("url")
	val url: String? = null,

	@field:SerializedName("content")
	val content: String? = null
) {
	fun isRemoved(): Boolean {
		return title == "[Removed]" || content == "[Removed]"
	}
}
