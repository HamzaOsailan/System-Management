from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    # Ollama
    ollama_base_url: str = "http://127.0.0.1:11434"
    ollama_chat_model: str = "gemma4:26b"
    ollama_embedding_model: str = "bge-m3:latest"
    ollama_timeout_seconds: int = Field(default=300, ge=1, le=3600)

    # Qdrant
    qdrant_url: str = "http://127.0.0.1:6333"
    qdrant_collection: str = "management_knowledge"
    qdrant_timeout_seconds: int = Field(default=30, ge=1, le=600)

    # Embeddings
    embedding_dimensions: int = Field(default=1024, ge=1)


settings = Settings()
