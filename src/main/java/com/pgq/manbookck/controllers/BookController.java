package com.pgq.manbookck.controllers;

import com.pgq.manbookck.models.*;
import com.pgq.manbookck.repositories.*;
import com.pgq.manbookck.services.*;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // For validation if you add it
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final AuthorRepo authorRepository; // For populating dropdowns
    private final GenreRepo genreRepository;   // For populating dropdowns
    private final FileStorageService fileStorageService;


    public BookController(BookService bookService, AuthorRepo authorRepository,
                          GenreRepo genreRepository, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAllBooks());
        return "books/list"; // points to src/main/resources/templates/books/list.html
    }

    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("allAuthors", authorRepository.findAllByOrderByNameAsc());
        model.addAttribute("allGenres", genreRepository.findAllByOrderByNameAsc());
        model.addAttribute("pageTitle", "Add New Book");
        return "books/form"; // points to src/main/resources/templates/books/form.html
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") Book book, // Add @Valid and BindingResult for validation
                           @RequestParam("coverImageFile") MultipartFile coverImageFile,
                           @RequestParam("ebookFile") MultipartFile ebookFile,
                           @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
                           @RequestParam(value = "genreIds", required = false) List<Long> genreIds,
                           RedirectAttributes redirectAttributes) {

        // Basic check for ebook file as it's mandatory by schema (but not by @ModelAttribute)
        if (book.getId() == null && (ebookFile == null || ebookFile.isEmpty())) {
             // For new books, ebook file is essential.
             // For updates, it might not be provided if not changing.
             // This logic can be refined.
            redirectAttributes.addFlashAttribute("errorMessage", "Ebook file is required for new books.");
            // Need to repopulate authors and genres for the form if redirecting back
            // This part is tricky with redirects. Better to return to form view directly.
            // For now, let's proceed and let service/DB handle constraint if path is null
        }

        try {
            if (book.getId() == null) { // New book
                 bookService.saveBook(book, coverImageFile, ebookFile, authorIds, genreIds);
                 redirectAttributes.addFlashAttribute("successMessage", "Book added successfully!");
            } else { // Existing book
                bookService.updateBook(book.getId(), book, coverImageFile, ebookFile, authorIds, genreIds);
                redirectAttributes.addFlashAttribute("successMessage", "Book updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving book: " + e.getMessage());
            // Consider redirecting back to the form with errors and populated fields
            // Or just log and show a generic error message
             return "redirect:/books" + (book.getId() == null ? "/add" : "/edit/" + book.getId());
        }
        return "redirect:/books";
    }

    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookService.findBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("allAuthors", authorRepository.findAllByOrderByNameAsc());
                    model.addAttribute("allGenres", genreRepository.findAllByOrderByNameAsc());
                    model.addAttribute("pageTitle", "Edit Book: " + book.getTitle());
                    return "books/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Book not found with ID: " + id);
                    return "redirect:/books";
                });
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting book: " + e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/view/{id}")
    public String viewBook(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
         return bookService.findBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "books/view"; // points to src/main/resources/templates/books/view.html
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Book not found with ID: " + id);
                    return "redirect:/books";
                });
    }

    // Endpoint to serve uploaded files (covers and ebooks)
    // This is a simplified way. For production, consider more robust access control.
    @GetMapping("/files/{fileType}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String fileType, @PathVariable String filename) {
        FileStorageService.FileType type = "covers".equals(fileType) ?
                                            FileStorageService.FileType.COVER :
                                            FileStorageService.FileType.EBOOK;
        Resource file = fileStorageService.loadFileAsResource(filename, type);
        String contentType = null;
        try {
            contentType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            // log error
        }
        if (contentType == null) {
            contentType = "application/octet-stream"; // Default
        }

        // For ebooks, force download
        HttpHeaders headers = new HttpHeaders();
        if (type == FileStorageService.FileType.EBOOK) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"");
        }


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .headers(headers)
                .body(file);
    }
}