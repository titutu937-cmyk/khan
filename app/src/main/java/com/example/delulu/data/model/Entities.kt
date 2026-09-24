package com.example.delulu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Student",
    val studentClass: String = "Class 10",
    val board: String = "CBSE",
    val selectedSubjects: String = "Mathematics,Science,English,Social Science",
    val dailyStudyTargetMinutes: Int = 120,
    val studyPreference: String = "Evening Focus",
    val currentRoutine: String = "School Day",
    val avatarEmoji: String = "🎓"
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String,
    val iconName: String,
    val isCustom: Boolean = false
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subject: String,
    val chapter: String = "",
    val topic: String = "",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val subject: String,
    val dueDate: String, // YYYY-MM-DD
    val dueTime: String = "17:00",
    val priority: String = "Medium", // High, Medium, Low
    val category: String = "Homework", // Homework, Assignment, Project, Exam Prep, General
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: String, // Monday, Tuesday, etc. or Everyday
    val routineType: String = "School Day", // School Day, Weekend, Holiday, Exam Preparation
    val title: String,
    val startTime: String, // e.g. 08:00 AM
    val endTime: String,   // e.g. 09:00 AM
    val subject: String,
    val category: String = "School", // School, Tuition, Coaching, Self Study, Break, Sleep, Personal
    val orderIndex: Int = 0
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val triggerTime: Long,
    val timeFormatted: String,
    val type: String = "Homework", // Homework, Study, Test, Break, Wellness, Custom
    val isActive: Boolean = true
)

@Entity(tableName = "events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val eventDate: String, // YYYY-MM-DD
    val eventType: String = "Exam", // Exam, Mock Test, Submission, Holiday, Session
    val subject: String = "General",
    val notes: String = ""
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String = "",
    val durationMinutes: Int,
    val mode: String = "Pomodoro", // Pomodoro, Stopwatch, Countdown
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val studentClass: String,
    val difficulty: String = "Medium",
    val durationMinutes: Int = 30,
    val totalMarks: Int = 10,
    val obtainedMarks: Int = 0,
    val percentage: Float = 0f,
    val weakTopics: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_questions")
data class TestQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val questionText: String,
    val questionType: String = "MCQ", // MCQ, Short, Long, Assertion, Numerical
    val optionsJson: String = "", // JSON array or pipe-separated options
    val correctAnswer: String,
    val explanation: String = "",
    val topic: String = "",
    val marks: Int = 1
)

@Entity(tableName = "test_answers")
data class TestAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val questionId: Long,
    val studentAnswer: String,
    val isCorrect: Boolean
)

@Entity(tableName = "progress")
data class ProgressRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekNumber: Int,
    val year: Int,
    val subject: String,
    val averageScore: Float,
    val studyHours: Float,
    val tasksCompleted: Int
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "saved_questions")
data class SavedQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val subject: String,
    val topic: String,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_answers")
data class SavedAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val finalAnswer: String,
    val stepByStep: String,
    val importantPoints: String,
    val commonMistake: String,
    val practiceQuestion: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawText: String,
    val interpretedAction: String,
    val status: String, // CONFIRMED, CANCELLED, EXECUTED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)
