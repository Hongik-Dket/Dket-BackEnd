package com.example.demo.domain.metadata.service.impl;

import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.metadata.service.MetadataCommandService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetadataCommandServiceImpl implements MetadataCommandService {

    private final MetadataRepository metadataRepository;

    @Override
    @Transactional
    public void setMetadataCid(Long metadataId, String cid) {
        Metadata metadata = metadataRepository.findById(metadataId)
                .orElseThrow(() -> new CustomException(ErrorStatus.METADATA_NOT_FOUND));

        metadata.setCid(cid);
    }

}
