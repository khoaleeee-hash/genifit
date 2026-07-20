package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.repository.AIScanHistoryRepository;
import com.examp.genifit.repository.GuestRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.prompt.FoodScanPrompt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GeminiFoodScanServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private AIScanHistoryRepository aiScanHistoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestClient geminiRestClient;

    @Mock
    private FoodScanPrompt foodScanPrompt;

    @InjectMocks
    private GeminiFoodScanServiceImpl geminiFoodScanService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testScanFoodImage_NullUserAndGuest() {
        // Arrange
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            geminiFoodScanService.scanFoodImage(image, null, null);
        });
        
        // This validates `validateUserOrGuest`
        assertTrue(exception.getMessage().contains("user") || exception.getMessage().contains("guest") || exception.getMessage().contains("vui lòng cung cấp"));
    }

    @Test
    void testScanFoodImage_NullImage() {
        // Arrange
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            geminiFoodScanService.scanFoodImage(null, 1, null);
        });

        // This validates `validateImage`
        assertTrue(exception.getMessage().contains("Ảnh") || exception.getMessage().contains("trống"));
    }

    @Test
    void testScanFoodImage_EmptyImage() {
        // Arrange
        MockMultipartFile emptyImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[0]);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            geminiFoodScanService.scanFoodImage(emptyImage, 1, null);
        });

        // This validates `validateImage` empty check
        assertTrue(exception.getMessage().contains("Ảnh") || exception.getMessage().contains("trống"));
    }
}
