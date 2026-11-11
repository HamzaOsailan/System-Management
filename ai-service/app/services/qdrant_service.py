from math import isfinite

from qdrant_client import AsyncQdrantClient

from app.core.config import settings


class QdrantServiceError(RuntimeError):
    """Raised when Qdrant cannot complete a requested operation."""


class QdrantService:
    def __init__(self):
        self.client = AsyncQdrantClient(
            url=settings.qdrant_url,
            timeout=settings.qdrant_timeout_seconds,
        )
        self.collection_name = settings.qdrant_collection
        self.embedding_dimensions = settings.embedding_dimensions

    async def get_collection_info(self):
        try:
            return await self.client.get_collection(
                collection_name=self.collection_name
            )
        except Exception as exc:
            raise QdrantServiceError(
                "Could not retrieve Qdrant collection information."
            ) from exc

    async def validate_collection(self):
        collection = await self.get_collection_info()

        vectors_config = collection.config.params.vectors
        vector_size = getattr(vectors_config, "size", None)

        if vector_size != self.embedding_dimensions:
            raise QdrantServiceError(
                f"Collection vector size mismatch: "
                f"expected {self.embedding_dimensions}, "
                f"received {vector_size}."
            )

    async def search(
        self,
        vector: list[float],
        limit: int = 5,
        score_threshold: float | None = None,
    ):
        if not isinstance(vector, list):
            raise ValueError("Query vector must be a list.")

        if len(vector) != settings.embedding_dimensions:
            raise ValueError(
                f"Expected {settings.embedding_dimensions} dimensions, "
                f"received {len(vector)}."
            )

        if not all(
            isinstance(value, (int, float))
            and not isinstance(value, bool)
            and isfinite(value)
            for value in vector
        ):
            raise ValueError(
                "Query vector contains invalid numeric values."
            )

        if not isinstance(limit, int) or isinstance(limit, bool):
            raise ValueError("Limit must be an integer.")

        if not 1 <= limit <= 50:
            raise ValueError("Limit must be between 1 and 50.")

        if score_threshold is not None:
            if (
                not isinstance(score_threshold, (int, float))
                or isinstance(score_threshold, bool)
                or not isfinite(score_threshold)
                or not 0 <= score_threshold <= 1
            ):
                raise ValueError(
                    "Score threshold must be between 0 and 1."
                )

        await self.validate_collection()

        try:
            response = await self.client.query_points(
                collection_name=self.collection_name,
                query=[float(value) for value in vector],
                limit=limit,
                with_payload=True,
                with_vectors=False,
                score_threshold=score_threshold,
            )

            return response.points

        except Exception as exc:
            raise QdrantServiceError(
                "Qdrant semantic search failed."
            ) from exc

    async def close(self):
        await self.client.close()


qdrant_service = QdrantService()