from fastapi import FastAPI

app = FastAPI(
    title="Management System AI Service",
    description="Independent AI service for the Management System",
    version="0.1.0",
)


@app.get("/")
async def root():
    return {
        "service": "Management System AI Service",
        "status": "running",
        "message": "خدمة الذكاء الاصطناعي تعمل بنجاح",
    }


@app.get("/health")
async def health():
    return {
        "status": "healthy",
        "service": "ai-service",
    }