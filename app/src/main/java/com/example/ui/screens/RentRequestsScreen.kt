package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RentRequest
import com.example.data.repository.PhoneticSearchHelper
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseBadge
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RentRequestsScreen(
    viewModel: MainViewModel,
    onCreateRequestClick: () -> Unit
) {
    val context = LocalContext.current
    val requests by viewModel.filteredRentRequests.collectAsState()
    val searchQuery by viewModel.requestSearchQuery.collectAsState()
    val selectedArea by viewModel.selectedRequestArea.collectAsState()
    val selectedType by viewModel.selectedRequestType.collectAsState()
    val locations by viewModel.locations.collectAsState()

    val rentalTypes = listOf("all" to "সব ধরন", "ফ্যামিলি" to "ফ্যামিলি", "ব্যাচেলর" to "ব্যাচেলর", "মেস সিট" to "মেস সিট", "সাবলেট" to "সাবলেট")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF0369A1), Color(0xFF0284C7)))
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("বাসা চাই — অনুরোধসমূহ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("বাসা খুঁজছেন? আপনার চাহিদা জানিয়ে সরাসরি বাড়িওয়ালাদের দৃষ্টি আকর্ষণ করুন", color = Color.White.copy(0.9f), fontSize = 11.sp)
                    }
                }
            }

            // Filters Card
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
                            onValueChange = { viewModel.requestSearchQuery.value = it },
                            placeholder = { Text("নাম বা এলাকা দিয়ে অনুরোধ খুঁজুন...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.requestSearchQuery.value = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Type filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(rentalTypes) { (key, label) ->
                                FilterChip(
                                    selected = selectedType == key,
                                    onClick = { viewModel.selectedRequestType.value = key },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0284C7),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Area filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedArea == "all",
                                    onClick = { viewModel.selectedRequestArea.value = "all" },
                                    label = { Text("সব এলাকা", fontSize = 11.sp) }
                                )
                            }
                            items(locations) { loc ->
                                FilterChip(
                                    selected = selectedArea == loc,
                                    onClick = { viewModel.selectedRequestArea.value = loc },
                                    label = { Text(loc, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Count
            item {
                Text(
                    text = "অনুরোধ তালিকা (${PhoneticSearchHelper.toBengaliNumber(requests.size.toLong())}টি)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Empty state
            if (requests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো অনুরোধ পাওয়া যায়নি", fontSize = 14.sp, color = SlateTextSecondary)
                    }
                }
            } else {
                items(requests) { req ->
                    RentRequestCard(
                        request = req,
                        onCallClick = {
                            val uri = Uri.parse("tel:${req.phone}")
                            context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                        },
                        onWhatsAppClick = {
                            val uri = Uri.parse("https://wa.me/88${req.phone}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )
                }
            }
        }

        // Post request FAB
        FloatingActionButton(
            onClick = onCreateRequestClick,
            containerColor = Color(0xFF0284C7),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 75.dp, end = 16.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "অনুরোধ পাঠান")
                Spacer(modifier = Modifier.width(6.dp))
                Text("অনুরোধ পাঠান", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RentRequestCard(
    request: RentRequest,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val isDone = request.status == "done"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Column {
            // Header Bar
            Surface(
                color = if (isDone) Color(0xFFF1F5F9) else Color(0xFFF0FDF4),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldLight) {
                            Text(
                                text = request.type.ifBlank { "যেকোনো ধরন" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDone) EmeraldPrimary else AmberAccent
                    ) {
                        Text(
                            text = if (isDone) "বাসা পাওয়া গেছে" else "বাসা খুঁজছেন",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(request.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Areas Requested
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val areas = if (request.areas.isNotEmpty()) request.areas else listOf(request.area.ifBlank { "যেকোনো এলাকা" })
                    areas.forEach { areaName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(areaName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldDark)
                        }
                    }
                }

                if (request.members.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("থাকবেন: ${request.members}", fontSize = 11.sp, color = SlateTextSecondary)
                }

                if (request.needFrom.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কবে থেকে: ${request.needFrom}", fontSize = 11.sp, color = SlateTextSecondary)
                    }
                }

                if (request.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(request.desc, fontSize = 12.sp, color = SlateTextSecondary, maxLines = 2)
                }
            }

            // Bottom Bar: Budget & Call
            Surface(color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("বাজেট", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        if (request.budget > 0) {
                            Text("৳${PhoneticSearchHelper.toBengaliNumber(request.budget)} পর্যন্ত", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        } else {
                            Text("আলোচনা সাপেক্ষে", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
