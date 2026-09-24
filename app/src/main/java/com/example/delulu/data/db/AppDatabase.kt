package com.example.delulu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.delulu.data.dao.DeluluDao
import com.example.delulu.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        UserProfile::class,
        SubjectEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        TimetableEntity::class,
        ReminderEntity::class,
        CalendarEventEntity::class,
        StudySessionEntity::class,
        TestEntity::class,
        TestQuestionEntity::class,
        TestAnswerEntity::class,
        ProgressRecordEntity::class,
        SettingEntity::class,
        SavedQuestionEntity::class,
        SavedAnswerEntity::class,
        CommandHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deluluDao(): DeluluDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delulu_student_db.db"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial offline essentials
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database.deluluDao())
                    }
                }
            }

            private suspend fun seedInitialData(dao: DeluluDao) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date())

                // 1. Initial User Profile
                dao.saveUserProfile(
                    UserProfile(
                        id = 1,
                        name = "Alex",
                        studentClass = "Class 10",
                        board = "CBSE",
                        selectedSubjects = "Mathematics,Science,English,Social Science,Computer Science",
                        dailyStudyTargetMinutes = 120,
                        studyPreference = "Evening Focus",
                        currentRoutine = "School Day",
                        avatarEmoji = "🚀"
                    )
                )

                // 2. Default Curriculum Subjects
                val defaultSubjects = listOf(
                    SubjectEntity(name = "Mathematics", code = "MATH", colorHex = "#6366F1", iconName = "Calculate"),
                    SubjectEntity(name = "Science", code = "SCI", colorHex = "#10B981", iconName = "Science"),
                    SubjectEntity(name = "English", code = "ENG", colorHex = "#F59E0B", iconName = "MenuBook"),
                    SubjectEntity(name = "Social Science", code = "SOC", colorHex = "#EC4899", iconName = "Public"),
                    SubjectEntity(name = "Computer Science", code = "CS", colorHex = "#06B6D4", iconName = "Computer")
                )
                defaultSubjects.forEach { dao.insertSubject(it) }

                // 3. Initial Study Timetable
                val defaultTimetable = listOf(
                    TimetableEntity(dayOfWeek = "Monday", routineType = "School Day", title = "School Classes", startTime = "08:00 AM", endTime = "02:00 PM", subject = "School", category = "School", orderIndex = 1),
                    TimetableEntity(dayOfWeek = "Monday", routineType = "School Day", title = "Lunch & Rest Break", startTime = "02:00 PM", endTime = "03:30 PM", subject = "General", category = "Break", orderIndex = 2),
                    TimetableEntity(dayOfWeek = "Monday", routineType = "School Day", title = "Mathematics Tuition / Practice", startTime = "04:00 PM", endTime = "05:30 PM", subject = "Mathematics", category = "Tuition", orderIndex = 3),
                    TimetableEntity(dayOfWeek = "Monday", routineType = "School Day", title = "Science Chapter Revision", startTime = "06:00 PM", endTime = "07:30 PM", subject = "Science", category = "Self Study", orderIndex = 4),
                    TimetableEntity(dayOfWeek = "Monday", routineType = "School Day", title = "Daily Homework & Tasks", startTime = "08:00 PM", endTime = "09:30 PM", subject = "General", category = "Homework", orderIndex = 5),
                    
                    // Holiday / Weekend routines
                    TimetableEntity(dayOfWeek = "Saturday", routineType = "Weekend", title = "Morning Mathematics Revision", startTime = "09:00 AM", endTime = "11:00 AM", subject = "Mathematics", category = "Self Study", orderIndex = 1),
                    TimetableEntity(dayOfWeek = "Saturday", routineType = "Weekend", title = "Weekly Mock Test Attempt", startTime = "02:00 PM", endTime = "03:30 PM", subject = "Science", category = "Coaching", orderIndex = 2),
                    TimetableEntity(dayOfWeek = "Saturday", routineType = "Weekend", title = "Personal Reading & Hobby", startTime = "05:00 PM", endTime = "06:30 PM", subject = "English", category = "Personal", orderIndex = 3)
                )
                defaultTimetable.forEach { dao.insertTimetable(it) }

                // 4. Starter Tasks & Homework
                val defaultTasks = listOf(
                    TaskEntity(title = "Complete Light Chapter NCERT Numericals", description = "Exercise 10.1 and 10.2 questions 1-15", subject = "Science", dueDate = today, dueTime = "18:00", priority = "High", category = "Homework", isCompleted = false),
                    TaskEntity(title = "Solve Quadratic Equations Practice Set", description = "Formula method and factorisation exercises", subject = "Mathematics", dueDate = today, dueTime = "20:00", priority = "Medium", category = "Homework", isCompleted = false),
                    TaskEntity(title = "Prepare First Flight Chapter Summary", description = "Character sketch and central themes", subject = "English", dueDate = today, dueTime = "21:30", priority = "Low", category = "Assignment", isCompleted = true)
                )
                defaultTasks.forEach { dao.insertTask(it) }

                // 5. Initial Offline Notes
                val defaultNotes = listOf(
                    NoteEntity(
                        title = "Key Trigonometric Identities",
                        content = "1. sin²θ + cos²θ = 1\n2. 1 + tan²θ = sec²θ\n3. 1 + cot²θ = cosec²θ\n\nTips:\n• Convert everything to sin and cos when proving identities.\n• Watch out for sign conventions in different quadrants.",
                        subject = "Mathematics",
                        chapter = "Introduction to Trigonometry",
                        topic = "Trigonometric Identities",
                        tags = "Formulas,Trigonometry,Important",
                        isFavorite = true
                    ),
                    NoteEntity(
                        title = "Newton's Laws & Optics Rules",
                        content = "Laws of Reflection:\n1. Angle of incidence equals angle of reflection (∠i = ∠r).\n2. Incident ray, reflected ray, and normal all lie in the same plane.\n\nMirror Formula: 1/f = 1/v + 1/u\nMagnification: m = -v/u = h'/h",
                        subject = "Science",
                        chapter = "Light: Reflection and Refraction",
                        topic = "Spherical Mirrors",
                        tags = "Physics,Light,Formulas",
                        isFavorite = true
                    )
                )
                defaultNotes.forEach { dao.insertNote(it) }

                // 6. Seed initial test records for real progress tracking
                val test1 = TestEntity(
                    title = "Mathematics Chapter Test: Polynomials",
                    subject = "Mathematics",
                    studentClass = "Class 10",
                    difficulty = "Medium",
                    durationMinutes = 20,
                    totalMarks = 10,
                    obtainedMarks = 7,
                    percentage = 70f,
                    weakTopics = "Factor Theorem, Long Division",
                    completedAt = System.currentTimeMillis() - (7 * 24 * 3600 * 1000L)
                )
                dao.insertTest(test1)

                val test2 = TestEntity(
                    title = "Science Chapter Test: Light",
                    subject = "Science",
                    studentClass = "Class 10",
                    difficulty = "Medium",
                    durationMinutes = 25,
                    totalMarks = 10,
                    obtainedMarks = 8,
                    percentage = 80f,
                    weakTopics = "Sign convention for concave lenses",
                    completedAt = System.currentTimeMillis() - (3 * 24 * 3600 * 1000L)
                )
                dao.insertTest(test2)

                // 7. Seed sample study session
                dao.insertStudySession(
                    StudySessionEntity(
                        subject = "Mathematics",
                        topic = "Quadratic Equations",
                        durationMinutes = 45,
                        mode = "Pomodoro",
                        date = today,
                        isCompleted = true
                    )
                )

                // 8. Seed sample reminder
                dao.insertReminder(
                    ReminderEntity(
                        title = "Science Homework Submission",
                        description = "Light chapter numericals due tomorrow morning",
                        triggerTime = System.currentTimeMillis() + (2 * 3600 * 1000L),
                        timeFormatted = "07:00 PM",
                        type = "Homework",
                        isActive = true
                    )
                )
            }
        }
    }
}
