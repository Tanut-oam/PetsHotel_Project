package com.example.petshotel.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.request.ExtraServiceRequest;
import com.example.petshotel.dto.response.ExtraServiceResponse;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/extra-services")
public class ExtraServiceRestController {
    private final ExtraServiceService extraServiceService;
    private final ExtraServiceMapper extraServiceMapper;

    public ExtraServiceRestController(
        ExtraServiceService extraServiceService,
        ExtraServiceMapper extraServiceMapper){
            this.extraServiceService = extraServiceService;
            this.extraServiceMapper = extraServiceMapper;
    }

    @GetMapping 
    public List<ExtraServiceResponse> getAllExtraServices(){
        return extraServiceService.getAllExtraServices().stream()
                .map(extraServiceMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ExtraServiceResponse getExtraServiceById(@PathVariable Long id){
        return extraServiceMapper.toResponse(extraServiceService.getExtraServiceById(id));
    }

    @PostMapping 
    public ResponseEntity<ExtraServiceResponse> createExtraService(
            @Valid @RequestBody ExtraServiceRequest request){
                ExtraServiceResponse response = extraServiceMapper.toResponse(
                    extraServiceService.createExtraService(
                        extraServiceMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ExtraServiceResponse updateExtraService(
        @PathVariable Long id,
        @Valid @RequestBody ExtraServiceRequest request){
            return extraServiceMapper.toResponse(
                extraServiceService.updateExtraService(id, extraServiceMapper.toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtraService(@PathVariable Long id){
        extraServiceService.deleteExtraService(id);
        return ResponseEntity.noContent().build();
    }


}
