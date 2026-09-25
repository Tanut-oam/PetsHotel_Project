package com.example.petshotel.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.repository.ExtraServiceRepository;
import com.example.petshotel.service.ExtraServiceService;

@Service
public class ExtraServiceServiceImpl implements ExtraServiceService {

    private final ExtraServiceRepository extraServiceRepository;

    public ExtraServiceServiceImpl(ExtraServiceRepository extraServiceRepository) {
        this.extraServiceRepository = extraServiceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtraService> getAllExtraServices() {
        return extraServiceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ExtraService getExtraServiceById(Long id) {
        return extraServiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExtraService not found: " + id));
    }

    @Override
    @Transactional
    public ExtraService createExtraService(ExtraService extraService) {
        if (extraService == null
                || extraService.getName() == null
                || extraService.getName().isBlank()
                || extraService.getPrice() == null
                || extraService.getPrice().signum() < 0) {
            throw new IllegalArgumentException(
                    "Extra service requires a name and a non-negative price");
        }
        extraService.setName(extraService.getName().trim());

        if (extraService.getActive() == null) {
            extraService.setActive(true);
        }

        return extraServiceRepository.save(extraService);
    }

    @Override
    @Transactional
    public ExtraService updateExtraService(Long id, ExtraService extraServiceDetails) {
        if (extraServiceDetails == null
                || extraServiceDetails.getName() == null
                || extraServiceDetails.getName().isBlank()
                || extraServiceDetails.getPrice() == null
                || extraServiceDetails.getPrice().signum() < 0) {
            throw new IllegalArgumentException(
                    "Extra service requires a name and a non-negative price");
        }

        ExtraService extraService = getExtraServiceById(id);
        extraService.setName(extraServiceDetails.getName().trim());
        extraService.setDescription(extraServiceDetails.getDescription());
        extraService.setPrice(extraServiceDetails.getPrice());

        if (extraServiceDetails.getActive() != null) {
            extraService.setActive(extraServiceDetails.getActive());
        }
        return extraServiceRepository.save(extraService);
    }

    @Override
    @Transactional
    public void deleteExtraService(Long id) {
        ExtraService extraService = extraServiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExtraService not found: " + id));
        extraService.setActive(false);
        extraServiceRepository.save(extraService);
    }
}