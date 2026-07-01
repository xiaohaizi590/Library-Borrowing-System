package net.togogo.controller;

import net.togogo.common.Result;
import net.togogo.dto.BookDTO;
import net.togogo.dto.BorrowRecordDTO;
import net.togogo.dto.BorrowRequest;
import net.togogo.dto.CreateBookRequest;
import net.togogo.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BookDTO> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookDTO bookDTO = bookService.createBook(request);
        return Result.success("创建成功", bookDTO);
    }

    @GetMapping("/getById/{id}")
    public Result<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO bookDTO = bookService.getBookById(id);
        return Result.success(bookDTO);
    }

    @GetMapping("/getAll")
    public Result<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        return Result.success(books);
    }

    @GetMapping("/searchByTitle")
    public Result<Page<BookDTO>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BookDTO> books = bookService.searchByTitle(title, pageable);
        return Result.success(books);
    }

    @GetMapping("/searchByAuthor")
    public Result<Page<BookDTO>> searchByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BookDTO> books = bookService.searchByAuthor(author, pageable);
        return Result.success(books);
    }

    @GetMapping("/searchByCategory")
    public Result<Page<BookDTO>> searchByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BookDTO> books = bookService.searchByCategory(category, pageable);
        return Result.success(books);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BookDTO> updateBook(@PathVariable Long id, @RequestBody CreateBookRequest request) {
        BookDTO bookDTO = bookService.updateBook(id, request);
        return Result.success("更新成功", bookDTO);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @PostMapping("/borrow")
    @PreAuthorize("isAuthenticated()")
    public Result<BorrowRecordDTO> borrowBook(@Valid @RequestBody BorrowRequest request) {
        BorrowRecordDTO record = bookService.borrowBook(request);
        return Result.success("借阅成功", record);
    }

    @PostMapping("/return/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public Result<BorrowRecordDTO> returnBook(@PathVariable Long recordId) {
        BorrowRecordDTO record = bookService.returnBook(recordId);
        return Result.success("归还成功", record);
    }

    @PostMapping("/renew/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public Result<BorrowRecordDTO> renewBook(@PathVariable Long recordId) {
        BorrowRecordDTO record = bookService.renewBook(recordId);
        return Result.success("续借成功", record);
    }

    @GetMapping("/borrowRecords/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<BorrowRecordDTO>> getBorrowRecordsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowTime"));
        Page<BorrowRecordDTO> records = bookService.getBorrowRecordsByUser(userId, pageable);
        return Result.success(records);
    }

    @GetMapping("/borrowRecords/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BorrowRecordDTO>> getBorrowRecordsByBook(@PathVariable Long bookId) {
        List<BorrowRecordDTO> records = bookService.getBorrowRecordsByBook(bookId);
        return Result.success(records);
    }

    @GetMapping("/borrowRecords/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<BorrowRecordDTO>> getAllBorrowRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowTime"));
        Page<BorrowRecordDTO> records = bookService.getAllBorrowRecords(pageable);
        return Result.success(records);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<BorrowRecordDTO>> getOverdueRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowTime"));
        Page<BorrowRecordDTO> records = bookService.getOverdueRecords(pageable);
        return Result.success(records);
    }
}