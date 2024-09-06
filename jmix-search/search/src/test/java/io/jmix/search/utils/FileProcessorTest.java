
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.search.exception.UnsupportedFileFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileProcessorTest {

    public static final String FILE_NAME_EXAMPLE = "the-file-with-not-supported-extension.sql";
    public static final String FILE_NAME_EXAMPLE_2 = "another-file.smt";

    @Test
    void extractFileContent_1() {
        FileStorageLocator storageLocatorMock = mock(FileStorageLocator.class);
        FileProcessor fileProcessor = new FileProcessor(storageLocatorMock);
        FileRef fileRefMock = mock(FileRef.class);
        when(fileRefMock.getFileName()).thenReturn(FILE_NAME_EXAMPLE);
        UnsupportedFileFormatException exception = assertThrows(
                UnsupportedFileFormatException.class,
                () -> fileProcessor.extractFileContent(fileRefMock));
        assertEquals(
                "The file the-file-with-not-supported-extension.sql with 'sql' extension is not supported.",
                exception.getMessage());
    }

    @Test
    void extractFileContent_2() {
        FileStorageLocator storageLocatorMock = mock(FileStorageLocator.class);
        FileProcessor fileProcessor = new FileProcessor(storageLocatorMock);
        FileRef fileRefMock = mock(FileRef.class);
        when(fileRefMock.getFileName()).thenReturn(FILE_NAME_EXAMPLE_2);
        UnsupportedFileFormatException exception = assertThrows(
                UnsupportedFileFormatException.class,
                () -> fileProcessor.extractFileContent(fileRefMock));
        assertEquals(
                "The file another-file.smt with 'smt' extension is not supported.",
                exception.getMessage());
    }

}