package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * User.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Positive(message = "User ID must be positive number")
    private Integer id;

    private String name;

    @NotNull
    @NotBlank
    private String login;

    @Email(message = "Not valid email format")
    private String email;

    @PastOrPresent
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /**
     * Terms of reference: The name for display can be empty - in this case, the login will be used
     */
    public String getName() {
        return StringUtils.hasText(name) ? this.name: this.login;
    }
}
