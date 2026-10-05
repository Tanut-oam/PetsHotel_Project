package com.example.petshotel.controller.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.PetService;
import com.example.petshotel.exception.ResourceNotFoundException;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pets", description = "จัดการข้อมูลสัตว์เลี้ยงของเจ้าของ")
@RestController 
@RequestMapping("/api/owners/{ownerId}/pets")
public class PetRestController {
    private  final PetService petService;
    private final CurrentUserService currentUserService;

    public PetRestController(PetService petService,CurrentUserService currentUserService) {
        this.petService = petService;
        this.currentUserService = currentUserService;
    }

    @Operation(summary = "เพิ่มสัตว์เลี้ยง",
        description = "เจ้าของบัญชีเพิ่มสัตว์เลี้ยงของตนเอง "
                + "· ต้องส่งข้อมูลที่ผ่านการตรวจสอบ "
                + "· สัตว์เลี้ยงที่สร้างจะมีสถานะใช้งาน")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "เพิ่มสัตว์เลี้ยงสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "ข้อมูลที่ส่งไม่ถูกต้อง"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "ไม่มีสิทธิ์หรือ CSRF token ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "404", description = "ไม่พบเจ้าของ")
    })
    @PostMapping 
    public  ResponseEntity<PetResponse> createPet(@PathVariable Long ownerId,Principal principal,@Valid  @RequestBody CreatePetRequest request){
        requireCurrentOwner(ownerId, principal);
        PetResponse response = petService.createPet(ownerId, request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "ดูรายการสัตว์เลี้ยงของเจ้าของ",
        description = "เจ้าของบัญชีดูรายการสัตว์เลี้ยงของตนเอง "
                + "· แสดงเฉพาะสัตว์เลี้ยงที่ยังใช้งาน "
                + "· เรียกดูสัตว์เลี้ยงของบัญชีอื่นไม่ได้")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ดึงรายการสัตว์เลี้ยงสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "รหัสเจ้าของไม่ถูกต้อง"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "ไม่มีสิทธิ์ดูสัตว์เลี้ยงของเจ้าของนี้"),
        @ApiResponse(responseCode = "404", description = "ไม่พบเจ้าของ")
    })
    @GetMapping 
    public List<PetResponse> getPetsByOwner(@PathVariable Long ownerId,Principal principal){
        requireCurrentOwner(ownerId, principal);
        return petService.getPetsByOwner(ownerId);
    }


    @Operation(summary = "ดูข้อมูลสัตว์เลี้ยงหนึ่งตัว",
        description = "ดูข้อมูลสัตว์เลี้ยงตามรหัส petId "
                + "· เจ้าของบัญชีดูได้เฉพาะสัตว์เลี้ยงของตนเอง")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ดึงข้อมูลสัตว์เลี้ยงสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "รูปแบบรหัสใน URL ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "ไม่มีสิทธิ์เข้าถึงข้อมูลของเจ้าของนี้"),
        @ApiResponse(responseCode = "404", description = "ไม่พบสัตว์เลี้ยงของเจ้าของนี้")
    })
    @GetMapping("/{petId}")
    public PetResponse getPetById(@PathVariable Long ownerId,@PathVariable Long petId,Principal principal){
        requireCurrentOwner(ownerId, principal);
        return  petService.getPetById(petId, ownerId);
    }

    @Operation(summary = "แก้ไขข้อมูลสัตว์เลี้ยง",
        description = "เจ้าของบัญชีแก้ไขข้อมูลสัตว์เลี้ยงของตนเอง "
                + "· ต้องส่งข้อมูลใหม่ที่ผ่านการตรวจสอบ "
                + "· ไม่สามารถแก้ไขสัตว์เลี้ยงของบัญชีอื่นได้")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "แก้ไขข้อมูลสัตว์เลี้ยงสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "ข้อมูลที่ส่งหรือรหัสใน URL ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "ไม่มีสิทธิ์แก้ไขข้อมูลของเจ้าของนี้ หรือ CSRF token ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "404", description = "ไม่พบสัตว์เลี้ยงของเจ้าของนี้")
    })
    @PutMapping("/{petId}")
    public  PetResponse updatePet(@PathVariable Long ownerId,@PathVariable Long petId,Principal principal,@Valid @RequestBody UpdatePetRequest request){
        requireCurrentOwner(ownerId, principal);
        return petService.updatePet(petId, ownerId, request);
    }

    @Operation(summary = "ปิดใช้งานสัตว์เลี้ยง",
        description = "เจ้าของบัญชีปิดใช้งานสัตว์เลี้ยงของตนเอง "
                + "· ข้อมูลยังอยู่ในระบบ "
                + "· สัตว์เลี้ยงจะไม่ปรากฏในรายการที่ยังใช้งาน")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "ปิดใช้งานสัตว์เลี้ยงสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "รูปแบบรหัสใน URL ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "ไม่มีสิทธิ์แก้ไขข้อมูลของเจ้าของนี้ หรือ CSRF token ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "404", description = "ไม่พบสัตว์เลี้ยงของเจ้าของนี้")
    })
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deactivatePet(@PathVariable Long ownerId,@PathVariable Long petId,Principal principal){
        requireCurrentOwner(ownerId, principal);
        petService.deactivatePet(petId, ownerId);
        return ResponseEntity.noContent().build();
    }
    
    private  void requireCurrentOwner(Long ownerId, Principal principal){
        if(principal == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User currentUser;
        try {
            currentUser = currentUserService.getByEmail(
                    principal.getName());
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(currentUser.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!ownerId.equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Cannot access another owner's pets");
        }
    }
}
