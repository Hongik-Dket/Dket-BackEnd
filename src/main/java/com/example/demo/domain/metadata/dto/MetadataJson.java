package com.example.demo.domain.metadata.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataJson {
    private String name;
    private String description;
    private String image;
    private List<Attribute> attributes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attribute {
        private String trait_type;
        private String value;
    }
}
