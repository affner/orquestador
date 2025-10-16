package mx.com.actinver.orquestador.rest;

import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/")
@Api(tags = {"Orquestador"})
public class OrquestadorRest {

    private static final Logger LOG = LogManager.getLogger(OrquestadorRest.class);

    @Autowired
    private OrquestadorRest orquestadorRest;




}
