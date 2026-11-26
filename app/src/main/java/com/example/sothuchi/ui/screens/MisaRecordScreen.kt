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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sothuchi.data.local.CategoryEntity
import com.example.sothuchi.data.local.TransactionType
import com.example.sothuchi.data.local.WalletEntity
import com.example.sothuchi.ui.components.MisaMoneyKeypad
import com.example.sothuchi.ui.components.MoneyExpressionEvaluator
import com.example.sothuchi.ui.components.formatDateOnly
import com.example.sothuchi.ui.components.formatTimeOnly
import com.example.sothuchi.ui.components.formatVnd
import com.example.sothuchi.ui.theme.MisaColors

fun categoryIconForKey(key: String): ImageVector {
    return when (key) {
        "food" -> Icons.Default.Restaurant
        "kids" -> Icons.Default.ChildCare
        "shopping" -> Icons.Default.ShoppingBag
        "transport" -> Icons.Default.DirectionsCar
        "home" -> Icons.Default.Home
        "gift" -> Icons.Default.Favorite
        "salary", "bonus", "interest" -> Icons.Default.Payments
        else -> Icons.Default.Inventory2
    }
}

@Composable
fun MisaRecordScreen(
    modifier: Modifier = Modifier,
    wallets: List<WalletEntity>,
    categories: List<CategoryEntity>,
    onOpenHistory: () -> Unit,
    onOpenAddCategory: () -> Unit,
    onSaveTransaction: (
        walletId: Long,
        toWalletId: Long?,
        categoryId: Long?,
        type: TransactionType,
        amount: Long,
        note: String,
        eventTrip: String,
        counterparty: String,
        location: String,
        isBorrowedToPay: Boolean,
        hasFee: Boolean,
        excludeFromReport: Boolean
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }
    var selectedCategoryId by remember(filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull()?.id)
    }

    var fromWalletId by remember(wallets) {
        mutableStateOf(wallets.firstOrNull()?.id ?: 0L)
    }
    var toWalletId by remember(wallets, fromWalletId) {
        mutableStateOf(wallets.firstOrNull { it.id != fromWalletId }?.id)
    }
    var walletPickerExpanded by remember { mutableStateOf(false) }

    var amountExpression by remember { mutableStateOf("0") }
    var showKeypad by remember { mutableStateOf(false) }

    var frequentExpanded by remember { mutableStateOf(true) }
    var showDetails by remember { mutableStateOf(true) }

    var note by remember { mutableStateOf("") }
    var eventTrip by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isBorrowedToPay by remember { mutableStateOf(false) }
    var hasFee by remember { mutableStateOf(false) }
    var excludeFromReport by remember { mutableStateOf(false) }

    val now = remember { System.currentTimeMillis() }
    val amountColor = when (selectedType) {
        TransactionType.EXPENSE -> MisaColors.ExpenseRed
        TransactionType.INCOME -> MisaColors.IncomeGreen
        TransactionType.TRANSFER -> MisaColors.BrandBlue
    }

    val evaluated = MoneyExpressionEvaluator.evaluate(amountExpression)
    val selectedWallet = wallets.find { it.id == fromWalletId }
    val selectedCategory = filteredCategories.find { it.id == selectedCategoryId }

    fun submitRecord(finalAmount: Long) {
        if (finalAmount <= 0L || fromWalletId <= 0L) return
        if (selectedType == TransactionType.TRANSFER && (toWalletId == null || toWalletId == fromWalletId)) return
        onSaveTransaction(
            fromWalletId,
            toWalletId,
            selectedCategoryId,
            selectedType,
            finalAmount,
            note,
            eventTrip,
            counterparty,
            location,
            isBorrowedToPay,
            hasFee,
            excludeFromReport
        )
        amountExpression = "0"
        note = ""
        eventTrip = ""
        counterparty = ""
        location = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDCE6FA))
    ) {
        // Top Header kiểu MISA: [History]  [Chi tiền v]  [✓]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenHistory) {
                Icon(Icons.Default.History, contentDescription = "Lịch sử ghi chép", tint = MisaColors.TextPrimary)
            }

            Box {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { typeMenuExpanded = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedType.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MisaColors.BrandBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MisaColors.BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    TransactionType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.label, fontWeight = FontWeight.Medium) },
                            onClick = {
                                selectedType = t
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    val amt = MoneyExpressionEvaluator.evaluate(amountExpression) ?: 0L
                    submitRecord(amt)
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Lưu", tint = MisaColors.TextPrimary)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Số tiền
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showKeypad = !showKeypad },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Số tiền",
                            style = MaterialTheme.typography.bodySmall,
                            color = MisaColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${MoneyExpressionEvaluator.formatExpressionDisplay(amountExpression)} đ",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                        if (MoneyExpressionEvaluator.hasPendingOperator(amountExpression) && evaluated != null) {
                            Text(
                                text = "= ${formatVnd(evaluated)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MisaColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Card 2: Chọn hạng mục & Mục hay dùng (lưới 4x2)
            if (selectedType != TransactionType.TRANSFER) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MisaColors.ScreenBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedCategory != null) {
                                        Icon(
                                            imageVector = categoryIconForKey(selectedCategory.iconKey),
                                            contentDescription = null,
                                            tint = Color(selectedCategory.colorHex),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MisaColors.TextSecondary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = selectedCategory?.name ?: "Chọn hạng mục",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedCategory != null) MisaColors.TextPrimary else MisaColors.TextSecondary
                                )
                                Row(
                                    modifier = Modifier.clickable { onOpenAddCategory() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tất cả",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MisaColors.BrandBlue
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MisaColors.BrandBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { frequentExpanded = !frequentExpanded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mục hay dùng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MisaColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (frequentExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MisaColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (frequentExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val displayList = filteredCategories.take(7)
                                val allSlots = displayList.map<CategoryEntity, CategoryEntity?> { it } + listOf(null)
                                val chunked = allSlots.chunked(4)

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    chunked.forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowItems.forEach { cat ->
                                                if (cat != null) {
                                                    val isSelected = cat.id == selectedCategoryId
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { selectedCategoryId = cat.id }
                                                            .padding(vertical = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(Color(cat.colorHex).copy(alpha = 0.14f))
                                                                .then(
                                                                    if (isSelected) Modifier.border(
                                                                        1.5.dp,
                                                                        MisaColors.BrandBlue,
                                                                        RoundedCornerShape(10.dp)
                                                                    ) else Modifier
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = categoryIconForKey(cat.iconKey),
                                                                contentDescription = null,
                                                                tint = Color(cat.colorHex),
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = cat.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center,
                                                            color = if (isSelected) MisaColors.BrandBlue else MisaColors.TextPrimary
                                                        )
                                                    }
                                                } else {
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { onOpenAddCategory() }
                                                            .padding(vertical = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(MisaColors.BrandBlueSoft),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = null,
                                                                tint = MisaColors.BrandBlue,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Chỉnh sửa",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MisaColors.BrandBlue,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                            repeat(4 - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Ví nguồn + Ngày giờ + Diễn giải
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { walletPickerExpanded = true }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MisaColors.BrandBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = selectedWallet?.name ?: "Chọn ví",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MisaColors.TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = walletPickerExpanded,
                                onDismissRequest = { walletPickerExpanded = false }
                            ) {
                                wallets.forEach { w ->
                                    DropdownMenuItem(
                                        text = { Text("${w.name} (${formatVnd(w.balance)})") },
                                        onClick = {
                                            fromWalletId = w.id
                                            if (toWalletId == w.id) {
                                                toWalletId = wallets.firstOrNull { it.id != w.id }?.id
                                            }
                                            walletPickerExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedType == TransactionType.TRANSFER) {
                            HorizontalDivider(color = MisaColors.DividerSubtle)
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Chuyển sang ví:", style = MaterialTheme.typography.labelSmall, color = MisaColors.TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(wallets.filter { it.id != fromWalletId }, key = { it.id }) { target ->
                                        val isTarget = target.id == toWalletId
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { toWalletId = target.id },
                                            color = if (isTarget) MisaColors.BrandBlue else MisaColors.ScreenBackground,
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = target.name,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                color = if (isTarget) Color.White else MisaColors.TextPrimary,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MisaColors.DividerSubtle)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MisaColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Hôm nay - ${formatDateOnly(now)}",
                                modifier = Modifier.weight(1f),
                                color = MisaColors.TextPrimary
                            )
                            Text(
                                text = formatTimeOnly(now),
                                fontWeight = FontWeight.Medium,
                                color = MisaColors.TextPrimary
                            )
                        }

                        HorizontalDivider(color = MisaColors.DividerSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                                tint = MisaColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                placeholder = { Text("Diễn giải", color = MisaColors.TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (showDetails) {
                // Card 4: Chuyến đi sự kiện / Chi cho ai / Địa điểm
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                            DetailInputRow(
                                icon = Icons.Default.Event,
                                placeholder = "Chuyến đi, sự kiện",
                                value = eventTrip,
                                onValueChange = { eventTrip = it }
                            )
                            HorizontalDivider(color = MisaColors.DividerSubtle)
                            DetailInputRow(
                                icon = Icons.Default.PersonOutline,
                                placeholder = if (selectedType == TransactionType.INCOME) "Thu từ ai" else "Chi cho ai",
                                value = counterparty,
                                onValueChange = { counterparty = it }
                            )
                            HorizontalDivider(color = MisaColors.DividerSubtle)
                            DetailInputRow(
                                icon = Icons.Default.LocationOn,
                                placeholder = "Địa điểm",
                                value = location,
                                onValueChange = { location = it }
                            )
                        }
                    }
                }

                // Switch rows: Đi vay để trả khoản này / Phí / Không tính vào báo cáo
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Đi vay để trả khoản này", fontWeight = FontWeight.Medium)
                            Switch(checked = isBorrowedToPay, onCheckedChange = { isBorrowedToPay = it })
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Phí", fontWeight = FontWeight.Medium)
                            Switch(checked = hasFee, onCheckedChange = { hasFee = it })
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Không tính vào báo cáo", fontWeight = FontWeight.Medium)
                                Switch(checked = excludeFromReport, onCheckedChange = { excludeFromReport = it })
                            }
                            Text(
                                text = "Ghi chép này sẽ không được thống kê vào TẤT CẢ các báo cáo (trừ báo cáo Tài chính hiện tại)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MisaColors.TextSecondary
                            )
                        }
                    }
                }

                // Photo & Camera buttons bar
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Thư viện ảnh", tint = MisaColors.TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(MisaColors.DividerSubtle)
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Chụp hóa đơn", tint = MisaColors.TextSecondary)
                            }
                        }
                    }
                }
            }

            // Nút "Ẩn chi tiết" / "Thêm chi tiết"
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDetails = !showDetails },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showDetails) "Ẩn chi tiết" else "Thêm chi tiết",
                            fontWeight = FontWeight.SemiBold,
                            color = MisaColors.BrandBlue
                        )
                    }
                }
            }

            // Nút "Lưu lại" xanh lớn
            item {
                Button(
                    onClick = {
                        val amt = MoneyExpressionEvaluator.evaluate(amountExpression) ?: 0L
                        submitRecord(amt)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MisaColors.BrandBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Lưu lại",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showKeypad) {
            MisaMoneyKeypad(
                expression = amountExpression,
                onExpressionChange = { amountExpression = it },
                onConfirm = { amount ->
                    showKeypad = false
                    submitRecord(amount)
                }
            )
        }
    }
}

@Composable
private fun DetailInputRow(
    icon: ImageVector,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MisaColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MisaColors.TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MisaColors.TextSecondary
        )
    }
}
