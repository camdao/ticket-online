# dto

Generate request and response DTOs following the project's modern record-based pattern.

## Usage

```
/dto request <DomainName> <fields...>
/dto response <DomainName> <fields...>
```

## Examples

```
/dto request Cinema name:String:NotBlank brand:String:NotBlank logoUrl:String city:String:NotBlank
/dto response Cinema id:Long name:String brand:String logoUrl:String city:String createdAt:LocalDateTime
```

## Instructions

When invoked with `request`:
1. Generate a Java **record** for the request DTO
2. Place it in `domain/<domain>/dto/request/<Name>Request.java`
3. Follow this pattern:
   - Package: `com.ticket_online.domain.<domain>.dto.request`
   - Use Java `record` keyword
   - Add `@Schema` annotation with description
   - For each field:
     - Add `@Schema` with description, example, and `requiredMode` if required
     - Add Jakarta validation annotations based on field name/type:
       - `@NotBlank` for required String fields
       - `@NotNull` for required non-String fields
       - `@Email` for email fields
       - `@Size` for length constraints
       - `@Min`/`@Max` for numeric constraints
       - `@Pattern` for regex patterns (e.g., phone numbers)
     - Add custom validation message
4. Import only what's used:
   - `io.swagger.v3.oas.annotations.media.Schema`
   - `jakarta.validation.constraints.*` (specific ones)

When invoked with `response`:
1. Generate a Java **record** for the response DTO
2. Place it in `domain/<domain>/dto/response/<Name>Response.java`
3. Follow this pattern:
   - Package: `com.ticket_online.domain.<domain>.dto.response`
   - Use Java `record` keyword
   - Add `@Schema` annotation with description
   - For each field:
     - Add `@Schema` with description and example
   - Add static factory method `from()` that takes the domain entity
   - If the entity field needs formatting (like `@JsonProperty`), add it
4. Import only what's used:
   - `io.swagger.v3.oas.annotations.media.Schema`
   - `com.fasterxml.jackson.annotation.JsonProperty` (if needed)
   - Domain entity class

## Field Format

Fields should be specified as: `fieldName:Type:Constraint1:Constraint2`

Common types:
- `String`, `Long`, `Integer`, `BigDecimal`, `Boolean`
- `LocalDateTime`, `LocalDate`, `LocalTime`
- `List<Type>`, `Set<Type>`
- Custom enums or domain types

Common constraints for requests:
- `NotBlank` - required String
- `NotNull` - required non-String
- `Email` - email validation
- `Size:min:max` - length constraint
- `Min:value` - minimum value
- `Max:value` - maximum value
- `Pattern:regex` - regex pattern
- `Phone` - phone number (auto-generates pattern)

## Template: Request DTO

```java
package com.ticket_online.domain.{domain}.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for {action}")
public record {Name}Request(
        @Schema(
                        description = "{Field description}",
                        example = "{example value}",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "{Field name} cannot be blank")
                @Size(max = 255, message = "{Field name} must not exceed 255 characters")
                String fieldName,
        
        @Schema(
                        description = "{Field description}",
                        example = "{example value}",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String optionalField) {}
```

## Template: Response DTO

```java
package com.ticket_online.domain.{domain}.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticket_online.domain.{domain}.domain.{Entity};
import java.time.LocalDateTime;

@Schema(description = "Response containing {entity} information")
public record {Name}Response(
        @Schema(description = "{Field description}", example = "{example}")
                Long id,
        @Schema(description = "{Field description}", example = "{example}")
                String name,
        @Schema(description = "{Field description}", example = "{example}")
                @JsonProperty("createdAt")
                LocalDateTime createdAt) {

    public static {Name}Response from({Entity} entity) {
        return new {Name}Response(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt());
    }
}
```

## Notes

- Use descriptive field names in camelCase
- Add meaningful validation messages
- Include realistic examples in `@Schema`
- Keep descriptions clear and concise
- For optional fields, use `requiredMode = Schema.RequiredMode.NOT_REQUIRED`
- Response DTOs should mirror entity fields needed by the client
- Static factory methods simplify entity-to-DTO conversion
- Follow the record pattern (immutable, no setters needed)
