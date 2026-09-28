package com.example.petshotel.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.dto.response.ExtraServiceResponse;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;

import org.springframework.ui.Model;

@Controller 
public class AdminExtraServicePageController {
    private final ExtraServiceService extraServiceService;
    private final ExtraServiceMapper extraServiceMapper;

    public AdminExtraServicePageController(ExtraServiceService extraServiceService, ExtraServiceMapper extraServiceMapper){
        this.extraServiceService = extraServiceService;
        this.extraServiceMapper = extraServiceMapper;
    }

    @GetMapping ("/admin/services")
    public String showServices(Model model){
        List<ExtraServiceResponse> responses = new ArrayList<>();

        for(ExtraService service : extraServiceService.getAllExtraServices()){
            ExtraServiceResponse response = extraServiceMapper.toResponse(service);
            responses.add(response);
        }

        model.addAttribute("extraServices", responses);
        return "admin/services";
    }
}
