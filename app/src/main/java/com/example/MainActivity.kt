package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Listing
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.MainTopAppBar
import com.example.ui.dialogs.AuthDialog
import com.example.ui.dialogs.ChatRoomDialog
import com.example.ui.dialogs.CreateEditListingDialog
import com.example.ui.dialogs.CreateEditMarketDialog
import com.example.ui.dialogs.CreateRentRequestDialog
import com.example.ui.dialogs.ImageLightboxDialog
import com.example.ui.dialogs.ListingDetailDialog
import com.example.ui.dialogs.ReportDialog
import com.example.ui.dialogs.SmartMatchDialog
import com.example.ui.dialogs.VideoPlayerDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RentRequestsScreen
import com.example.ui.theme.ToLetGopalganjTheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToLetGopalganjTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val allChats by viewModel.allChats.collectAsState()
    val activeChatRoomKey by viewModel.activeChatRoomKey.collectAsState()
    val activeChatListingTitle by viewModel.activeChatListingTitle.collectAsState()
    val activeChatPartnerName by viewModel.activeChatPartnerName.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()
    val filteredListings by viewModel.filteredListings.collectAsState()
    val filteredRequests by viewModel.filteredRentRequests.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog States
    var showAuthDialog by remember { mutableStateOf(false) }
    var selectedListingForDetail by remember { mutableStateOf<Listing?>(null) }
    var showCreateListingDialog by remember { mutableStateOf(false) }
    var editingListing by remember { mutableStateOf<Listing?>(null) }
    var showCreateMarketDialog by remember { mutableStateOf(false) }
    var showCreateRequestDialog by remember { mutableStateOf(false) }
    var reportAdCode by remember { mutableStateOf<String?>(null) }
    var lightboxData by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    var videoPlayerDataUrl by remember { mutableStateOf<String?>(null) }
    var showSmartMatchDialog by remember { mutableStateOf(false) }

    // Toast feedback observation
    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.toastMessage.value = null
        }
    }

    // Trigger Smart Match popup on app load if user has active requirements
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            val userRequests = filteredRequests.filter { it.ownerPhone == user.phone && it.status != "done" }
            val userListings = filteredListings.filter { it.ownerPhone == user.phone && !it.rented }
            if (userRequests.isNotEmpty() || userListings.isNotEmpty()) {
                showSmartMatchDialog = true
            }
        }
    }

    // Unread messages count
    val unreadCount = remember(allChats, currentUser) {
        val user = currentUser
        if (user == null) 0
        else allChats.count { it.recipientPhone == user.phone }
    }

    // Handle Back Press to pop dialogs or return to Home tab
    BackHandler(enabled = selectedTab != 0 || activeChatRoomKey != null || selectedListingForDetail != null) {
        when {
            activeChatRoomKey != null -> viewModel.closeChat()
            selectedListingForDetail != null -> selectedListingForDetail = null
            showCreateListingDialog -> {
                showCreateListingDialog = false
                editingListing = null
            }
            showCreateMarketDialog -> showCreateMarketDialog = false
            showCreateRequestDialog -> showCreateRequestDialog = false
            reportAdCode != null -> reportAdCode = null
            lightboxData != null -> lightboxData = null
            videoPlayerDataUrl != null -> videoPlayerDataUrl = null
            showAuthDialog -> showAuthDialog = false
            selectedTab != 0 -> viewModel.selectTab(0)
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                onHomeClick = { viewModel.selectTab(0) },
                onReportClick = { reportAdCode = "" },
                onCreateAdClick = {
                    if (currentUser == null) {
                        viewModel.toastMessage.value = "বিজ্ঞাপন দিতে আগে লগইন করুন"
                        showAuthDialog = true
                    } else {
                        editingListing = null
                        showCreateListingDialog = true
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavBar(
                selectedTab = selectedTab,
                unreadMessagesCount = unreadCount,
                onTabSelected = { tabIdx -> viewModel.selectTab(tabIdx) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onListingClick = { selectedListingForDetail = it },
                    onOpenChat = { listing ->
                        if (currentUser == null) {
                            viewModel.toastMessage.value = "চ্যাট করতে আগে লগইন করুন"
                            showAuthDialog = true
                        } else {
                            viewModel.openChat(listing, listing.ownerPhone, listing.postedBy)
                        }
                    }
                )
                1 -> MarketplaceScreen(
                    viewModel = viewModel,
                    onCreateMarketItemClick = {
                        if (currentUser == null) {
                            viewModel.toastMessage.value = "বিজ্ঞাপন দিতে আগে লগইন করুন"
                            showAuthDialog = true
                        } else {
                            showCreateMarketDialog = true
                        }
                    },
                    onItemClick = { /* Can show detail or initiate WhatsApp/Call */ }
                )
                2 -> RentRequestsScreen(
                    viewModel = viewModel,
                    onCreateRequestClick = {
                        if (currentUser == null) {
                            viewModel.toastMessage.value = "অনুরোধ পাঠাতে আগে লগইন করুন"
                            showAuthDialog = true
                        } else {
                            showCreateRequestDialog = true
                        }
                    }
                )
                3 -> InboxScreen(
                    viewModel = viewModel,
                    onLoginClick = { showAuthDialog = true },
                    onOpenConversation = { roomKey, listingId, listingTitle, partnerName, partnerPhone ->
                        viewModel.openDirectChat(roomKey, listingId, listingTitle, partnerName, partnerPhone)
                    }
                )
                4 -> ProfileScreen(
                    viewModel = viewModel,
                    onLoginClick = { showAuthDialog = true },
                    onEditListing = { item ->
                        editingListing = item
                        showCreateListingDialog = true
                    }
                )
            }
        }
    }

    // ---------------- MODALS & DIALOGS ----------------

    // 1. Auth Dialog (Login & Register)
    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onLogin = { phoneOrEmail, pass, rememberMe ->
                viewModel.login(phoneOrEmail, pass, rememberMe) {
                    showAuthDialog = false
                }
            },
            onRegister = { name, email, phone, pass ->
                viewModel.register(name, email, phone, pass) {
                    showAuthDialog = false
                }
            },
            onForgotPasswordClick = {
                viewModel.toastMessage.value = "পাসওয়ার্ড পুনরুদ্ধারের জন্য সরাসরি আপনার মোবাইল নম্বরে যোগাযোগ করুন।"
            }
        )
    }

    // 2. Listing Detail Dialog
    selectedListingForDetail?.let { listing ->
        ListingDetailDialog(
            listing = listing,
            currentUser = currentUser,
            onDismiss = { selectedListingForDetail = null },
            onOpenChat = {
                selectedListingForDetail = null
                if (currentUser == null) {
                    viewModel.toastMessage.value = "চ্যাট করতে আগে লগইন করুন"
                    showAuthDialog = true
                } else {
                    viewModel.openChat(listing, listing.ownerPhone, listing.postedBy)
                }
            },
            onOpenLightbox = { images, idx ->
                lightboxData = Pair(images, idx)
            },
            onPlayVideo = { url ->
                videoPlayerDataUrl = url
            },
            onReportClick = { adCode ->
                reportAdCode = adCode
            },
            onSubmitReview = { rating, comment ->
                viewModel.submitReview(listing.id, rating, comment) {}
            }
        )
    }

    // 3. Create or Edit House Listing Dialog
    if (showCreateListingDialog) {
        CreateEditListingDialog(
            initialListing = editingListing,
            locations = locations,
            currentUser = currentUser,
            onDismiss = {
                showCreateListingDialog = false
                editingListing = null
            },
            onSave = { id, title, type, area, address, rent, rentNeg, avail, phone, specs, desc, video, newUris, existingImgs ->
                viewModel.saveListing(
                    id, title, type, area, address, rent, rentNeg, avail, phone, specs, desc, video, newUris, existingImgs
                ) {
                    showCreateListingDialog = false
                    editingListing = null
                }
            }
        )
    }

    // 4. Create Market Item Dialog
    if (showCreateMarketDialog) {
        CreateEditMarketDialog(
            locations = locations,
            currentUser = currentUser,
            onDismiss = { showCreateMarketDialog = false },
            onSave = { id, title, category, condition, area, address, usage, price, neg, phone, desc, newUris, existingImgs ->
                viewModel.saveMarketItem(
                    id, title, category, condition, area, address, usage, price, neg, phone, desc, newUris, existingImgs
                ) {
                    showCreateMarketDialog = false
                }
            }
        )
    }

    // 5. Create Rent Request Dialog ("বাসা চাই")
    if (showCreateRequestDialog) {
        CreateRentRequestDialog(
            locations = locations,
            currentUser = currentUser,
            onDismiss = { showCreateRequestDialog = false },
            onSave = { id, name, phone, areas, type, budget, needFrom, members, desc ->
                viewModel.saveRentRequest(
                    id, name, phone, areas, type, budget, needFrom, members, desc
                ) {
                    showCreateRequestDialog = false
                }
            }
        )
    }

    // 6. Report Dialog
    reportAdCode?.let { adCode ->
        ReportDialog(
            initialAdCode = adCode,
            onDismiss = { reportAdCode = null },
            onSubmit = { code, type, message, name, phone, email ->
                viewModel.submitReport(code, type, message, name, phone, email) {
                    reportAdCode = null
                }
            }
        )
    }

    // 7. Lightbox Dialog
    lightboxData?.let { (images, index) ->
        ImageLightboxDialog(
            images = images,
            initialIndex = index,
            onDismiss = { lightboxData = null }
        )
    }

    // 8. Video Player Dialog
    videoPlayerDataUrl?.let { url ->
        VideoPlayerDialog(
            videoUrl = url,
            onDismiss = { videoPlayerDataUrl = null }
        )
    }

    // 9. Smart Match Suggestions Dialog
    if (showSmartMatchDialog && currentUser != null) {
        val userPhone = currentUser!!.phone
        val matchedListings = filteredListings.filter { it.ownerPhone != userPhone && !it.rented }
        val matchedRequests = filteredRequests.filter { it.ownerPhone != userPhone && it.status != "done" }

        if (matchedListings.isNotEmpty() || matchedRequests.isNotEmpty()) {
            SmartMatchDialog(
                matchedListings = matchedListings,
                matchedRequests = matchedRequests,
                onDismiss = { showSmartMatchDialog = false },
                onListingChatClick = { listing ->
                    showSmartMatchDialog = false
                    viewModel.openChat(listing, listing.ownerPhone, listing.postedBy)
                }
            )
        }
    }

    // 10. 1-on-1 Chat Room Dialog
    activeChatRoomKey?.let { _ ->
        ChatRoomDialog(
            partnerName = activeChatPartnerName,
            listingTitle = activeChatListingTitle,
            currentUser = currentUser,
            messages = activeChatMessages,
            voiceRecorder = viewModel.voiceRecorder,
            voicePlayer = viewModel.voicePlayer,
            onSendMessage = { text -> viewModel.sendTextMessage(text) },
            onSendVoiceMessage = { audioData, duration -> viewModel.sendVoiceMessage(audioData, duration) },
            onDismiss = { viewModel.closeChat() }
        )
    }
}
