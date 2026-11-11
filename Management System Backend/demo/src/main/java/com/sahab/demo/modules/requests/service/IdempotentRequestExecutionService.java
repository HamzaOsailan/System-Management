
package com.sahab.demo.modules.requests.service;
import com.sahab.demo.service.IdempotencyReservationService;
import com.sahab.demo.service.IdempotencyService;


import com.sahab.demo.modules.requests.dto.CreateRequestDTO;
import com.sahab.demo.modules.requests.dto.RequestResponse;
import com.sahab.demo.entity.Request;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IdempotentRequestExecutionService {

    private final RequestService requestService;
    private final IdempotencyService idempotencyService;
    private final IdempotencyReservationService reservationService;

    public IdempotentRequestExecutionService(
            RequestService requestService,
            IdempotencyService idempotencyService,
            IdempotencyReservationService reservationService
    ) {
        this.requestService = requestService;
        this.idempotencyService = idempotencyService;
        this.reservationService = reservationService;
    }

    @Transactional
    public RequestResponse createAndComplete(
            CreateRequestDTO dto,
            MultipartFile file,
            Long reservationId
    ) {
        Request request = requestService.createRequest(dto, file);

        RequestResponse response =
                requestService.toRequestResponse(request);

        reservationService.complete(
                reservationId,
                request.getId(),
                idempotencyService.serializeResponse(response),
                HttpStatus.OK.value()
        );

        return response;
    }
}
