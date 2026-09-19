package com.example.petshotel.controller.api;

import java.util.List;

import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.request.UpdateStatusRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.service.RoomService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;






@RestController
@RequestMapping("/api/room")
public class RoomRestController {
    private RoomService roomService;

    public RoomRestController(RoomService roomService){
        this.roomService = roomService;
    }

    @GetMapping("")
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PatchMapping("/{id}/deactivate")
    public RoomResponse deactivateRoom(@PathVariable Long id) {
        return roomService.deactivateRoom(id);
    }

    @PatchMapping("/{id}/status")
    public RoomResponse setRoomStatus(@PathVariable Long id,@RequestBody UpdateStatusRequest request){
        return  roomService.setRoomStatus(id, request);
    }


    @PostMapping("")
    public RoomResponse createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }
    

    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @RequestBody UpdateRoomRequest request) {

        return roomService.updateRoom(id, request);
    }

}
