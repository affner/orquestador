package mx.com.actinver.orquestador.service;

import mx.com.actinver.orquestador.dto.DescargaCfdiRequestDto;
import mx.com.actinver.orquestador.dto.DescargaCfdiResponseDto;

public interface DescargaCfdiService {
    DescargaCfdiResponseDto getDescargaCfdi(DescargaCfdiRequestDto descargaCfdiRequestDto,Long executor);
}
