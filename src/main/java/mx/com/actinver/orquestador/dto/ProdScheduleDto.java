package mx.com.actinver.orquestador.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ProdScheduleDto {

    private final long idProd;
    private final Instant runAt;

}
