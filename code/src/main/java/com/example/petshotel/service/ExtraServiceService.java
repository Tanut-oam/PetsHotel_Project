package com.example.petshotel.service;
import com.example.petshotel.domain.entity.ExtraService;
import java.util.List;

public interface ExtraServiceService {
    List<ExtraService> getAllExtraServices();
    ExtraService getExtraServiceById(Long id);
    ExtraService createExtraService(ExtraService extraService);
    ExtraService updateExtraService(Long id, ExtraService extraServiceDetails);
    void deleteExtraService(Long id);
}