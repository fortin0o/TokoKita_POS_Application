package com.donald.aplikasikedua.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Design Tokens ---
val PrimaryTeal = Color(0xFF0D5C6A)
val BackgroundGray = Color(0xFFF7F9FA)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF757575)

@Composable
fun POSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryTeal,
            onPrimary = Color.White,
            background = BackgroundGray,
            surface = SurfaceWhite,
            onSurface = TextDark
        ),
        content = content
    )
}

// --- 1. Dashboard Layout ---
@Composable
fun DashboardScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(BackgroundGray).padding(padding)) {
            // Top Teal Background (200dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(PrimaryTeal)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(32.dp))
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Selamat Sore, Shakilla!",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "20 Mei 2026",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 130.dp) // Adjusted offset
            ) {
                // Overlapping Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = "Estimasi Hari Ini", fontSize = 12.sp, color = TextGray)
                        Text(
                            text = "Rp0",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(thickness = 1.dp, color = BackgroundGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            QuickActionItem(Icons.Default.ShoppingCart, "Transaksi")
                            QuickActionItem(Icons.Default.Groups, "Pelanggan")
                            QuickActionItem(Icons.AutoMirrored.Filled.Assignment, "Laporan")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Siap melayani pelanggan dengan setulus hati\nJangan Kecewakan Pelanggan",
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // Navigation Grid (3x2)
                val navItems = listOf(
                    Pair(Icons.Default.Inventory, "Menu"),
                    Pair(Icons.AutoMirrored.Filled.Label, "Kategori"),
                    Pair(Icons.Default.Badge, "Pegawai"),
                    Pair(Icons.Default.LibraryBooks, "Layanan"),
                    Pair(Icons.Default.Storefront, "Cabang"),
                    Pair(Icons.Default.Print, "Printer")
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(navItems) { item ->
                        NavigationCard(item.first, item.second)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = PrimaryTeal, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
fun NavigationCard(icon: ImageVector, label: String) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = PrimaryTeal, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// --- 2. Bottom Sheet Modal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Pusat Kendali Owner",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = "Kelola sistem dan pengaturan aplikasi",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            BottomSheetItem(Icons.Default.Store, "Pengaturan Nama Toko & Struk")
            BottomSheetItem(Icons.Default.Description, "Ekspor Laporan ke Excel")
            BottomSheetItem(Icons.Default.Info, "Tentang Aplikasi")
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundGray)
            BottomSheetItem(Icons.Default.ExitToApp, "Keluar dari Aplikasi", textColor = Color.Red)
        }
    }
}

@Composable
fun BottomSheetItem(icon: ImageVector, label: String, textColor: Color = PrimaryTeal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 14.sp, color = if (textColor == Color.Red) Color.Red else TextDark)
    }
}

// --- 3. Form Inputs ---
@Composable
fun POSInputField(label: String, value: String, onValueChange: (String) -> Unit, leadingIcon: ImageVector? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(label, color = TextGray, fontSize = 14.sp) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp)) } },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true
        )
    }
}

@Composable
fun ToggleChipGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onOptionSelected(option) },
                shape = RoundedCornerShape(24.dp),
                color = if (isSelected) PrimaryTeal.copy(alpha = 0.1f) else BackgroundGray,
                border = if (isSelected) BorderStroke(1.5.dp, PrimaryTeal) else BorderStroke(1.dp, Color.Transparent)
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) PrimaryTeal else TextGray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// --- 4. List Items ---
@Composable
fun MenuListItem(name: String, price: String, category: String, status: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextGray, modifier = Modifier.size(30.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text(text = "Rp $price", color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = category, fontSize = 12.sp, color = TextGray)
                Spacer(modifier = Modifier.height(6.dp))
                StatusChip(status)
            }
            
            // Actions
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Edit", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Cancel, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    Surface(
        color = if (status == "Aktif") Color(0xFF8BC34A) else Color.LightGray,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// --- Previews ---
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    POSTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ListPreview() {
    POSTheme {
        LazyColumn(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
            items(3) {
                MenuListItem("Croissant", "40.000", "Pastry", "Aktif")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormPreview() {
    var text by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Aktif") }
    
    POSTheme {
        Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).padding(24.dp)) {
            Text("Tambah Kategori", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryTeal)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    POSInputField("Nama Kategori", text, { text = it }, Icons.Default.Label)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Status", fontSize = 12.sp, color = TextGray)
                    ToggleChipGroup(listOf("Aktif", "Tidak Aktif"), status) { status = it }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Simpan Data", fontWeight = FontWeight.Bold)
            }
        }
    }
}
