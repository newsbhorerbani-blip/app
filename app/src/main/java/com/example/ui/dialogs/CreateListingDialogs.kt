package com.example.ui.dialogs

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Listing
import com.example.data.model.MarketItem
import com.example.data.model.RentRequest
import com.example.data.model.User
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseBadge
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

// ==================== 1. CREATE / EDIT HOUSE LISTING ====================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditListingDialog(
    initialListing: Listing? = null,
    locations: List<String>,
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (
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
        newImageUris: List<Uri>,
        existingImages: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialListing?.title ?: "") }
    var selectedType by remember { mutableStateOf(initialListing?.type ?: "ফ্যামিলি") }
    var selectedArea by remember { mutableStateOf(initialListing?.area ?: (locations.firstOrNull() ?: "নবীনবাগ")) }
    var isAreaDropdownExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf(initialListing?.address ?: "") }
    var rentText by remember { mutableStateOf(if (initialListing != null && !initialListing.rentNegotiable && initialListing.rent > 0) initialListing.rent.toString() else "") }
    var rentNegotiable by remember { mutableStateOf(initialListing?.rentNegotiable ?: false) }
    var availableFrom by remember { mutableStateOf(initialListing?.available ?: "আগামী মাসের ১ তারিখ থেকে") }
    var phone by remember { mutableStateOf(initialListing?.phone ?: (currentUser?.phone ?: "")) }
    var specs by remember { mutableStateOf(initialListing?.specs ?: "") }
    var desc by remember { mutableStateOf(initialListing?.desc ?: "") }
    var videoUrl by remember { mutableStateOf(initialListing?.video ?: "") }

    val newImageUris = remember { mutableStateListOf<Uri>() }
    val existingImages = remember { mutableStateListOf<String>().apply { initialListing?.images?.let { addAll(it) } } }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        newImageUris.addAll(uris)
    }

    val typeOptions = listOf("ফ্যামিলি", "ব্যাচেলর", "মেস সিট", "সাবলেট")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (initialListing == null) "নতুন বাসার বিজ্ঞাপন দিন" else "বিজ্ঞাপন এডিট করুন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = Color.White)
                    }
                }

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    Text("বিজ্ঞাপনের শিরোনাম *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("উদা: নিরিবিলি পরিবেশে ২ রুমের ফ্যামিলি ফ্ল্যাট", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Rental Type
                    Text("ভাড়ার ধরন *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        typeOptions.forEach { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text(t, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Area Dropdown
                    Text("এলাকা *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = isAreaDropdownExpanded,
                        onExpandedChange = { isAreaDropdownExpanded = !isAreaDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedArea,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAreaDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isAreaDropdownExpanded,
                            onDismissRequest = { isAreaDropdownExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc, fontSize = 13.sp) },
                                    onClick = {
                                        selectedArea = loc
                                        isAreaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Address
                    Text("বিস্তারিত ঠিকানা", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = { Text("৫ নং রোড, বাড়ি নং ১২, মসজিদের পাশে", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Rent & Negotiable
                    Text("মাসিক ভাড়া (টাকা) *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = if (rentNegotiable) "" else rentText,
                        onValueChange = { rentText = it },
                        placeholder = { Text(if (rentNegotiable) "আলোচনা সাপেক্ষে" else "৬৫০০", fontSize = 12.sp) },
                        enabled = !rentNegotiable,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rentNegotiable = !rentNegotiable }
                    ) {
                        Checkbox(
                            checked = rentNegotiable,
                            onCheckedChange = { rentNegotiable = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                        )
                        Text("ভাড়া আলোচনা সাপেক্ষে (টাকা দিতে হবে না)", fontSize = 12.sp, color = SlateTextPrimary)
                    }

                    // Available From
                    Text("কবে থেকে খালি? *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = availableFrom,
                        onValueChange = { availableFrom = it },
                        placeholder = { Text("উদা: আগামী মাসের ১ তারিখ থেকে", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Contact Phone
                    Text("যোগাযোগের মোবাইল *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text("017XXXXXXXX", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Specs & Description
                    Text("রুমের বিবরণ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = specs,
                        onValueChange = { specs = it },
                        placeholder = { Text("২ বেড, ১ বাথ, ১ বারান্দা, গ্যাস সংযোগ", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text("অন্যান্য বিবরণ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        placeholder = { Text("বাসাটির পরিবেশ সম্পর্কে অতিরিক্ত বিবরণ...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Video URL (Optional)
                    Text("রুমের ভিডিও লিংক (ঐচ্ছিক)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        placeholder = { Text("YouTube / Facebook / Drive লিংক", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Photos Selection
                    Text("রুমের ছবি (সর্বোচ্চ ৬টি)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ছবি নির্বাচন করুন", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Display selected photos preview
                    if (existingImages.isNotEmpty() || newImageUris.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(existingImages) { index, imgUrl ->
                                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = imgUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    IconButton(
                                        onClick = { existingImages.removeAt(index) },
                                        modifier = Modifier.size(22.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            itemsIndexed(newImageUris) { index, uri ->
                                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    IconButton(
                                        onClick = { newImageUris.removeAt(index) },
                                        modifier = Modifier.size(22.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Submit Button
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            val rentVal = if (rentNegotiable) 0L else (rentText.toLongOrNull() ?: 0L)
                            onSave(
                                initialListing?.id ?: "",
                                title,
                                selectedType,
                                selectedArea,
                                address,
                                rentVal,
                                rentNegotiable,
                                availableFrom,
                                phone,
                                specs,
                                desc,
                                videoUrl,
                                newImageUris,
                                existingImages
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বিজ্ঞাপন সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==================== 2. CREATE / EDIT MARKET ITEM ====================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateEditMarketDialog(
    initialItem: MarketItem? = null,
    locations: List<String>,
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (
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
        newImageUris: List<Uri>,
        existingImages: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: "ফার্নিচার") }
    var selectedCondition by remember { mutableStateOf(initialItem?.condition ?: "ভালো") }
    var selectedArea by remember { mutableStateOf(initialItem?.area ?: (locations.firstOrNull() ?: "নবীনবাগ")) }
    var isAreaDropdownExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf(initialItem?.address ?: "") }
    var usage by remember { mutableStateOf(initialItem?.usage ?: "১ বছর") }
    var priceText by remember { mutableStateOf(if (initialItem != null && !initialItem.negotiable && initialItem.price > 0) initialItem.price.toString() else "") }
    var priceNegotiable by remember { mutableStateOf(initialItem?.negotiable ?: false) }
    var phone by remember { mutableStateOf(initialItem?.phone ?: (currentUser?.phone ?: "")) }
    var desc by remember { mutableStateOf(initialItem?.desc ?: "") }

    val newImageUris = remember { mutableStateListOf<Uri>() }
    val existingImages = remember { mutableStateListOf<String>().apply { initialItem?.images?.let { addAll(it) } } }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        newImageUris.addAll(uris)
    }

    val categories = listOf("ফার্নিচার", "ইলেকট্রনিক্স", "মোবাইল/ল্যাপটপ", "বই/স্টেশনারি", "রান্নাঘরের জিনিস", "সাইকেল/বাইক", "অন্যান্য")
    val conditions = listOf("নতুনের মতো", "ভালো", "মোটামুটি")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(EmeraldPrimary).padding(16.dp)) {
                    Text(if (initialItem == null) "পুরনো জিনিস বিক্রির বিজ্ঞাপন দিন" else "বিজ্ঞাপন এডিট করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                Column(modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("পণ্যের নাম *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("উদা: স্টাডি টেবিল ও চেয়ার", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth())

                    Text("ক্যাটাগরি *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat, fontSize = 11.sp) })
                        }
                    }

                    Text("জিনিসের অবস্থা *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        conditions.forEach { cond ->
                            FilterChip(selected = selectedCondition == cond, onClick = { selectedCondition = cond }, label = { Text(cond, fontSize = 11.sp) })
                        }
                    }

                    Text("এলাকা *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(expanded = isAreaDropdownExpanded, onExpandedChange = { isAreaDropdownExpanded = !isAreaDropdownExpanded }) {
                        OutlinedTextField(value = selectedArea, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = isAreaDropdownExpanded, onDismissRequest = { isAreaDropdownExpanded = false }) {
                            locations.forEach { loc ->
                                DropdownMenuItem(text = { Text(loc, fontSize = 13.sp) }, onClick = { selectedArea = loc; isAreaDropdownExpanded = false })
                            }
                        }
                    }

                    Text("কতদিন ব্যবহার করা হয়েছে", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = usage, onValueChange = { usage = it }, placeholder = { Text("উদা: ৬ মাস", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth())

                    Text("দাম (টাকা) *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = if (priceNegotiable) "" else priceText, onValueChange = { priceText = it }, placeholder = { Text(if (priceNegotiable) "আলোচনা সাপেক্ষে" else "২৫০০", fontSize = 12.sp) }, enabled = !priceNegotiable, modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { priceNegotiable = !priceNegotiable }) {
                        Checkbox(checked = priceNegotiable, onCheckedChange = { priceNegotiable = it }, colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary))
                        Text("দাম আলোচনা সাপেক্ষে", fontSize = 12.sp)
                    }

                    Text("যোগাযোগের মোবাইল *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth())

                    Text("বিবরণ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

                    Text("ছবি নির্বাচন করুন (সর্বোচ্চ ৪টি)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Button(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight)) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ছবি বাছুন", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (existingImages.isNotEmpty() || newImageUris.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(existingImages) { index, imgUrl ->
                                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = imgUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    IconButton(onClick = { existingImages.removeAt(index) }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.6f))) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                            itemsIndexed(newImageUris) { index, uri ->
                                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    IconButton(onClick = { newImageUris.removeAt(index) }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.6f))) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            val priceVal = if (priceNegotiable) 0L else (priceText.toLongOrNull() ?: 0L)
                            onSave(initialItem?.id ?: "", title, selectedCategory, selectedCondition, selectedArea, address, usage, priceVal, priceNegotiable, phone, desc, newImageUris, existingImages)
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("বিজ্ঞাপন সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==================== 3. CREATE / EDIT RENT REQUEST ("বাসা চাই") ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRentRequestDialog(
    initialReq: RentRequest? = null,
    locations: List<String>,
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (
        id: String,
        name: String,
        phone: String,
        areas: List<String>,
        type: String,
        budget: Long,
        needFrom: String,
        members: String,
        desc: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialReq?.name ?: (currentUser?.name ?: "")) }
    var phone by remember { mutableStateOf(initialReq?.phone ?: (currentUser?.phone ?: "")) }
    val selectedAreas = remember { mutableStateListOf<String>().apply { initialReq?.areas?.let { addAll(it) } } }
    var selectedType by remember { mutableStateOf(initialReq?.type ?: "ফ্যামিলি") }
    var budgetText by remember { mutableStateOf(if (initialReq != null && initialReq.budget > 0) initialReq.budget.toString() else "") }
    var needFrom by remember { mutableStateOf(initialReq?.needFrom ?: "আগামী মাসের ১ তারিখ") }
    var members by remember { mutableStateOf(initialReq?.members ?: "ছোট ফ্যামিলি") }
    var desc by remember { mutableStateOf(initialReq?.desc ?: "") }

    val types = listOf("ফ্যামিলি", "ব্যাচেলর", "মেস সিট", "সাবলেট")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(EmeraldPrimary).padding(16.dp)) {
                    Text(if (initialReq == null) "বাসা ভাড়ার অনুরোধ পাঠান (বাসা চাই)" else "অনুরোধ এডিট করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                Column(modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("আপনার নাম *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth())

                    Text("মোবাইল নম্বর *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth())

                    Text("কোন কোন এলাকায় বাসা খুঁজছেন? (একাধিক বাছতে পারেন)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        locations.forEach { loc ->
                            val isSelected = selectedAreas.contains(loc)
                            FilterChip(
                                selected = isSelected,
                                onClick = { if (isSelected) selectedAreas.remove(loc) else selectedAreas.add(loc) },
                                label = { Text(loc, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    Text("ভাড়ার ধরন *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        types.forEach { t ->
                            FilterChip(selected = selectedType == t, onClick = { selectedType = t }, label = { Text(t, fontSize = 12.sp) })
                        }
                    }

                    Text("সর্বোচ্চ বাজেট (টাকা)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = budgetText, onValueChange = { budgetText = it }, placeholder = { Text("ফাঁকা = আলোচনা সাপেক্ষে", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth())

                    Text("কবে থেকে লাগবে", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = needFrom, onValueChange = { needFrom = it }, modifier = Modifier.fillMaxWidth())

                    Text("কতজন থাকবেন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = members, onValueChange = { members = it }, modifier = Modifier.fillMaxWidth())

                    Text("অন্যান্য চাহিদা", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }

                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            val budgetVal = budgetText.toLongOrNull() ?: 0L
                            onSave(initialReq?.id ?: "", name, phone, selectedAreas.toList(), selectedType, budgetVal, needFrom, members, desc)
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("অনুরোধ নিশ্চিত করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==================== 4. REPORT DIALOG ====================
@Composable
fun ReportDialog(
    initialAdCode: String = "",
    onDismiss: () -> Unit,
    onSubmit: (adCode: String, type: String, message: String, name: String, phone: String, email: String) -> Unit
) {
    var adCode by remember { mutableStateOf(initialAdCode) }
    var selectedType by remember { mutableStateOf("ভুয়া বিজ্ঞাপন") }
    var message by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val reportTypes = listOf("ভুয়া বিজ্ঞাপন", "প্রতারণা বা টাকা চাওয়া", "অনুপযুক্ত আচরণ", "ভুল তথ্য", "অন্যান্য")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RoseBadge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সমস্যা বা অভিযোগ রিপোর্ট", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                }

                Text("বিজ্ঞাপনের আইডি (ঐচ্ছিক)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = adCode, onValueChange = { adCode = it.uppercase() }, placeholder = { Text("উদা: BS-LKJ3F9", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth())

                Text("কী সমস্যা হয়েছে? *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = message, onValueChange = { message = it }, placeholder = { Text("বিস্তারিত লিখুন...", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), maxLines = 4)

                Text("আপনার নাম ও মোবাইল (ঐচ্ছিক)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("নাম", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, placeholder = { Text("মোবাইল", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = SlateTextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(adCode, selectedType, message, name, phone, email) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseBadge)
                    ) {
                        Text("রিপোর্ট পাঠান", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== 5. IMAGE LIGHTBOX DIALOG ====================
@Composable
fun ImageLightboxDialog(
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0))) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.padding(16.dp).align(Alignment.TopEnd).size(36.dp).background(Color.White.copy(0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = Color.White)
            }

            // Image
            if (images.isNotEmpty()) {
                AsyncImage(
                    model = images[currentIndex],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 60.dp)
                )
            }

            // Prev & Next Buttons
            if (images.size > 1) {
                IconButton(
                    onClick = { currentIndex = if (currentIndex > 0) currentIndex - 1 else images.size - 1 },
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterStart).size(42.dp).background(Color.White.copy(0.25f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "আগেরটি", tint = Color.White)
                }

                IconButton(
                    onClick = { currentIndex = if (currentIndex < images.size - 1) currentIndex + 1 else 0 },
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterEnd).size(42.dp).background(Color.White.copy(0.25f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "পরেরটি", tint = Color.White)
                }

                // Counter
                Text(
                    text = "${currentIndex + 1} / ${images.size}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp).align(Alignment.BottomCenter).background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ==================== 6. VIDEO PLAYER DIALOG ====================
@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f))) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("রুমের ভিডিও প্লেয়ার", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            webViewClient = WebViewClient()
                            loadUrl(videoUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            }
        }
    }
}
