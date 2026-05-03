
package com.example.devforge.dto;

import com.example.devforge.entity.enums.PrivacyType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityRequestDto {

    private String name;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private PrivacyType privacy;
}