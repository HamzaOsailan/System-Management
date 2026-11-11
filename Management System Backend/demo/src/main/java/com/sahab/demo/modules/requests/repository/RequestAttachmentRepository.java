package com.sahab.demo.modules.requests.repository;

import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.RequestAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestAttachmentRepository
        extends JpaRepository<RequestAttachment, Long> {

    Optional<RequestAttachment> findByRequest(Request request);

    Optional<RequestAttachment> findByIdAndRequest(
            Long id,
            Request request
    );

    Optional<RequestAttachment> findByRequestId(
            Long requestId
    );

    void deleteByRequest(Request request);
}