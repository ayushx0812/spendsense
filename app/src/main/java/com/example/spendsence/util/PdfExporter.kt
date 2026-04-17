package com.example.spendsence.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class PdfReportData(
    val monthLabel: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val incomeBySource: Map<String, Double>,
    val expenseByCategory: Map<String, Double>,
    val totalSavings: Double,
    val budgetUsageByCategory: Map<String, Pair<Double, Double>> // category -> (spent, limit)
)

/**
 * Generates a professional monthly PDF report using Android's built-in PdfDocument API.
 * Returns the File path of the saved PDF, or null if generation failed.
 */
fun generatePdfReport(context: Context, data: PdfReportData): File? {
    return try {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1976D2")
            textSize = 24f
            isFakeBoldText = true
        }
        val headingPaint = Paint().apply {
            color = Color.parseColor("#212121")
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#424242")
            textSize = 13f
        }
        val greenPaint = Paint().apply {
            color = Color.parseColor("#43A047")
            textSize = 13f
        }
        val redPaint = Paint().apply {
            color = Color.parseColor("#E53935")
            textSize = 13f
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }

        var y = 60f

        // ── Header ──────────────────────────────────────────────────────────
        canvas.drawText("SpendSense", 40f, y, titlePaint)
        y += 30f
        canvas.drawText("Monthly Report — ${data.monthLabel}", 40f, y, bodyPaint)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        // ── Summary ─────────────────────────────────────────────────────────
        canvas.drawText("Summary", 40f, y, headingPaint)
        y += 24f
        canvas.drawText("Total Income:", 40f, y, bodyPaint)
        canvas.drawText("₹${"%.2f".format(data.totalIncome)}", 300f, y, greenPaint)
        y += 20f
        canvas.drawText("Total Expense:", 40f, y, bodyPaint)
        canvas.drawText("₹${"%.2f".format(data.totalExpense)}", 300f, y, redPaint)
        y += 20f
        canvas.drawText("Net Balance:", 40f, y, bodyPaint)
        canvas.drawText("₹${"%.2f".format(data.balance)}", 300f, y, if (data.balance >= 0) greenPaint else redPaint)
        y += 20f
        canvas.drawText("Total Savings:", 40f, y, bodyPaint)
        canvas.drawText("₹${"%.2f".format(data.totalSavings)}", 300f, y, greenPaint)
        y += 12f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 28f

        // ── Income by Source ─────────────────────────────────────────────────
        canvas.drawText("Income by Source", 40f, y, headingPaint)
        y += 24f
        if (data.incomeBySource.isEmpty()) {
            canvas.drawText("No income recorded.", 40f, y, bodyPaint); y += 20f
        } else {
            data.incomeBySource.forEach { (src, amt) ->
                canvas.drawText(src, 40f, y, bodyPaint)
                canvas.drawText("₹${"%.2f".format(amt)}", 300f, y, greenPaint)
                y += 18f
            }
        }
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 28f

        // ── Expense by Category ───────────────────────────────────────────────
        canvas.drawText("Expense by Category", 40f, y, headingPaint)
        y += 24f
        if (data.expenseByCategory.isEmpty()) {
            canvas.drawText("No expenses recorded.", 40f, y, bodyPaint); y += 20f
        } else {
            data.expenseByCategory.forEach { (cat, amt) ->
                canvas.drawText(cat, 40f, y, bodyPaint)
                canvas.drawText("₹${"%.2f".format(amt)}", 300f, y, redPaint)
                y += 18f
            }
        }
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 28f

        // ── Budget Status ─────────────────────────────────────────────────────
        if (data.budgetUsageByCategory.isNotEmpty()) {
            canvas.drawText("Budget Status", 40f, y, headingPaint)
            y += 24f
            data.budgetUsageByCategory.forEach { (cat, spentLimit) ->
                val (spent, limit) = spentLimit
                val pct = if (limit > 0) (spent / limit * 100).toInt() else 0
                val paint = if (pct >= 100) redPaint else if (pct >= 80) Paint().apply {
                    color = Color.parseColor("#FFA000"); textSize = 13f
                } else greenPaint
                canvas.drawText("$cat:", 40f, y, bodyPaint)
                canvas.drawText("₹${"%.0f".format(spent)} / ₹${"%.0f".format(limit)} ($pct%)", 200f, y, paint)
                y += 18f
            }
            y += 10f
        }

        // Footer
        canvas.drawLine(40f, 812f, 555f, 812f, linePaint)
        canvas.drawText(
            "Generated by SpendSense • ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}",
            40f, 830f,
            Paint().apply { color = Color.GRAY; textSize = 10f }
        )

        pdfDoc.finishPage(page)

        // Save to Downloads folder
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "SpendSense_${data.monthLabel.replace(" ", "_")}.pdf"
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
