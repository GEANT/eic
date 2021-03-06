package eu.einfracentral.domain;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class ServiceHistory extends Metadata {

    private String version;

    private boolean versionChange = false;

    private String coreVersionId = null;

    public ServiceHistory() {
    }

    public ServiceHistory(Metadata metadata, String version, boolean versionChange) {
        super(metadata);
        this.version = version;
        this.versionChange = versionChange;
    }

    public ServiceHistory(Metadata metadata, String version, String coreVersionId, boolean versionChange) {
        super(metadata);
        this.version = version;
        this.coreVersionId = coreVersionId;
        this.versionChange = versionChange;
    }

    public ServiceHistory(InfraService service, boolean versionChange) {
        super(service.getMetadata());
        this.version = service.getService().getVersion();
        this.versionChange = versionChange;
    }

    public ServiceHistory(InfraService service, String coreVersionId, boolean versionChange) {
        super(service.getMetadata());
        this.version = service.getService().getVersion();
        this.coreVersionId = coreVersionId;
        this.versionChange = versionChange;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public boolean isVersionChange() {
        return versionChange;
    }

    public void setVersionChange(boolean versionChange) {
        this.versionChange = versionChange;
    }

    public void setCoreVersionId(String coreVersionId) {
        this.coreVersionId = coreVersionId;
    }

    public String getCoreVersionId() {
        return coreVersionId;
    }

}
