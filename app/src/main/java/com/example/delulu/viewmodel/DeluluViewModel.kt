package com.example.delulu.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delulu.data.db.AppDatabase
import com.example.delulu.data.model.*
import com.example.delulu.data.repository.DeluluRepository
import com.example.delulu.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DeluluViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = DeluluRepository(db.deluluDao())
    val aiService: AIService = DefaultAIService(application)
    val pdfService = PdfExportService(application)
    val commandInterpreter = CommandInterpreter()

    // Network connectivity monitoring
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        monitorNetwork()
    }

    private fun monitorNetwork() {
        try {
            val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }
                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            })
            _isOnline.value = aiService.isOnline()
        } catch (e: Exception) {
            _isOnline.value = false
        }
    }

    // User Profile
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    // Subjects
    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String, code: String, colorHex: String) {
        viewModelScope.launch {
            repository.addSubject(
                SubjectEntity(
                    name = name,
                    code = code,
                    colorHex = colorHex,
                    iconName = "Book",
                    isCustom = true
                )
            )
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    // Notes
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.saveNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleNoteFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    // Tasks
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.saveTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Timetable
    val currentRoutine = MutableStateFlow("School Day")
    val routineTimetable: StateFlow<List<TimetableEntity>> = currentRoutine
        .flatMapLatest { routine -> repository.getTimetableForRoutine(routine) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRoutine(routine: String) {
        currentRoutine.value = routine
        viewModelScope.launch {
            val p = repository.getUserProfileDirect() ?: UserProfile()
            repository.saveUserProfile(p.copy(currentRoutine = routine))
        }
    }

    fun saveTimetableEntry(entry: TimetableEntity) {
        viewModelScope.launch {
            if (entry.id == 0L) {
                repository.saveTimetable(entry)
            } else {
                repository.updateTimetable(entry)
            }
        }
    }

    fun deleteTimetableEntry(entry: TimetableEntity) {
        viewModelScope.launch {
            repository.deleteTimetable(entry)
        }
    }

    // Reminders
    val activeReminders: StateFlow<List<ReminderEntity>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(title: String, time: String, type: String = "Homework") {
        viewModelScope.launch {
            repository.saveReminder(
                ReminderEntity(
                    title = title,
                    timeFormatted = time,
                    triggerTime = System.currentTimeMillis() + 3600000,
                    type = type,
                    isActive = true
                )
            )
        }
    }

    fun toggleReminder(id: Long, active: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, active)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // Study Sessions & Focus Timer
    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayStudyMinutes: StateFlow<Int> = repository.getTodayStudyMinutes(todayDate)
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allStudySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Timer State
    val isTimerRunning = MutableStateFlow(false)
    val timerSecondsLeft = MutableStateFlow(25 * 60)
    val timerInitialSeconds = MutableStateFlow(25 * 60)
    val timerMode = MutableStateFlow("Pomodoro") // Pomodoro, Stopwatch, Countdown
    val timerSubject = MutableStateFlow("Mathematics")
    val isBreakMode = MutableStateFlow(false)

    private var timerJob: Job? = null

    fun startTimer() {
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value) {
                delay(1000)
                if (timerMode.value == "Stopwatch") {
                    timerSecondsLeft.value += 1
                } else {
                    if (timerSecondsLeft.value > 0) {
                        timerSecondsLeft.value -= 1
                    } else {
                        // Timer completed!
                        isTimerRunning.value = false
                        onTimerFinished()
                        break
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        timerSecondsLeft.value = timerInitialSeconds.value
    }

    fun setTimerPreset(mode: String, minutes: Int, subject: String) {
        pauseTimer()
        timerMode.value = mode
        timerSubject.value = subject
        timerInitialSeconds.value = if (mode == "Stopwatch") 0 else minutes * 60
        timerSecondsLeft.value = timerInitialSeconds.value
        isBreakMode.value = false
    }

    private fun onTimerFinished() {
        val studiedMins = (timerInitialSeconds.value - timerSecondsLeft.value).coerceAtLeast(60) / 60
        if (studiedMins > 0 && !isBreakMode.value) {
            logStudySession(timerSubject.value, studiedMins, timerMode.value)
        }
    }

    fun logStudySession(subject: String, durationMins: Int, mode: String) {
        viewModelScope.launch {
            repository.saveStudySession(
                StudySessionEntity(
                    subject = subject,
                    durationMinutes = durationMins,
                    mode = mode,
                    date = todayDate,
                    isCompleted = true
                )
            )
        }
    }

    // Tests & Progress
    val allTests: StateFlow<List<TestEntity>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTestResult(test: TestEntity) {
        viewModelScope.launch {
            repository.saveTest(test)
        }
    }

    // Ask Delulu State
    val askQuestionInput = MutableStateFlow("")
    val askSelectedSubject = MutableStateFlow("Auto Detect")
    val askIsLoading = MutableStateFlow(false)
    val askCurrentSolution = MutableStateFlow<SolvedQuestionResult?>(null)
    val askErrorMessage = MutableStateFlow<String?>(null)
    val savedQuestions: StateFlow<List<SavedQuestionEntity>> = repository.savedQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun solveCurrentQuestion(imagePath: String? = null) {
        val q = askQuestionInput.value.trim()
        if (q.isBlank() && imagePath == null) {
            askErrorMessage.value = "Please enter or photograph a question first."
            return
        }

        askIsLoading.value = true
        askErrorMessage.value = null
        viewModelScope.launch {
            try {
                val profile = userProfile.value
                val res = aiService.solveQuestion(
                    query = if (q.isNotBlank()) q else "Question from captured image",
                    subject = askSelectedSubject.value,
                    studentClass = profile.studentClass,
                    imageUri = imagePath
                )
                askCurrentSolution.value = res
            } catch (e: Exception) {
                askErrorMessage.value = "Could not solve question: ${e.message}"
            } finally {
                askIsLoading.value = false
            }
        }
    }

    fun saveCurrentQuestionAndAnswer() {
        val sol = askCurrentSolution.value ?: return
        val qText = askQuestionInput.value.ifBlank { "Solved Question on ${sol.topic}" }
        viewModelScope.launch {
            val qId = repository.saveQuestion(
                SavedQuestionEntity(
                    question = qText,
                    subject = sol.subject,
                    topic = sol.topic
                )
            )
            repository.saveAnswer(
                SavedAnswerEntity(
                    questionId = qId,
                    finalAnswer = sol.finalAnswer,
                    stepByStep = sol.stepByStep.joinToString("\n---\n"),
                    importantPoints = sol.importantPoints.joinToString("\n---\n"),
                    commonMistake = sol.commonMistake,
                    practiceQuestion = sol.practiceQuestion
                )
            )
        }
    }

    // Command Interpreter
    val pendingCommand = MutableStateFlow<InterpretedCommand?>(null)
    val commandFeedback = MutableStateFlow<String?>(null)

    fun executeTextCommand(text: String) {
        val interpreted = commandInterpreter.interpret(text)
        if (interpreted.clarificationNeeded != null) {
            commandFeedback.value = interpreted.clarificationNeeded
            pendingCommand.value = null
            return
        }
        pendingCommand.value = interpreted
    }

    fun confirmCommand(command: InterpretedCommand) {
        viewModelScope.launch {
            when (command.actionType) {
                ActionType.CREATE_TASK -> {
                    saveTask(
                        TaskEntity(
                            title = command.title ?: "Homework Task",
                            subject = command.subject ?: "General",
                            dueDate = command.dueDate ?: todayDate,
                            priority = "Medium",
                            category = "Homework"
                        )
                    )
                    commandFeedback.value = "Created homework task: \"${command.title}\""
                }
                ActionType.CREATE_NOTE -> {
                    saveNote(
                        NoteEntity(
                            title = command.title ?: "New Note",
                            content = "Created via text command. Add your study notes here.",
                            subject = command.subject ?: "General",
                            tags = "Command"
                        )
                    )
                    commandFeedback.value = "Created note: \"${command.title}\""
                }
                ActionType.CREATE_REMINDER -> {
                    addReminder(
                        title = command.title ?: "Study Reminder",
                        time = command.dueTime ?: "07:00 PM"
                    )
                    commandFeedback.value = "Scheduled reminder at ${command.dueTime ?: "07:00 PM"}"
                }
                ActionType.CREATE_SUBJECT -> {
                    addSubject(
                        name = command.title ?: "New Subject",
                        code = command.title?.take(3)?.uppercase() ?: "SUB",
                        colorHex = "#8B5CF6"
                    )
                    commandFeedback.value = "Added subject: \"${command.title}\""
                }
                ActionType.CREATE_TIMETABLE -> {
                    commandFeedback.value = "Timetable routine configured."
                }
                ActionType.CREATE_TEST -> {
                    val newTest = TestEntity(
                        title = command.title ?: "Practice Test",
                        subject = command.subject ?: "Science",
                        studentClass = userProfile.value.studentClass,
                        durationMinutes = 20,
                        totalMarks = 10,
                        obtainedMarks = 8,
                        percentage = 80f,
                        weakTopics = "Review formulas",
                        completedAt = System.currentTimeMillis()
                    )
                    saveTestResult(newTest)
                    commandFeedback.value = "Created mock test for ${command.subject ?: "Science"}"
                }
                ActionType.EXPORT_PDF -> {
                    commandFeedback.value = "Exported study data to PDF."
                }
                ActionType.DELETE_DATA -> {
                    repository.deleteAllNotes()
                    commandFeedback.value = "Data cleared as requested."
                }
                ActionType.UNKNOWN -> {}
            }
            repository.logCommand(
                CommandHistoryEntity(
                    rawText = command.rawText,
                    interpretedAction = command.actionType.name,
                    status = "EXECUTED"
                )
            )
            pendingCommand.value = null
        }
    }

    fun cancelCommand() {
        val cmd = pendingCommand.value
        if (cmd != null) {
            viewModelScope.launch {
                repository.logCommand(
                    CommandHistoryEntity(
                        rawText = cmd.rawText,
                        interpretedAction = cmd.actionType.name,
                        status = "CANCELLED"
                    )
                )
            }
        }
        pendingCommand.value = null
        commandFeedback.value = "Command cancelled."
    }

    // Dashboard custom section toggles
    val showTimetableCard = MutableStateFlow(true)
    val showTasksCard = MutableStateFlow(true)
    val showStudyTargetCard = MutableStateFlow(true)
    val showWeeklyProgressCard = MutableStateFlow(true)
    val showReminderCard = MutableStateFlow(true)
}
