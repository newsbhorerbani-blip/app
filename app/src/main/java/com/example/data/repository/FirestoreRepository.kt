package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.Listing
import com.example.data.model.MarketItem
import com.example.data.model.RentRequest
import com.example.data.model.Report
import com.example.data.model.Review
import com.example.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(context: Context) {

    private val db: FirebaseFirestore

    init {
        // Ensure FirebaseApp is initialized with the to-let-gopalganj project options
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyCXsm7jMaWdYIe-8Ya2IGvWMmICUNRL9Ms")
                .setApplicationId("1:475633532628:android:c5235bc01cf79779f0c541")
                .setProjectId("to-let-gopalganj")
                .setStorageBucket("to-let-gopalganj.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(context, options)
        }
        db = Firebase.firestore
    }

    // ---------------- LISTINGS ----------------
    fun observeListings(): Flow<List<Listing>> = callbackFlow {
        val listener = db.collection("listings").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreRepo", "Listings listener error", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val reviewsList = (doc.get("reviews") as? List<*>)?.mapNotNull { item ->
                            if (item is Map<*, *>) {
                                Review(
                                    id = (item["id"] as? Number)?.toLong() ?: 0L,
                                    userName = item["userName"] as? String ?: "",
                                    userPhone = item["userPhone"] as? String ?: "",
                                    rating = (item["rating"] as? Number)?.toLong() ?: 5L,
                                    comment = item["comment"] as? String ?: "",
                                    date = item["date"] as? String ?: ""
                                )
                            } else null
                        } ?: emptyList()

                        Listing(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            type = doc.getString("type") ?: "ফ্যামিলি",
                            area = doc.getString("area") ?: "",
                            address = doc.getString("address") ?: "",
                            rent = doc.getLong("rent") ?: 0L,
                            rentNegotiable = doc.getBoolean("rentNegotiable") ?: false,
                            available = doc.getString("available") ?: "",
                            phone = doc.getString("phone") ?: "",
                            specs = doc.getString("specs") ?: "",
                            desc = doc.getString("desc") ?: "",
                            images = (doc.get("images") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            video = doc.getString("video") ?: "",
                            reviews = reviewsList,
                            postedBy = doc.getString("postedBy") ?: "মালিক",
                            ownerPhone = doc.getString("ownerPhone") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            rented = doc.getBoolean("rented") ?: false,
                            rentedAt = doc.getLong("rentedAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e("FirestoreRepo", "Error parsing listing ${doc.id}", e)
                        null
                    }
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveListing(listing: Listing): Result<Unit> = runCatching {
        val docId = if (listing.id.isNotBlank()) listing.id else System.currentTimeMillis().toString()
        val data = hashMapOf(
            "title" to listing.title,
            "type" to listing.type,
            "area" to listing.area,
            "address" to listing.address,
            "rent" to listing.rent,
            "rentNegotiable" to listing.rentNegotiable,
            "available" to listing.available,
            "phone" to listing.phone,
            "specs" to listing.specs,
            "desc" to listing.desc,
            "images" to listing.images,
            "video" to listing.video,
            "postedBy" to listing.postedBy,
            "ownerPhone" to listing.ownerPhone,
            "createdAt" to if (listing.createdAt > 0L) listing.createdAt else System.currentTimeMillis(),
            "rented" to listing.rented,
            "rentedAt" to listing.rentedAt
        )
        db.collection("listings").document(docId).set(data).await()
    }

    suspend fun deleteListing(id: String): Result<Unit> = runCatching {
        db.collection("listings").document(id).delete().await()
    }

    suspend fun toggleRented(id: String, rented: Boolean): Result<Unit> = runCatching {
        val patch = hashMapOf<String, Any>(
            "rented" to rented,
            "rentedAt" to if (rented) System.currentTimeMillis() else 0L
        )
        db.collection("listings").document(id).update(patch).await()
    }

    suspend fun addReview(listingId: String, review: Review): Result<Unit> = runCatching {
        val reviewMap = hashMapOf(
            "id" to review.id,
            "userName" to review.userName,
            "userPhone" to review.userPhone,
            "rating" to review.rating,
            "comment" to review.comment,
            "date" to review.date
        )
        db.collection("listings").document(listingId)
            .update("reviews", FieldValue.arrayUnion(reviewMap)).await()
    }

    // ---------------- USERS ----------------
    suspend fun loginUser(phoneOrEmail: String, passwordInput: String): User? {
        val cleanInput = phoneOrEmail.trim().lowercase()
        val usersSnap = db.collection("users").get().await()
        for (doc in usersSnap.documents) {
            val phone = doc.getString("phone") ?: ""
            val email = (doc.getString("email") ?: "").lowercase()
            val pass = doc.getString("password") ?: ""
            val name = doc.getString("name") ?: ""
            val role = doc.getString("role") ?: "user"

            if ((phone == cleanInput || email == cleanInput) && pass == passwordInput) {
                return User(phone, name, email, pass, role)
            }
        }
        return null
    }

    suspend fun registerUser(user: User): Result<Unit> = runCatching {
        val docRef = db.collection("users").document("user_${user.phone}")
        val existing = docRef.get().await()
        if (existing.exists()) {
            throw Exception("মোবাইল নম্বরটি আগেই ব্যবহৃত হয়েছে!")
        }
        val data = hashMapOf(
            "name" to user.name,
            "phone" to user.phone,
            "email" to user.email,
            "password" to user.password,
            "role" to "user"
        )
        docRef.set(data).await()
    }

    suspend fun updatePassword(phone: String, newPass: String): Result<Unit> = runCatching {
        db.collection("users").document("user_$phone")
            .update("password", newPass).await()
    }

    suspend fun findUserByEmail(email: String): User? {
        val target = email.trim().lowercase()
        val snap = db.collection("users").get().await()
        for (doc in snap.documents) {
            if ((doc.getString("email") ?: "").lowercase() == target) {
                return User(
                    phone = doc.getString("phone") ?: "",
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    password = doc.getString("password") ?: "",
                    role = doc.getString("role") ?: "user"
                )
            }
        }
        return null
    }

    // ---------------- CHATS ----------------
    fun observeChats(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("chats").orderBy("id").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreRepo", "Chats listener error", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L),
                            roomKey = doc.getString("roomKey") ?: "",
                            listingId = doc.getString("listingId") ?: "",
                            listingTitle = doc.getString("listingTitle") ?: "",
                            senderPhone = doc.getString("senderPhone") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            recipientPhone = doc.getString("recipientPhone") ?: "",
                            text = doc.getString("text") ?: "",
                            type = doc.getString("type") ?: "text",
                            audioData = doc.getString("audioData") ?: "",
                            duration = doc.getString("duration") ?: "00:05",
                            time = doc.getString("time") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(msg: ChatMessage): Result<Unit> = runCatching {
        val docId = msg.id.toString()
        val data = hashMapOf(
            "id" to msg.id,
            "roomKey" to msg.roomKey,
            "listingId" to msg.listingId,
            "listingTitle" to msg.listingTitle,
            "senderPhone" to msg.senderPhone,
            "senderName" to msg.senderName,
            "recipientPhone" to msg.recipientPhone,
            "text" to msg.text,
            "type" to msg.type,
            "audioData" to msg.audioData,
            "duration" to msg.duration,
            "time" to msg.time
        )
        db.collection("chats").document(docId).set(data).await()
    }

    // ---------------- MARKET ITEMS ----------------
    fun observeMarketItems(): Flow<List<MarketItem>> = callbackFlow {
        val listener = db.collection("market_items").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreRepo", "MarketItems error", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        MarketItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            category = doc.getString("category") ?: "ফার্নিচার",
                            condition = doc.getString("condition") ?: "ভালো",
                            area = doc.getString("area") ?: "",
                            address = doc.getString("address") ?: "",
                            usage = doc.getString("usage") ?: "",
                            price = doc.getLong("price") ?: 0L,
                            negotiable = doc.getBoolean("negotiable") ?: false,
                            phone = doc.getString("phone") ?: "",
                            desc = doc.getString("desc") ?: "",
                            images = (doc.get("images") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            status = doc.getString("status") ?: "available",
                            soldAt = doc.getLong("soldAt") ?: 0L,
                            owner = doc.getString("owner") ?: "",
                            ownerPhone = doc.getString("ownerPhone") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveMarketItem(item: MarketItem): Result<Unit> = runCatching {
        val docId = if (item.id.isNotBlank()) item.id else System.currentTimeMillis().toString()
        val data = hashMapOf(
            "title" to item.title,
            "category" to item.category,
            "condition" to item.condition,
            "area" to item.area,
            "address" to item.address,
            "usage" to item.usage,
            "price" to item.price,
            "negotiable" to item.negotiable,
            "phone" to item.phone,
            "desc" to item.desc,
            "images" to item.images,
            "status" to item.status,
            "soldAt" to item.soldAt,
            "owner" to item.owner,
            "ownerPhone" to item.ownerPhone,
            "createdAt" to if (item.createdAt > 0L) item.createdAt else System.currentTimeMillis()
        )
        db.collection("market_items").document(docId).set(data).await()
    }

    suspend fun deleteMarketItem(id: String): Result<Unit> = runCatching {
        db.collection("market_items").document(id).delete().await()
    }

    suspend fun toggleMarketSold(id: String, sold: Boolean): Result<Unit> = runCatching {
        val patch = hashMapOf<String, Any>(
            "status" to if (sold) "sold" else "available",
            "soldAt" to if (sold) System.currentTimeMillis() else 0L
        )
        db.collection("market_items").document(id).update(patch).await()
    }

    // ---------------- RENT REQUESTS (বাসা চাই) ----------------
    fun observeRentRequests(): Flow<List<RentRequest>> = callbackFlow {
        val listener = db.collection("rent_requests").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreRepo", "RentRequests error", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val areasList = (doc.get("areas") as? List<*>)?.filterIsInstance<String>()
                            ?: doc.getString("area")?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
                            ?: emptyList()

                        RentRequest(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            areas = areasList,
                            area = doc.getString("area") ?: areasList.joinToString(", "),
                            type = doc.getString("type") ?: "",
                            budget = doc.getLong("budget") ?: 0L,
                            needFrom = doc.getString("needFrom") ?: "",
                            members = doc.getString("members") ?: "",
                            desc = doc.getString("desc") ?: "",
                            status = doc.getString("status") ?: "open",
                            doneAt = doc.getLong("doneAt") ?: 0L,
                            ownerPhone = doc.getString("ownerPhone") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveRentRequest(req: RentRequest): Result<Unit> = runCatching {
        val docId = if (req.id.isNotBlank()) req.id else System.currentTimeMillis().toString()
        val data = hashMapOf(
            "name" to req.name,
            "phone" to req.phone,
            "areas" to req.areas,
            "area" to req.area,
            "type" to req.type,
            "budget" to req.budget,
            "needFrom" to req.needFrom,
            "members" to req.members,
            "desc" to req.desc,
            "status" to req.status,
            "doneAt" to req.doneAt,
            "ownerPhone" to req.ownerPhone,
            "createdAt" to if (req.createdAt > 0L) req.createdAt else System.currentTimeMillis()
        )
        db.collection("rent_requests").document(docId).set(data).await()
    }

    suspend fun deleteRentRequest(id: String): Result<Unit> = runCatching {
        db.collection("rent_requests").document(id).delete().await()
    }

    suspend fun toggleRequestDone(id: String, done: Boolean): Result<Unit> = runCatching {
        val patch = hashMapOf<String, Any>(
            "status" to if (done) "done" else "open",
            "doneAt" to if (done) System.currentTimeMillis() else 0L
        )
        db.collection("rent_requests").document(id).update(patch).await()
    }

    // ---------------- REPORTS ----------------
    suspend fun submitReport(report: Report): Result<Unit> = runCatching {
        val docId = report.id.toString()
        val data = hashMapOf(
            "id" to report.id,
            "type" to report.type,
            "message" to report.message,
            "name" to report.name,
            "phone" to report.phone,
            "email" to report.email,
            "reporterPhone" to report.reporterPhone,
            "status" to report.status,
            "createdAt" to report.createdAt,
            "date" to report.date
        )
        db.collection("reports").document(docId).set(data).await()
    }

    // ---------------- LOCATIONS & SITE SETTINGS ----------------
    fun observeLocations(): Flow<List<String>> = callbackFlow {
        val defaultLocations = listOf(
            "নবীনবাগ", "থানাপাড়া", "বেদগ্রাম", "কোর্ট মসজিদ রোড",
            "ঘোনাপাড়া (BSMRSTU এলাকা)", "মিয়াপাড়া", "হরিদাসপুর"
        )
        trySend(defaultLocations)

        val listener = db.collection("site_settings").document("locations")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val list = (doc.get("list") as? List<*>)?.filterIsInstance<String>()
                    if (!list.isNullOrEmpty()) {
                        trySend(list)
                    }
                }
            }
        awaitClose { listener.remove() }
    }
}
