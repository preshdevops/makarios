package com.makarios.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.makarios.app.data.CommunityRepository
import com.makarios.app.data.UserProfile
import com.makarios.app.ui.theme.*

@Composable
fun FindFriendsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }
    val connectedUids = remember { mutableStateListOf<String>() }

    fun doSearch() {
        if (query.isBlank()) return
        isSearching = true
        hasSearched = true
        CommunityRepository.searchUsers(
            rawQuery = query,
            onResult = { users ->
                isSearching = false
                searchResults = users
            },
            onError = { error ->
                isSearching = false
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(26.dp))
                .background(Porcelain)
                .border(1.dp, Border, RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Find Friends",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = Espresso
                        )
                        Text(
                            text = "Connect with friends on Makarios",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = Stone
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(0.5.dp, Border, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Stone,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = StoneMuted,
                            modifier = Modifier.size(18.dp)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "@username, email, or MK-XXXX",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 13.5.sp,
                                    color = StoneMuted
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = {
                                    query = it
                                    doSearch()
                                },
                                textStyle = TextStyle(
                                    color = Espresso,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 14.sp
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Olive,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results or Hint
                if (!hasSearched && query.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Search by @username, email address, or a friend's 6-character Friend Code.",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.5.sp,
                            color = StoneMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else if (hasSearched && searchResults.isEmpty() && !isSearching) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No members found matching \"$query\"",
                            fontFamily = BodyFontFamily,
                            fontSize = 13.sp,
                            color = Stone
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults, key = { it.userId }) { user ->
                            val isConnected = connectedUids.contains(user.userId) ||
                                    CommunityRepository.friendsList.any { it.userId == user.userId }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Surface)
                                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Espresso
                                    )
                                    if (user.username.isNotBlank()) {
                                        Text(
                                            text = "@${user.username} · ${user.friendCode}",
                                            fontFamily = BodyFontFamily,
                                            fontSize = 12.sp,
                                            color = StoneMuted
                                        )
                                    }
                                }

                                if (isConnected) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Sage,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Connected",
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Sage
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            CommunityRepository.addFriend(
                                                targetUser = user,
                                                onSuccess = {
                                                    connectedUids.add(user.userId)
                                                    Toast.makeText(context, "Connected with ${user.displayName} ✓", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Olive,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Add",
                                                fontFamily = BodyFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
