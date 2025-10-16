package mx.com.actinver.orquestador.service.impl;

import mx.com.actinver.orquestador.dto.InputDataDto;
import mx.com.actinver.orquestador.service.OrquestadorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class OrquestadorServiceImpl implements OrquestadorService {

    private static final Logger LOG = LogManager.getLogger(OrquestadorServiceImpl.class);


    @Override
    public InputDataDto genericService(InputDataDto request) {
        return null;
    }
}
