package com.example.sothuchi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sothuchi.data.local.AccountGroup
import com.example.sothuchi.data.local.FinancialRecordEntity
import com.example.sothuchi.data.local.RecordKind
import com.example.sothuchi.data.local.WalletEntity
import com.example.sothuchi.ui.components.formatVnd
import com.example.sothuchi.ui.theme.MisaColors

@Composable
fun MisaAccountsScreen(
    modifier: Modifier = Modifier,
    wallets: List<WalletEntity>,
    records: List<FinancialRecordEntity>,
    onOpenAddWallet: () -> Unit,
    onDeleteWallet: (Long) -> Unit,
    onOpenAddRecord: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Tài khoản, 1: Sổ tiết kiệm, 2: Tích lũy
    var hideBalance by remember { mutableStateOf(false) }

    val totalAssets = wallets.sumOf { it.balance }
    val activeCashAndBank = wallets
        .filter { it.groupType == AccountGroup.CASH || it.groupType == AccountGroup.BANK }
        .sumOf { it.balance }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MisaColors.ScreenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Gradient Royal Blue Top Header như ảnh 1
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MisaColors.HeaderBlueTop, MisaColors.HeaderBlueBottom)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tài khoản của tôi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Tùy chỉnh", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thẻ trắng Tổng tài sản có nút con mắt ẩn/hiện số dư
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = "Tổng tài sản",
                            style = MaterialTheme.typography.bodySmall,
                            color = MisaColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (hideBalance) "•••••••• đ" else formatVnd(totalAssets),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MisaColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Ẩn/hiện số tiền",
                                tint = MisaColors.TextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { hideBalance = !hideBalance }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 chip bo tròn: Tài khoản | Sổ tiết kiệm | Tích lũy
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val subTabs = listOf("Tài khoản", "Sổ tiết kiệm", "Tích lũy")
                    subTabs.forEachIndexed { idx, label ->
                        val active = selectedSubTab == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) Color.White.copy(alpha = 0.22f) else Color.Transparent)
                                .then(
                                    if (active) Modifier.border(1.dp, Color.White, RoundedCornerShape(20.dp))
                                    else Modifier
                                )
                                .clickable { selectedSubTab = idx }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            if (selectedSubTab == 0) {
                // Dòng Tổng tiền màu trắng ngay dưới header
                Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tổng tiền", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (hideBalance) "•••••• đ" else formatVnd(activeCashAndBank),
                            fontWeight = FontWeight.Bold,
                            color = MisaColors.TextPrimary
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đang sử dụng (${if (hideBalance) "•••" else formatVnd(activeCashAndBank)})",
                                color = MisaColors.IncomeGreen,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MisaColors.TextSecondary)
                        }
                    }

                    val groups = listOf(
                        AccountGroup.CASH,
                        AccountGroup.BANK,
                        AccountGroup.CREDIT_CARD,
                        AccountGroup.INVESTMENT,
                        AccountGroup.SAVING,
                        AccountGroup.OTHER
                    )

                    groups.forEach { group ->
                        val groupWallets = wallets.filter { it.groupType == group }
                        if (groupWallets.isNotEmpty()) {
                            val groupSum = groupWallets.sumOf { it.balance }
                            item(key = "header_${group.name}") {
                                Text(
                                    text = "${group.label} (${if (hideBalance) "•••" else formatVnd(groupSum)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MisaColors.TextSecondary,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
                                )
                            }

                            items(groupWallets, key = { it.id }) { wallet ->
                                AccountRowCard(
                                    wallet = wallet,
                                    hideBalance = hideBalance,
                                    onDelete = { onDeleteWallet(wallet.id) }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            } else {
                // Tab Sổ tiết kiệm / Tích lũy
                val filteredRecords = records.filter {
                    if (selectedSubTab == 1) it.kind == RecordKind.SAVING else it.kind != RecordKind.SAVING
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { rec ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(rec.title, fontWeight = FontWeight.SemiBold)
                                    if (rec.note.isNotBlank()) {
                                        Text(rec.note, style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                                    }
                                }
                                Text(
                                    text = if (hideBalance) "•••• đ" else formatVnd(rec.amount),
                                    fontWeight = FontWeight.Bold,
                                    color = MisaColors.BrandBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB xanh dương góc phải dưới như ảnh 1
        FloatingActionButton(
            onClick = { if (selectedSubTab == 0) onOpenAddWallet() else onOpenAddRecord() },
            containerColor = MisaColors.BrandBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm tài khoản")
        }
    }
}

@Composable
private fun AccountRowCard(
    wallet: WalletEntity,
    hideBalance: Boolean,
    onDelete: () -> Unit
) {
    val (icon, iconTint) = accountIconAndColor(wallet.groupType)
    val balanceColor = if (wallet.balance < 0L) MisaColors.ExpenseRed else MisaColors.TextSecondary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(wallet.name, fontWeight = FontWeight.SemiBold, color = MisaColors.TextPrimary)
                Text(
                    text = if (hideBalance) "•••••• đ" else formatVnd(wallet.balance),
                    style = MaterialTheme.typography.bodySmall,
                    color = balanceColor
                )
                if (!wallet.includeInReport) {
                    Text(
                        text = "Không tính vào báo cáo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MisaColors.TextSecondary
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = MisaColors.TextSecondary)
            }
        }
    }
}

private fun accountIconAndColor(group: AccountGroup): Pair<ImageVector, Color> {
    return when (group) {
        AccountGroup.CASH -> Icons.Default.Payments to Color(0xFF00ACC1)
        AccountGroup.BANK -> Icons.Default.AccountBalance to Color(0xFF1E88E5)
        AccountGroup.CREDIT_CARD -> Icons.Default.CreditCard to Color(0xFF43A047)
        AccountGroup.INVESTMENT -> Icons.Default.TrendingUp to Color(0xFFFB8C00)
        AccountGroup.SAVING -> Icons.Default.Savings to Color(0xFFEC407A)
        AccountGroup.OTHER -> Icons.Default.Payments to Color(0xFF29B6F6)
    }
}
