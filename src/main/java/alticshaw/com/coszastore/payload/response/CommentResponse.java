package alticshaw.com.coszastore.payload.response;

import alticshaw.com.coszastore.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String content;
    private String email;
    private String website;
    private String name;
    private Timestamp created_at;
    private Timestamp updated_at;
    private int blog_id;

    public CommentResponse mapCommentEntityToCommentResponse(CommentEntity commentEntity) {
        return new CommentResponse(commentEntity.getContent(),
                commentEntity.getEmail(),
                commentEntity.getWebsite(),
                commentEntity.getName(),
                commentEntity.getCreatedTime(),
                commentEntity.getUpdatedTime(),
                commentEntity.getBlog().getId()
        );
    }
}
