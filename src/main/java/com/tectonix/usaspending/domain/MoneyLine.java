package com.tectonix.usaspending.domain;

//Interesting fields
// 0 award_id_piid
// 2 transaction_number
// 3 parent_award_agency_id
// 4 parent_award_agency_name
// 8 total_dollars_obligated
// 9 base_and_exercised_options_value
// 10 current_total_value_of_award
// 11 base_and_all_options_value
// 12 potential_total_value_of_award
// 13 action_date
// 14 period_of_performance_start_date
// 15 period_of_performance_current_end_date
// 16 period_of_performance_potential_end_date
// 18 awarding_agency_code
// 19 awarding_agency_name
// 20 awarding_sub_agency_code
// 21 awarding_sub_agency_name
// 22 awarding_office_code
// 23 awarding_office_name
// 24 funding_agency_code
// 25 funding_agency_name
// 30 foreign_funding
// 31 foreign_funding_description


// 34 Recipient DUNS
// 35 Recipient Name
// 36 Recipient_Doing_Business_As_Name
// 37 CAGE_Code
// 38 Recipient Parent Name
// 41 Recipient Country Name
// 42 Recipient Address Line 1
// 43 Recipient Address Line 2
// 44 Recipient City Name
// 46 Recipient State Name
// 47 Zip with 4 xtra digits

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MoneyLine {

    String awardIdPiid;
    String transactionNumber;
    String parentAwardAgencyId;
    String parentAwardAgencyName;
    String totalDollarsObligated;
    String baseAndExercisedOptionsValue;
    String currentTotalValueOfAward;
    String baseAndAllOptionsValue;
    String potentialTotalValueOfAward;
    String actionDate;
    String periodOfPerformanceStartDate;
    String periodOfPerformanceCurrentEndDate;
    String periodOfPerformancePotentialEndDate;
    String awardingAgencyCode;
    String awardingAgencyName;
    String awardingSubAgencyCode;
    String awardingSubAgencyName;
    String awardingOfficeCode;
    String awardingOfficeName;
    String fundingAgencyCode;
    String fundingAgencyName;
    String foreignFunding;
    String foreignFundingDescription;

    String recipientDUNS;
    String recipientName;
    String recipientDoingBusinessAsName;
    String cageCode;
    String recipientParentName;
    String recipientCountryName;
    String recipientAddressLine1;
    String recipientAddressLine2;
    String recipientCityName;
    String recipientStateName;
    String zipWith4XtraDigits;

    String address;
    Boolean isUSAddress;

    public static String[] getRelevantHeaderFields = {
            "awardIdPiid",
            "transactionNumber",
            "parentAwardAgencyId",
            "parentAwardAgencyName",
            "totalDollarsObligated",
            "baseAndExercisedOptionsValue",
            "currentTotalValueOfAward",
            "baseAndAllOptionsValue",
            "potentialTotalValueOfAward",
            "actionDate",
            "periodOfPerformanceStartDate",
            "periodOfPerformanceCurrentEndDate",
            "periodOfPerformancePotentialEndDate",
            "awardingAgencyCode",
            "awardingAgencyName",
            "awardingSubAgencyCode",
            "awardingSubAgencyName",
            "awardingOfficeCode",
            "awardingOfficeName",
            "fundingAgencyCode",
            "fundingAgencyName",
            "foreignFunding",
            "foreignFundingDescription",
            "recipientDUNS",
            "recipientName",
            "recipientDoingBusinessAsName",
            "cageCode",
            "recipientParentName",
            "recipientCountryName",
            "recipientAddressLine1",
            "recipientAddressLine2",
            "recipientCityName",
            "recipientStateName",
            "zipWith4XtraDigits",
            "address",
            "isUSAddress"
    };

    public MoneyLine(){}

    public MoneyLine(String[] line){
        this.awardIdPiid = line[0];
        this.transactionNumber = line[2];
        this.parentAwardAgencyId = line[3];
        this.parentAwardAgencyName = line[4];
        this.totalDollarsObligated = line[8];
        this.baseAndExercisedOptionsValue = line[9];
        this.currentTotalValueOfAward = line[10];
        this.baseAndAllOptionsValue = line[11];
        this.potentialTotalValueOfAward = line[12];
        this.actionDate = line[13];
        this.periodOfPerformanceStartDate = line[14];
        this.periodOfPerformanceCurrentEndDate = line[15];
        this.periodOfPerformancePotentialEndDate = line[16];
        this.awardingAgencyCode = line[18];
        this.awardingAgencyName = line[19];
        this.awardingSubAgencyCode = line[20];
        this.awardingSubAgencyName = line[21];
        this.awardingOfficeCode = line[22];
        this.awardingOfficeName = line[23];
        this.fundingAgencyCode = line[24];
        this.fundingAgencyName = line[25];
        this.foreignFunding = line[30];
        this.foreignFundingDescription = line[31];

        this.recipientDUNS = line[34];
        this.recipientName = line[35];
        this.recipientDoingBusinessAsName = line[36];
        this.cageCode = line[37];
        this.recipientParentName = line[38];
        this.recipientCountryName = line[41];
        this.recipientAddressLine1 = line[42];
        this.recipientAddressLine2 = line[43];
        this.recipientCityName = line[44];
        this.recipientStateName = line[46];
        this.zipWith4XtraDigits = line[47];

        this.address = aggregateAddress(line);
        this.isUSAddress = isValidState(line[46]);
    }

    public String aggregateAddress(String[] split){
        String queryAddress = "";

        String line1 = split[42];
        String line2 = split[43];
        String cityName = split[44];
        String stateName = split[46];
        if (line1.length() > 0) {
            queryAddress = queryAddress + line1.replace(" ", "+");
        }
        if (line2.length() > 0) {
            queryAddress = queryAddress + line2.replace(" ", "+");
        }

        if (cityName != null) {
            queryAddress = queryAddress + "+" + cityName.replace(" ", "+") + "+";
        } else {
            return "";
        }

        if (stateName != null) {
            queryAddress = queryAddress + stateName;
        }

        return queryAddress;
    }

    public String[] toCSV(){
        String[] returnVal = {
                this.awardIdPiid,
                this.transactionNumber,
                this.parentAwardAgencyId,
                this.parentAwardAgencyName,
                this.totalDollarsObligated,
                this.baseAndExercisedOptionsValue,
                this.currentTotalValueOfAward,
                this.baseAndAllOptionsValue,
                this.potentialTotalValueOfAward,
                this.actionDate,
                this.periodOfPerformanceStartDate,
                this.periodOfPerformanceCurrentEndDate,
                this.periodOfPerformancePotentialEndDate,
                this.awardingAgencyCode,
                this.awardingAgencyName,
                this.awardingSubAgencyCode,
                this.awardingSubAgencyName,
                this.awardingOfficeCode,
                this.awardingOfficeName,
                this.fundingAgencyCode,
                this.fundingAgencyName,
                this.foreignFunding,
                this.foreignFundingDescription,
                this.recipientDUNS,
                this.recipientName,
                this.recipientDoingBusinessAsName,
                this.cageCode,
                this.recipientParentName,
                this.recipientCountryName,
                this.recipientAddressLine1,
                this.recipientAddressLine2,
                this.recipientCityName,
                this.recipientStateName,
                this.zipWith4XtraDigits,
                this.address,
                isValidState(this.recipientStateName).toString()
        };
        return returnVal;
    }

    public static MoneyLine fromFilteredCSV(String[] line){
        MoneyLine ml = new MoneyLine();
        ml.awardIdPiid = line[0];
        ml.transactionNumber = line[1];
        ml.parentAwardAgencyId = line[2];
        ml.parentAwardAgencyName = line[3];
        ml.totalDollarsObligated = line[4];
        ml.baseAndExercisedOptionsValue = line[5];
        ml.currentTotalValueOfAward = line[6];
        ml.baseAndAllOptionsValue = line[7];
        ml.potentialTotalValueOfAward = line[8];
        ml.actionDate = line[9];
        ml.periodOfPerformanceStartDate = line[10];
        ml.periodOfPerformanceCurrentEndDate = line[11];
        ml.periodOfPerformancePotentialEndDate = line[12];
        ml.awardingAgencyCode = line[13];
        ml.awardingAgencyName = line[14];
        ml.awardingSubAgencyCode = line[15];
        ml.awardingSubAgencyName = line[16];
        ml.awardingOfficeCode = line[17];
        ml.awardingOfficeName = line[18];
        ml.fundingAgencyCode = line[19];
        ml.fundingAgencyName = line[20];
        ml.foreignFunding = line[21];
        ml.foreignFundingDescription = line[22];

        ml.recipientDUNS = line[23];
        ml.recipientName = line[24];
        ml.recipientDoingBusinessAsName = line[25];
        ml.cageCode = line[26];
        ml.recipientParentName = line[27];
        ml.recipientCountryName = line[28];
        ml.recipientAddressLine1 = line[29];
        ml.recipientAddressLine2 = line[30];
        ml.recipientCityName = line[31];
        ml.recipientStateName = line[32];
        ml.zipWith4XtraDigits = line[33];

        ml.address = line[34];
        ml.isUSAddress = Boolean.valueOf(line[35]);
        return ml;
    }

    private static List<String> listOfStates = Arrays.asList(
            "Alabama",
            "Alaska",
            "Arizona",
            "Arkansas",
            "California",
            "Colorado",
            "Connecticut",
            "Delaware",
            "Florida",
            "Georgia",
            "Hawaii",
            "Idaho",
            "Illinois",
            "Indiana",
            "Iowa",
            "Kansas",
            "Kentucky",
            "Louisiana",
            "Maine",
            "Maryland",
            "Massachusetts",
            "Michigan",
            "Minnesota",
            "Mississippi",
            "Missouri",
            "Montana",
            "Nebraska",
            "Nevada",
            "New Hampshire",
            "New Jersey",
            "New Mexico",
            "New York",
            "North Carolina",
            "North Dakota",
            "Ohio",
            "Oklahoma",
            "Oregon",
            "Pennsylvania",
            "Rhode Island",
            "South Carolina",
            "South Dakota",
            "Tennessee",
            "Texas",
            "Utah",
            "Vermont",
            "Virginia",
            "Washington",
            "West Virginia",
            "Wisconsin",
            "Wyoming",
            "District of Columbia",
            "Puerto Rico",
            "Guam",
            "American Samoa",
            "U.S. Virgin Islands",
            "Northern Mariana Islands"
    );

    public Boolean isValidState(String inputVal){
        List<String> foundState = listOfStates.stream().filter(state -> inputVal.equalsIgnoreCase(state)).collect(Collectors.toList());
        if(this.recipientStateName != null && foundState.size() > 0){
            return true;
        }else{
            return false;
        }
    }

    public String getAddress(){
        return address;
    }
}
