package eu.einfracentral.registry.controller;

import eu.einfracentral.domain.Identifiable;
import eu.einfracentral.exception.ResourceException;
import eu.einfracentral.registry.service.ResourceService;
import eu.openminted.registry.core.domain.*;
import eu.openminted.registry.core.exception.*;
import io.swagger.annotations.ApiOperation;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by pgl on 25/07/17.
 */
//the below line contains the only produces needed for spring to work in the entire project; all others are there until springfox fix their bugs
@RequestMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class ResourceController<T extends Identifiable> {
    protected final ResourceService<T> service;

    ResourceController(ResourceService<T> service) {
        this.service = service;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<T> get(@PathVariable("id") String id, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return new ResponseEntity<>(service.get(id), HttpStatus.OK);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<T> add(@RequestBody T t, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return new ResponseEntity<>(service.add(t), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<T> update(@RequestBody T t, @ApiIgnore @CookieValue(defaultValue = "") String jwt) throws ResourceNotFoundException {
        return new ResponseEntity<>(service.update(t), HttpStatus.OK);
    }

    @RequestMapping(value = "validate", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<T> validate(@RequestBody T t, @ApiIgnore @CookieValue(defaultValue = "") String jwt) throws ResourceNotFoundException {
        return new ResponseEntity<>(service.validate(t), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<T> delete(@RequestBody T t, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return new ResponseEntity<>(service.del(t), HttpStatus.OK);
    }

    @RequestMapping(path = "all", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<T>> delAll(@ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return new ResponseEntity<>(service.delAll(), HttpStatus.OK);
    }

    @RequestMapping(path = "all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Browsing<T>> getAll(@ApiIgnore @RequestParam Map<String, Object> allRequestParams, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        FacetFilter filter = new FacetFilter();
        filter.setKeyword(allRequestParams.get("query") != null ? (String) allRequestParams.remove("query") : "");
        filter.setFrom(allRequestParams.get("from") != null ? Integer.parseInt((String) allRequestParams.remove("from")) : 0);
        filter.setQuantity(allRequestParams.get("quantity") != null ? Integer.parseInt((String) allRequestParams.remove("quantity")) : 10);
        filter.setFilter(allRequestParams);
        return new ResponseEntity<>(service.getAll(filter), HttpStatus.OK);
    }

    @RequestMapping(path = "byID/{ids}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<T>> getSome(@PathVariable String[] ids, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return new ResponseEntity<>(service.getSome(ids), HttpStatus.OK);
    }

    @RequestMapping(path = "by/{field}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Map<String, List<T>>> getBy(@PathVariable String field, @ApiIgnore @CookieValue(defaultValue = "") String jwt) {
        return new ResponseEntity<>(service.getBy(field), HttpStatus.OK);
    }

    @ExceptionHandler(ResourceException.class)
    @ResponseBody
    public ResponseEntity<ServerError> handleResourceException(HttpServletRequest req, ResourceException e) {
        return new ResponseEntity<>(new ServerError(req.getRequestURL().toString(), e), e.getStatus());
    }

    @ApiOperation(value = "Retrieves service history.")
    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<T>> history(@RequestBody T t, @ApiIgnore @CookieValue(defaultValue = "") String jwt) throws ResourceNotFoundException {
        return new ResponseEntity<>(service.history(t.getId()), HttpStatus.OK);
    }
}
