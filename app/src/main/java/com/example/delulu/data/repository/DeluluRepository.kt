package com.example.delulu.data.repository

import com.example.delulu.data.dao.DeluluDao
import com.example.delulu.data.model.*
import kotlinx.coroutines.flow.Flow

class DeluluRepository(private val dao: DeluluDao) {

    // User Profile
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    suspend fun getUserProfileDirect(): UserProfile? = dao.getUserProfileDirect()
    suspend fun saveUserProfile(profile: UserProfile) = dao.saveUserProfile(profile)

    // Subjects
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    suspend fun addSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun deleteSubject(id: Long) = dao.deleteSubject(id)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    fun getNotesBySubject(subject: String): Flow<List<NoteEntity>> = dao.getNotesBySubject(subject)
    suspend fun getNoteById(id: Long): NoteEntity? = dao.getNoteById(id)
    suspend fun saveNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)
    suspend fun deleteAllNotes() = dao.deleteAllNotes()

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    fun getTasksByDate(date: String): Flow<List<TaskEntity>> = dao.getTasksByDate(date)
    suspend fun saveTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) = dao.setTaskCompleted(taskId, completed)

    // Timetable
    fun getTimetableForRoutine(routine: String): Flow<List<TimetableEntity>> = dao.getTimetableForRoutine(routine)
    val allTimetable: Flow<List<TimetableEntity>> = dao.getAllTimetable()
    suspend fun saveTimetable(entry: TimetableEntity): Long = dao.insertTimetable(entry)
    suspend fun updateTimetable(entry: TimetableEntity) = dao.updateTimetable(entry)
    suspend fun deleteTimetable(entry: TimetableEntity) = dao.deleteTimetable(entry)

    // Reminders
    val activeReminders: Flow<List<ReminderEntity>> = dao.getActiveReminders()
    suspend fun saveReminder(reminder: ReminderEntity): Long = dao.insertReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = dao.deleteReminder(reminder)
    suspend fun toggleReminder(id: Long, active: Boolean) = dao.toggleReminder(id, active)

    // Events
    val allEvents: Flow<List<CalendarEventEntity>> = dao.getAllEvents()
    fun getEventsForDate(date: String): Flow<List<CalendarEventEntity>> = dao.getEventsForDate(date)
    suspend fun saveEvent(event: CalendarEventEntity): Long = dao.insertEvent(event)
    suspend fun deleteEvent(event: CalendarEventEntity) = dao.deleteEvent(event)

    // Study Sessions
    val allStudySessions: Flow<List<StudySessionEntity>> = dao.getAllStudySessions()
    fun getStudySessionsForDate(date: String): Flow<List<StudySessionEntity>> = dao.getStudySessionsForDate(date)
    suspend fun saveStudySession(session: StudySessionEntity): Long = dao.insertStudySession(session)
    fun getTodayStudyMinutes(date: String): Flow<Int?> = dao.getTodayStudyMinutes(date)

    // Tests & Questions
    val allTests: Flow<List<TestEntity>> = dao.getAllTests()
    fun getTestsForSubject(subject: String): Flow<List<TestEntity>> = dao.getTestsForSubject(subject)
    suspend fun saveTest(test: TestEntity): Long = dao.insertTest(test)
    suspend fun getQuestionsForTest(testId: Long): List<TestQuestionEntity> = dao.getQuestionsForTest(testId)
    suspend fun saveTestQuestions(questions: List<TestQuestionEntity>) = dao.insertQuestions(questions)
    suspend fun saveTestAnswer(answer: TestAnswerEntity) = dao.insertAnswer(answer)

    // Progress
    val allProgress: Flow<List<ProgressRecordEntity>> = dao.getAllProgress()
    suspend fun saveProgress(record: ProgressRecordEntity) = dao.insertProgress(record)

    // Settings
    suspend fun getSetting(key: String): String? = dao.getSetting(key)
    suspend fun setSetting(key: String, value: String) = dao.setSetting(SettingEntity(key, value))

    // Saved Questions & Answers
    val savedQuestions: Flow<List<SavedQuestionEntity>> = dao.getAllSavedQuestions()
    suspend fun saveQuestion(q: SavedQuestionEntity): Long = dao.insertSavedQuestion(q)
    suspend fun getAnswerForQuestion(questionId: Long): SavedAnswerEntity? = dao.getAnswerForQuestion(questionId)
    suspend fun saveAnswer(a: SavedAnswerEntity): Long = dao.insertSavedAnswer(a)
    suspend fun deleteSavedQuestion(q: SavedQuestionEntity) = dao.deleteSavedQuestion(q)

    // Command History
    val commandHistory: Flow<List<CommandHistoryEntity>> = dao.getCommandHistory()
    suspend fun logCommand(cmd: CommandHistoryEntity): Long = dao.insertCommand(cmd)
}
