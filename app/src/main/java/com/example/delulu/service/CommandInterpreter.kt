package com.example.delulu.service

import java.text.SimpleDateFormat
import java.util.*

enum class ActionType {
    CREATE_TASK,
    CREATE_NOTE,
    CREATE_REMINDER,
    CREATE_SUBJECT,
    CREATE_TIMETABLE,
    CREATE_TEST,
    EXPORT_PDF,
    DELETE_DATA,
    UNKNOWN
}

data class InterpretedCommand(
    val rawText: String,
    val actionType: ActionType,
    val subject: String? = null,
    val title: String? = null,
    val content: String? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val isDestructive: Boolean = false,
    val confirmationPrompt: String,
    val clarificationNeeded: String? = null
)

class CommandInterpreter {

    fun interpret(rawInput: String): InterpretedCommand {
        val input = rawInput.trim()
        val lower = input.lowercase()

        // 1. Destructive Commands Check
        if (lower.contains("delete all") || lower.contains("clear notes") || lower.contains("reset database") || lower.contains("remove all")) {
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.DELETE_DATA,
                isDestructive = true,
                confirmationPrompt = "⚠️ DANGER: You are requesting to permanently delete or clear stored data. This action CANNOT be undone. Are you sure you want to proceed?"
            )
        }

        // 2. Task creation
        if (lower.startsWith("add a") && (lower.contains("task") || lower.contains("homework") || lower.contains("assignment")) ||
            lower.startsWith("create a") && (lower.contains("task") || lower.contains("homework")) ||
            lower.contains("homework called") || lower.contains("task called")) {

            val subject = extractSubject(input)
            val title = extractTitle(input, listOf("called", "named", "to", "for"))
            val due = extractDueDate(input)

            if (title.isBlank()) {
                return InterpretedCommand(
                    rawText = input,
                    actionType = ActionType.CREATE_TASK,
                    clarificationNeeded = "I understood that you want to create a homework task, but what should its title be?",
                    confirmationPrompt = ""
                )
            }

            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.CREATE_TASK,
                subject = subject ?: "General",
                title = title,
                dueDate = due,
                confirmationPrompt = "Confirm adding new homework task:\n\n• Subject: ${subject ?: "General"}\n• Title: $title\n• Due: ${due ?: "Today"}"
            )
        }

        // 3. Subject creation
        if (lower.contains("add a subject") || lower.contains("add subject") || lower.contains("new subject")) {
            val subjectName = extractAfterKeywords(input, listOf("called", "named", "subject"))
            if (subjectName.isBlank()) {
                return InterpretedCommand(
                    rawText = input,
                    actionType = ActionType.CREATE_SUBJECT,
                    clarificationNeeded = "I understood that you want to add a new subject, but what is the subject name?",
                    confirmationPrompt = ""
                )
            }
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.CREATE_SUBJECT,
                title = subjectName,
                confirmationPrompt = "Confirm adding new subject:\n\n• Name: $subjectName"
            )
        }

        // 4. Note creation
        if (lower.startsWith("create a") && lower.contains("note") || lower.startsWith("add a") && lower.contains("note")) {
            val subject = extractSubject(input)
            val title = extractTitle(input, listOf("called", "named", "on", "about"))
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.CREATE_NOTE,
                subject = subject ?: "General",
                title = title.ifBlank { "Quick Note" },
                confirmationPrompt = "Confirm creating new note:\n\n• Subject: ${subject ?: "General"}\n• Title: ${title.ifBlank { "Quick Note" }}"
            )
        }

        // 5. Reminder creation
        if (lower.contains("reminder") || lower.contains("remind me")) {
            val timeStr = extractTime(input)
            val title = extractTitle(input, listOf("to", "about", "for", "called")).ifBlank { "Study Reminder" }
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.CREATE_REMINDER,
                title = title,
                dueTime = timeStr ?: "07:00 PM",
                confirmationPrompt = "Confirm setting local reminder:\n\n• Alert: $title\n• Time: ${timeStr ?: "07:00 PM"}"
            )
        }

        // 6. Timetable
        if (lower.contains("timetable") || lower.contains("schedule")) {
            val day = when {
                lower.contains("tomorrow") -> "Tomorrow"
                lower.contains("today") -> "Today"
                lower.contains("monday") -> "Monday"
                lower.contains("sunday") -> "Sunday"
                lower.contains("weekend") -> "Weekend"
                lower.contains("holiday") -> "Holiday"
                else -> null
            }
            if (day == null) {
                return InterpretedCommand(
                    rawText = input,
                    actionType = ActionType.CREATE_TIMETABLE,
                    clarificationNeeded = "I understood that you want to create a timetable routine, but which day or routine type (School Day, Weekend, Holiday, or Exam Prep)?",
                    confirmationPrompt = ""
                )
            }
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.CREATE_TIMETABLE,
                title = "$day Routine",
                confirmationPrompt = "Confirm setting up study timetable for: $day"
            )
        }

        // 7. Test creation
        if (lower.contains("test") || lower.contains("quiz") || lower.contains("mock")) {
            val subject = extractSubject(input) ?: "Science"
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.CREATE_TEST,
                subject = subject,
                title = "$subject Practice Test",
                confirmationPrompt = "Confirm generating mock test:\n\n• Subject: $subject\n• Questions: 10\n• Time: 20 minutes"
            )
        }

        // 8. PDF Export
        if (lower.contains("pdf") || lower.contains("export")) {
            return InterpretedCommand(
                rawText = input,
                actionType = ActionType.EXPORT_PDF,
                confirmationPrompt = "Confirm generating PDF export for your study material?"
            )
        }

        // Unknown
        return InterpretedCommand(
            rawText = input,
            actionType = ActionType.UNKNOWN,
            clarificationNeeded = "I couldn't quite map that to an action. You can say things like:\n• 'Add a Maths homework task called Solve quadratic equations tomorrow'\n• 'Add a reminder at 7 PM'\n• 'Add a subject called Bengali'\n• 'Create a Science test'",
            confirmationPrompt = ""
        )
    }

    private fun extractSubject(text: String): String? {
        val lower = text.lowercase()
        return when {
            lower.contains("math") -> "Mathematics"
            lower.contains("physics") -> "Physics"
            lower.contains("chem") -> "Chemistry"
            lower.contains("bio") -> "Biology"
            lower.contains("sci") -> "Science"
            lower.contains("eng") -> "English"
            lower.contains("hist") || lower.contains("geog") || lower.contains("social") -> "Social Science"
            lower.contains("comp") -> "Computer Science"
            lower.contains("bengali") -> "Bengali"
            lower.contains("hindi") -> "Hindi"
            else -> null
        }
    }

    private fun extractTitle(text: String, markers: List<String>): String {
        for (m in markers) {
            val idx = text.indexOf(" $m ", ignoreCase = true)
            if (idx != -1) {
                var candidate = text.substring(idx + m.length + 2).trim()
                // Trim trailing "tomorrow", "today", "at 7 pm", etc.
                val stopWords = listOf(" tomorrow", " today", " at ", " next week")
                for (sw in stopWords) {
                    val swIdx = candidate.indexOf(sw, ignoreCase = true)
                    if (swIdx != -1) {
                        candidate = candidate.substring(0, swIdx).trim()
                    }
                }
                if (candidate.isNotBlank()) return candidate
            }
        }
        return ""
    }

    private fun extractAfterKeywords(text: String, markers: List<String>): String {
        for (m in markers) {
            val idx = text.indexOf(m, ignoreCase = true)
            if (idx != -1) {
                val candidate = text.substring(idx + m.length).trim()
                if (candidate.isNotBlank()) return candidate.replace(".", "").trim()
            }
        }
        return ""
    }

    private fun extractDueDate(text: String): String {
        val lower = text.lowercase()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            return sdf.format(cal.time)
        }
        return sdf.format(cal.time)
    }

    private fun extractTime(text: String): String? {
        val regex = Regex("(\\d{1,2}(:\\d{2})?\\s*(am|pm|AM|PM)?)")
        val match = regex.find(text)
        return match?.value?.trim()
    }
}
