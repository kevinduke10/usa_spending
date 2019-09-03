package com.tectonix.usaspending.domain;

public class UniqueAgency{
    public String awardingAgencyName;
    public String awardingSubAgencyName;
    public String awardingOfficeName;

    public UniqueAgency(String awardingAgencyName, String awardingSubAgencyName, String awardingOfficeName){
        this.awardingAgencyName = awardingAgencyName;
        this.awardingSubAgencyName = awardingSubAgencyName;
        this.awardingOfficeName = awardingOfficeName;
    }

}
