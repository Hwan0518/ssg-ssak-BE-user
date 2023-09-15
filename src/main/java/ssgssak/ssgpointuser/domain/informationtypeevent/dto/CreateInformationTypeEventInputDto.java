package ssgssak.ssgpointuser.domain.informationtypeevent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInformationTypeEventInputDto {
    private Long eventListId;
    private String title;
    private String contentsImageUrl;
}
