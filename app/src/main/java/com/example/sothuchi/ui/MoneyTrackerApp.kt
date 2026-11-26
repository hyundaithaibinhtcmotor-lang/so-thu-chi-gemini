package com.example.sothuchi.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sothuchi.data.local.AccountGroup
import com.example.sothuchi.data.local.BudgetProgress
import com.example.sothuchi.data.local.CategoryEntity
import com.example.sothuchi.data.local.FinancialRecordEntity
import com.example.sothuchi.data.local.RecordKind
import com.example.sothuchi.data.local.TransactionType
import com.example.sothuchi.data.local.TransactionWithDetails
import com.example.sothuchi.data.local.WalletEntity
import com.example.sothuchi.data.repository.MoneyRepository
import com.example.sothuchi.ui.components.exportTransactionsToCsvAndShare
import com.example.sothuchi.ui.components.formatDateTime
import com.example.sothuchi.ui.components.formatVnd
import com.example.sothuchi.ui.screens.MisaAccountsScreen
import com.example.sothuchi.ui.screens.MisaRecordScreen
import com.example.sothuchi.ui.screens.MisaReportsScreen
import com.example.sothuchi.ui.screens.categoryIconForKey
import com.example.sothuchi.ui.theme.MisaColors
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class MainNavTab(val label: String) {
    OVERVIEW("Tổng quan"),
    ACCOUNTS("Tài khoản"),
    RECORD("Ghi chép"),
    REPORTS("Báo cáo"),
    MORE("Khác")
}

@Composable
fun MoneyTrackerApp(repository: MoneyRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val now = remember { Calendar.getInstance() }
    val curYear = now.get(Calendar.YEAR)
    val curMonthZero = now.get(Calendar.MONTH)

    val wallets by repository.wallets.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by repository.transactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val monthExpense by repository.observeMonthExpense(curYear, curMonthZero).collectAsStateWithLifecycle(initialValue = 0L)
    val monthIncome by repository.observeMonthIncome(curYear, curMonthZero).collectAsStateWithLifecycle(initialValue = 0L)
    val budgetProgress by repository.observeBudgetProgressCurrentMonth().collectAsStateWithLifecycle(initialValue = emptyList())
    val financialRecords by repository.financialRecords.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by remember { mutableStateOf(MainNavTab.OVERVIEW) }
    var showAddWalletDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddRecordDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val totalBalance = wallets.filter { it.includeInReport }.sumOf { it.balance }
    val daysLeftInMonth = remember {
        val today = now.get(Calendar.DAY_OF_MONTH)
        val maxDay = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        (maxDay - today).coerceAtLeast(0)
    }

    Scaffold(
        containerColor = MisaColors.ScreenBackground,
        bottomBar = {
            MisaPillBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainNavTab.OVERVIEW -> MisaOverviewScreen(
                modifier = Modifier.padding(innerPadding),
                totalBalance = totalBalance,
                monthIncome = monthIncome,
                monthExpense = monthExpense,
                daysLeftInMonth = daysLeftInMonth,
                budgets = budgetProgress,
                recentTransactions = transactions.take(5),
                onDeleteTransaction = { id -> scope.launch { repository.deleteTransaction(id) } },
                onOpenReports = { selectedTab = MainNavTab.REPORTS }
            )

            MainNavTab.ACCOUNTS -> MisaAccountsScreen(
                modifier = Modifier.padding(innerPadding),
                wallets = wallets,
                records = financialRecords,
                onOpenAddWallet = { showAddWalletDialog = true },
                onDeleteWallet = { id -> scope.launch { repository.deleteWallet(id) } },
                onOpenAddRecord = { showAddRecordDialog = true }
            )

            MainNavTab.RECORD -> MisaRecordScreen(
                modifier = Modifier.padding(innerPadding),
                wallets = wallets,
                categories = categories,
                onOpenHistory = { selectedTab = MainNavTab.REPORTS },
                onOpenAddCategory = { showAddCategoryDialog = true },
                onSaveTransaction = { walletId, toWalletId, categoryId, type, amount, note, eventTrip, counterparty, location, isBorrowed, hasFee, exclude ->
                    scope.launch {
                        repository.addTransaction(
                            walletId = walletId,
                            toWalletId = toWalletId,
                            categoryId = categoryId,
                            type = type,
                            amount = amount,
                            note = note,
                            eventTrip = eventTrip,
                            counterparty = counterparty,
                            location = location,
                            isBorrowedToPay = isBorrowed,
                            hasFee = hasFee,
                            excludeFromReport = exclude
                        )
                        Toast.makeText(context, "Đã lưu ghi chép!", Toast.LENGTH_SHORT).show()
                        selectedTab = MainNavTab.OVERVIEW
                    }
                }
            )

            MainNavTab.REPORTS -> MisaReportsScreen(
                modifier = Modifier.padding(innerPadding),
                repository = repository,
                wallets = wallets,
                transactions = transactions,
                records = financialRecords,
                onDeleteTransaction = { id -> scope.launch { repository.deleteTransaction(id) } },
                onExportCsv = {
                    val res = exportTransactionsToCsvAndShare(context, transactions)
                    Toast.makeText(context, res.getOrElse { "Lỗi xuất file: ${it.message}" }, Toast.LENGTH_SHORT).show()
                }
            )

            MainNavTab.MORE -> MisaMoreScreen(
                modifier = Modifier.padding(innerPadding),
                budgets = budgetProgress,
                records = financialRecords,
                onOpenAddBudget = { showAddBudgetDialog = true },
                onDeleteBudget = { id -> scope.launch { repository.deleteBudget(id) } },
                onOpenAddRecord = { showAddRecordDialog = true },
                onToggleRecordSettled = { id, settled -> scope.launch { repository.setRecordSettled(id, settled) } },
                onDeleteRecord = { id -> scope.launch { repository.deleteFinancialRecord(id) } },
                onOpenAddCategory = { showAddCategoryDialog = true },
                onExportCsv = {
                    val res = exportTransactionsToCsvAndShare(context, transactions)
                    Toast.makeText(context, res.getOrElse { "Lỗi xuất file: ${it.message}" }, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onSave = { name, group, balance, includeInReport ->
                scope.launch {
                    repository.addWallet(name, group, balance, includeInReport)
                    showAddWalletDialog = false
                }
            }
        )
    }

    if (showAddBudgetDialog) {
        val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
        if (expenseCategories.isNotEmpty()) {
            AddBudgetDialog(
                expenseCategories = expenseCategories,
                onDismiss = { showAddBudgetDialog = false },
                onSave = { categoryId, limitAmount ->
                    scope.launch {
                        repository.saveBudget(categoryId, limitAmount)
                        showAddBudgetDialog = false
                    }
                }
            )
        }
    }

    if (showAddRecordDialog) {
        AddFinancialRecordDialog(
            onDismiss = { showAddRecordDialog = false },
            onSave = { kind, title, amount, note ->
                scope.launch {
                    repository.addFinancialRecord(kind, title, amount, note)
                    showAddRecordDialog = false
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, type, colorHex ->
                scope.launch {
                    repository.addCategory(name, type, colorHex)
                    showAddCategoryDialog = false
                }
            }
        )
    }
}

@Composable
private fun MisaPillBottomBar(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            color = MisaColors.BrandBlue,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    icon = Icons.Default.Home,
                    label = MainNavTab.OVERVIEW.label,
                    selected = selectedTab == MainNavTab.OVERVIEW,
                    onClick = { onSelectTab(MainNavTab.OVERVIEW) }
                )
                BottomBarItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = MainNavTab.ACCOUNTS.label,
                    selected = selectedTab == MainNavTab.ACCOUNTS,
                    onClick = { onSelectTab(MainNavTab.ACCOUNTS) }
                )

                // Nút "+" tròn trắng ở giữa cho Ghi chép như đúng ảnh
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(MainNavTab.RECORD) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ghi chép",
                            tint = MisaColors.BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = MainNavTab.RECORD.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == MainNavTab.RECORD) FontWeight.Bold else FontWeight.Medium
                    )
                }

                BottomBarItem(
                    icon = Icons.Default.PieChart,
                    label = MainNavTab.REPORTS.label,
                    selected = selectedTab == MainNavTab.REPORTS,
                    onClick = { onSelectTab(MainNavTab.REPORTS) }
                )
                BottomBarItem(
                    icon = Icons.Default.GridView,
                    label = MainNavTab.MORE.label,
                    selected = selectedTab == MainNavTab.MORE,
                    onClick = { onSelectTab(MainNavTab.MORE) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.72f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MisaOverviewScreen(
    modifier: Modifier,
    totalBalance: Long,
    monthIncome: Long,
    monthExpense: Long,
    daysLeftInMonth: Int,
    budgets: List<BudgetProgress>,
    recentTransactions: List<TransactionWithDetails>,
    onDeleteTransaction: (Long) -> Unit,
    onOpenReports: () -> Unit
) {
    val netSaving = monthIncome - monthExpense
    val maxBarValue = maxOf(monthIncome, monthExpense, 1L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MisaColors.ScreenBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MisaColors.HeaderBlueTop, MisaColors.HeaderBlueBottom)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text("Sổ Thu Chi MISA", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tài chính hiện tại", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatVnd(totalBalance), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MisaColors.BrandBlue)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TÌNH HÌNH THU CHI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MisaColors.TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp),
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val hRatio = (monthIncome.toFloat() / maxBarValue.toFloat()).coerceIn(0.08f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .fillMaxHeight(hRatio)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(MisaColors.IncomeGreen)
                                )
                                val eRatio = (monthExpense.toFloat() / maxBarValue.toFloat()).coerceIn(0.08f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .fillMaxHeight(eRatio)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(MisaColors.ExpenseRed)
                                )
                            }
                            Column(modifier = Modifier.weight(1.35f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Thu", color = MisaColors.TextSecondary)
                                    Text(formatVnd(monthIncome), color = MisaColors.IncomeGreen, fontWeight = FontWeight.Bold)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Chi", color = MisaColors.TextSecondary)
                                    Text(formatVnd(monthExpense), color = MisaColors.ExpenseRed, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(color = MisaColors.DividerSubtle)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tích lũy", color = MisaColors.TextSecondary)
                                    Text(formatVnd(netSaving), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (budgets.isNotEmpty()) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("HẠN MỨC CHI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MisaColors.TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            budgets.take(2).forEachIndexed { idx, b ->
                                if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MisaColors.DividerSubtle)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(b.categoryName, fontWeight = FontWeight.SemiBold)
                                        Text("Còn $daysLeftInMonth ngày", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                                    }
                                    Text(formatVnd(b.limitAmount), fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { b.ratio.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (b.spentAmount > b.limitAmount) MisaColors.ExpenseRed else MisaColors.BrandBlue
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GHI CHÉP GẦN ĐÂY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MisaColors.TextSecondary)
                            IconButton(onClick = onOpenReports, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MisaColors.TextSecondary)
                            }
                        }
                        if (recentTransactions.isEmpty()) {
                            Text("Chưa có giao dịch gần đây", color = MisaColors.TextSecondary)
                        } else {
                            recentTransactions.forEachIndexed { idx, tx ->
                                if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MisaColors.DividerSubtle)
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(tx.categoryColor).copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(categoryIconForKey(tx.categoryIconKey), contentDescription = null, tint = Color(tx.categoryColor), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tx.categoryName, fontWeight = FontWeight.SemiBold)
                                        Text("${tx.walletName} • ${formatDateTime(tx.timestamp)}", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                                    }
                                    val color = when (tx.type) {
                                        TransactionType.EXPENSE -> MisaColors.ExpenseRed
                                        TransactionType.INCOME -> MisaColors.IncomeGreen
                                        TransactionType.TRANSFER -> MisaColors.BrandBlue
                                    }
                                    val prefix = when (tx.type) {
                                        TransactionType.EXPENSE -> "-"
                                        TransactionType.INCOME -> "+"
                                        TransactionType.TRANSFER -> ""
                                    }
                                    Text("$prefix${formatVnd(tx.amount)}", fontWeight = FontWeight.Bold, color = color)
                                    IconButton(onClick = { onDeleteTransaction(tx.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = MisaColors.TextSecondary, modifier = Modifier.size(18.dp))
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

@Composable
private fun MisaMoreScreen(
    modifier: Modifier,
    budgets: List<BudgetProgress>,
    records: List<FinancialRecordEntity>,
    onOpenAddBudget: () -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onOpenAddRecord: () -> Unit,
    onToggleRecordSettled: (Long, Boolean) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onOpenAddCategory: () -> Unit,
    onExportCsv: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tiện ích mở rộng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onOpenAddBudget, modifier = Modifier.weight(1f)) { Text("+ Hạn mức chi") }
                        OutlinedButton(onClick = onOpenAddRecord, modifier = Modifier.weight(1f)) { Text("+ Vay / Tiết kiệm") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenAddCategory, modifier = Modifier.weight(1f)) { Text("+ Hạng mục") }
                        OutlinedButton(onClick = onExportCsv, modifier = Modifier.weight(1f)) { Text("Xuất file CSV") }
                    }
                }
            }
        }

        if (budgets.isNotEmpty()) {
            items(budgets, key = { it.budgetId }) { b ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${b.categoryName}: ${formatVnd(b.spentAmount)} / ${formatVnd(b.limitAmount)}", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(progress = { b.ratio.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                        }
                        IconButton(onClick = { onDeleteBudget(b.budgetId) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa")
                        }
                    }
                }
            }
        }

        items(records, key = { it.id }) { rec ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rec.isSettled, onCheckedChange = { onToggleRecordSettled(rec.id, it) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text("[${rec.kind.label}] ${rec.title}", fontWeight = FontWeight.SemiBold)
                        Text(formatVnd(rec.amount), style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                    }
                    IconButton(onClick = { onDeleteRecord(rec.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWalletDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, group: AccountGroup, balance: Long, includeInReport: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(AccountGroup.CASH) }
    var balanceText by remember { mutableStateOf("0") }
    var includeInReport by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm tài khoản mới") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên tài khoản (VD: Ví chung, TK VCB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AccountGroup.entries) { grp ->
                        FilterChip(
                            selected = selectedGroup == grp,
                            onClick = { selectedGroup = grp },
                            label = { Text(grp.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { raw -> balanceText = raw.filter { it.isDigit() || it == '-' } },
                    label = { Text("Số dư ban đầu (VND)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tính vào báo cáo")
                    Switch(checked = includeInReport, onCheckedChange = { includeInReport = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toLongOrNull() ?: 0L
                    if (name.isNotBlank()) onSave(name, selectedGroup, bal, includeInReport)
                }
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun AddBudgetDialog(
    expenseCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, limitAmount: Long) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(expenseCategories.first().id) }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thiết lập hạn mức chi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(expenseCategories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = cat.id == selectedCategoryId,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { raw -> limitText = raw.filter { it.isDigit() } },
                    label = { Text("Hạn mức tháng (VND)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toLongOrNull() ?: 0L
                    if (limit > 0L) onSave(selectedCategoryId, limit)
                }
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun AddFinancialRecordDialog(
    onDismiss: () -> Unit,
    onSave: (kind: RecordKind, title: String, amount: Long, note: String) -> Unit
) {
    var kind by remember { mutableStateOf(RecordKind.LEND) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sổ vay nợ / tiết kiệm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RecordKind.entries) { item ->
                        FilterChip(selected = kind == item, onClick = { kind = item }, label = { Text(item.label) })
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên khoản") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw -> amountText = raw.filter { it.isDigit() } },
                    label = { Text("Số tiền (VND)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (title.isNotBlank() && amount > 0L) onSave(kind, title, amount, note)
                }
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: TransactionType, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    val palette = listOf(0xFFE53935, 0xFFFB8C00, 0xFF43A047, 0xFF1E88E5, 0xFFEC407A)
    var selectedColor by remember { mutableStateOf(palette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm hạng mục") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE }, label = { Text("Chi tiền") })
                    FilterChip(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME }, label = { Text("Thu tiền") })
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên hạng mục") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name, type, selectedColor) }) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
