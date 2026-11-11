from math import isfinite

import httpx

from app.core.config import settings


class OllamaServiceError(RuntimeError):
    """Raised when Ollama cannot complete a requested operation."""


class OllamaService:
    def __init__(self):
        self.base_url = settings.ollama_base_url.rstrip("/")
        self.chat_model = settings.ollama_chat_model
        self.embedding_model = settings.ollama_embedding_model

    async def generate(self, prompt: str) -> str:
        if not prompt or not prompt.strip():
            raise ValueError("Prompt cannot be empty.")

        payload = {
            "model": self.chat_model,
            "prompt": prompt,
            "stream": False,
        }

        try:
            async with httpx.AsyncClient(
                timeout=settings.ollama_timeout_seconds
            ) as client:
                response = await client.post(
                    f"{self.base_url}/api/generate",
                    json=payload,
                )
                response.raise_for_status()
                result = response.json()

            answer = result.get("response")

            if not isinstance(answer, str) or not answer.strip():
                raise OllamaServiceError(
                    "Ollama returned an empty or invalid response."
                )

            return answer.strip()

        except httpx.HTTPStatusError as exc:
            raise OllamaServiceError(
                f"Ollama generation failed with HTTP {exc.response.status_code}."
            ) from exc
        except httpx.RequestError as exc:
            raise OllamaServiceError(
                "Could not connect to Ollama during generation."
            ) from exc

    async def embed(self, text: str) -> list[float]:
        if not text or not text.strip():
            raise ValueError("Embedding text cannot be empty.")

        payload = {
            "model": self.embedding_model,
            "input": text,
        }

        try:
            async with httpx.AsyncClient(
                timeout=settings.ollama_timeout_seconds
            ) as client:
                response = await client.post(
                    f"{self.base_url}/api/embed",
                    json=payload,
                )
                response.raise_for_status()
                result = response.json()

            embeddings = result.get("embeddings")

            if not isinstance(embeddings, list) or not embeddings:
                raise OllamaServiceError(
                    "Ollama returned an invalid embeddings response."
                )

            vector = embeddings[0]

            if not isinstance(vector, list):
                raise OllamaServiceError(
                    "Ollama returned an invalid embedding vector."
                )

            if len(vector) != settings.embedding_dimensions:
                raise OllamaServiceError(
                    f"Expected {settings.embedding_dimensions} dimensions, "
                    f"received {len(vector)}."
                )

            if not all(
                isinstance(value, (int, float))
                and not isinstance(value, bool)
                and isfinite(value)
                for value in vector
            ):
                raise OllamaServiceError(
                    "Embedding vector contains invalid numeric values."
                )

            return [float(value) for value in vector]

        except httpx.HTTPStatusError as exc:
            raise OllamaServiceError(
                f"Ollama embedding failed with HTTP {exc.response.status_code}."
            ) from exc
        except httpx.RequestError as exc:
            raise OllamaServiceError(
                "Could not connect to Ollama during embedding."
            ) from exc


ollama_service = OllamaService()
