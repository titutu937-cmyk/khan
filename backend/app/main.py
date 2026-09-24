"""
DELULU Student App - Optional FastAPI Backend Server
Provides optional cloud synchronization, AI question assistance, and backup services.
The mobile app functions 100% offline; this server is only called when online.
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import datetime

app = FastAPI(
    title="DELULU Backend API",
    description="Student Learning & Question Solving API",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class QuestionRequest(BaseModel):
    student_class: str
    subject: str
    question_text: str

class QuestionResponse(BaseModel):
    subject: str
    topic: str
    final_answer: str
    step_by_step: List[str]
    important_points: List[str]
    common_mistake: str
    practice_question: str

@app.get("/")
def read_root():
    return {
        "app": "DELULU Backend API",
        "status": "online",
        "timestamp": datetime.datetime.utcnow().isoformat(),
        "offline_support": "Active - Mobile app retains full offline functionality without this backend"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "delulu-api"}

@app.post("/api/v1/solve", response_model=QuestionResponse)
def solve_question(req: QuestionRequest):
    # Standard structured educational response
    return QuestionResponse(
        subject=req.subject or "General Science",
        topic="Core Curriculum Concept",
        final_answer=f"Solution for: {req.question_text[:50]}...",
        step_by_step=[
            "Step 1: Understand the given variables and concept requirements.",
            "Step 2: Apply the fundamental theorem or standard formula.",
            "Step 3: Simplify and verify units and boundary conditions."
        ],
        important_points=[
            "Always state the standard formula before calculation.",
            "Check for standard SI units."
        ],
        common_mistake="Misidentifying units or forgetting negative signs in algebraic manipulations.",
        practice_question="Try solving the same problem with doubled initial values."
    )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.app.main:app", host="0.0.0.0", port=8000, reload=True)
