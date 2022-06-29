package io.github.severianofsp.quarkussocial.rest.dto;

import io.github.severianofsp.quarkussocial.domain.model.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class PostResponse {
    private String text;
    private LocalDateTime dateTime;

    public static PostResponse fromEntity(Post post) {
        return new PostResponseBuilder()
                .dateTime(post.getDateTime())
                .text(post.getText())
                .build();
    }
}
