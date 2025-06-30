package com.marketplace;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "cloudinary.cloud_name=test_cloud",
    "cloudinary.api_key=test_key", 
    "cloudinary.api_secret=test_secret"
})
public class CloudinaryTest {

    @Autowired
    private Cloudinary cloudinary;

    @Test
    public void testCloudinaryConfiguration() {
        assertNotNull(cloudinary, "Cloudinary should be configured");
        assertNotNull(cloudinary.config.cloudName, "Cloud name should be set");
        assertNotNull(cloudinary.config.apiKey, "API key should be set");
        assertNotNull(cloudinary.config.apiSecret, "API secret should be set");
    }
} 