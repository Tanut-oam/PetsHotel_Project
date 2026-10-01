package com.example.petshotel.repository;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PetType;
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
class BookingPetRepositoryTest {

    private static final List<BookingStatus>
            RESERVING_STATUSES = List.of(
                    BookingStatus.PENDING,
                    BookingStatus.CONFIRMED,
                    BookingStatus.CHECKED_IN
            );

    @Autowired
    private BookingPetRepository bookingPetRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Room room;
    private Pet pet;

    @BeforeEach
    void setUp() {
        user = createUser();
        room = createRoom();
        pet = createPet("มอคอ");
    }

    @Test
    void overlappingBookingShouldReturnPetId() {
        saveBooking(
                pet,
                BookingStatus.CONFIRMED,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        List<Long> overlappingPetIds =
                bookingPetRepository.findOverlappingPetIds(
                        List.of(pet.getId()),
                        LocalDate.of(2026, 10, 11),
                        LocalDate.of(2026, 10, 13),
                        RESERVING_STATUSES
                );

        assertThat(overlappingPetIds)
                .containsExactly(pet.getId());
    }

    @Test
    void pendingBookingShouldReservePetDates() {
        saveBooking(
                pet,
                BookingStatus.PENDING,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        List<Long> overlappingPetIds =
                bookingPetRepository.findOverlappingPetIds(
                        List.of(pet.getId()),
                        LocalDate.of(2026, 10, 11),
                        LocalDate.of(2026, 10, 13),
                        RESERVING_STATUSES
                );

        assertThat(overlappingPetIds)
                .containsExactly(pet.getId());
    }

    @Test
    void adjacentDatesShouldNotOverlap() {
        saveBooking(
                pet,
                BookingStatus.CONFIRMED,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        List<Long> overlappingPetIds =
                bookingPetRepository.findOverlappingPetIds(
                        List.of(pet.getId()),
                        LocalDate.of(2026, 10, 12),
                        LocalDate.of(2026, 10, 14),
                        RESERVING_STATUSES
                );

        assertThat(overlappingPetIds).isEmpty();
    }

    @Test
    void cancelledBookingShouldNotReservePetDates() {
        saveBooking(
                pet,
                BookingStatus.CANCELLED,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        List<Long> overlappingPetIds =
                bookingPetRepository.findOverlappingPetIds(
                        List.of(pet.getId()),
                        LocalDate.of(2026, 10, 11),
                        LocalDate.of(2026, 10, 13),
                        RESERVING_STATUSES
                );

        assertThat(overlappingPetIds).isEmpty();
    }

    private User createUser() {
        User newUser = new User();
        newUser.setFirstName("Test");
        newUser.setLastName("Customer");
        newUser.setEmail("pet-overlap@example.com");
        newUser.setPassword("encoded-password");
        newUser.setPhoneNumber("0811111111");
        newUser.setRole(UserRole.CUSTOMER);
        newUser.setActive(true);

        entityManager.persist(newUser);
        return newUser;
    }

    private Room createRoom() {
        Room newRoom = new Room();
        newRoom.setRoomNumber("TEST-101");
        newRoom.setName("Repository Test Room");
        newRoom.setDescription("Test room");
        newRoom.setCapacity(3);
        newRoom.setPricePerPetPerNight(
                new BigDecimal("500.00")
        );
        newRoom.setStatus(RoomStatus.ACTIVE);

        entityManager.persist(newRoom);
        return newRoom;
    }

    private Pet createPet(String name) {
        Pet newPet = new Pet();
        newPet.setName(name);
        newPet.setType(PetType.DOG);
        newPet.setOwner(user);
        newPet.setActive(true);

        entityManager.persist(newPet);
        return newPet;
    }

    private Booking saveBooking(
            Pet bookingPet,
            BookingStatus status,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(status)
                .totalPrice(new BigDecimal("1000.00"))
                .roomAmount(new BigDecimal("1000.00"))
                .serviceAmount(BigDecimal.ZERO)
                .surchargeAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .build();

        booking.addPet(bookingPet);

        return bookingRepository.saveAndFlush(booking);
    }
}
