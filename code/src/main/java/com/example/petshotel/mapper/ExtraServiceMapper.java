package com.example.petshotel.mapper;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.dto.request.ExtraServiceRequest;
import com.example.petshotel.dto.response.ExtraServiceResponse;

@Component 
public class ExtraServiceMapper {
    public ExtraService toEntity(ExtraServiceRequest request){
        ExtraService extraService = new ExtraService();
        extraService.setName(request.name());
        extraService.setDescription(request.description());
        extraService.setPrice(request.price());
        extraService.setActive(request.active());
        return extraService;
    }

    public ExtraServiceResponse toResponse(ExtraService extraService){
        return new ExtraServiceResponse(
            extraService.getId(),
            extraService.getName(),
            extraService.getDescription(),
            extraService.getPrice(),
            extraService.getActive()
        );
    }

}
