# Write Swagger Documentation

Document REST APIs with Swagger/OpenAPI annotations following project conventions.

## When to Use

- Adding or modifying REST controllers
- Creating or updating DTOs
- Writing new API endpoints
- Updating request/response models

## Controller Documentation Pattern

```java
@Tag(name = "Domain Name", description = "Brief description of domain functionality")
@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class ExampleController {

    @Operation(
            summary = "Brief action description (imperative form)",
            description = "Detailed explanation of what this endpoint does and when to use it"
    )
    @PostMapping("/action")
    public ResponseEntity<ResponseDto> action(@Valid @RequestBody RequestDto request) {
        // implementation
    }
}
```

## DTO Documentation Pattern

### Request/Response DTOs

```java
@Schema(description = "Brief description of what this DTO represents")
public record ExampleDto(
    @Schema(description = "Field purpose and constraints", example = "example-value")
    @NotBlank(message = "Field is required")
    String field1,
    
    @Schema(description = "Numeric field description", example = "100")
    @Min(value = 1, message = "Must be positive")
    Integer field2,
    
    @Schema(description = "Optional field description", nullable = true)
    String optionalField
) {
    // Factory methods don't need Swagger docs
    public static ExampleDto from(Entity entity) {
        return new ExampleDto(
            entity.getField1(),
            entity.getField2(),
            entity.getOptionalField()
        );
    }
}
```

### Nested Objects

```java
@Schema(description = "Container for multiple related items")
public record ContainerDto(
    @Schema(description = "List of items")
    List<ItemDto> items,
    
    @Schema(description = "Total count", example = "42")
    Integer total
) {
    @Schema(description = "Individual item representation")
    public record ItemDto(
        @Schema(description = "Item identifier", example = "123")
        Long id,
        
        @Schema(description = "Item name", example = "Example Item")
        String name
    ) {}
}
```

## Real Examples from Project

### Controller Example (AuthController)

```java
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Operation(
            summary = "Register a new user",
            description =
                    "Creates a new user account and returns access and refresh tokens. Tokens are also"
                            + " set as HTTP-only cookies.")
    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@Valid @RequestBody RegisterRequest request) {
        TokenPairResponse response = authService.register(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).headers(tokenHeaders).body(response);
    }

}
```

### DTO Example

```java
@Schema(description = "Response containing a new access token")
public record AccessTokenResponse(
        @Schema(
                description = "JWT access token",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Token type", example = "Bearer") String tokenType,
        @Schema(description = "Access token expiration time in seconds", example = "3600")
        @JsonProperty("expiresIn")
        Long expiresIn) {

    public static AccessTokenResponse from(String accessToken, Long expiresIn) {
        return new AccessTokenResponse(accessToken, "Bearer", expiresIn);
    }
}
```

## Guidelines

### Required Documentation

1. **All public endpoints**: `@Operation` with summary and description
2. **All response codes**: Common ones (200, 400, 401, 404, 500)
3. **Request/Response DTOs**: `@Schema` at class level
4. **All DTO fields**: `@Schema` with description and example
5. **Controller classes**: `@Tag` for grouping in Swagger UI

### Response Code Standards

- **200** - Success with body
- **201** - Created (POST that creates a resource)
- **204** - Success with no content (DELETE)
- **400** - Bad request (validation errors)
- **401** - Unauthorized (missing/invalid token)
- **403** - Forbidden (valid token, insufficient permissions)
- **404** - Not found
- **409** - Conflict (e.g., seat already booked)
- **500** - Internal server error

### Authentication Documentation

### Validation Error Documentation

For endpoints with `@Valid` request bodies, add:

``` java
@ApiResponse(
    responseCode = "400",
    description = "Validation error - check field constraints",
    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
)
```

### Path Parameter Documentation

```java
@Operation(summary = "Get resource by ID")
@GetMapping("/{id}")
public ResponseEntity<ResourceDto> getById(
    @Parameter(description = "Resource ID", example = "123", required = true)
    @PathVariable Long id
) {
    // implementation
}
```

### Query Parameter Documentation

```java
@Operation(summary = "Search resources")
@GetMapping("/search")
public ResponseEntity<List<ResourceDto>> search(
    @Parameter(description = "Search keyword", example = "spring")
    @RequestParam(required = false) String keyword,
    
    @Parameter(description = "Page number (0-based)", example = "0")
    @RequestParam(defaultValue = "0") int page,
    
    @Parameter(description = "Page size", example = "20")
    @RequestParam(defaultValue = "20") int size
) {
    // implementation
}
```

## Common Mistakes to Avoid

1. ❌ Missing `@Tag` on controller - endpoints won't be grouped properly
2. ❌ No error response documentation - API consumers don't know what can go wrong
3. ❌ Missing examples on DTO fields - harder to understand expected format
4. ❌ Generic descriptions like "DTO" or "Request" - not helpful
5. ❌ Documenting internal/private methods - only document public API
6. ❌ Inconsistent language - stick to Korean or English throughout
7. ❌ Missing authentication requirements - consumers won't know to send token

## Checklist

Before committing controller changes:

- [ ] Controller has `@Tag` annotation
- [ ] Request DTOs have `@Schema` on class and all fields
- [ ] Response DTOs have `@Schema` on class and all fields
- [ ] Examples provided for all fields
- [ ] Authentication requirements clearly stated

## Testing

After adding documentation, verify in Swagger UI:

```bash
./gradlew bootRun
```

Visit: http://localhost:8080/swagger-ui

Check:
1. Endpoint appears in correct tag group
2. Request/response schemas are complete
3. Examples are helpful and realistic
4. Error responses are clear
