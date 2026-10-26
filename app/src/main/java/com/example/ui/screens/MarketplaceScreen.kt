package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MarketItem
import com.example.data.repository.PhoneticSearchHelper
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseBadge
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MarketplaceScreen(
    viewModel: MainViewModel,
    onCreateMarketItemClick: () -> Unit,
    onItemClick: (MarketItem) -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.filteredMarketItems.collectAsState()
    val searchQuery by viewModel.marketSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedMarketCategory.collectAsState()

    val categories = listOf("all" to "সব ক্যাটাগরি", "ফার্নিচার" to "ফার্নিচার", "ইলেকট্রনিক্স" to "ইলেকট্রনিক্স", "মোবাইল/ল্যাপটপ" to "মোবাইল/ল্যাপটপ", "বই/স্টেশনারি" to "বই/স্টেশনারি", "রান্নাঘরের জিনিস" to "রান্নাঘর", "সাইকেল/বাইক" to "সাইকেল/বাইক", "অন্যান্য" to "অন্যান্য")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFFD97706)))
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Recycling, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পুরনো জিনিস কেনা-বেচা", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ফার্নিচার, ইলেকট্রনিক্স ও বই সাশ্রয়ী মূল্যে কিনুন ও বিক্রি করুন", color = Color.White.copy(0.9f), fontSize = 11.sp)
                    }
                }
            }

            // Search & Category Filters
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.marketSearchQuery.value = it },
                            placeholder = { Text("কী কিনতে চান? (উদা: খাট, চেয়ার, টেবিল)", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmberAccent) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.marketSearchQuery.value = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberAccent,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { (key, label) ->
                                val isSelected = selectedCategory == key
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) AmberAccent else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { viewModel.selectedMarketCategory.value = key }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateTextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Header Count
            item {
                Text(
                    text = "বর্তমান পণ্যসমূহ (${PhoneticSearchHelper.toBengaliNumber(items.size.toLong())}টি)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Empty state
            if (items.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো পণ্য পাওয়া যায়নি", fontSize = 14.sp, color = SlateTextSecondary)
                    }
                }
            } else {
                items(items) { item ->
                    MarketItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onCallClick = {
                            val uri = Uri.parse("tel:${item.phone}")
                            context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                        },
                        onWhatsAppClick = {
                            val uri = Uri.parse("https://wa.me/88${item.phone}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreateMarketItemClick,
            containerColor = AmberAccent,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 75.dp, end = 16.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "বিক্রি করুন")
                Spacer(modifier = Modifier.width(6.dp))
                Text("বিজ্ঞাপন দিন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MarketItemCard(
    item: MarketItem,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val isSold = item.status == "sold"
    val images = item.images.ifEmpty { listOf("https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&auto=format&fit=crop&q=80") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = images[0],
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(0.95f)) {
                        Text(item.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = EmeraldLight) {
                        Text(item.condition, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                if (isSold) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoseBadge.copy(0.9f))
                            .align(Alignment.BottomCenter)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("বিক্রি হয়ে গেছে", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(item.area, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                    }

                    if (item.usage.isNotBlank()) {
                        Text(
                            text = "ব্যবহার: ${item.usage}",
                            fontSize = 10.sp,
                            color = SlateTextSecondary,
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary, maxLines = 1)
            }

            // Bottom Bar: Price & Action Buttons
            Surface(color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("দাম", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        if (item.negotiable) {
                            Text("আলোচনা সাপেক্ষে", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                        } else {
                            Text("৳${PhoneticSearchHelper.toBengaliNumber(item.price)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onWhatsAppClick,
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0F2FE))
                        ) {
                            Text("WA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        }

                        Button(
                            onClick = onCallClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("কল", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
