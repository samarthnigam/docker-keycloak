package com.example.librarymanagement.config;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private BookService bookService;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if no books exist
        // Temporarily disabled for testing
        /*
        if (bookService.findAllBooks().isEmpty()) {
            loadSampleBooks();
        }
        */
    }

    private void loadSampleBooks() {
        Book[] sampleBooks = {
            createBook("The Great Gatsby", "F. Scott Fitzgerald", "978-0-7432-7356-5",
                      "A classic American novel set in the Jazz Age."),
            createBook("To Kill a Mockingbird", "Harper Lee", "978-0-06-112008-4",
                      "A gripping tale of racial injustice and childhood innocence."),
            createBook("1984", "George Orwell", "978-0-452-28423-4",
                      "A dystopian social science fiction novel."),
            createBook("Pride and Prejudice", "Jane Austen", "978-0-14-143951-8",
                      "A romantic novel of manners."),
            createBook("The Catcher in the Rye", "J.D. Salinger", "978-0-316-76948-0",
                      "A controversial novel about teenage rebellion."),
            createBook("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "978-0-7475-3269-9",
                      "The first book in the Harry Potter series."),
            createBook("The Lord of the Rings", "J.R.R. Tolkien", "978-0-544-00003-7",
                      "An epic fantasy adventure."),
            createBook("The Alchemist", "Paulo Coelho", "978-0-06-112241-5",
                      "A philosophical novel about following dreams.")
        };

        for (Book book : sampleBooks) {
            bookService.saveBook(book);
        }

        System.out.println("Loaded " + sampleBooks.length + " sample books into the database.");
    }

    private Book createBook(String title, String author, String isbn, String description) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setDescription(description);
        book.setStatus(Book.BookStatus.AVAILABLE);
        return book;
    }
}