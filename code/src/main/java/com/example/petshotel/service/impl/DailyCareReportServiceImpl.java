package com.example.petshotel.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.DailyCareReport;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.mapper.DailyCareReportMapper;
import com.example.petshotel.repository.BookingPetRepository;
import com.example.petshotel.repository.DailyCareReportRepository;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.DailyCareReportService;

@Service
public class DailyCareReportServiceImpl implements DailyCareReportService {
    private  final DailyCareReportRepository reportRepository;
    private  final BookingPetRepository bookingPetRepository;
    private  final UserRepository userRepository;
    private  final DailyCareReportMapper mapper;
    
    public DailyCareReportServiceImpl(DailyCareReportRepository reportRepository,
            BookingPetRepository bookingPetRepository, UserRepository userRepository, DailyCareReportMapper mapper) {
        this.reportRepository = reportRepository;
        this.bookingPetRepository = bookingPetRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override 
    @Transactional 
    public DailyCareReportResponse createReport(Long bookingPetId,Long currentUserId,CreateDailyCareReportRequest request){
        if (request == null || request.reportDate() == null){
            throw new IllegalArgumentException("Report  date is required");
        }
        
        User currentUser = findUser(currentUserId);
        requireStaffOrAdmin(currentUser);

        BookingPet bookingPet = findBookingPet(bookingPetId);
        Booking booking = bookingPet.getBooking();

        if (booking.getStatus() != BookingStatus.CHECKED_IN && booking.getStatus() != BookingStatus.CHECKED_OUT){
            throw new IllegalStateException("A care report requirees a checked-in booking");
        }
        
        requireDateWithinStay(request.reportDate(), booking);

        if(reportRepository.existsByBookingPet_IdAndReportDate(bookingPetId, request.reportDate())){
            throw new IllegalStateException("A report already exists for this pet and date");
        }

        DailyCareReport report = new DailyCareReport();
        report.setBookingPet(bookingPet);
        report.setRecordedBy(currentUser);
        report.setReportDate(request.reportDate());
        report.setFeedingMorning(request.feedingMorning());
        report.setFeedingEvening(request.feedingEvening());
        report.setWalkingMinutes(request.walkingMinutes());
        report.setGrooming(request.grooming());
        report.setMood(request.mood());
        report.setHealthNote(request.healthNote());
        report.setGeneralNote(request.generalNote());

        return  mapper.toResponse(reportRepository.save(report));
    }

    @Override 
    @Transactional(readOnly = true)
    public DailyCareReportResponse getReportById(Long reportId,Long currentUserId){
        User currentUser  = findUser(currentUserId);
        DailyCareReport report = findReport(reportId);
        requireReadAccess(currentUser, report.getBookingPet().getBooking());
        return  mapper.toResponse(report);
    }

    @Override 
    @Transactional(readOnly = true)
    public List<DailyCareReportResponse> getReportsByBookingPet(Long bookingPetId,Long currentUserId){
        User currentUser = findUser(currentUserId);
        BookingPet bookingPet = findBookingPet(bookingPetId);
        requireReadAccess(currentUser, bookingPet.getBooking());

        return reportRepository.findByBookingPet_IdOrderByReportDateDesc(bookingPetId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional 
    public DailyCareReportResponse updateReport(Long reportId,Long currentUserId,UpdateDailyCareReportRequest request){

        if (request == null || request.bookingPetId() == null || request.reportDate()==null){
            throw new IllegalArgumentException("Booking pet and report date are required");
        }
        User currentUser = findUser(currentUserId);
        requireStaffOrAdmin(currentUser);

        DailyCareReport report = findReport(reportId);
        Booking originalBooking = report.getBookingPet().getBooking();
        BookingPet selectedBookingPet =
                findBookingPet(request.bookingPetId());
        
                if (!originalBooking.getId()
                .equals(selectedBookingPet.getBooking().getId())) {
            throw new IllegalArgumentException(
                    "The selected pet must be in the original booking"
            );
        }
        
        requireDateWithinStay(request.reportDate(), originalBooking);

        if (reportRepository.existsByBookingPet_IdAndReportDateAndIdNot(
                request.bookingPetId(), request.reportDate(), reportId)) {
            throw new IllegalStateException("A report already exists for this pet and date");
        }

        report.setBookingPet(selectedBookingPet);
        report.setReportDate(request.reportDate());
        report.setFeedingMorning(request.feedingMorning());
        report.setFeedingEvening(request.feedingEvening());
        report.setWalkingMinutes(request.walkingMinutes());
        report.setGrooming(request.grooming());
        report.setMood(request.mood());
        report.setHealthNote(request.healthNote());
        report.setGeneralNote(request.generalNote());

        return mapper.toResponse(reportRepository.save(report));
    }
    
    private User findUser(Long userId){
        return userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("User not found: " +userId));
    }

    private BookingPet findBookingPet(Long bookingPetId) {
        return bookingPetRepository.findById(bookingPetId).orElseThrow(() ->new IllegalArgumentException("Booking pet not found: " + bookingPetId));
    }

    private DailyCareReport findReport(Long reportId){
        return reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    }

    private  void requireStaffOrAdmin(User user){
        if (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.ADMIN){
            throw new IllegalArgumentException("Only staff or admin can change care reports");
        }
    }

    private  void requireReadAccess(User user,Booking booking){
        
        if(user.getRole() == UserRole.STAFF || user.getRole() == UserRole.ADMIN){
            return;
        }

        if(user.getRole() != UserRole.CUSTOMER || !booking.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("You cannot view this care report");
        }
    }

    private  void requireDateWithinStay(LocalDate reporDate,Booking booking){

        if(reporDate.isBefore(booking.getCheckInDate()) || reporDate.isAfter(booking.getCheckOutDate())){
            throw new IllegalArgumentException("Report date must be within the stay");
        }
    }
}
