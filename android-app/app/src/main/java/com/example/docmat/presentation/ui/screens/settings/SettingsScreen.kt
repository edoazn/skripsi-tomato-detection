package com.example.docmat.presentation.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.docmat.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogoutSuccess: () -> Unit = {},
) {
    val auth = FirebaseAuth.getInstance()
    var showLogoutDialog by remember { mutableStateOf(false) }
    // Settings states
    var darkMode by remember { mutableStateOf(false) }

    // Dialog states
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pengaturan",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile foto, name, email
            item {
                UserSection()
            }


            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Tampilan & Notifikasi
            item {
                SettingsCategory(
                    title = "Tampilan & Notifikasi",
                    icon = Icons.Default.Palette
                ) {
                    // Dark Mode
                    SwitchSettingItem(
                        title = "Mode Gelap",
                        subtitle = "Ubah tema aplikasi ke mode gelap",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }

            // 3. Tentang & Bantuan
            item {
                SettingsCategory(
                    title = "Tentang & Bantuan",
                    icon = Icons.Default.Info
                ) {
                    // App Version
                    ClickableSettingItem(
                        title = "Versi Aplikasi",
                        subtitle = "Docmat v1.0.0",
                        icon = Icons.Default.AppSettingsAlt,
                        onClick = { showAboutDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Help Guide
                    ClickableSettingItem(
                        title = "Panduan Penggunaan",
                        subtitle = "Cara menggunakan aplikasi",
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = { showHelpDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // In the Logout ClickableSettingItem, replace the onClick with:
                    ClickableSettingItem(
                        title = "Keluar",
                        subtitle = "Keluar dari aplikasi",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = {
                            showLogoutDialog = true
                        }
                    )

                    // Add this dialog at the end with other dialogs:
                    if (showLogoutDialog) {
                        ConfirmationDialog(
                            title = "Keluar dari Aplikasi",
                            message = "Apakah Anda yakin ingin keluar dari akun Anda?",
                            onConfirm = {
                                auth.signOut()
                                showLogoutDialog = false
                                onLogoutSuccess()
                            },
                            onDismiss = { showLogoutDialog = false }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showClearHistoryDialog) {
        ConfirmationDialog(
            title = "Hapus Semua Riwayat",
            message = "Apakah Anda yakin ingin menghapus semua riwayat scan? Tindakan ini tidak dapat dibatalkan.",
            onConfirm = {
                // TODO: Implement clear history
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    if (showClearCacheDialog) {
        ConfirmationDialog(
            title = "Hapus Cache",
            message = "Hapus cache aplikasi untuk membersihkan penyimpanan?",
            onConfirm = {
                // TODO: Implement clear cache
                showClearCacheDialog = false
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false }
        )
    }
}

@Composable
fun UserSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Circle container for profile image
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(4.dp)
                .clip(RoundedCornerShape(50))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            // Profile Image
            Image(
                // Placeholder image sementara
                painter = painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                // Ambil nama pengguna dari FirebaseAuth
                text = FirebaseAuth.getInstance().currentUser?.displayName ?: "Nama Pengguna",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                // Ambil email pengguna dari FirebaseAuth
                text = FirebaseAuth.getInstance().currentUser?.email ?: "Email Pengguna",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

        }
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            content()
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ClickableSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorScheme.error
                )
            ) {
                Text("Ya, Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tentang Docmat") },
        text = {
            Column {
                Text("📱 Docmat v1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aplikasi deteksi penyakit tomat menggunakan AI untuk membantu petani mengidentifikasi masalah kesehatan tanaman.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("👨‍💻 Dikembangkan untuk tugas akhir/skripsi")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun HelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Panduan Penggunaan") },
        text = {
            Column {
                Text("📸 Cara Menggunakan:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. Ambil foto daun tomat yang jelas")
                Text("2. Pastikan pencahayaan cukup")
                Text("3. Tunggu hasil analisis AI")
                Text("4. Lihat diagnosis dan saran perawatan")
                Spacer(modifier = Modifier.height(8.dp))
                Text("💡 Tips: Foto dari jarak 15-30cm untuk hasil optimal")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Mengerti")
            }
        }
    )
}
