package eu.einfracentral.domain;

import eu.einfracentral.annotation.FieldValidation;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(namespace = "http://einfracentral.eu")
public abstract class Bundle<T extends Identifiable> implements Identifiable {

    @ApiModelProperty(hidden = true)
    @XmlTransient
    @FieldValidation
    private T payload;

    @XmlElement(name = "metadata")
    @FieldValidation
    private Metadata metadata;

    @XmlElement
    private boolean active;

    @XmlElement
    private String status;

    public Bundle() {
    }

    @Override
    public String getId() {
        return payload.getId();
    }

    @Override
    public void setId(String id) {
        this.payload.setId(id);
    }

    T getPayload() {
        return payload;
    }

    void setPayload(T payload) {
        this.payload = payload;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Bundle{" +
                "payload=" + payload +
                ", metadata=" + metadata +
                ", active=" + active +
                ", status='" + status + '\'' +
                '}';
    }
}
