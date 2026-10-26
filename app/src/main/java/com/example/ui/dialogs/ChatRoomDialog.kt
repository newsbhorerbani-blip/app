package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.audio.AudioVoicePlayer
import com.example.audio.AudioVoiceRecorder
import com.example.data.model.ChatMessage
import com.example.data.model.User
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseBadge
import com.example.ui.theme.RoseLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TealSecondary

@Composable
fun ChatRoomDialog(
    partnerName: String,
    listingTitle: String,
    currentUser: User?,
    messages: List<ChatMessage>,
    voiceRecorder: AudioVoiceRecorder,
    voicePlayer: AudioVoicePlayer,
    onSendMessage: (String) -> Unit,
    onSendVoiceMessage: (audioData: String, duration: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isRecording by voiceRecorder.isRecording.collectAsState()
    val recordingSeconds by voiceRecorder.recordingSeconds.collectAsState()
    val amplitudes by voiceRecorder.audioAmplitudes.collectAsState()

    val currentlyPlayingId by voicePlayer.currentlyPlayingId.collectAsState()
    val playbackProgress by voicePlayer.playbackProgress.collectAsState()

    // Permission launcher for microphone
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            voiceRecorder.startRecording()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF1F5F9)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(EmeraldPrimary, TealSecondary))
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partnerName.take(1).ifBlank { "U" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = partnerName.ifBlank { "বাড়িওয়ালা / ভাড়াটিয়া" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = listingTitle.ifBlank { "বাসার বিজ্ঞাপন" },
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = Color.White
                        )
                    }
                }

                // Messages Feed
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "কোনো পূর্ববর্তী মেসেজ নেই।",
                                        color = SlateTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "নিচে লিখে বা ভয়েস পাঠিয়ে সরাসরি ব্যক্তিগত চ্যাট শুরু করুন।",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(messages) { msg ->
                            val isMe = currentUser?.phone == msg.senderPhone
                            MessageBubble(
                                message = msg,
                                isMe = isMe,
                                isPlaying = currentlyPlayingId == msg.id,
                                playbackProgress = if (currentlyPlayingId == msg.id) playbackProgress else 0f,
                                onPlayVoice = {
                                    voicePlayer.playAudio(msg.id, msg.audioData)
                                }
                            )
                        }
                    }
                }

                // Recording Banner if recording
                AnimatedVisibility(visible = isRecording) {
                    Surface(
                        color = RoseLight,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(RoseBadge)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ভয়েস রেকর্ড হচ্ছে...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = RoseBadge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SlateTextPrimary
                                )
                            }

                            Row {
                                TextButton(
                                    onClick = { voiceRecorder.stopRecording(discard = true) }
                                ) {
                                    Text("বাতিল", color = SlateTextSecondary, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        val result = voiceRecorder.stopRecording(discard = false)
                                        if (result != null) {
                                            onSendVoiceMessage(result.first, result.second)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseBadge),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("পাঠান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Bottom Input Bar
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("একটি মেসেজ লিখুন...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = SlateBorder
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Text Button
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSendMessage(textInput)
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "পাঠান",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Mic Button
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    val result = voiceRecorder.stopRecording(discard = false)
                                    if (result != null) {
                                        onSendVoiceMessage(result.first, result.second)
                                    }
                                } else {
                                    if (hasMicPermission) {
                                        voiceRecorder.startRecording()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) RoseBadge else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "ভয়েস রেকর্ড",
                                tint = if (isRecording) Color.White else EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    isPlaying: Boolean,
    playbackProgress: Float,
    onPlayVoice: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Text(
            text = "${message.senderName} • ${message.time}",
            fontSize = 10.sp,
            color = SlateTextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) EmeraldPrimary else Color.White,
            shadowElevation = 1.dp
        ) {
            if (message.type == "voice") {
                // Voice Message Bubble with Waveforms
                VoiceMessageBubbleContent(
                    isMe = isMe,
                    duration = message.duration,
                    isPlaying = isPlaying,
                    progress = playbackProgress,
                    onPlayClick = onPlayVoice
                )
            } else {
                Text(
                    text = message.text,
                    color = if (isMe) Color.White else SlateTextPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceMessageBubbleContent(
    isMe: Boolean,
    duration: String,
    isPlaying: Boolean,
    progress: Float,
    onPlayClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedHeight by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .width(220.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isMe) Color.White.copy(alpha = 0.25f) else EmeraldPrimary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Waveform bar visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heights = if (isPlaying) {
                    listOf(animatedHeight, 18f, animatedHeight * 0.7f, 16f, animatedHeight * 0.5f)
                } else {
                    listOf(8f, 14f, 18f, 12f, 10f)
                }
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(h.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isMe) Color.White else EmeraldPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Playback progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isMe) Color.White else EmeraldPrimary,
                trackColor = if (isMe) Color.White.copy(alpha = 0.3f) else Color.LightGray
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎙️ ভয়েস",
                    fontSize = 9.sp,
                    color = if (isMe) EmeraldLight else SlateTextSecondary
                )
                Text(
                    text = duration,
                    fontSize = 9.sp,
                    color = if (isMe) EmeraldLight else SlateTextSecondary
                )
            }
        }
    }
}
