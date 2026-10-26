package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Listing
import com.example.data.model.User
import com.example.data.repository.PhoneticSearchHelper
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseBadge
import com.example.ui.theme.RoseLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@Composable
fun ListingDetailDialog(
    listing: Listing,
    currentUser: User?,
    onDismiss: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenLightbox: (images: List<String>, index: Int) -> Unit,
    onPlayVideo: (String) -> Unit,
    onReportClick: (adCode: String) -> Unit,
    onSubmitReview: (rating: Long, comment: String) -> Unit
) {
    val context = LocalContext.current
    val images = listing.images.ifEmpty { listOf("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&auto=format&fit=crop&q=80") }
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    var reviewRating by remember { mutableLongStateOf(5L) }
    var reviewComment by remember { mutableStateOf("") }
    var showReviewInput by remember { mutableStateOf(false) }

    val adCode = "BS-" + listing.id.takeLast(6).uppercase()
    val isOwner = currentUser?.phone == listing.ownerPhone

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "আইডি: $adCode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { onReportClick(adCode) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "রিপোর্ট", tint = RoseBadge)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image Banner with Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = images.getOrNull(selectedImageIndex) ?: images[0],
                            contentDescription = listing.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onOpenLightbox(images, selectedImageIndex) }
                        )

                        // Badge: Type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                text = listing.type,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Image count indicator
                        if (images.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "${selectedImageIndex + 1}/${images.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Video Button if available
                        if (listing.video.isNotBlank()) {
                            Button(
                                onClick = { onPlayVideo(listing.video) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseBadge),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ভিডিও দেখুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Rented overlay
                        if (listing.rented) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RoseBadge.copy(alpha = 0.9f))
                                    .align(Alignment.BottomCenter)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ভাড়া হয়ে গেছে",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Thumbnail thumbnails row if multiple images
                    if (images.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(images) { index, imgUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedImageIndex = index }
                                        .background(if (selectedImageIndex == index) EmeraldPrimary else Color.Transparent)
                                        .padding(2.dp)
                                ) {
                                    AsyncImage(
                                        model = imgUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Title & Price Section
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = listing.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = listing.area,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldDark
                                    )
                                }
                            }

                            // Rent Price
                            Column(horizontalAlignment = Alignment.End) {
                                Text("মাসিক ভাড়া", fontSize = 10.sp, color = SlateTextSecondary)
                                if (listing.rentNegotiable) {
                                    Text(
                                        text = "আলোচনা সাপেক্ষে",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "৳${PhoneticSearchHelper.toBengaliNumber(listing.rent)}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                        Text("/মাস", fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                }
                            }
                        }

                        if (listing.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ঠিকানা: ${listing.address}",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Available from pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SlateTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "কবে থেকে খালি: ${listing.available}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateTextPrimary
                            )
                        }

                        // Room Specs
                        if (listing.specs.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldLight.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "রুমের বিবরণ: ${listing.specs}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EmeraldDark,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Description
                        if (listing.desc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("বিস্তারিত বিবরণ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = listing.desc,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = SlateTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.HorizontalDivider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Landlord / Owner Contact Card
                        Text("মালিকের তথ্য", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = listing.postedBy.ifBlank { "মালিক" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = listing.phone, fontSize = 12.sp, color = SlateTextSecondary)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // WhatsApp Button
                                    IconButton(
                                        onClick = {
                                            val uri = Uri.parse("https://wa.me/88${listing.phone}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldLight)
                                    ) {
                                        Text("WA", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldDark)
                                    }

                                    // Call Button
                                    IconButton(
                                        onClick = {
                                            val uri = Uri.parse("tel:${listing.phone}")
                                            context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "কল", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.HorizontalDivider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Reviews & Ratings Section
                        val reviews = listing.reviews
                        val avgRating = if (reviews.isNotEmpty()) {
                            reviews.map { it.rating }.average()
                        } else 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("রেটিং ও রিভিউ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (reviews.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "★ ${String.format("%.1f", avgRating)} (${reviews.size}টি)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent
                                    )
                                }
                            }

                            if (!isOwner) {
                                Text(
                                    text = if (showReviewInput) "বাতিল" else "+ রিভিউ দিন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.clickable { showReviewInput = !showReviewInput }
                                )
                            }
                        }

                        // Review Input Box
                        if (showReviewInput) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("আপনার রেটিং দিন:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 5 Stars selector
                                    Row {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = if (i <= reviewRating) Icons.Default.Star else Icons.Outlined.Star,
                                                contentDescription = "$i Stars",
                                                tint = if (i <= reviewRating) AmberAccent else Color.Gray,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clickable { reviewRating = i.toLong() }
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("$reviewRating স্টার", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = reviewComment,
                                        onValueChange = { reviewComment = it },
                                        placeholder = { Text("বাসাটির পরিবেশ সম্পর্কে আপনার মতামত লিখুন...", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EmeraldPrimary,
                                            unfocusedBorderColor = SlateBorder
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            onSubmitReview(reviewRating, reviewComment)
                                            reviewComment = ""
                                            showReviewInput = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("রিভিউ সাবমিট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Reviews list
                        Spacer(modifier = Modifier.height(10.dp))
                        if (reviews.isEmpty()) {
                            Text(
                                text = "এখনো কোনো রিভিউ নেই। প্রথম রিভিউটি আপনি দিন!",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            reviews.forEach { rv ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rv.userName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(rv.date, fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                            for (s in 1..rv.rating.toInt()) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Text(rv.comment, fontSize = 12.sp, color = SlateTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Sticky Bottom Bar: Direct Call, WhatsApp, Chat
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chat Button
                        Button(
                            onClick = onOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("চ্যাট করুন", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Call Button
                        Button(
                            onClick = {
                                val uri = Uri.parse("tel:${listing.phone}")
                                context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সরাসরি কল", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
