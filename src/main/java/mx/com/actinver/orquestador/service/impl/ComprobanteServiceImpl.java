package mx.com.actinver.orquestador.service.impl;

import mx.com.actinver.orquestador.dao.ComprobanteDao;
import mx.com.actinver.orquestador.service.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para la gestión de comprobantes.
 * Proporciona la lógica de negocio para consultar comprobantes basándose en filtros y paginación.
 */
@Service
public class ComprobanteServiceImpl implements ComprobanteService {

    /**
     * DAO para interactuar con la base de datos de comprobantes.
     */
    @Autowired
    private ComprobanteDao comprobanteDao;


}