@XmlSchema(
        namespace = "http://einfracentral.eu",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns = {
                @XmlNs(prefix = "tns", namespaceURI = "http://einfracentral.eu")//,
                //@XmlNs(prefix = "ns0", namespaceURI = "http://einfracentral.eu")
        }
)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
package eu.einfracentral.domain;

import javax.xml.bind.annotation.*;
