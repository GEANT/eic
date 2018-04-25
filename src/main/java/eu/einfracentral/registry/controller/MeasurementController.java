package eu.einfracentral.registry.controller;
/**
 * Created by pgl on 24/04/18.
 */
import eu.einfracentral.domain.*;
import eu.einfracentral.registry.service.MeasurementService;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("measurement")
public class MeasurementController extends ResourceController<Measurement> {
    @Autowired
    MeasurementController(MeasurementService service) {
        super(service);
    }

    @ApiOperation(value = "Returns the measurement assigned the given id.")
    @RequestMapping(path = "{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Measurement> get(@PathVariable("id") String id, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return super.get(id, jwt);
    }

    @CrossOrigin
    @ApiOperation(value = "Adds the given measurement.")
    @RequestMapping(method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Measurement> add(@RequestBody Measurement measurement, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return super.add(measurement, jwt);
    }

    @ApiOperation(value = "Updates the measurement assigned the given id with the given measurement, keeping a versions of revisions.")
    @RequestMapping(method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Measurement> update(@RequestBody Measurement measurement, @ApiIgnore @CookieValue(defaultValue = "") String jwt) throws ResourceNotFoundException {
        return super.update(measurement, jwt);
    }
}
