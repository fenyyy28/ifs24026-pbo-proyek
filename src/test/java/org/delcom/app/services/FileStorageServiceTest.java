package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        // Inject tempDir sebagai uploadDir
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void testStoreFile_Success() throws IOException {
        UUID todoId = UUID.randomUUID();
        String originalFilename = "test.png";
        byte[] content = "Hello World".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", originalFilename, "image/png", content);

        String storedFilename = fileStorageService.storeFile(file, todoId);

        assertTrue(storedFilename.startsWith("cover_" + todoId));
        assertTrue(storedFilename.endsWith(".png"));

        Path filePath = tempDir.resolve(storedFilename);
        assertTrue(Files.exists(filePath));
        assertArrayEquals(content, Files.readAllBytes(filePath));
    }

    @Test
    void testStoreFile_WithNoExtension() throws IOException {
        UUID todoId = UUID.randomUUID();
        // Filename tanpa titik
        MockMultipartFile file = new MockMultipartFile("file", "testfile", "text/plain", "content".getBytes());

        String storedFilename = fileStorageService.storeFile(file, todoId);
        
        // Extension kosong, nama file berakhir dengan UUID
        assertTrue(storedFilename.endsWith(todoId.toString())); 
    }

    // --- TEST BARU: COVERAGE NULL FILENAME ---
    @Test
    void testStoreFile_NullOriginalFilename() throws IOException {
        UUID todoId = UUID.randomUUID();
        
        // Kita mock MultipartFile karena MockMultipartFile biasanya tidak mengizinkan null filename di constructor
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String storedFilename = fileStorageService.storeFile(mockFile, todoId);

        // Harus tetap tersimpan tapi tanpa ekstensi
        assertTrue(storedFilename.startsWith("cover_" + todoId));
        assertFalse(storedFilename.contains(".")); // Tidak ada titik karena ext kosong
        
        // Verifikasi file tersimpan
        Path filePath = tempDir.resolve(storedFilename);
        assertTrue(Files.exists(filePath));
    }

    @Test
    void testStoreFile_CreatesDirectory_IfNotExist() throws IOException {
        // Arahkan uploadDir ke subfolder yang belum ada
        Path subDir = tempDir.resolve("new_folder");
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", subDir.toString());
        
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "A".getBytes());
        fileStorageService.storeFile(file, UUID.randomUUID());
        
        // Cek apakah folder dibuat otomatis
        assertTrue(Files.exists(subDir));
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        String filename = "todelete.txt";
        Path filePath = tempDir.resolve(filename);
        Files.createFile(filePath);

        boolean deleted = fileStorageService.deleteFile(filename);
        assertTrue(deleted);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NotFound() {
        boolean deleteFail = fileStorageService.deleteFile("ghost.txt");
        assertFalse(deleteFail);
    }

    // --- TEST BARU: COVERAGE CATCH IOException ---
    @Test
    void testDeleteFile_IOException() {
        // Kita gunakan MockStatic pada class Files untuk memicu IOException
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Ketika Files.deleteIfExists dipanggil dengan Path apapun, lempar IOException
            filesMock.when(() -> Files.deleteIfExists(any(Path.class)))
                     .thenThrow(new IOException("Disk error simulasi"));

            // Panggil method service
            boolean result = fileStorageService.deleteFile("anyfile.txt");

            // Harus return false karena masuk catch block
            assertFalse(result);
        }
    }

    @Test
    void testLoadFile() {
        Path path = fileStorageService.loadFile("abc.jpg");
        assertEquals(tempDir.resolve("abc.jpg"), path);
    }

    @Test
    void testFileExists() throws IOException {
        String filename = "exists.txt";
        Files.createFile(tempDir.resolve(filename));
        
        assertTrue(fileStorageService.fileExists(filename));
        assertFalse(fileStorageService.fileExists("nope.txt"));
    }
}