-- Ebook Management System Schema

-- -----------------------------------------------------
-- Table `authors`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,  
    name VARCHAR(255) NOT NULL,
    biography TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

);

-- -----------------------------------------------------
-- Table `genres`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, 
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   
);

-- -----------------------------------------------------
-- Table `books`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, 
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,              -- International Standard Book Number
    description TEXT,
    cover_image_path VARCHAR(512),        -- Path to the cover image file
    ebook_file_path VARCHAR(512) NOT NULL, -- Path to the ebook file (PDF, EPUB, etc.)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   
);

-- -----------------------------------------------------
-- Table `book_authors` (Join Table for Many-to-Many between Books and Authors)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table `book_genres` (Join Table for Many-to-Many between Books and Genres)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS book_genres (
    book_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Optional: Add some sample data (Uncomment to use)
-- -----------------------------------------------------
/*
-- Sample Authors
INSERT INTO authors (name, biography) VALUES
('J.K. Rowling', 'British author, best known for writing the Harry Potter fantasy series.'),
('George Orwell', 'English novelist, essayist, journalist and critic.'),
('J.R.R. Tolkien', 'English writer, poet, philologist, and academic.');

-- Sample Genres
INSERT INTO genres (name, description) VALUES
('Fantasy', 'Genre of speculative fiction set in a fictional universe, often inspired by real world myth and folklore.'),
('Dystopian', 'Genre of speculative fiction that explores social and political structures in a dark, nightmare world.'),
('Science Fiction', 'Genre of speculative fiction which typically deals with imaginative and futuristic concepts.');

-- Sample Books (Assuming files are stored elsewhere and paths are recorded)
-- For now, let's assume some book IDs exist for the foreign keys in join tables
-- We'll insert books first, then link them

INSERT INTO books (title, isbn, description, cover_image_path, ebook_file_path) VALUES
('Harry Potter and the Sorcerer''s Stone', '978-0590353427', 'The first novel in the Harry Potter series.', '/covers/hp1.jpg', '/ebooks/hp1.epub'),
('Nineteen Eighty-Four', '978-0451524935', 'A dystopian social science fiction novel and cautionary tale.', '/covers/1984.jpg', '/ebooks/1984.pdf'),
('The Hobbit', '978-0547928227', 'A fantasy novel and children''s book.', '/covers/hobbit.jpg', '/ebooks/hobbit.epub');

-- Link Books to Authors (Assuming author IDs are 1, 2, 3 and book IDs are 1, 2, 3 respectively)
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), -- Harry Potter by J.K. Rowling
(2, 2), -- 1984 by George Orwell
(3, 3); -- The Hobbit by J.R.R. Tolkien

-- Link Books to Genres
INSERT INTO book_genres (book_id, genre_id) VALUES
(1, 1), -- Harry Potter is Fantasy
(2, 2), -- 1984 is Dystopian
(3, 1); -- The Hobbit is Fantasy
*/