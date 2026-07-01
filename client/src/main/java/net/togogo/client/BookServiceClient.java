package net.togogo.client;

import net.togogo.common.Result;
import net.togogo.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "nacon7-server-books", url = "http://localhost:8081", configuration = UserFeignConfig.class)
public interface BookServiceClient {

    @PostMapping("/api/books/create")
    Result<BookDTO> createBook(@RequestBody CreateBookRequest request);

    @GetMapping("/api/books/getById/{id}")
    Result<BookDTO> getBookById(@PathVariable Long id);

    @GetMapping("/api/books/getAll")
    Result<PageResponse<BookDTO>> getAllBooks(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/api/books/searchByTitle")
    Result<PageResponse<BookDTO>> searchByTitle(@RequestParam("title") String title,
                                                 @RequestParam("page") int page,
                                                 @RequestParam("size") int size);

    @GetMapping("/api/books/searchByAuthor")
    Result<PageResponse<BookDTO>> searchByAuthor(@RequestParam("author") String author,
                                                  @RequestParam("page") int page,
                                                  @RequestParam("size") int size);

    @GetMapping("/api/books/searchByCategory")
    Result<PageResponse<BookDTO>> searchByCategory(@RequestParam("category") String category,
                                                    @RequestParam("page") int page,
                                                    @RequestParam("size") int size);

    @PutMapping("/api/books/update/{id}")
    Result<BookDTO> updateBook(@PathVariable Long id, @RequestBody CreateBookRequest request);

    @DeleteMapping("/api/books/delete/{id}")
    Result<Void> deleteBook(@PathVariable Long id);

    @PostMapping("/api/books/borrow")
    Result<BorrowRecordDTO> borrowBook(@RequestBody BorrowRequest request);

    @PostMapping("/api/books/return/{recordId}")
    Result<BorrowRecordDTO> returnBook(@PathVariable Long recordId);

    @PostMapping("/api/books/renew/{recordId}")
    Result<BorrowRecordDTO> renewBook(@PathVariable Long recordId);

    @GetMapping("/api/books/borrowRecords/user/{userId}")
    Result<PageResponse<BorrowRecordDTO>> getBorrowRecordsByUser(@PathVariable Long userId,
                                                                  @RequestParam("page") int page,
                                                                  @RequestParam("size") int size);

    @GetMapping("/api/books/borrowRecords/book/{bookId}")
    Result<List<BorrowRecordDTO>> getBorrowRecordsByBook(@PathVariable Long bookId);

    @GetMapping("/api/books/borrowRecords/all")
    Result<PageResponse<BorrowRecordDTO>> getAllBorrowRecords(@RequestParam("page") int page,
                                                               @RequestParam("size") int size);

    @GetMapping("/api/books/overdue")
    Result<PageResponse<BorrowRecordDTO>> getOverdueRecords(@RequestParam("page") int page,
                                                             @RequestParam("size") int size);
}
