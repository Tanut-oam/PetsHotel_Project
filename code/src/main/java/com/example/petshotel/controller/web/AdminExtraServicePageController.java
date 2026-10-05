package com.example.petshotel.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.dto.request.ExtraServiceRequest;
import com.example.petshotel.dto.response.ExtraServiceResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller 
@RequestMapping("/admin/services")
public class AdminExtraServicePageController {

    private final ExtraServiceService extraServiceService;
    private final ExtraServiceMapper extraServiceMapper;

    public AdminExtraServicePageController(ExtraServiceService extraServiceService, ExtraServiceMapper extraServiceMapper){
        this.extraServiceService = extraServiceService;
        this.extraServiceMapper = extraServiceMapper;
    }

    @GetMapping
    public String showServices(@RequestParam(name = "editId", required = false) Long editId, Model model){
        List<ExtraServiceResponse> responses = new ArrayList<>();

        for(ExtraService service : extraServiceService.getAllExtraServices()){
            ExtraServiceResponse response = extraServiceMapper.toResponse(service);
            responses.add(response);
        }

        model.addAttribute("extraServices", responses);
        model.addAttribute("editingId", editId);

        if (!model.containsAttribute("serviceForm")) {
            ExtraServiceRequest form;

            if (editId == null) {
                form = new ExtraServiceRequest("", "", null, true);
            }else{
                ExtraService service = extraServiceService.getExtraServiceById(editId);

                form = new ExtraServiceRequest(
                    service.getName(),
                    service.getDescription(),
                    service.getPrice(),
                    service.getActive());
            }
            model.addAttribute("serviceForm", form);
        }
        return "admin/services";
    }

    @PostMapping 
    public String saveService(@RequestParam(name = "id", required = false) Long id, @Valid @ModelAttribute("serviceForm") ExtraServiceRequest request,
        BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            return showServices(id, model);
        }

        try{
            ExtraService service = extraServiceMapper.toEntity(request);

            if (id == null) {
                extraServiceService.createExtraService(service);
            }else{
                extraServiceService.updateExtraService(id, service);
            }
        }catch(IllegalArgumentException exception){
            bindingResult.reject("service.invalid", exception.getMessage());
            return  showServices(id, model);
        }

        redirectAttributes.addFlashAttribute("message", id == null ? "เพิ่มบริการเสริมสำเร็จ" : "แก้ไขบริการเสริมสำเร็จ");
        return "redirect:/admin/services";

    }

    @PostMapping("/{id}/deactivate")
    public String deactivateService(@PathVariable Long id, RedirectAttributes redirectAttributes){
        extraServiceService.deleteExtraService(id);
        redirectAttributes.addFlashAttribute("message", "ปิดใช้บริการเสริมสำเร็จ");
        return "redirect:/admin/services";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException exception, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("error", exception.getMessage());
        return "redirect:/admin/services";
    }

}