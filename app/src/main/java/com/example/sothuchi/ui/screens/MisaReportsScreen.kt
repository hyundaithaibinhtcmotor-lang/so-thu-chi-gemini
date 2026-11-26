package com.example.sothuchi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sothuchi.data.local.AccountGroup
import com.example.sothuchi.data.local.CategoryExpenseSlice
import com.example.sothuchi.data.local.FinancialRecordEntity
import com.example.sothuchi.data.local.RecordKind
import com.example.sothuchi.data.local.TransactionType
import com.example.sothuchi.data.local.TransactionWithDetails
import com.example.sothuchi.data.local.WalletEntity
import com.example.sothuchi.data.repository.MoneyRepository
import com.example.sothuchi.ui.components.formatCompactK
import com.example.sothuchi.ui.components.formatDateTime
import com.example.sothuchi.ui.components.formatVnd
import com.example.sothuchi.ui.theme.MisaColors
import java.util.Calendar

enum class ReportSubPage(val title: String) {
    HUB("Báo cáo"),
    CURRENT_FINANCE("Tài chính hiện tại"),
    INCOME_EXPENSE_TREND("Tình hình thu chi"),
    EXPENSE_ANALYSIS("Phân tích chi tiêu"),
    INCOME_ANALYSIS("Phân tích thu"),
    DEBT_TRACKING("Theo dõi vay nợ"),
    COUNTERPARTY("Đối tượng thu/chi"),
    EVENT_TRIP("Chuyến đi, sự kiện"),
    FINANCIAL_ANALYSIS("Phân tích tài chính")
}

@Composable
fun MisaReportsScreen(
    modifier: Modifier = Modifier,
    repository: MoneyRepository,
    wallets: List<WalletEntity>,
    transactions: List<TransactionWithDetails>,
    records: List<FinancialRecordEntity>,
    onDeleteTransaction: (Long) -> Unit,
    onExportCsv: () -> Unit
) {
    var activePage by remember { mutableStateOf(ReportSubPage.HUB) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val now = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var selectedMonthZero by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }

    val monthIncome by repository.observeMonthIncome(selectedYear, selectedMonthZero).collectAsStateWithLifecycle(initialValue = 0L)
    val monthExpense by repository.observeMonthExpense(selectedYear, selectedMonthZero).collectAsStateWithLifecycle(initialValue = 0L)
    val expenseSlices by repository.observeByCategoryInMonth(TransactionType.EXPENSE, selectedYear, selectedMonthZero).collectAsStateWithLifecycle(initialValue = emptyList())
    val incomeSlices by repository.observeByCategoryInMonth(TransactionType.INCOME, selectedYear, selectedMonthZero).collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MisaColors.ScreenBackground)
    ) {
        if (activePage == ReportSubPage.HUB) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCE6FA))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Báo cáo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MisaColors.TextPrimary
                )
            }
        } else {
            // Sub-page Top Bar như ảnh 4, 5, 6, 7: [<-] [Dropdown pill v] [Share/Export]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCE6FA))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activePage = ReportSubPage.HUB }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = MisaColors.TextPrimary)
                }

                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activePage.title,
                                fontWeight = FontWeight.Bold,
                                color = MisaColors.BrandBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MisaColors.BrandBlue)
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        ReportSubPage.entries.filter { it != ReportSubPage.HUB }.forEach { page ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(page.title)
                                        if (page == activePage) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MisaColors.BrandBlue)
                                        }
                                    }
                                },
                                onClick = {
                                    activePage = page
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = onExportCsv) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Xuất báo cáo", tint = MisaColors.TextPrimary)
                }
            }
        }

        when (activePage) {
            ReportSubPage.HUB -> ReportHubContent(
                wallets = wallets,
                records = records,
                transactions = transactions,
                selectedYear = selectedYear,
                selectedMonthZero = selectedMonthZero,
                monthIncome = monthIncome,
                monthExpense = monthExpense,
                onPrevMonth = {
                    if (selectedMonthZero == 0) {
                        selectedMonthZero = 11
                        selectedYear -= 1
                    } else {
                        selectedMonthZero -= 1
                    }
                },
                onNextMonth = {
                    if (selectedMonthZero == 11) {
                        selectedMonthZero = 0
                        selectedYear += 1
                    } else {
                        selectedMonthZero += 1
                    }
                },
                onNavigatePage = { activePage = it }
            )

            ReportSubPage.CURRENT_FINANCE -> CurrentFinanceSubPage(wallets = wallets, records = records)

            ReportSubPage.EXPENSE_ANALYSIS -> CategoryChartSubPage(
                isExpense = true,
                year = selectedYear,
                totalAmount = monthExpense,
                slices = expenseSlices,
                transactions = transactions.filter { it.type == TransactionType.EXPENSE },
                onDeleteTransaction = onDeleteTransaction
            )

            ReportSubPage.INCOME_ANALYSIS -> CategoryChartSubPage(
                isExpense = false,
                year = selectedYear,
                totalAmount = monthIncome,
                slices = incomeSlices,
                transactions = transactions.filter { it.type == TransactionType.INCOME },
                onDeleteTransaction = onDeleteTransaction
            )

            ReportSubPage.INCOME_EXPENSE_TREND -> IncomeExpenseTrendSubPage(
                year = selectedYear,
                transactions = transactions
            )

            else -> GenericReportListSubPage(
                page = activePage,
                transactions = transactions,
                records = records,
                onDeleteTransaction = onDeleteTransaction
            )
        }
    }
}

@Composable
private fun ReportHubContent(
    wallets: List<WalletEntity>,
    records: List<FinancialRecordEntity>,
    transactions: List<TransactionWithDetails>,
    selectedYear: Int,
    selectedMonthZero: Int,
    monthIncome: Long,
    monthExpense: Long,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigatePage: (ReportSubPage) -> Unit
) {
    val totalAssets = wallets.filter { it.balance > 0 }.sumOf { it.balance } +
        records.filter { it.kind == RecordKind.LEND && !it.isSettled }.sumOf { it.amount }
    val totalDebts = wallets.filter { it.balance < 0 }.sumOf { -it.balance } +
        records.filter { it.kind == RecordKind.BORROW && !it.isSettled }.sumOf { it.amount }
    val netFinance = totalAssets - totalDebts

    // Tính số tiền thu/chi theo từng ngày trong tháng để hiển thị trên lưới lịch (ảnh 3)
    val dailyNetMap = remember(transactions, selectedYear, selectedMonthZero) {
        val cal = Calendar.getInstance()
        val map = mutableMapOf<Int, Long>()
        transactions.filter { !it.excludeFromReport && it.type != TransactionType.TRANSFER }.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            if (cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonthZero) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val delta = if (tx.type == TransactionType.INCOME) tx.amount else -tx.amount
                map[day] = (map[day] ?: 0L) + delta
            }
        }
        map
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card xanh gradient "Tài chính hiện tại" + "Tổng có" + "Tổng nợ"
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MisaColors.HeaderBlueTop, MisaColors.HeaderBlueBottom)
                        )
                    )
                    .clickable { onNavigatePage(ReportSubPage.CURRENT_FINANCE) }
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Tài chính hiện tại", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatVnd(netFinance),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng có", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatVnd(totalAssets), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng nợ", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatVnd(totalDebts), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Card Tình hình thu chi 5 tháng gần nhất
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigatePage(ReportSubPage.INCOME_EXPENSE_TREND) },
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tình hình thu chi", fontWeight = FontWeight.Bold)
                        Text("Xem chi tiết", color = MisaColors.BrandBlue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Text("5 tháng gần nhất", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val heights = listOf(0.85f, 0.2f, 0.45f, 0.15f, 0.25f)
                            val monthLabels = (4 downTo 0).map { offset ->
                                val m = ((selectedMonthZero - offset + 12) % 12) + 1
                                m.toString()
                            }
                            heights.forEachIndexed { idx, ratio ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .fillMaxHeight(ratio)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(MisaColors.IncomeGreen)
                                    )
                                    Text(monthLabels[idx], style = MaterialTheme.typography.labelSmall, color = MisaColors.TextSecondary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (monthExpense == 0L) "Chi tiêu tháng hiện tại bằng 0, chưa có dữ liệu so sánh"
                            else "Chi tiêu tháng ${selectedMonthZero + 1}: ${formatVnd(monthExpense)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MisaColors.TextSecondary,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        // Card Lịch thu chi theo tháng (ảnh 3)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = MisaColors.TextSecondary)
                        }
                        Text("tháng ${selectedMonthZero + 1} $selectedYear", fontWeight = FontWeight.Bold)
                        IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = MisaColors.TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tổng thu | Tổng chi | Chênh lệch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MisaColors.ScreenBackground)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tổng thu", style = MaterialTheme.typography.labelSmall, color = MisaColors.TextSecondary)
                            Text(formatVnd(monthIncome), color = MisaColors.IncomeGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tổng chi", style = MaterialTheme.typography.labelSmall, color = MisaColors.TextSecondary)
                            Text(formatVnd(monthExpense), color = MisaColors.ExpenseRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Chênh lệch", style = MaterialTheme.typography.labelSmall, color = MisaColors.TextSecondary)
                            Text(formatVnd(monthIncome - monthExpense), color = MisaColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Header T2..CN
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { d ->
                            Text(
                                text = d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MisaColors.TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Lưới ngày trong tháng
                    val firstDayCal = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonthZero, 1)
                    }
                    val maxDays = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    // Calendar.SUNDAY = 1 -> offset 6; MONDAY = 2 -> offset 0
                    val leadingBlanks = (firstDayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val cells = List(leadingBlanks) { null } + (1..maxDays).toList()

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        cells.chunked(7).forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                week.forEach { day ->
                                    if (day == null) {
                                        Spacer(modifier = Modifier.weight(1f).height(42.dp))
                                    } else {
                                        val netDay = dailyNetMap[day] ?: 0L
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .border(0.5.dp, MisaColors.DividerSubtle, RoundedCornerShape(4.dp))
                                                .padding(2.dp)
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = MisaColors.TextPrimary
                                            )
                                            if (netDay != 0L) {
                                                Text(
                                                    text = formatCompactK(kotlin.math.abs(netDay)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (netDay > 0) MisaColors.IncomeGreen else MisaColors.ExpenseRed,
                                                    modifier = Modifier.align(Alignment.BottomEnd)
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(7 - week.size) {
                                    Spacer(modifier = Modifier.weight(1f).height(42.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigatePage(ReportSubPage.EXPENSE_ANALYSIS) },
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Lịch sử ghi chép",
                            color = MisaColors.BrandBlue,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 6 ô chức năng phân tích (2 cột x 3 hàng) như nửa dưới ảnh 3
        val gridTiles = listOf(
            Triple(ReportSubPage.EXPENSE_ANALYSIS, "Phân tích chi tiêu", Icons.Default.BarChart),
            Triple(ReportSubPage.INCOME_ANALYSIS, "Phân tích thu", Icons.Default.TrendingUp),
            Triple(ReportSubPage.DEBT_TRACKING, "Theo dõi vay nợ", Icons.Default.FormatListNumbered),
            Triple(ReportSubPage.COUNTERPARTY, "Đối tượng thu/chi", Icons.Default.PeopleOutline),
            Triple(ReportSubPage.EVENT_TRIP, "Chuyến đi/Sự kiện", Icons.Default.Event),
            Triple(ReportSubPage.FINANCIAL_ANALYSIS, "Phân tích tài chính", Icons.Default.Savings)
        )

        items(gridTiles.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { (page, label, icon) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigatePage(page) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(icon, contentDescription = null, tint = MisaColors.BrandBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(label, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentFinanceSubPage(
    wallets: List<WalletEntity>,
    records: List<FinancialRecordEntity>
) {
    val cashTotal = wallets.filter { it.groupType == AccountGroup.CASH }.sumOf { it.balance }
    val bankTotal = wallets.filter { it.groupType == AccountGroup.BANK }.sumOf { it.balance }
    val investTotal = wallets.filter { it.groupType == AccountGroup.INVESTMENT }.sumOf { it.balance }
    val savingTotal = wallets.filter { it.groupType == AccountGroup.SAVING }.sumOf { it.balance }
    val otherTotal = wallets.filter { it.groupType == AccountGroup.OTHER }.sumOf { it.balance }
    val lendTotal = records.filter { it.kind == RecordKind.LEND && !it.isSettled }.sumOf { it.amount }

    val creditCardDebt = wallets.filter { it.groupType == AccountGroup.CREDIT_CARD && it.balance < 0 }.sumOf { -it.balance }
    val borrowDebt = records.filter { it.kind == RecordKind.BORROW && !it.isSettled }.sumOf { it.amount }

    val totalHave = cashTotal + bankTotal + investTotal + savingTotal + otherTotal + lendTotal
    val totalDebt = creditCardDebt + borrowDebt
    val currentFinance = totalHave - totalDebt

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tài chính hiện tại (1) - (2)", color = MisaColors.TextSecondary)
                    Text(formatVnd(currentFinance), fontWeight = FontWeight.Bold, color = MisaColors.BrandBlue)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tổng tiền (1)", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatVnd(totalHave), fontWeight = FontWeight.Bold)
                    }
                }
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tổng nợ (2)", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatVnd(totalDebt), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Tổng có (1)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }

        val haveRows = listOf(
            Triple("Tiền mặt", cashTotal, Icons.Default.Payments to Color(0xFF00ACC1)),
            Triple("Tài khoản ngân hàng", bankTotal, Icons.Default.AccountBalance to Color(0xFF1E88E5)),
            Triple("Tài khoản đầu tư", investTotal, Icons.Default.TrendingUp to Color(0xFFFB8C00)),
            Triple("Tài khoản tiết kiệm", savingTotal, Icons.Default.Savings to Color(0xFFEC407A)),
            Triple("Khác", otherTotal, Icons.Default.AccountBalanceWallet to Color(0xFF29B6F6)),
            Triple("Cho vay", lendTotal, Icons.Default.Payments to Color(0xFF26A69A))
        )

        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    haveRows.forEachIndexed { idx, (label, amount, iconPair) ->
                        if (idx > 0) HorizontalDivider(color = MisaColors.DividerSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(iconPair.second.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(iconPair.first, contentDescription = null, tint = iconPair.second, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text(formatVnd(amount), fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MisaColors.TextSecondary)
                        }
                    }
                }
            }
        }

        item {
            Text("Tổng nợ (2)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }

        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFFFB8C00))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Thẻ tín dụng", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Text(formatVnd(creditCardDebt), fontWeight = FontWeight.SemiBold)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MisaColors.TextSecondary)
                    }
                    HorizontalDivider(color = MisaColors.DividerSubtle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF00ACC1))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Nợ phải trả", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Text(formatVnd(borrowDebt), fontWeight = FontWeight.SemiBold)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MisaColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChartSubPage(
    isExpense: Boolean,
    year: Int,
    totalAmount: Long,
    slices: List<CategoryExpenseSlice>,
    transactions: List<TransactionWithDetails>,
    onDeleteTransaction: (Long) -> Unit
) {
    var periodTab by remember { mutableIntStateOf(1) } // 0: Ngày, 1: Tháng, 2: Năm

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Thanh tab Ngày | Tháng | Năm như ảnh 5 & 6
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Ngày", "Tháng", "Năm").forEachIndexed { idx, label ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { periodTab = idx },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (periodTab == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (periodTab == idx) MisaColors.BrandBlue else MisaColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(if (periodTab == idx) MisaColors.BrandBlue else Color.Transparent)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MisaColors.TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("01/$year - 12/$year", fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Biểu đồ cột màu xanh dương tươi (#00AEEF) như ảnh 5 & 6
                    val maxSlice = maxOf(slices.maxOfOrNull { it.totalAmount } ?: 1L, 1L)
                    if (slices.isEmpty()) {
                        Text("Chưa có dữ liệu phát sinh trong kỳ", color = MisaColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            slices.take(6).forEach { slice ->
                                val ratio = (slice.totalAmount.toFloat() / maxSlice.toFloat()).coerceIn(0.12f, 1f)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .fillMaxHeight(ratio)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(MisaColors.ChartCyan)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MisaColors.DividerSubtle)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isExpense) "Tổng chi tiêu" else "Tổng thu", color = MisaColors.TextSecondary)
                        Text(formatVnd(totalAmount), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isExpense) "Trung bình chi/tháng" else "Trung bình thu/tháng", color = MisaColors.TextSecondary)
                        Text(formatVnd(totalAmount / 12L), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (slices.isNotEmpty()) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        slices.forEachIndexed { idx, slice ->
                            if (idx > 0) HorizontalDivider(color = MisaColors.DividerSubtle)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(slice.categoryName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Text(
                                    text = formatVnd(slice.totalAmount),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isExpense) MisaColors.ExpenseRed else MisaColors.IncomeGreen
                                )
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MisaColors.TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        items(transactions, key = { it.id }) { tx ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tx.categoryName, fontWeight = FontWeight.SemiBold)
                        Text("${tx.walletName} • ${formatDateTime(tx.timestamp)}", style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                    }
                    Text(
                        text = (if (isExpense) "-" else "+") + formatVnd(tx.amount),
                        fontWeight = FontWeight.Bold,
                        color = if (isExpense) MisaColors.ExpenseRed else MisaColors.IncomeGreen
                    )
                    IconButton(onClick = { onDeleteTransaction(tx.id) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = MisaColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeExpenseTrendSubPage(
    year: Int,
    transactions: List<TransactionWithDetails>
) {
    val monthlyStats = remember(transactions, year) {
        val cal = Calendar.getInstance()
        (12 downTo 1).map { monthOneBased ->
            val mZero = monthOneBased - 1
            var inc = 0L
            var exp = 0L
            transactions.filter { !it.excludeFromReport }.forEach { tx ->
                cal.timeInMillis = tx.timestamp
                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == mZero) {
                    if (tx.type == TransactionType.INCOME) inc += tx.amount
                    if (tx.type == TransactionType.EXPENSE) exp += tx.amount
                }
            }
            Triple(monthOneBased, inc, exp)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(monthlyStats, key = { it.first }) { (monthNum, inc, exp) ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tháng $monthNum", fontWeight = FontWeight.Bold)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatVnd(inc), color = MisaColors.IncomeGreen, fontWeight = FontWeight.SemiBold)
                        Text(formatVnd(exp), color = MisaColors.ExpenseRed, fontWeight = FontWeight.SemiBold)
                        Text(formatVnd(inc - exp), color = MisaColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenericReportListSubPage(
    page: ReportSubPage,
    transactions: List<TransactionWithDetails>,
    records: List<FinancialRecordEntity>,
    onDeleteTransaction: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (page == ReportSubPage.DEBT_TRACKING) {
            items(records, key = { it.id }) { rec ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("[${rec.kind.label}] ${rec.title}", fontWeight = FontWeight.SemiBold)
                            if (rec.note.isNotBlank()) Text(rec.note, style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                        }
                        Text(formatVnd(rec.amount), fontWeight = FontWeight.Bold, color = MisaColors.BrandBlue)
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.categoryName, fontWeight = FontWeight.SemiBold)
                            val meta = listOf(tx.counterparty, tx.eventTrip, tx.location, tx.note).filter { it.isNotBlank() }.joinToString(" • ")
                            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MisaColors.TextSecondary)
                        }
                        Text(formatVnd(tx.amount), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onDeleteTransaction(tx.id) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = MisaColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
