package com.example

import com.example.delulu.service.ActionType
import com.example.delulu.service.CommandInterpreter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  private val interpreter = CommandInterpreter()

  @Test
  fun testCreateTaskCommand() {
    val result = interpreter.interpret("Add a Maths homework task called solve quadratic equations tomorrow")
    assertEquals(ActionType.CREATE_TASK, result.actionType)
    assertEquals("Mathematics", result.subject)
    assertEquals("solve quadratic equations", result.title)
    assertFalse(result.isDestructive)
  }

  @Test
  fun testDestructiveCommandDetection() {
    val result = interpreter.interpret("Delete all my notes and reset database")
    assertEquals(ActionType.DELETE_DATA, result.actionType)
    assertTrue(result.isDestructive)
  }

  @Test
  fun testCreateSubjectCommand() {
    val result = interpreter.interpret("Add a subject called Bengali")
    assertEquals(ActionType.CREATE_SUBJECT, result.actionType)
    assertEquals("Bengali", result.title)
  }
}

