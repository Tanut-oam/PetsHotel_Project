package com.example.petshotel.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.repository.ExtraServiceRepository;
import com.example.petshotel.service.ExtraServiceService;

@Service 
public class ExtraServiceServiceImpl implements ExtraServiceService{
    private final ExtraServiceRepository extraServiceRepository;

    public ExtraServiceServiceImpl(ExtraServiceRepository extraServiceRepository) {
        this.extraServiceRepository = extraServiceRepository;
    }

    @Override 
    @Transactional(readOnly = true)
    public List<ExtraService> getAllExtraServices(){
        return extraServiceRepository.findAll();
    }

    @Override 
    @Transactional(readOnly = true)
    public Optional<ExtraService> getExtraServiceById(Long id){
        return extraServiceRepository.findById(id);
    }

    @Override 
    @Transactional 
    public ExtraService createExtraService(ExtraService extraService){
        return extraServiceRepository.save(extraService);
    }

    @Override 
    @Transactional 
    public ExtraService updateExtraService(Long id, ExtraService extraServiceDetails) {
        return  extraServiceRepository.findById(id)
                .map(existingService ->{
                    existingService.setName(extraServiceDetails.getName());
                    existingService.setDescription(extraServiceDetails.getDescription());
                    existingService.setPrice(extraServiceDetails.getPrice());
                    return extraServiceRepository.save(existingService);
                })
                .orElseThrow(() -> new RuntimeException("ExtraService not found with id: " + id));
    }

    @Override 
    @Transactional 
    public void deleteExtraService(Long id){
        extraServiceRepository.deleteById(id);
    }


}
