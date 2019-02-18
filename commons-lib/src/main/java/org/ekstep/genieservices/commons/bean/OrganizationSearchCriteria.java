package org.ekstep.genieservices.commons.bean;

public class OrganizationSearchCriteria {

    private boolean isRootOrg;

    private OrganizationSearchCriteria(boolean isRootOrg) {
        this.isRootOrg = isRootOrg;
    }

    public boolean isRootOrg() {
        return isRootOrg;
    }

    public static class SearchBuilder {
        private boolean isRootOrg;

        public SearchBuilder rootOrg() {
            this.isRootOrg = true;
            return this;
        }

        public OrganizationSearchCriteria build() {
            return new OrganizationSearchCriteria(isRootOrg);
        }
    }

}
