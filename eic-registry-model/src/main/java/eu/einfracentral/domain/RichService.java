package eu.einfracentral.domain;


import java.util.List;

// FIXME: change to composition instead of inheritance.
public class RichService extends Service {

    //    private Service service;
    private ServiceMetadata serviceMetadata;
    private String superCategoryName;
    private String categoryName;
    private List<String> subCategoryNames;
    private List<String> languageNames;
    private List<String> placeNames;
    private String trlName;
    private String phaseName;
    private List<String> scientificDomainNames;
    private List<String> scientificSubDomainNames;
    private List<String> targetUsersNames;
    private List<String> accessTypeNames;
    private List<String> accessModeNames;
    private List<String> fundedByNames;
    private String orderTypeName;
    private int views;
    private int ratings;
    private float userRate;
    private float hasRate;
    private int favourites;
    private boolean isFavourite;


    public RichService() {

    }

    public RichService(Service service, ServiceMetadata serviceMetadata) {
//        this.service = service;
        super(service);
        this.serviceMetadata = serviceMetadata;
    }

    public RichService(InfraService service) {
//        this.service = (Service) service;
        super(service);
        this.serviceMetadata = service.getServiceMetadata();
    }


    // Getters/Setters for VocabularyNames
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getSubCategoryNames() {
        return subCategoryNames;
    }

    public void setSubCategoryNames(List<String> subCategoryNames) {
        this.subCategoryNames = subCategoryNames;
    }

    public List<String> getLanguageNames() {
        return languageNames;
    }

    public void setLanguageNames(List<String> languageNames) {
        this.languageNames = languageNames;
    }

    public String getTrlName() {
        return trlName;
    }

    public void setTrlName(String trlName) {
        this.trlName = trlName;
    }

    public List<String> getPlaceNames() {
        return placeNames;
    }

    public void setPlaceNames(List<String> placeNames) {
        this.placeNames = placeNames;
    }

    public String getSuperCategoryName() {
        return superCategoryName;
    }

    public void setSuperCategoryName(String superCategoryName) {
        this.superCategoryName = superCategoryName;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public List<String> getScientificDomainNames() {
        return scientificDomainNames;
    }

    public void setScientificDomainNames(List<String> scientificDomainNames) {
        this.scientificDomainNames = scientificDomainNames;
    }

    public List<String> getScientificSubDomainNames() {
        return scientificSubDomainNames;
    }

    public void setScientificSubDomainNames(List<String> scientificSubDomainNames) {
        this.scientificSubDomainNames = scientificSubDomainNames;
    }

    public List<String> getTargetUsersNames() {
        return targetUsersNames;
    }

    public void setTargetUsersNames(List<String> targetUsersNames) {
        this.targetUsersNames = targetUsersNames;
    }

    public List<String> getAccessTypeNames() {
        return accessTypeNames;
    }

    public void setAccessTypeNames(List<String> accessTypeNames) {
        this.accessTypeNames = accessTypeNames;
    }

    public List<String> getAccessModeNames() {
        return accessModeNames;
    }

    public void setAccessModeNames(List<String> accessModeNames) {
        this.accessModeNames = accessModeNames;
    }

    public List<String> getFundedByNames() {
        return fundedByNames;
    }

    public void setFundedByNames(List<String> fundedByNames) {
        this.fundedByNames = fundedByNames;
    }

    public String getOrderTypeName() {
        return orderTypeName;
    }

    public void setOrderTypeName(String orderTypeName) {
        this.orderTypeName = orderTypeName;
    }


    // Getters/Setters for Statistics
    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
    }

    public float getHasRate() {
        return hasRate;
    }

    public void setHasRate(float hasRate) {
        this.hasRate = hasRate;
    }

    public int getFavourites() {
        return favourites;
    }

    public void setFavourites(int favourites) {
        this.favourites = favourites;
    }

    public boolean getIsFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public float getUserRate() {
        return userRate;
    }

    public void setUserRate(float userRate) {
        this.userRate = userRate;
    }

}
