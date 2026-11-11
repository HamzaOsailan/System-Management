import secrets

from fastapi import HTTPException, Security, status
from fastapi.security import APIKeyHeader

from app.core.config import settings


internal_api_key_header = APIKeyHeader(
    name="X-Internal-Api-Key",
    scheme_name="InternalApiKey",
    auto_error=False,
)


async def verify_internal_api_key(
    api_key: str | None = Security(internal_api_key_header),
) -> None:
    configured_key = settings.internal_api_key

    if configured_key is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Internal API authentication is not configured.",
        )

    expected_key = configured_key.get_secret_value()

    if not expected_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Internal API authentication is not configured.",
        )

    if api_key is None or not secrets.compare_digest(
        api_key.encode("utf-8"),
        expected_key.encode("utf-8"),
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing internal API key.",
        )
