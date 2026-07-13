// server/src/main/java/net/togogo/service/BookService.java
package net.togogo.service;

import net.togogo.dto.BookDTO;
import net.togogo.dto.BorrowRecordDTO;
import net.togogo.dto.CreateBookRequest;
import net.togogo.dto.BorrowRequest;
import net.togogo.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    BookDTO createBook(CreateBookRequest request);
    BookDTO getBookById(Long id);
    PageResponse<BookDTO> getAllBooks(Pageable pageable);
    PageResponse<BookDTO> searchByTitle(String title, Pageable pageable);
    PageResponse<BookDTO> searchByAuthor(String author, Pageable pageable);
    PageResponse<BookDTO> searchByCategory(String category, Pageable pageable);
    BookDTO updateBook(Long id, CreateBookRequest request);
    void deleteBook(Long id);

    BorrowRecordDTO borrowBook(BorrowRequest request);
    BorrowRecordDTO returnBook(Long recordId);
    BorrowRecordDTO renewBook(Long recordId);
    PageResponse<BorrowRecordDTO> getBorrowRecordsByUser(Long userId, Pageable pageable);
    List<BorrowRecordDTO> getBorrowRecordsByBook(Long bookId);
    PageResponse<BorrowRecordDTO> getOverdueRecords(Pageable pageable);
    PageResponse<BorrowRecordDTO> getAllBorrowRecords(Pageable pageable);
}