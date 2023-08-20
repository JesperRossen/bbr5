package uk.co.bbr.services.pieces.dto;

import lombok.Getter;
import lombok.Setter;
import uk.co.bbr.services.pieces.dao.PieceDao;

@Getter
public class BestOwnChoiceDto {
    private final PieceDao piece;
    @Setter private int topThree;
    @Setter private int points;


    public BestOwnChoiceDto(PieceDao piece, int topThree, int points) {
        this.piece = piece;
        this.topThree = topThree;
        this.points = points;
    }
}
