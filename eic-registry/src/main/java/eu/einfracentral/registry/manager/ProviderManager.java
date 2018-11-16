package eu.einfracentral.registry.manager;

import eu.einfracentral.config.security.EICAuthoritiesMapper;
import eu.einfracentral.domain.InfraService;
import eu.einfracentral.domain.Provider;
import eu.einfracentral.domain.Service;
import eu.einfracentral.domain.User;
import eu.einfracentral.registry.service.InfraServiceService;
import eu.einfracentral.registry.service.ProviderService;
import eu.einfracentral.service.MailService;
import eu.einfracentral.service.SecurityService;
import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service("providerManager")
public class ProviderManager extends ResourceManager<Provider> implements ProviderService<Provider, Authentication> {

    private static final Logger logger = LogManager.getLogger(ProviderManager.class);
    private InfraServiceService<InfraService, InfraService> infraServiceService;
    private MailService mailService;
    private EICAuthoritiesMapper authoritiesMapper;
    private Configuration cfg;
    private SecurityService securityService;

    @Value("${webapp.front:beta.einfracentral.eu}")
    private String endpoint;

    @Autowired
    public ProviderManager(InfraServiceService<InfraService, InfraService> infraServiceService, MailService mailService,
                           @Lazy EICAuthoritiesMapper authoritiesMapper, Configuration cfg,
                           @Lazy SecurityService securityService) {
        super(Provider.class);
        this.infraServiceService = infraServiceService;
        this.mailService = mailService;
        this.authoritiesMapper = authoritiesMapper;
        this.cfg = cfg;
        this.securityService = securityService;
    }


    @Override
    public String getResourceType() {
        return "provider";
    }

    @Override
    public Provider add(Provider provider, Authentication auth) {
        List<User> users;
        User authUser = new User(auth);
        Provider ret;
        if (provider.getId() == null) {
            provider.setId(provider.getName());
        }
        provider.setId(StringUtils
                .stripAccents(provider.getId())
                .replaceAll("[^a-zA-Z0-9\\s\\-\\_]+", "")
                .replaceAll(" ", "_"));

        users = provider.getUsers();
        if (users == null) {
            users = new ArrayList<>();
        }
        if (users.stream().noneMatch(u -> u.getEmail().equals(authUser.getEmail()))) {
            users.add(authUser);
            provider.setUsers(users);
        }
        provider.setStatus(Provider.States.INIT.getKey());
        sendProviderMails(provider, new User(auth), Provider.States.INIT);

        provider.setActive(false);
        provider.setStatus(Provider.States.PENDING_1.getKey());

//        ret = super.add(provider, null);
        authoritiesMapper.mapProviders(provider.getUsers());

        // TODO: fix function
//        createProviderMail(provider, new User(auth), Provider.States.INIT);
        return null;
//        return ret;
    }

    @Override
    public Provider update(Provider provider, Authentication auth) {
        Resource existing = whereID(provider.getId(), true);
        Provider ex = deserialize(existing);
        provider.setActive(ex.getActive());
        provider.setStatus(ex.getStatus());
        existing.setPayload(serialize(provider));
        existing.setResourceType(resourceType);
        resourceService.updateResource(existing);
        if (provider.getUsers() != null && !provider.getUsers().isEmpty()) {
            authoritiesMapper.mapProviders(provider.getUsers());
        }
        return provider;
    }

    @Override
    public Provider get(String id, Authentication auth) {
        Provider provider = get(id);
        if (auth == null) {
            provider.setUsers(null);
        } else if (securityService.hasRole(auth, "ROLE_ADMIN")) {
            return provider;
        } else if (securityService.hasRole(auth, "ROLE_PROVIDER") && securityService.userIsProviderAdmin(auth, provider)) {
            return provider;
        }
        return provider;
    }

    @Override
    public Browsing<Provider> getAll(FacetFilter ff, Authentication auth) {
        List<Provider> userProviders = null;
        if (auth != null && auth.isAuthenticated()) {
            if (securityService.hasRole(auth, "ROLE_ADMIN")) {
                return super.getAll(ff, auth);
            }
            // if user is not an admin, check if he is a provider
            User authUser = new User(auth);
            userProviders = getMyServiceProviders(auth);
        }
        Browsing<Provider> providers = super.getAll(ff, auth);
        List<Provider> modified = providers.getResults()
                .stream()
                .map(p -> {
                    p.setUsers(null);
                    return p;
                })
                .collect(Collectors.toList());

        if (userProviders != null) {
            userProviders.forEach(x -> {
                modified.removeIf(provider -> provider.getId().equals(x.getId()));
                modified.add(x);
            });
        }
        providers.setResults(modified);
        return providers;
    }

    @Override
    public Provider verifyProvider(String id, Provider.States status, Boolean active, Authentication auth) {
        Provider provider = get(id);
        User user = new User(auth);

        switch (status) {
            case REJECTED:
                logger.info("Deleting provider: " + provider.getName());
                List<InfraService> services = this.getInfraServices(provider.getId());
                services.forEach(s -> {
                    try {
                        infraServiceService.delete(s);
                    } catch (ResourceNotFoundException e) {
                        logger.error("Error deleting Service", e);
                    }
                });
                this.delete(provider);
                return null;
            case APPROVED:
                provider.setActive(true);
                break;
            case PENDING_1:

                provider.setActive(false);
                break;
            case PENDING_2:
                provider.setActive(false);
                break;
            case REJECTED_ST:
                provider.setActive(false);
                break;
            default:
        }
        sendProviderMails(provider, user, status);

        if (active != null) {
            provider.setActive(active);
            if (!active) {
                deactivateServices(provider.getId());
            } else {
                activateServices(provider.getId());
            }
        }
        provider.setStatus(status.getKey());
        return super.update(provider, auth);
    }

    // TODO: CHECK THIS!!!
    @Override
    public List<Provider> getServiceProviders(String email, Authentication auth) {
        List<Provider> providers;
        if (auth == null) {
//            return null; // TODO: enable this when front end can handle 401 properly
            return new ArrayList<>();
        } else if (securityService.hasRole(auth, "ROLE_ADMIN")) {
            FacetFilter ff = new FacetFilter();
            ff.setQuantity(10000);
            providers = super.getAll(ff, null).getResults();
        } else if (securityService.hasRole(auth, "ROLE_PROVIDER")) {
            providers = getMyServiceProviders(auth);
        } else {
            return new ArrayList<>();
        }
        return providers
                .stream()
                .map(p -> {
                    if (p.getUsers() != null && p.getUsers().stream().filter(Objects::nonNull).anyMatch(u -> u.getEmail().equals(email))) {
                        return p;
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Provider> getMyServiceProviders(Authentication auth) {
        if (auth == null) {
//            return null; // TODO: enable this when front end can handle 401 properly
            return new ArrayList<>();
        }
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        return super.getAll(ff, null).getResults()
                .stream().map(p -> {
                    if (p.getUsers() != null && p.getUsers().stream().filter(Objects::nonNull).anyMatch(u -> u.getEmail().equals(new User(auth).getEmail()))) {
                        return p;
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<InfraService> getInfraServices(String providerId) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("providers", providerId);
        ff.setQuantity(10000);
        return infraServiceService.getAll(ff, null).getResults();
    }

    @Override
    public List<Service> getServices(String providerId) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("providers", providerId);
        ff.addFilter("latest", "true");
        ff.setQuantity(10000);
        return infraServiceService.getAll(ff, null).getResults().stream().map(Service::new).collect(Collectors.toList());
    }

    @Override
    public List<Service> getActiveServices(String providerId) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("providers", providerId);
        ff.addFilter("active", "true");
        ff.addFilter("latest", "true"); // TODO: check if it is needed
        ff.setQuantity(10000);
        return infraServiceService.getAll(ff, null).getResults().stream().map(Service::new).collect(Collectors.toList());
    }

    @Override
    public Service getFeaturedService(String providerId) {
        // TODO: change this method
        List<Service> services = getServices(providerId);
        Service featuredService = null;
        if (!services.isEmpty()) {
            Random random = new Random();
            featuredService = services.get(random.nextInt(services.size()));
        }
        return featuredService;
    }

    @Override
    public List<Provider> getInactive() {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("active", false);
        ff.setFrom(0);
        ff.setQuantity(10000);
        return getAll(ff, null).getResults();
    }

    @Override
    public List<InfraService> getInactiveServices(String providerId) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("providers", providerId);
        ff.addFilter("active", false);
        ff.setFrom(0);
        ff.setQuantity(10000);
        return infraServiceService.getAll(ff, null).getResults();
    }

    public void activateServices(String providerId) { // TODO: decide how to use service.status variable
        List<InfraService> services = this.getInfraServices(providerId);
        for (InfraService service : services) {
            service.setActive(service.getStatus() == null || service.getStatus().equals("true"));
            service.setStatus(null);
            try {
                infraServiceService.update(service, null);
            } catch (ResourceNotFoundException e) {
                logger.error("Could not update service " + service.getName());
            }
        }
    }

    public void deactivateServices(String providerId) { // TODO: decide how to use service.status variable
        List<InfraService> services = this.getInfraServices(providerId);
        for (InfraService service : services) {
            service.setStatus(service.getActive() != null ? service.getActive().toString() : "true");
            service.setActive(false);
            try {
                infraServiceService.update(service, null);
            } catch (ResourceNotFoundException e) {
                logger.error("Could not update service " + service.getName());
            }
        }
    }

    // TODO: complete this method
    private void sendProviderMails(Provider provider, User user, Provider.States state) {
        Map<String, Object> root = new HashMap<>();
        StringWriter out = new StringWriter();
        String providerMail;
        String regTeamMail;
        root.put("user", user);
        root.put("provider", provider);
        root.put("endpoint", endpoint);

        String providerSubject = null;
        String regTeamSubject = null;

        List<Service> serviceList = getServices(provider.getId());
        Service serviceTemplate = null;
        if (!serviceList.isEmpty()) {
            root.put("service", serviceList.get(0));
            serviceTemplate = serviceList.get(0);
        } else {
            serviceTemplate = new Service();
            serviceTemplate.setName("");
        }
//        switch (Provider.States.valueOf(provider.getStatus())) {
        switch (state) {
            case INIT:
                providerSubject = String.format("[eInfraCentral] Your application for registering [%s] as a new service provider has been received", provider.getName());
                regTeamSubject = String.format("[eInfraCentral] A new application for registering [%s] as a new service provider has been submitted", provider.getName());
                break;
            case PENDING_1:
                providerSubject = String.format("[eInfraCentral] Your application for registering [%s] as a new service provider has been accepted", provider.getName());
                regTeamSubject = String.format("[eInfraCentral] The application of [%s] for registering as a new service provider has been accepted", provider.getName());
                break;
            case PENDING_2:
                assert serviceTemplate != null;
                providerSubject = String.format("[eInfraCentral] Your service [%s] has been received and its approval is pending ", serviceTemplate.getName());
                regTeamSubject = String.format("[eInfraCentral] Approve or reject the information about the new service: [%s] – [%s] ", provider.getName(), serviceTemplate.getName());
                break;
            case APPROVED:
                assert serviceTemplate != null;
                providerSubject = String.format("[eInfraCentral] Your service [%s] – [%s]  has been accepted", provider.getName(), serviceTemplate.getName());
                regTeamSubject = String.format("[eInfraCentral] The service [%s] has been accepted", serviceTemplate.getId());
                break;
            case REJECTED_ST:
                assert serviceTemplate != null;
                providerSubject = String.format("[eInfraCentral] Your service [%s] – [%s]  has been rejected", provider.getName(), serviceTemplate.getName());
                regTeamSubject = String.format("[eInfraCentral] The service [%s] has been rejected", serviceTemplate.getId());
                break;
            case REJECTED:
                providerSubject = String.format("[eInfraCentral] Your application for registering [%s] as a new service provider has been rejected", provider.getName());
                regTeamSubject = String.format("[eInfraCentral] The application of [%s] for registering as a new service provider has been rejected", provider.getName());
                break;
        }

        try {
            Template temp = cfg.getTemplate("providerMailTemplate.ftl");
            temp.process(root, out);
            providerMail = out.getBuffer().toString();
            out.flush();

            // TODO: fix mail service and enable this
            mailService.sendMail(user.getEmail(), providerSubject, providerMail);
            logger.info(String.format("Recipient: %s%nTitle: %s%nMail body: %n%s", user.getEmail(), providerSubject, providerMail));
            temp = cfg.getTemplate("registrationTeamMailTemplate.ftl");
//            out = new StringWriter();
            out.getBuffer().setLength(0);
            temp.process(root, out);
            regTeamMail = out.getBuffer().toString();
            out.flush();

            // TODO: fix mail service and enable this
            mailService.sendMail("registration@einfracentral.eu", regTeamSubject, regTeamMail);
            logger.info(String.format("Recipient: %s%nTitle: %s%nMail body: %n%s", "registration@einfracentral.eu", regTeamSubject, regTeamMail));
            out.close();
        } catch (IOException e) {
            logger.error("Error finding mail template", e);
        } catch (TemplateException e) {
            logger.error("ERROR", e);
        } catch (MessagingException e) {
            logger.error("Could not send mail", e);
        }
    }
}
