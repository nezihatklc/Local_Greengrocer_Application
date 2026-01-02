package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.ProductDAO;
import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Product;
import junit.framework.TestCase;
import java.util.List;

public class TestImageBlobStorage extends TestCase {

    private ProductDAO productDAO;
    private int testProductId;

    public void setUp() {
        productDAO = new ProductDAO();
    }

    public void tearDown() {
        if (testProductId > 0) {
            productDAO.delete(testProductId);
        }
    }

    public void testImageSaveAndRetrieve() {
        // 1. Create dummy image bytes (e.g. 1KB)
        byte[] originalImage = new byte[1024];
        for (int i = 0; i < originalImage.length; i++) {
            originalImage[i] = (byte) (i % 256);
        }

        // 2. Create Product with Image
        Product p = new Product();
        p.setName("TestBlobImageProduct");
        p.setCategory(Category.FRUIT);
        p.setPrice(10.0);
        p.setStock(10.0);
        p.setThreshold(5.0);
        p.setImage(originalImage);
        p.setUnit("kg");

        // 3. Save to DB
        boolean inserted = productDAO.insert(p);
        assertTrue("Product should be inserted", inserted);
        testProductId = p.getId(); // Save ID for cleanup

        // 4. Retrieve from DB
        Product retrieved = productDAO.findById(testProductId);
        assertNotNull("Retrieved product should not be null", retrieved);

        // 5. Verify Image Bytes
        byte[] retrievedImage = retrieved.getImage();
        assertNotNull("Image BLOB should not be null", retrievedImage);
        assertEquals("Image size matches", originalImage.length, retrievedImage.length);

        for (int i = 0; i < originalImage.length; i++) {
            assertEquals("Byte at index " + i + " must match", originalImage[i], retrievedImage[i]);
        }
    }
}
