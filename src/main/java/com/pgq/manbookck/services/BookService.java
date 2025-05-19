package com.pgq.manbookck.services;


import com.pgq.manbookck.models.*;
import com.pgq.manbookck.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepo bookRepository;
    private final AuthorRepo authorRepository;
    private final GenreRepo genreRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepo bookRepository, AuthorRepo authorRepository,
                       GenreRepo genreRepository, FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Book> findAllBooks() {
        return bookRepository.findAllWithAuthorsAndGenres();
    }

    public Optional<Book> findBookById(Long id) {
        return Optional.ofNullable(bookRepository.findByIdWithAuthorsAndGenres(id));
    }

    @Transactional
    public Book saveBook(Book book, MultipartFile coverImageFile, MultipartFile ebookFile,
                         List<Long> authorIds, List<Long> genreIds) {
        // Handle file uploads
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverFileName = fileStorageService.storeFile(coverImageFile, FileStorageService.FileType.COVER);
            book.setCoverImagePath(coverFileName);
        }
        if (ebookFile != null && !ebookFile.isEmpty()) {
            String ebookFileName = fileStorageService.storeFile(ebookFile, FileStorageService.FileType.EBOOK);
            book.setEbookFilePath(ebookFileName);
        }

        // Handle Authors
        if (authorIds != null && !authorIds.isEmpty()) {
            Set<Author> authors = authorIds.stream()
                    .map(authorRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            book.setAuthors(authors);
        } else {
            book.setAuthors(new HashSet<>());
        }

        // Handle Genres
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = genreIds.stream()
                    .map(genreRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            book.setGenres(genres);
        } else {
            book.setGenres(new HashSet<>());
        }

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book bookDetails, MultipartFile coverImageFile, MultipartFile ebookFile,
                           List<Long> authorIds, List<Long> genreIds) {
        Book existingBook = bookRepository.findByIdWithAuthorsAndGenres(id);
        if (existingBook == null) {
            throw new RuntimeException("Book not found with id: " + id);
        }

        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setIsbn(bookDetails.getIsbn());
        existingBook.setDescription(bookDetails.getDescription());

        // Update cover image if a new one is provided
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // Delete old cover if it exists
            if (existingBook.getCoverImagePath() != null) {
                fileStorageService.deleteFile(existingBook.getCoverImagePath(), FileStorageService.FileType.COVER);
            }
            String newCoverFileName = fileStorageService.storeFile(coverImageFile, FileStorageService.FileType.COVER);
            existingBook.setCoverImagePath(newCoverFileName);
        }

        // Update ebook file if a new one is provided
        if (ebookFile != null && !ebookFile.isEmpty()) {
            // Delete old ebook file if it exists
            if (existingBook.getEbookFilePath() != null) {
                fileStorageService.deleteFile(existingBook.getEbookFilePath(), FileStorageService.FileType.EBOOK);
            }
            String newEbookFileName = fileStorageService.storeFile(ebookFile, FileStorageService.FileType.EBOOK);
            existingBook.setEbookFilePath(newEbookFileName);
        }


        // Update Authors
        if (authorIds != null) { // Allow empty list to remove all authors
            Set<Author> authors = authorIds.stream()
                    .map(authorRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            existingBook.setAuthors(authors);
        } else {
             existingBook.setAuthors(new HashSet<>());
        }


        // Update Genres
        if (genreIds != null) { // Allow empty list to remove all genres
            Set<Genre> genres = genreIds.stream()
                    .map(genreRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            existingBook.setGenres(genres);
        } else {
            existingBook.setGenres(new HashSet<>());
        }

        return bookRepository.save(existingBook);
    }


    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        // Delete associated files
        if (book.getCoverImagePath() != null) {
            fileStorageService.deleteFile(book.getCoverImagePath(), FileStorageService.FileType.COVER);
        }
        if (book.getEbookFilePath() != null) {
            fileStorageService.deleteFile(book.getEbookFilePath(), FileStorageService.FileType.EBOOK);
        }
        bookRepository.deleteById(id);
    }
}