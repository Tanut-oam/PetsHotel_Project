package com.example.petshotel.repository;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.domain.enums.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("repository-test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Room room;

    @BeforeEach
    void setUp() {
        user = createUser(
                "customer1@example.com",
                "0811111111"
        );

        room = createRoom(
                "A101",
                "Standard Room"
        );
    }

    @Test
    void saveShouldGenerateIdAndApplyDefaultValues() {
        Booking booking = createBooking(
                user,
                room,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                null
        );

        Booking saved = bookingRepository.saveAndFlush(booking);

        entityManager.clear();

        Booking found = bookingRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(found.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getRoom().getId()).isEqualTo(room.getId());
    }

    @Test
    void derivedQueriesShouldReturnMatchingBookings() {
        Booking expected = createBooking(
                user,
                room,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                BookingStatus.CONFIRMED
        );

        User anotherUser = createUser(
                "customer2@example.com",
                "0822222222"
        );

        Room anotherRoom = createRoom(
                "B202",
                "Deluxe Room"
        );

        Booking otherBooking = createBooking(
                anotherUser,
                anotherRoom,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3),
                BookingStatus.CANCELLED
        );

        bookingRepository.saveAllAndFlush(
                List.of(expected, otherBooking)
        );

        assertThat(bookingRepository.findByUserId(user.getId()))
                .extracting((Booking booking) -> booking.getId())
                .containsExactly(expected.getId());

        assertThat(bookingRepository.findByStatus(BookingStatus.CONFIRMED))
                .extracting((Booking booking) -> booking.getId())
                .containsExactly(expected.getId());

        assertThat(bookingRepository.findByRoomId(room.getId()))
                .extracting((Booking booking) -> booking.getId())
                .containsExactly(expected.getId());
    }

    @Test
    void existsOverlappingBookingShouldRespectDatesAndStatuses() {
        Booking existingBooking = createBooking(
                user,
                room,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                BookingStatus.CONFIRMED
        );

        bookingRepository.saveAndFlush(existingBooking);

        boolean overlapping = bookingRepository.existsOverlappingBooking(
                room.getId(),
                LocalDate.of(2026, 10, 11),
                LocalDate.of(2026, 10, 13),
                List.of(
                        BookingStatus.CONFIRMED,
                        BookingStatus.CHECKED_IN
                )
        );

        boolean startsOnPreviousCheckoutDate =
                bookingRepository.existsOverlappingBooking(
                        room.getId(),
                        LocalDate.of(2026, 10, 12),
                        LocalDate.of(2026, 10, 14),
                        List.of(BookingStatus.CONFIRMED)
                );

        boolean endsOnPreviousCheckInDate =
                bookingRepository.existsOverlappingBooking(
                        room.getId(),
                        LocalDate.of(2026, 10, 8),
                        LocalDate.of(2026, 10, 10),
                        List.of(BookingStatus.CONFIRMED)
                );

        boolean excludedStatus =
                bookingRepository.existsOverlappingBooking(
                        room.getId(),
                        LocalDate.of(2026, 10, 11),
                        LocalDate.of(2026, 10, 13),
                        List.of(BookingStatus.PENDING)
                );

        assertThat(overlapping).isTrue();
        assertThat(startsOnPreviousCheckoutDate).isFalse();
        assertThat(endsOnPreviousCheckInDate).isFalse();
        assertThat(excludedStatus).isFalse();
    }

    @Test
    void findByIdForUpdateShouldReturnExistingBooking() {
        Booking booking = createBooking(
                user,
                room,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                BookingStatus.PENDING
        );

        Booking saved = bookingRepository.saveAndFlush(booking);

        Booking lockedBooking = bookingRepository
                .findByIdForUpdate(saved.getId())
                .orElseThrow();

        assertThat(lockedBooking.getId()).isEqualTo(saved.getId());
        assertThat(lockedBooking.getStatus())
                .isEqualTo(BookingStatus.PENDING);
    }

    private User createUser(String email, String phoneNumber) {
        User newUser = new User();
        newUser.setFirstName("Test");
        newUser.setLastName("Customer");
        newUser.setEmail(email);
        newUser.setPassword("encoded-password");
        newUser.setPhoneNumber(phoneNumber);
        newUser.setRole(UserRole.CUSTOMER);
        newUser.setActive(true);

        entityManager.persist(newUser);
        return newUser;
    }

    private Room createRoom(String roomNumber, String name) {
        Room newRoom = new Room();
        newRoom.setRoomNumber(roomNumber);
        newRoom.setName(name);
        newRoom.setDescription("Room for repository testing");
        newRoom.setCapacity(3);
        newRoom.setPricePerPetPerNight(
                new BigDecimal("500.00")
        );
        newRoom.setStatus(RoomStatus.ACTIVE);

        entityManager.persist(newRoom);
        return newRoom;
    }

    private Booking createBooking(
            User bookingUser,
            Room bookingRoom,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BookingStatus status
    ) {
        return Booking.builder()
                .user(bookingUser)
                .room(bookingRoom)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .status(status)
                .totalPrice(new BigDecimal("1000.00"))
                .roomAmount(new BigDecimal("1000.00"))
                .serviceAmount(BigDecimal.ZERO)
                .surchargeAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .build();
    }
}