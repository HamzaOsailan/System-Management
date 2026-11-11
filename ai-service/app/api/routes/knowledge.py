from typing import Annotated

from fastapi import APIRouter, Depends, Path
from pydantic import BaseModel, ConfigDict, Field, field_validator

from app.core.security import verify_internal_api_key
from app.schemas.knowledge import KnowledgeChunkPayload, RetrievedChunk
from app.services.knowledge_service import knowledge_service
from app.services.retriever_service import (
    RetrievalAccess,
    retriever_service,
)


router = APIRouter(
    prefix="/internal/v1/knowledge",
    tags=["Internal Knowledge"],
    dependencies=[Depends(verify_internal_api_key)],
)


class RetrievalRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    query: str = Field(min_length=1, max_length=4000)
    access: RetrievalAccess
    limit: int = Field(default=5, ge=1, le=50)
    score_threshold: float | None = Field(default=None, ge=0, le=1)

    @field_validator("query")
    @classmethod
    def validate_query(cls, value: str) -> str:
        cleaned = value.strip()
        if not cleaned:
            raise ValueError("Query cannot contain only whitespace.")
        return cleaned


class IngestionResponse(BaseModel):
    status: str
    document_id: str
    chunks_received: int


class DeletionResponse(BaseModel):
    status: str
    document_id: str


class RetrievalResponse(BaseModel):
    results: list[RetrievedChunk]
    count: int


@router.post(
    "/documents/chunks",
    response_model=IngestionResponse,
)
async def ingest_knowledge_chunks(
    chunks: Annotated[
        list[KnowledgeChunkPayload],
        Field(min_length=1, max_length=500),
    ],
):
    await knowledge_service.upsert_chunks(chunks)

    return IngestionResponse(
        status="stored",
        document_id=chunks[0].document_id,
        chunks_received=len(chunks),
    )


@router.delete(
    "/documents/{document_id}",
    response_model=DeletionResponse,
)
async def delete_knowledge_document(
    document_id: str = Path(min_length=1, max_length=200),
):
    await knowledge_service.delete_document(document_id)

    return DeletionResponse(
        status="deleted",
        document_id=document_id,
    )


@router.post(
    "/retrieve",
    response_model=RetrievalResponse,
)
async def retrieve_knowledge(
    request: RetrievalRequest,
):
    results = await retriever_service.retrieve(
        query=request.query,
        access=request.access,
        limit=request.limit,
        score_threshold=request.score_threshold,
    )

    return RetrievalResponse(
        results=results,
        count=len(results),
    )
