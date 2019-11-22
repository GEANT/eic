package eu.einfracentral.registry.manager;

import eu.einfracentral.domain.InfraService;
import eu.einfracentral.registry.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("pendingServiceManager")
public class PendingServiceManager extends ResourceManager<InfraService> implements ResourceService<InfraService, Authentication> {

    @Autowired
    public PendingServiceManager() {
        super(InfraService.class);
    }

    @Override
    public String getResourceType() {
        return "pending_service";
    }
}
