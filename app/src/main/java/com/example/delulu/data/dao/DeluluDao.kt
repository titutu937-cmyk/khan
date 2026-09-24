package com.example.delulu.data.dao

import androidx.room.*
import com.example.delulu.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeluluDao {
    // User Profile
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: Long)

    // Notes
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subject = :subject ORDER BY updatedAt DESC")
    fun getNotesBySubject(subject: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY isCompleted ASC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)

    // Timetable
    @Query("SELECT * FROM timetable WHERE routineType = :routineType ORDER BY orderIndex ASC")
    fun getTimetableForRoutine(routineType: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable ORDER BY routineType, orderIndex ASC")
    fun getAllTimetable(): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(entry: TimetableEntity): Long

    @Update
    suspend fun updateTimetable(entry: TimetableEntity)

    @Delete
    suspend fun deleteTimetable(entry: TimetableEntity)

    // Reminders
    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY triggerTime ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isActive = :active WHERE id = :id")
    suspend fun toggleReminder(id: Long, active: Boolean)

    // Calendar Events
    @Query("SELECT * FROM events ORDER BY eventDate ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM events WHERE eventDate = :date")
    fun getEventsForDate(date: String): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE date = :date")
    fun getStudySessionsForDate(date: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE date = :date")
    fun getTodayStudyMinutes(date: String): Flow<Int?>

    // Tests
    @Query("SELECT * FROM tests ORDER BY completedAt DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE subject = :subject ORDER BY completedAt ASC")
    fun getTestsForSubject(subject: String): Flow<List<TestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    // Test Questions & Answers
    @Query("SELECT * FROM test_questions WHERE testId = :testId")
    suspend fun getQuestionsForTest(testId: Long): List<TestQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<TestQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: TestAnswerEntity)

    // Progress Records
    @Query("SELECT * FROM progress ORDER BY year DESC, weekNumber DESC")
    fun getAllProgress(): Flow<List<ProgressRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(record: ProgressRecordEntity)

    // Settings
    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    // Saved Questions & Answers (Ask Delulu)
    @Query("SELECT * FROM saved_questions ORDER BY timestamp DESC")
    fun getAllSavedQuestions(): Flow<List<SavedQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedQuestion(q: SavedQuestionEntity): Long

    @Query("SELECT * FROM saved_answers WHERE questionId = :questionId LIMIT 1")
    suspend fun getAnswerForQuestion(questionId: Long): SavedAnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAnswer(a: SavedAnswerEntity): Long

    @Delete
    suspend fun deleteSavedQuestion(q: SavedQuestionEntity)

    // Command History
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 50")
    fun getCommandHistory(): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(cmd: CommandHistoryEntity): Long
}
