from uuid import NAMESPACE_URL, uuid5

from qdrant_client.models import FieldCondition, Filter, MatchValue, PointStruct

from app.core.config import settings
from app.schemas.knowledge import KnowledgeChunkPayload
from app.services.ollama_service import ollama_service
from app.services.qdrant_service import qdrant_service


class KnowledgeServiceError(RuntimeError):
    """Raised when knowledge ingestion or deletion fails."""


class KnowledgeService:
    """Validate, embed, and store knowledge chunks in Qdrant."""

    @staticmethod
    def _point_id(document_id: str, chunk_index: int) -> str:
        identity = (
            f"management-knowledge:{document_id}:{chunk_index}"
        )
        return str(uuid5(NAMESPACE_URL, identity))

    async def upsert_chunks(
        self,
        chunks: list[KnowledgeChunkPayload],
    ):
        if not isinstance(chunks, list) or not chunks:
            raise ValueError("At least one knowledge chunk is required.")

        if len(chunks) > 500:
            raise ValueError("A maximum of 500 chunks is allowed per batch.")

        if not all(
            isinstance(chunk, KnowledgeChunkPayload)
            for chunk in chunks
        ):
            raise ValueError(
                "Every item must be a validated KnowledgeChunkPayload."
            )

        document_ids = {chunk.document_id for chunk in chunks}

        if len(document_ids) != 1:
            raise ValueError(
                "All chunks in one batch must belong to the same document."
            )

        chunk_indices = [chunk.chunk_index for chunk in chunks]

        if len(chunk_indices) != len(set(chunk_indices)):
            raise ValueError(
                "Chunk indices cannot be duplicated within a batch."
            )

        points: list[PointStruct] = []

        try:
            for chunk in chunks:
                vector = await ollama_service.embed(chunk.content)

                if len(vector) != settings.embedding_dimensions:
                    raise KnowledgeServiceError(
                        "Embedding dimensions do not match configuration."
                    )

                points.append(
                    PointStruct(
                        id=self._point_id(
                            chunk.document_id,
                            chunk.chunk_index,
                        ),
                        vector=vector,
                        payload=chunk.model_dump(mode="json"),
                    )
                )

        except KnowledgeServiceError:
            raise
        except Exception as exc:
            raise KnowledgeServiceError(
                "Could not generate embeddings for all knowledge chunks."
            ) from exc

        try:
            result = await qdrant_service.client.upsert(
                collection_name=settings.qdrant_collection,
                points=points,
                wait=True,
            )
            return result

        except Exception as exc:
            raise KnowledgeServiceError(
                "Could not store knowledge chunks in Qdrant."
            ) from exc

    async def delete_document(self, document_id: str):
        if not isinstance(document_id, str) or not document_id.strip():
            raise ValueError("Document ID cannot be empty.")

        document_id = document_id.strip()

        document_filter = Filter(
            must=[
                FieldCondition(
                    key="document_id",
                    match=MatchValue(value=document_id),
                )
            ]
        )

        try:
            return await qdrant_service.client.delete(
                collection_name=settings.qdrant_collection,
                points_selector=document_filter,
                wait=True,
            )

        except Exception as exc:
            raise KnowledgeServiceError(
                "Could not delete the requested knowledge document."
            ) from exc


knowledge_service = KnowledgeService()
