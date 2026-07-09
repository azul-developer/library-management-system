package com.liz.library.presentation.controller;

import com.liz.library.application.dto.BookFilter;
import com.liz.library.application.dto.BookResponse;
import com.liz.library.application.dto.PageResponse;
import com.liz.library.application.dto.CreateBookRequest;
import com.liz.library.application.dto.UpdateBookRequest;
import com.liz.library.application.service.BookService;
import com.liz.library.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.liz.library.presentation.exception.ApiError;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Books management operations")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Create a new book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Book already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        return bookService.create(request);
    }

    @Operation(
            summary = "Retrieve all books",
            description = "Returns a paginated list of books with optional filters(author, genre, and availability)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @Parameters({
            @Parameter(
                    name = "page",
                    in = ParameterIn.QUERY,
                    description = "Zero-based page index. Example: 0"
            ),
            @Parameter(
                    name = "size",
                    in = ParameterIn.QUERY,
                    description = "Number of records per page. Example: 10"
            ),
            @Parameter(
                    name = "sort",
                    in = ParameterIn.QUERY,
                    description = "Sorting criteria. Format: property,(asc|desc). Example: title,asc"
            )
    })
    @GetMapping
    public PageResponse<BookResponse> list(
            @ParameterObject
            @Valid
            @ModelAttribute
            BookFilter filter,

            @Parameter(hidden = true)
            Pageable pageable
    ) {
        return bookService.findAll(filter, pageable);
    }


    @Operation(summary = "Retrieve a book by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public BookResponse get(
            @Parameter(description = "Book identifier")
            @PathVariable UUID id) {

        return bookService.findById(id);
    }

    @Operation(summary = "Replace an existing book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Book already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public BookResponse update(
            @Parameter(description = "Book identifier")
            @PathVariable UUID id,
            @Valid @RequestBody CreateBookRequest request) {

        return bookService.update(id, request);
    }

    @Operation(summary = "Partially update an existing book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Book already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}")
    public BookResponse patch(
            @Parameter(description = "Book identifier")
            @PathVariable UUID id,
            @RequestBody UpdateBookRequest request) {

        return bookService.partialUpdate(id, request);
    }

    @Operation(summary = "Delete a book")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Book identifier")
            @PathVariable UUID id) {

        bookService.delete(id);
    }

    @Operation(
            summary = "Reserve a book",
            description = "Creates a loan for the authenticated user. The user identifier is obtained from the authenticated session."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book reserved successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "No copies available", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })



        @PostMapping("/{id}/reserve")
        @ResponseStatus(HttpStatus.OK)
        public void reserve(
                @Parameter(description = "Book identifier")
                @PathVariable UUID id,
                @AuthenticationPrincipal User user) {

            log.info("Reserve endpoint invoked. BookId={}", id);
            log.info("Authenticated principal: {}", user);

            if (user == null) {
                log.error("Authenticated user is null");
                throw new IllegalStateException("Authenticated user is null");
            }

            log.info("Authenticated user id={}", user.getId());

            bookService.createLoan(user.getId(), id);
        }

    @Operation(summary = "Get book availability (internal)")
    @GetMapping("/{id}/availability")
    public boolean availability(@Parameter(description = "Book identifier")
                                @PathVariable UUID id) {
        return bookService.isAvailable(id);
    }

}