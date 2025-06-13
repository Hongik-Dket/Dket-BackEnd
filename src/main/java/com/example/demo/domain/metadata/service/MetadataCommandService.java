package com.example.demo.domain.metadata.service;

import com.example.demo.domain.metadata.entity.Metadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetadataCommandService {

    @Transactional
    public void setMetadataCid(Metadata metadata, String cid) {
        metadata.setCid(cid);
    }

}
