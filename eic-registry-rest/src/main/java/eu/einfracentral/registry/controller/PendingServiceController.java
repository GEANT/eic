package eu.einfracentral.registry.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.einfracentral.domain.InfraService;
import eu.einfracentral.domain.Measurement;
import eu.einfracentral.domain.RichService;
import eu.einfracentral.domain.Service;
import eu.einfracentral.exception.ResourceException;
import eu.einfracentral.registry.service.InfraServiceService;
import eu.einfracentral.registry.service.MeasurementService;
import eu.einfracentral.registry.service.PendingResourceService;
import eu.einfracentral.service.IdCreator;
import eu.einfracentral.utils.FacetFilterUtils;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import eu.openminted.registry.core.service.ServiceException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("pendingService")
public class PendingServiceController extends ResourceController<InfraService, Authentication> {

    private static Logger logger = LogManager.getLogger(PendingServiceController.class);
    private final PendingResourceService<InfraService> pendingServiceManager;
    private final MeasurementService<Measurement, Authentication> measurementService;
    private final InfraServiceService<InfraService, InfraService> infraServiceService;
    private final IdCreator idCreator;

    @Autowired
    PendingServiceController(PendingResourceService<InfraService> pendingServiceManager,
                             MeasurementService<Measurement, Authentication> measurementService,
                             InfraServiceService<InfraService, InfraService> infraServiceService,
                             IdCreator idCreator) {
        super(pendingServiceManager);
        this.pendingServiceManager = pendingServiceManager;
        this.measurementService = measurementService;
        this.infraServiceService = infraServiceService;
        this.idCreator = idCreator;
    }


    @DeleteMapping(path = "{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.userIsServiceProviderAdmin(#auth, #id)")
    public ResponseEntity<InfraService> delete(@PathVariable("id") String id, @ApiIgnore Authentication auth) throws ResourceNotFoundException {
        InfraService service = pendingServiceManager.get(id);
        pendingServiceManager.delete(service);
        logger.info("User '{}' deleted PendingService '{}' with id: '{}'", auth.getName(), service.getService().getName(), service.getService().getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping(path = "/service/id", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Service> getService(@PathVariable String id) {
        return new ResponseEntity<>(pendingServiceManager.get(id).getService(), HttpStatus.OK);
    }


    @GetMapping(path = "/rich/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RichService> getPendingRich(@PathVariable("id") String id, Authentication auth) {
        return new ResponseEntity<>((RichService) pendingServiceManager.getPendingRich(id, auth), HttpStatus.OK);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "Keyword to refine the search", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "from", value = "Starting index in the result set", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "quantity", value = "Quantity to be fetched", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "order", value = "asc / desc", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "orderField", value = "Order field", dataType = "string", paramType = "query")
    })
    @GetMapping(path = "/byProvider/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.userIsProviderAdmin(#auth,#id)")
    public ResponseEntity<Paging<InfraService>> getProviderPendingServices(@ApiIgnore @RequestParam MultiValueMap<String, Object> allRequestParams, @PathVariable String id, @ApiIgnore Authentication auth) {
        FacetFilter ff = FacetFilterUtils.createMultiFacetFilter(allRequestParams);
        ff.addFilter("providers", id);
        return new ResponseEntity<>(pendingServiceManager.getAll(ff, null), HttpStatus.OK);
    }


    @PostMapping(path = "/addService", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Service> addService(@RequestBody Service service, @ApiIgnore Authentication auth) {
        InfraService infraService = new InfraService(service);
        return new ResponseEntity<>(pendingServiceManager.add(infraService, auth).getService(), HttpStatus.CREATED);
    }


    @PostMapping(path = "/updateService", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.userIsServiceProviderAdmin(#auth, #service)")
    public ResponseEntity<Service> updateService(@RequestBody Service service, @ApiIgnore Authentication auth) {
        InfraService infraService = pendingServiceManager.get(service.getId());
        infraService.setService(service);
        return new ResponseEntity<>(pendingServiceManager.update(infraService, auth).getService(), HttpStatus.OK);
    }


    @PostMapping("/transform/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.userIsServiceProviderAdmin(#auth, #serviceId)")
    public void transformServiceToPending(@RequestParam String serviceId, @ApiIgnore Authentication auth) {
        pendingServiceManager.transformToPending(serviceId, auth);
    }


    @PostMapping("/transform/service")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.providerCanAddServices(#auth, #serviceId)")
    public void transformServiceToInfra(@RequestParam String serviceId, @ApiIgnore Authentication auth) {
        pendingServiceManager.transformToActive(serviceId, auth);
    }


    @PutMapping(path = "/pending", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.userIsServiceProviderAdmin(#auth, #service)")
    public ResponseEntity<Service> temporarySavePending(@RequestBody Service service, @ApiIgnore Authentication auth) {
        InfraService infraService = new InfraService();
        try {
            infraService = pendingServiceManager.get(service.getId());
            infraService.setService(service);
            infraService = pendingServiceManager.update(infraService, auth);
        } catch (ResourceException e) {
            logger.debug("Pending Service with id '{}' does not exist. Creating it...", service.getId());
            infraService.setService(service);
            infraService = pendingServiceManager.add(infraService, auth);
        }
        return new ResponseEntity<>(infraService.getService(), HttpStatus.OK);
    }


    @PutMapping(path = "/service", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.userIsServiceProviderAdmin(#auth, #service)")
    public ResponseEntity<Service> temporarySaveService(@RequestBody Service service, @ApiIgnore Authentication auth) {
        pendingServiceManager.transformToPending(service.getId(), auth);
        InfraService infraService = pendingServiceManager.get(service.getId());
        infraService.setService(service);
        return new ResponseEntity<>(pendingServiceManager.update(infraService, auth).getService(), HttpStatus.OK);
    }


    @PutMapping(path = "/transform/service", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.providerCanAddServices(#auth, #json)")
    public ResponseEntity<Service> pendingToInfra(@RequestBody Map<String, JsonNode> json, @ApiIgnore Authentication auth) {
        Pair<Service, List<Measurement>> serviceAndMeasurementsPair = getServiceAndMeasurements(json);
        Service service = serviceAndMeasurementsPair.getValue0();
        List<Measurement> measurements = serviceAndMeasurementsPair.getValue1();

        if (service == null) {
            throw new ServiceException("Cannot add a null service");
        }
        InfraService infraService = null;

        try { // check if service already exists
            if (service.getId() == null || "".equals(service.getId())) { // if service id is not given, create it
                service.setId(idCreator.createServiceId(service));
            }
            infraService = this.pendingServiceManager.get(service.getId());
        } catch (ResourceException | eu.einfracentral.exception.ResourceNotFoundException e) {
            // continue with the creation of the service
        }

        if (infraService == null) { // if existing Pending Service is null, create a new Active Service
            infraService = infraServiceService.addService(new InfraService(service), auth);
            logger.info("User '{}' added Service:\n{}", auth.getName(), infraService);
        } else { // else update Pending Service and transform it to Active Service
            infraService.setService(service); // important to keep other fields of InfraService
            infraService = pendingServiceManager.update(infraService, auth);
            logger.info("User '{}' updated Pending Service:\n{}", auth.getName(), infraService);

            // transform to active
            infraService = pendingServiceManager.transformToActive(infraService.getId(), auth);
        }

        measurementService.updateAll(infraService.getId(), measurements, auth);

        return new ResponseEntity<>(infraService.getService(), HttpStatus.OK);
    }


    @PutMapping(path = "serviceWithMeasurements", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.providerCanAddServices(#auth, #json)")
    public ResponseEntity<Service> serviceWithKPIs(@RequestBody Map<String, JsonNode> json, @ApiIgnore Authentication auth) {
        Pair<Service, List<Measurement>> serviceAndMeasurementsPair = getServiceAndMeasurements(json);
        Service service = serviceAndMeasurementsPair.getValue0();
        List<Measurement> measurements = serviceAndMeasurementsPair.getValue1();

        if (service == null) {
            throw new ServiceException("Cannot add a null service");
        }
        InfraService infraService = null;
        try { // check if service already exists
            if (service.getId() == null || "".equals(service.getId())) { // if service id is not given, create it
                service.setId(idCreator.createServiceId(service));
            }
            infraService = this.pendingServiceManager.get(service.getId());
        } catch (ResourceException | eu.einfracentral.exception.ResourceNotFoundException e) {
            // continue with the creation of the service
        }

        if (infraService == null) { // if existing service is null, create it, else update it
            infraService = pendingServiceManager.add(new InfraService(service), auth);
            logger.info("User '{}' added Service:\n{}", auth.getName(), infraService);
        } else {
            infraService.setService(service); // important to keep other fields of InfraService
            infraService = pendingServiceManager.update(infraService, auth);
            logger.info("User '{}' updated Service:\n{}", auth.getName(), infraService);
        }
        measurementService.updateAll(infraService.getId(), measurements, auth);

        return new ResponseEntity<>(infraService.getService(), HttpStatus.OK);
    }


    private Pair<Service, List<Measurement>> getServiceAndMeasurements(Map<String, JsonNode> json) {
        ObjectMapper mapper = new ObjectMapper();
        Service service = null;
        List<Measurement> measurements = new ArrayList<>();
        try {
            service = mapper.readValue(json.get("service").toString(), Service.class);
            measurements = Arrays.stream(mapper.readValue(json.get("measurements").toString(), Measurement[].class)).collect(Collectors.toList());

        } catch (JsonParseException e) {
            logger.error("JsonParseException", e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return new Pair<>(service, measurements);
    }
}
