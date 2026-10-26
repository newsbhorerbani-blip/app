package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Listing
import com.example.data.model.MarketItem
import com.example.data.model.RentRequest
import com.example.data.repository.PhoneticSearchHelper
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseBadge
import com.example.ui.theme.RoseLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLoginClick: () -> Unit,
    onEditListing: (Listing) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allListings by viewModel.filteredListings.collectAsState()
    val allMarketItems by viewModel.filteredMarketItems.collectAsState()
    val allRentRequests by viewModel.filteredRentRequests.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: My Ads, 1: Password

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "প্রোফাইল দেখতে লগইন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "আপনার নিজের প্রকাশিত বিজ্ঞাপন দেখতে, আপডেট করতে এবং পাসওয়ার্ড পরিবর্তন করতে লগইন করুন।",
                        fontSize = 12.sp,
                        color = SlateTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onLoginClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("লগইন / নতুন অ্যাকাউন্ট", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val user = currentUser!!
    val myListings = allListings.filter { it.ownerPhone == user.phone }
    val myMarketItems = allMarketItems.filter { it.ownerPhone == user.phone }
    val myRentRequests = allRentRequests.filter { it.ownerPhone == user.phone }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // User Info Header Card
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).ifBlank { "U" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text("মোবাইল: ${user.phone}", fontSize = 12.sp, color = EmeraldLight)
                    if (user.email.isNotBlank()) {
                        Text("ইমেইল: ${user.email}", fontSize = 11.sp, color = EmeraldLight.copy(0.9f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("লগআউট", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sub Tabs: My Ads / Change Password
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = EmeraldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = EmeraldPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আমার বিজ্ঞাপন (${myListings.size + myMarketItems.size + myRentRequests.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("পাসওয়ার্ড পরিবর্তন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // MY ADS TAB
            if (myListings.isEmpty() && myMarketItems.isEmpty() && myRentRequests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("আপনার কোনো সক্রিয় বিজ্ঞাপন নেই।", fontSize = 13.sp, color = SlateTextSecondary)
                    }
                }
            } else {
                if (myListings.isNotEmpty()) {
                    item {
                        Text("বাসা বিজ্ঞাপনসমূহ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary, modifier = Modifier.padding(16.dp))
                    }
                    items(myListings) { item ->
                        MyListingRow(
                            listing = item,
                            onToggleRented = { viewModel.toggleRented(item) },
                            onEdit = { onEditListing(item) },
                            onDelete = { viewModel.deleteListing(item) }
                        )
                    }
                }

                if (myMarketItems.isNotEmpty()) {
                    item {
                        Text("পুরনো জিনিসের বিজ্ঞাপন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary, modifier = Modifier.padding(16.dp))
                    }
                    items(myMarketItems) { item ->
                        MyMarketRow(
                            item = item,
                            onToggleSold = { viewModel.toggleMarketSold(item) },
                            onDelete = { viewModel.deleteMarketItem(item) }
                        )
                    }
                }

                if (myRentRequests.isNotEmpty()) {
                    item {
                        Text("বাসা ভাড়ার অনুরোধ (বাসা চাই)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary, modifier = Modifier.padding(16.dp))
                    }
                    items(myRentRequests) { req ->
                        MyRequestRow(
                            req = req,
                            onToggleDone = { viewModel.toggleRequestDone(req) },
                            onDelete = { viewModel.deleteRentRequest(req) }
                        )
                    }
                }
            }
        } else {
            // PASSWORD CHANGE TAB
            item {
                ChangePasswordForm(
                    onChangePassword = { cur, new, conf ->
                        viewModel.changePassword(cur, new, conf) {
                            selectedTab = 0
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MyListingRow(
    listing: Listing,
    onToggleRented: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val img = listing.images.firstOrNull() ?: "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&auto=format&fit=crop&q=80"
            AsyncImage(
                model = img,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(listing.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text(
                    text = "${listing.area} • ৳${PhoneticSearchHelper.toBengaliNumber(listing.rent)}",
                    fontSize = 11.sp,
                    color = EmeraldDark
                )
                if (listing.rented) {
                    Text("ভাড়া হয়ে গেছে", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoseBadge)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onToggleRented,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (listing.rented) EmeraldLight else Color(0xFFFEF3C7)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (listing.rented) "আবার খালি" else "ভাড়া হয়েছে",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (listing.rented) EmeraldDark else AmberAccent
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "এডিট", tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RoseBadge, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun MyMarketRow(
    item: MarketItem,
    onToggleSold: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text("${item.category} • ${item.area} • ৳${PhoneticSearchHelper.toBengaliNumber(item.price)}", fontSize = 11.sp, color = AmberAccent)
                if (item.status == "sold") {
                    Text("বিক্রি হয়ে গেছে", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoseBadge)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onToggleSold,
                    colors = ButtonDefaults.buttonColors(containerColor = if (item.status == "sold") EmeraldLight else Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.status == "sold") "আবার খালি" else "বিক্রি হয়েছে",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.status == "sold") EmeraldDark else AmberAccent
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RoseBadge, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun MyRequestRow(
    req: RentRequest,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${req.type} বাসা চাই", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(req.areas.joinToString(", ").ifBlank { "যেকোনো এলাকা" }, fontSize = 11.sp, color = Color(0xFF0284C7))
                if (req.status == "done") {
                    Text("বাসা পাওয়া গেছে", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onToggleDone,
                    colors = ButtonDefaults.buttonColors(containerColor = if (req.status == "done") EmeraldLight else Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (req.status == "done") "খুঁজছি" else "পেয়েছি",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (req.status == "done") EmeraldDark else AmberAccent
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RoseBadge, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChangePasswordForm(
    onChangePassword: (current: String, new: String, confirm: String) -> Unit
) {
    var curPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("পাসওয়ার্ড আপডেট করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)

            Text("বর্তমান পাসওয়ার্ড *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = curPass,
                onValueChange = { curPass = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Text("নতুন পাসওয়ার্ড * (কমপক্ষে ৪ অক্ষর)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Text("নতুন পাসওয়ার্ড নিশ্চিত করুন *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = confPass,
                onValueChange = { confPass = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onChangePassword(curPass, newPass, confPass) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("পাসওয়ার্ড সেভ করুন", fontWeight = FontWeight.Bold)
            }
        }
    }
}
