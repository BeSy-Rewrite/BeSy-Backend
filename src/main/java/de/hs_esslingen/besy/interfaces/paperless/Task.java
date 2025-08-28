package de.hs_esslingen.besy.interfaces.paperless;

import lombok.*;

import java.time.ZonedDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Task {

    private Long id;
    private String task_id;
    private String task_name;
    private String task_file_name;
    private ZonedDateTime date_created;
    private ZonedDateTime date_done;
    private String type;
    private String status;
    private String result;
    private Boolean acknowledged;
    private String related_document;
    private Long owner;
}
