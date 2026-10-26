package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Listing(
    val id: String = "",
    val title: String = "",
    val type: String = "ফ্যামিলি",
    val area: String = "",
    val address: String = "",
    val rent: Long = 0L,
    val rentNegotiable: Boolean = false,
    val available: String = "",
    val phone: String = "",
    val specs: String = "",
    val desc: String = "",
    val images: List<String> = emptyList(),
    val video: String = "",
    val reviews: List<Review> = emptyList(),
    val postedBy: String = "মালিক",
    val ownerPhone: String = "",
    val createdAt: Long = 0L,
    val rented: Boolean = false,
    val rentedAt: Long = 0L
)

@IgnoreExtraProperties
data class Review(
    val id: Long = 0L,
    val userName: String = "",
    val userPhone: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val date: String = ""
)

@IgnoreExtraProperties
data class User(
    val phone: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "user"
)

@IgnoreExtraProperties
data class MarketItem(
    val id: String = "",
    val title: String = "",
    val category: String = "ফার্নিচার",
    val condition: String = "ভালো",
    val area: String = "",
    val address: String = "",
    val usage: String = "",
    val price: Long = 0L,
    val negotiable: Boolean = false,
    val phone: String = "",
    val desc: String = "",
    val images: List<String> = emptyList(),
    val status: String = "available", // "available" or "sold"
    val soldAt: Long = 0L,
    val owner: String = "",
    val ownerPhone: String = "",
    val createdAt: Long = 0L
)

@IgnoreExtraProperties
data class RentRequest(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val areas: List<String> = emptyList(),
    val area: String = "",
    val type: String = "",
    val budget: Long = 0L,
    val needFrom: String = "",
    val members: String = "",
    val desc: String = "",
    val status: String = "open", // "open" or "done"
    val doneAt: Long = 0L,
    val ownerPhone: String = "",
    val createdAt: Long = 0L
)

@IgnoreExtraProperties
data class ChatMessage(
    val id: Long = 0L,
    val roomKey: String = "",
    val listingId: String = "",
    val listingTitle: String = "",
    val senderPhone: String = "",
    val senderName: String = "",
    val recipientPhone: String = "",
    val text: String = "",
    val type: String = "text", // "text" or "voice"
    val audioData: String = "",
    val duration: String = "00:05",
    val time: String = ""
)

@IgnoreExtraProperties
data class Report(
    val id: Long = 0L,
    val type: String = "ভুয়া বিজ্ঞাপন",
    val message: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val reporterPhone: String = "",
    val status: String = "new",
    val createdAt: Long = 0L,
    val date: String = ""
)
