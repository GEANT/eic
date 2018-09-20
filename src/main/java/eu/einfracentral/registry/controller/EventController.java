package eu.einfracentral.registry.controller;

import eu.einfracentral.domain.Event;
import eu.einfracentral.registry.service.EventService;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("event")
public class EventController extends ResourceController<Event, Authentication> {

    private EventService service;

    @Autowired
    EventController(EventService service) {
        super(service);
        this.service = service;
    }

    private final Logger logger = Logger.getLogger(EventController.class);

    @ApiIgnore
    @ApiOperation("Retrieve all events.")
    @RequestMapping(path = "events/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Paging<Event>> getAll(Authentication authentication) {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        return new ResponseEntity<>(service.getAll(ff, authentication), HttpStatus.OK);
    }

    @ApiIgnore
    @ApiOperation("Retrieve the event with a specific ID.")
    @RequestMapping(path = "event/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Event> get(@PathVariable String id, Authentication authentication) {
        return new ResponseEntity<>(service.get(id), HttpStatus.OK);
    }


    // FAVORITES -------->
    @ApiOperation("Set a service as favorite for a user.")
    @RequestMapping(path = "favourite/service/{id}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Event> setFavourite(@PathVariable String id, @RequestParam Integer value, Authentication authentication) throws Exception {
        // TODO: check if user and service exists ?
        return new ResponseEntity<>(service.setFavourite(id, value, authentication), HttpStatus.OK);
    }

    @ApiOperation("Check if a service is favourited by the authenticated user.")
    @RequestMapping(path = "favourite/service/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<String> getFavourite(@PathVariable String id, Authentication authentication) {
        List<Event> events;
        try {
            events = service.getEvents(Event.UserActionType.FAVOURITE.getKey(), id, authentication);
            if (events.size() > 0) {
                return new ResponseEntity<>(events.get(0).getValue(), HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.info(e + "\nReturning favourite = 0");
        }
        return new ResponseEntity<>("0", HttpStatus.OK);
    }

    @ApiIgnore
    @ApiOperation("Retrieve all the favourited events.")
    @RequestMapping(path = "favourites/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<Event>> getFavourites() {
        return new ResponseEntity<>(service.getEvents(Event.UserActionType.FAVOURITE.getKey()), HttpStatus.OK);
    }

    @ApiOperation("Retrieve all the favourited events of the authenticated user.")
    @RequestMapping(path = "favourites", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<Event>> getUserFavourites(Authentication authentication) {
        try {
            return new ResponseEntity<>(service.getUserEvents(Event.UserActionType.FAVOURITE.getKey(), authentication), HttpStatus.OK);
        } catch (Exception e) {
            logger.info(e + "\nReturning favourites=0");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Retrieve all the favourited events of a infraService with the specified ID.")
    @RequestMapping(path = "favourites/service/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<Event>> getServiceFavourites(@PathVariable String id) {
        return new ResponseEntity<>(service.getServiceEvents(Event.UserActionType.FAVOURITE.getKey(), id), HttpStatus.OK);
    }
    // <-------- FAVORITES


    // RATINGS ---------->
    @ApiOperation("Set a rating to a service from the authenticated user.")
    @RequestMapping(path = "rating/service/{id}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Event> setUserRating(@PathVariable String id, @RequestParam("rating") String rating, Authentication authentication) throws Exception {
        // TODO: check if user and service exists ?
        return new ResponseEntity<>(service.setRating(id, rating, authentication), HttpStatus.OK);
    }

    @ApiOperation("Get the rating of the authenticated user.")
    @RequestMapping(path = "rating/service/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<String> getRating(@PathVariable String id, Authentication authentication) {
        List<Event> events;
        try {
            events = service.getEvents(Event.UserActionType.RATING.getKey(), id, authentication);
            if (events != null && events.size() > 0) {
                return new ResponseEntity<>(events.get(0).getValue(), HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.info(e + "\nReturning rate = null");
        }
        return new ResponseEntity<>("null", HttpStatus.OK);
    }

    @ApiIgnore
    @ApiOperation("Retrieve all rating events.")
    @RequestMapping(path = "ratings/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<Event>> getRatings() {
        return new ResponseEntity<>(service.getEvents(Event.UserActionType.RATING.getKey()), HttpStatus.OK);
    }

    @ApiOperation("Retrieve all the rating events of the authenticated user.")
    @RequestMapping(path = "ratings", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<Event>> getUserRating(Authentication authentication) {
        try {
            return new ResponseEntity<>(service.getUserEvents(Event.UserActionType.RATING.getKey(), authentication), HttpStatus.OK);
        } catch (Exception e) {
            logger.info(e + "\nReturning ratings = null");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Retrieve all the rating events of a infraService with the specified ID.")
    @RequestMapping(path = "ratings/service/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<Event>> getServiceRatings(@PathVariable String id) {
        return new ResponseEntity<>(service.getServiceEvents(Event.UserActionType.RATING.getKey(), id), HttpStatus.OK);
    }
    // <---------- RATINGS

}