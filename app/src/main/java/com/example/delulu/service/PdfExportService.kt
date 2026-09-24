package com.example.delulu.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.delulu.data.model.NoteEntity
import com.example.delulu.data.model.TestEntity
import com.example.delulu.data.model.TimetableEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExportService(private val context: Context) {

    suspend fun exportQuestionAnswerToPdf(
        question: String,
        solution: SolvedQuestionResult
    ): File? = withContext(Dispatchers.IO) {
        try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(79, 70, 229) // Indigo
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 12f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 11f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val highlightPaint = Paint().apply {
                color = Color.rgb(16, 185, 129) // Emerald
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            var y = 50f

            // Brand Header
            canvas.drawText("DELULU Student Learning System", 40f, y, titlePaint)
            y += 20f
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Subject: ${solution.subject} | Topic: ${solution.topic} | Generated: $dateStr", 40f, y, subtitlePaint)
            y += 30f

            // Question Box
            canvas.drawText("QUESTION:", 40f, y, headerPaint)
            y += 18f
            val questionLines = wrapText(question, 500f, bodyPaint)
            for (line in questionLines) {
                canvas.drawText(line, 40f, y, bodyPaint)
                y += 16f
            }
            y += 15f

            // Final Answer
            canvas.drawText("FINAL ANSWER / KEY TAKEAWAY:", 40f, y, highlightPaint)
            y += 18f
            val answerLines = wrapText(solution.finalAnswer, 500f, bodyPaint)
            for (line in answerLines) {
                canvas.drawText(line, 40f, y, bodyPaint)
                y += 16f
            }
            y += 20f

            // Step-by-Step Explanation
            canvas.drawText("STEP-BY-STEP EXPLANATION:", 40f, y, headerPaint)
            y += 18f
            for (step in solution.stepByStep) {
                val stepLines = wrapText(step, 500f, bodyPaint)
                for (line in stepLines) {
                    canvas.drawText(line, 40f, y, bodyPaint)
                    y += 16f
                }
                y += 6f
            }
            y += 15f

            // Important Points
            if (solution.importantPoints.isNotEmpty()) {
                canvas.drawText("IMPORTANT CONCEPTS:", 40f, y, headerPaint)
                y += 18f
                for (pt in solution.importantPoints) {
                    val ptLines = wrapText("• $pt", 500f, bodyPaint)
                    for (line in ptLines) {
                        canvas.drawText(line, 40f, y, bodyPaint)
                        y += 16f
                    }
                }
                y += 15f
            }

            // Common Mistakes
            if (solution.commonMistake.isNotBlank()) {
                canvas.drawText("COMMON EXAM MISTAKE TO AVOID:", 40f, y, headerPaint)
                y += 18f
                val mistakeLines = wrapText("⚠️ ${solution.commonMistake}", 500f, bodyPaint)
                for (line in mistakeLines) {
                    canvas.drawText(line, 40f, y, bodyPaint)
                    y += 16f
                }
                y += 15f
            }

            // Practice Question
            if (solution.practiceQuestion.isNotBlank()) {
                canvas.drawText("FOLLOW-UP PRACTICE QUESTION:", 40f, y, headerPaint)
                y += 18f
                val pqLines = wrapText(solution.practiceQuestion, 500f, bodyPaint)
                for (line in pqLines) {
                    canvas.drawText(line, 40f, y, bodyPaint)
                    y += 16f
                }
            }

            doc.finishPage(page)

            // Save PDF locally
            val fileDir = File(context.filesDir, "delulu_pdfs")
            if (!fileDir.exists()) fileDir.mkdirs()
            val fileName = "Delulu_Solution_${System.currentTimeMillis()}.pdf"
            val targetFile = File(fileDir, fileName)
            val outputStream = FileOutputStream(targetFile)
            doc.writeTo(outputStream)
            outputStream.close()
            doc.close()

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportNoteToPdf(note: NoteEntity): File? = withContext(Dispatchers.IO) {
        try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(79, 70, 229)
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val metaPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 12f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 13f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            var y = 60f
            canvas.drawText(note.title, 40f, y, titlePaint)
            y += 24f
            canvas.drawText("Subject: ${note.subject} | Chapter: ${note.chapter.ifBlank { "General" }} | Tags: ${note.tags}", 40f, y, metaPaint)
            y += 35f

            val contentLines = note.content.split("\n")
            for (rawLine in contentLines) {
                val wrapped = wrapText(rawLine, 515f, bodyPaint)
                for (line in wrapped) {
                    canvas.drawText(line, 40f, y, bodyPaint)
                    y += 18f
                }
            }

            doc.finishPage(page)
            val fileDir = File(context.filesDir, "delulu_pdfs")
            if (!fileDir.exists()) fileDir.mkdirs()
            val targetFile = File(fileDir, "Note_${note.id}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(targetFile)
            doc.writeTo(outputStream)
            outputStream.close()
            doc.close()
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportTimetableToPdf(routine: String, timetable: List<TimetableEntity>): File? = withContext(Dispatchers.IO) {
        try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(79, 70, 229)
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val rowPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 11f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            var y = 60f
            canvas.drawText("DELULU Study Timetable - $routine", 40f, y, titlePaint)
            y += 30f

            canvas.drawText("Time", 40f, y, headerPaint)
            canvas.drawText("Activity / Subject", 180f, y, headerPaint)
            canvas.drawText("Category", 420f, y, headerPaint)
            y += 15f
            canvas.drawLine(40f, y, 550f, y, rowPaint)
            y += 20f

            for (entry in timetable) {
                canvas.drawText("${entry.startTime} - ${entry.endTime}", 40f, y, rowPaint)
                canvas.drawText(entry.title, 180f, y, rowPaint)
                canvas.drawText(entry.category, 420f, y, rowPaint)
                y += 24f
            }

            doc.finishPage(page)
            val fileDir = File(context.filesDir, "delulu_pdfs")
            if (!fileDir.exists()) fileDir.mkdirs()
            val targetFile = File(fileDir, "Timetable_${routine.replace(" ", "_")}.pdf")
            val outputStream = FileOutputStream(targetFile)
            doc.writeTo(outputStream)
            outputStream.close()
            doc.close()
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val width = paint.measureText(candidate)
            if (width <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    lines.add(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share DELULU PDF Document")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
