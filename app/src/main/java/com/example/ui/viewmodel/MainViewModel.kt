package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioVoicePlayer
import com.example.audio.AudioVoiceRecorder
import com.example.data.model.ChatMessage
import com.example.data.model.Listing
import com.example.data.model.MarketItem
import com.example.data.model.RentRequest
import com.example.data.model.Report
import com.example.data.model.Review
import com.example.data.model.User
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.ImgBBUploader
import com.example.data.repository.PhoneticSearchHelper
import com.example.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FirestoreRepository(application)
    val sessionManager = UserSessionManager(application)
    val voiceRecorder = AudioVoiceRecorder(application)
    val voicePlayer = AudioVoicePlayer(application)

    // Current logged in user
    val currentUser: StateFlow<User?> = sessionManager.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Bottom Navigation Tab (0: Home, 1: Market, 2: Requests, 3: Inbox, 4: Profile)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // Locations list
    val locations: StateFlow<List<String>> = repository.observeLocations()
        .stateIn(
            viewModelScope, SharingStarted.Eagerly,
            listOf("নবীনবাগ", "থানাপাড়া", "বেদগ্রাম", "কোর্ট মসজিদ রোড", "ঘোনাপাড়া (BSMRSTU এলাকা)", "মিয়াপাড়া", "হরিদাসপুর")
        )

    // ---------------- HOME / LISTINGS ----------------
    private val _allListings = MutableStateFlow<List<Listing>>(emptyList())
    val searchQuery = MutableStateFlow("")
    val selectedArea = MutableStateFlow("all")
    val selectedRentalType = MutableStateFlow("all")

    val filteredListings: StateFlow<List<Listing>> = combine(
        _allListings,
        searchQuery,
        selectedArea,
        selectedRentalType
    ) { listings, query, area, type ->
        listings.filter { item ->
            val matchQuery = PhoneticSearchHelper.matches(
                query,
                item.title, item.desc, item.area, item.address, item.type, item.specs
            )
            val matchArea = area == "all" || item.area == area
            val matchType = type == "all" || item.type.split(",").map { it.trim() }.contains(type)
            matchQuery && matchArea && matchType
        }.sortedWith(
            compareBy<Listing> { it.rented }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- MARKET ITEMS ----------------
    private val _allMarketItems = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketSearchQuery = MutableStateFlow("")
    val selectedMarketCategory = MutableStateFlow("all")
    val selectedMarketArea = MutableStateFlow("all")

    val filteredMarketItems: StateFlow<List<MarketItem>> = combine(
        _allMarketItems,
        marketSearchQuery,
        selectedMarketCategory,
        selectedMarketArea
    ) { items, query, cat, area ->
        items.filter { item ->
            val matchQuery = PhoneticSearchHelper.matches(
                query,
                item.title, item.desc, item.area, item.address, item.category
            )
            val matchCat = cat == "all" || item.category == cat
            val matchArea = area == "all" || item.area == area
            matchQuery && matchCat && matchArea
        }.sortedWith(
            compareBy<MarketItem> { it.status == "sold" }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- RENT REQUESTS (বাসা চাই) ----------------
    private val _allRentRequests = MutableStateFlow<List<RentRequest>>(emptyList())
    val requestSearchQuery = MutableStateFlow("")
    val selectedRequestArea = MutableStateFlow("all")
    val selectedRequestType = MutableStateFlow("all")

    val filteredRentRequests: StateFlow<List<RentRequest>> = combine(
        _allRentRequests,
        requestSearchQuery,
        selectedRequestArea,
        selectedRequestType
    ) { reqs, query, area, type ->
        reqs.filter { req ->
            val matchQuery = PhoneticSearchHelper.matches(
                query,
                req.name, req.desc, req.areas.joinToString(" "), req.area, req.type
            )
            val matchArea = area == "all" || req.areas.isEmpty() || req.areas.contains(area)
            val matchType = type == "all" || req.type.isBlank() || req.type.split(",").map { it.trim() }.contains(type)
            matchQuery && matchArea && matchType
        }.sortedWith(
            compareBy<RentRequest> { it.status == "done" }
                .thenByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- CHATS & INBOX ----------------
    val allChats: StateFlow<List<ChatMessage>> = repository.observeChats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeChatRoomKey = MutableStateFlow<String?>(null)
    val activeChatListingId = MutableStateFlow<String>("")
    val activeChatListingTitle = MutableStateFlow<String>("")
    val activeChatPartnerName = MutableStateFlow<String>("")
    val activeChatPartnerPhone = MutableStateFlow<String>("")

    // Active room messages
    val activeChatMessages: StateFlow<List<ChatMessage>> = combine(
        allChats,
        activeChatRoomKey
    ) { chats, roomKey ->
        if (roomKey == null) emptyList()
        else chats.filter { it.roomKey == roomKey }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback
    val toastMessage = MutableStateFlow<String?>(null)

    init {
        // Observe Listings
        viewModelScope.launch {
            repository.observeListings().collect {
                _allListings.value = it
            }
        }
        // Observe Market Items
        viewModelScope.launch {
            repository.observeMarketItems().collect {
                _allMarketItems.value = it
            }
        }
        // Observe Rent Requests
        viewModelScope.launch {
            repository.observeRentRequests().collect {
                _allRentRequests.value = it
            }
        }
    }

    // ---------------- AUTHENTICATION ACTIONS ----------------
    fun login(phoneOrEmail: String, pass: String, rememberMe: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = repository.loginUser(phoneOrEmail, pass)
            if (user != null) {
                // Admin roles are treated as standard user in mobile app
                val safeUser = if (user.role == "admin") user.copy(role = "user") else user
                sessionManager.saveUser(safeUser, rememberMe)
                toastMessage.value = "স্বাগতম, ${safeUser.name}!"
                onSuccess()
            } else {
                toastMessage.value = "ভুল মোবাইল/ইমেইল অথবা পাসওয়ার্ড!"
            }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = User(
                name = name.trim(),
                email = email.trim().lowercase(),
                phone = phone.trim(),
                password = pass.trim(),
                role = "user"
            )
            val result = repository.registerUser(user)
            result.onSuccess {
                sessionManager.saveUser(user, true)
                toastMessage.value = "অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে!"
                onSuccess()
            }.onFailure {
                toastMessage.value = it.localizedMessage ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearUser()
            voicePlayer.stop()
            activeChatRoomKey.value = null
            toastMessage.value = "লগআউট সম্পন্ন হয়েছে"
        }
    }

    fun changePassword(currentPass: String, newPass: String, confirmPass: String, onSuccess: () -> Unit) {
        val user = currentUser.value ?: return
        if (user.password != currentPass) {
            toastMessage.value = "বর্তমান পাসওয়ার্ডটি সঠিক নয়!"
            return
        }
        if (newPass.length < 4) {
            toastMessage.value = "নতুন পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে!"
            return
        }
        if (newPass != confirmPass) {
            toastMessage.value = "নতুন পাসওয়ার্ড এবং কনফার্ম পাসওয়ার্ড মিলছে না!"
            return
        }

        viewModelScope.launch {
            val res = repository.updatePassword(user.phone, newPass)
            res.onSuccess {
                sessionManager.saveUser(user.copy(password = newPass))
                toastMessage.value = "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!"
                onSuccess()
            }.onFailure {
                toastMessage.value = "পাসওয়ার্ড পরিবর্তন ব্যর্থ হয়েছে!"
            }
        }
    }

    // ---------------- LISTINGS ACTIONS ----------------
    fun saveListing(
        id: String,
        title: String,
        type: String,
        area: String,
        address: String,
        rent: Long,
        rentNegotiable: Boolean,
        available: String,
        phone: String,
        specs: String,
        desc: String,
        videoUrl: String,
        imageUris: List<Uri>,
        existingImages: List<String>,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val uploadedUrls = mutableListOf<String>()
            uploadedUrls.addAll(existingImages)

            for (uri in imageUris) {
                try {
                    val url = ImgBBUploader.uploadImageUri(getApplication(), uri)
                    uploadedUrls.add(url)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed uploading image", e)
                }
            }

            if (uploadedUrls.isEmpty()) {
                uploadedUrls.add("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&auto=format&fit=crop&q=80")
            }

            val listing = Listing(
                id = id,
                title = title.trim(),
                type = type,
                area = area,
                address = address.trim(),
                rent = if (rentNegotiable) 0L else rent,
                rentNegotiable = rentNegotiable,
                available = available.trim(),
                phone = phone.trim(),
                specs = specs.trim(),
                desc = desc.trim(),
                images = uploadedUrls,
                video = videoUrl.trim(),
                postedBy = user.name,
                ownerPhone = user.phone,
                createdAt = if (id.isBlank()) System.currentTimeMillis() else 0L
            )

            val result = repository.saveListing(listing)
            result.onSuccess {
                toastMessage.value = "বিজ্ঞাপন সফলভাবে প্রকাশ করা হয়েছে!"
                onSuccess()
            }.onFailure {
                toastMessage.value = "সংরক্ষণ ব্যর্থ হয়েছে!"
            }
        }
    }

    fun deleteListing(listing: Listing) {
        val user = currentUser.value ?: return
        if (listing.ownerPhone != user.phone) return
        viewModelScope.launch {
            repository.deleteListing(listing.id)
            toastMessage.value = "বিজ্ঞাপন মুছে ফেলা হয়েছে!"
        }
    }

    fun toggleRented(listing: Listing) {
        val user = currentUser.value ?: return
        if (listing.ownerPhone != user.phone) return
        viewModelScope.launch {
            repository.toggleRented(listing.id, !listing.rented)
            toastMessage.value = if (!listing.rented) "বাসাটি 'ভাড়া হয়ে গেছে' হিসেবে মার্ক করা হয়েছে" else "বিজ্ঞাপন আবার সক্রিয় করা হয়েছে"
        }
    }

    fun submitReview(listingId: String, rating: Long, comment: String, onSuccess: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val review = Review(
                id = System.currentTimeMillis(),
                userName = user.name,
                userPhone = user.phone,
                rating = rating,
                comment = comment.trim().ifBlank { "কোনো লিখিত মন্তব্য নেই।" },
                date = dateFormat.format(Date())
            )
            repository.addReview(listingId, review)
            toastMessage.value = "আপনার রিভিউ সফলভাবে যুক্ত হয়েছে!"
            onSuccess()
        }
    }

    // ---------------- MARKET ACTIONS ----------------
    fun saveMarketItem(
        id: String,
        title: String,
        category: String,
        condition: String,
        area: String,
        address: String,
        usage: String,
        price: Long,
        negotiable: Boolean,
        phone: String,
        desc: String,
        imageUris: List<Uri>,
        existingImages: List<String>,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val uploaded = mutableListOf<String>()
            uploaded.addAll(existingImages)
            for (uri in imageUris) {
                try {
                    uploaded.add(ImgBBUploader.uploadImageUri(getApplication(), uri))
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Market image upload error", e)
                }
            }

            val item = MarketItem(
                id = id,
                title = title.trim(),
                category = category,
                condition = condition,
                area = area,
                address = address.trim(),
                usage = usage.trim(),
                price = if (negotiable) 0L else price,
                negotiable = negotiable,
                phone = phone.trim(),
                desc = desc.trim(),
                images = uploaded,
                owner = user.name,
                ownerPhone = user.phone,
                createdAt = if (id.isBlank()) System.currentTimeMillis() else 0L
            )

            repository.saveMarketItem(item)
            toastMessage.value = "পুরনো জিনিসের বিজ্ঞাপন সফলভাবে প্রকাশ করা হয়েছে!"
            onSuccess()
        }
    }

    fun toggleMarketSold(item: MarketItem) {
        val user = currentUser.value ?: return
        if (item.ownerPhone != user.phone) return
        viewModelScope.launch {
            repository.toggleMarketSold(item.id, item.status != "sold")
            toastMessage.value = if (item.status != "sold") "জিনিসটি 'বিক্রি হয়ে গেছে' হিসেবে মার্ক করা হয়েছে" else "বিজ্ঞাপন সক্রিয় করা হয়েছে"
        }
    }

    fun deleteMarketItem(item: MarketItem) {
        val user = currentUser.value ?: return
        if (item.ownerPhone != user.phone) return
        viewModelScope.launch {
            repository.deleteMarketItem(item.id)
            toastMessage.value = "বিজ্ঞাপন মুছে ফেলা হয়েছে!"
        }
    }

    // ---------------- RENT REQUEST ACTIONS ----------------
    fun saveRentRequest(
        id: String,
        name: String,
        phone: String,
        areas: List<String>,
        type: String,
        budget: Long,
        needFrom: String,
        members: String,
        desc: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val req = RentRequest(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                areas = areas,
                area = areas.joinToString(", "),
                type = type.trim(),
                budget = budget,
                needFrom = needFrom.trim(),
                members = members.trim(),
                desc = desc.trim(),
                ownerPhone = user.phone,
                createdAt = if (id.isBlank()) System.currentTimeMillis() else 0L
            )
            repository.saveRentRequest(req)
            toastMessage.value = "বাসা ভাড়ার অনুরোধ সফলভাবে পাঠানো হয়েছে!"
            onSuccess()
        }
    }

    fun toggleRequestDone(req: RentRequest) {
        val user = currentUser.value ?: return
        if (req.ownerPhone != user.phone) return
        viewModelScope.launch {
            repository.toggleRequestDone(req.id, req.status != "done")
            toastMessage.value = if (req.status != "done") "অনুরোধটি 'বাসা পেয়েছি' হিসেবে মার্ক করা হয়েছে" else "অনুরোধ আবার সক্রিয় করা হয়েছে"
        }
    }

    fun deleteRentRequest(req: RentRequest) {
        val user = currentUser.value ?: return
        if (req.ownerPhone != user.phone) return
        viewModelScope.launch {
            repository.deleteRentRequest(req.id)
            toastMessage.value = "অনুরোধ মুছে ফেলা হয়েছে!"
        }
    }

    // ---------------- CHAT ACTIONS ----------------
    fun openChat(listing: Listing, recipientPhone: String, partnerName: String) {
        val user = currentUser.value ?: return
        if (user.phone == recipientPhone) {
            toastMessage.value = "এটি আপনার নিজের বিজ্ঞাপন!"
            return
        }
        val sortedUsers = listOf(user.phone, recipientPhone).sorted()
        val roomKey = "chat_${listing.id}_${sortedUsers[0]}_${sortedUsers[1]}"

        activeChatRoomKey.value = roomKey
        activeChatListingId.value = listing.id
        activeChatListingTitle.value = listing.title
        activeChatPartnerName.value = partnerName
        activeChatPartnerPhone.value = recipientPhone
    }

    fun openDirectChat(roomKey: String, listingId: String, listingTitle: String, partnerName: String, recipientPhone: String) {
        activeChatRoomKey.value = roomKey
        activeChatListingId.value = listingId
        activeChatListingTitle.value = listingTitle
        activeChatPartnerName.value = partnerName
        activeChatPartnerPhone.value = recipientPhone
    }

    fun closeChat() {
        activeChatRoomKey.value = null
        voicePlayer.stop()
        voiceRecorder.stopRecording(discard = true)
    }

    fun sendTextMessage(text: String) {
        val user = currentUser.value ?: return
        val room = activeChatRoomKey.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val msg = ChatMessage(
                id = System.currentTimeMillis(),
                roomKey = room,
                listingId = activeChatListingId.value,
                listingTitle = activeChatListingTitle.value,
                senderPhone = user.phone,
                senderName = user.name,
                recipientPhone = activeChatPartnerPhone.value,
                text = text.trim(),
                type = "text",
                time = timeStr
            )
            repository.sendMessage(msg)
        }
    }

    fun sendVoiceMessage(audioDataUri: String, duration: String) {
        val user = currentUser.value ?: return
        val room = activeChatRoomKey.value ?: return

        viewModelScope.launch {
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val msg = ChatMessage(
                id = System.currentTimeMillis(),
                roomKey = room,
                listingId = activeChatListingId.value,
                listingTitle = activeChatListingTitle.value,
                senderPhone = user.phone,
                senderName = user.name,
                recipientPhone = activeChatPartnerPhone.value,
                text = "🎙️ ভয়েস মেসেজ",
                type = "voice",
                audioData = audioDataUri,
                duration = duration,
                time = timeStr
            )
            repository.sendMessage(msg)
        }
    }

    // ---------------- REPORTS ----------------
    fun submitReport(adId: String, type: String, message: String, name: String, phone: String, email: String, onSuccess: () -> Unit) {
        val user = currentUser.value
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
            val finalMsg = if (adId.isNotBlank()) "[বিজ্ঞাপন আইডি: $adId] $message" else message
            val report = Report(
                id = System.currentTimeMillis(),
                type = type,
                message = finalMsg,
                name = name.ifBlank { user?.name ?: "" },
                phone = phone.ifBlank { user?.phone ?: "" },
                email = email.ifBlank { user?.email ?: "" },
                reporterPhone = user?.phone ?: "",
                createdAt = System.currentTimeMillis(),
                date = dateStr
            )
            repository.submitReport(report)
            toastMessage.value = "ধন্যবাদ! আপনার রিপোর্টটি জমা দেওয়া হয়েছে।"
            onSuccess()
        }
    }
}
