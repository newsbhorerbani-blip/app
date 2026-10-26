package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun InboxScreen(
    viewModel: MainViewModel,
    onLoginClick: () -> Unit,
    onOpenConversation: (roomKey: String, listingId: String, listingTitle: String, partnerName: String, partnerPhone: String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allChats by viewModel.allChats.collectAsState()

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
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ইনবক্স মেসেজ দেখতে লগইন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "বাড়িওয়ালা ও ভাড়াটিয়াদের সাথে 1-on-1 চ্যাট ও ভয়েস মেসেজ আদান-প্রদান করতে লগইন করুন।",
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
                        Text("লগইন / সাইন-আপ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val userPhone = currentUser!!.phone

    // Group chats by roomKey to show the latest message for each room
    val conversations = remember(allChats, userPhone) {
        val map = mutableMapOf<String, ChatMessage>()
        allChats.filter { it.senderPhone == userPhone || it.recipientPhone == userPhone }
            .sortedBy { it.id }
            .forEach { msg ->
                map[msg.roomKey] = msg
            }
        map.values.sortedByDescending { it.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "মেসেজ ইনবক্স",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "বাড়িওয়ালা ও ভাড়াটিয়াদের সাথে চলমান চ্যাট",
                fontSize = 12.sp,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (conversations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("আপনার কোনো চ্যাট বা মেসেজ নেই।", fontSize = 14.sp, color = SlateTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("বিজ্ঞাপনের নিচে 'চ্যাট' বাটনে চাপ দিয়ে সরাসরি মেসেজ পাঠানো শুরু করুন।", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(conversations) { msg ->
                val partnerPhone = if (msg.senderPhone == userPhone) msg.recipientPhone else msg.senderPhone
                val partnerName = if (msg.senderPhone == userPhone) "বাড়িওয়ালা / ভাড়াটিয়া ($partnerPhone)" else msg.senderName

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            onOpenConversation(msg.roomKey, msg.listingId, msg.listingTitle, partnerName, partnerPhone)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partnerName.take(1).ifBlank { "U" },
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = partnerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SlateTextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = msg.time,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            if (msg.listingTitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.listingTitle,
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (msg.type == "voice") "🎙️ ভয়েস মেসেজ (${msg.duration})" else msg.text,
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
