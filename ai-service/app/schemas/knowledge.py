from typing import Any

from pydantic import BaseModel, ConfigDict, Field, field_validator


class KnowledgeChunkPayload(BaseModel):
    """Validated payload stored alongside a Qdrant knowledge vector."""

    model_config = ConfigDict(extra="forbid")

    document_id: str = Field(min_length=1, max_length=200)
    chunk_index: int = Field(ge=0)

    content: str = Field(min_length=1)
    title: str = Field(min_length=1, max_length=500)

    source: str | None = None
    category: str = Field(min_length=1, max_length=100)
    language: str = Field(default="ar", min_length=2, max_length=20)

    access_roles: list[str] = Field(min_length=1)
    access_user_ids: list[str] = Field(default_factory=list)
    access_departments: list[str] = Field(default_factory=list)

    is_active: bool = True
    metadata: dict[str, Any] = Field(default_factory=dict)

    @field_validator("access_roles", "access_user_ids", "access_departments")
    @classmethod
    def validate_access_values(cls, values: list[str]) -> list[str]:
        cleaned = [value.strip() for value in values]

        if any(not value for value in cleaned):
            raise ValueError("Access values cannot contain empty strings.")

        if len(cleaned) != len(set(cleaned)):
            raise ValueError("Access values cannot contain duplicates.")

        return cleaned

    @field_validator("content", "title", "document_id", "category")
    @classmethod
    def validate_non_blank_strings(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("This field cannot contain only whitespace.")

        return value.strip()


class RetrievedChunk(BaseModel):
    """A knowledge chunk returned by semantic retrieval."""

    model_config = ConfigDict(extra="forbid")

    document_id: str
    chunk_index: int
    content: str
    title: str
    source: str | None = None
    category: str
    language: str
    score: float
    metadata: dict[str, Any] = Field(default_factory=dict)
