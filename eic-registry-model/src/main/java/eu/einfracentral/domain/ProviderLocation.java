package eu.einfracentral.domain;

import eu.einfracentral.annotation.FieldValidation;
import eu.einfracentral.annotation.VocabularyValidation;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(namespace = "http://einfracentral.eu")
public class ProviderLocation {


    // Provider's Location Information
    /**
     * Provider's location street name and number.
     */
    @XmlElement(required = true)
    @ApiModelProperty(position = 1, example = "String (required)", required = true)
    @FieldValidation
    private String streetNameAndNumber;

    /**
     * Provider's location postal code.
     */
    @XmlElement(required = true)
    @ApiModelProperty(position = 2, example = "String (required)", required = true)
    @FieldValidation
    private String postalCode;

    /**
     * Provider's location city.
     */
    @XmlElement(required = true)
    @ApiModelProperty(position = 3, example = "String (required)", required = true)
    @FieldValidation
    private String city;

    /**
     * Provider's location region.
     */
    @XmlElement
    @ApiModelProperty(position = 4, example = "String (optional)")
    @FieldValidation(nullable = true)
    private String region;

    /**
     * Provider's location country.
     */
    @XmlElement(required = true)
    @ApiModelProperty(position = 5, example = "String (required)", required = true)
    @VocabularyValidation(type = Vocabulary.Type.PLACE)
    private String country;

    public ProviderLocation() {
    }

    public ProviderLocation(String streetNameAndNumber, String postalCode, String city, String region, String country) {
        this.streetNameAndNumber = streetNameAndNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.region = region;
        this.country = country;
    }

    @Override
    public String toString() {
        return "ProviderLocation{" +
                "streetNameAndNumber='" + streetNameAndNumber + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    public String getStreetNameAndNumber() {
        return streetNameAndNumber;
    }

    public void setStreetNameAndNumber(String streetNameAndNumber) {
        this.streetNameAndNumber = streetNameAndNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
