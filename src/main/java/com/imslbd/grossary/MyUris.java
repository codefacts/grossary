package com.imslbd.grossary;

/**
 * Created by someone on 12/11/2015.
 */
public enum MyUris {
    STATIC_RESOURCES_PATTERN("/my-static/*", "Static Resources"),
    PUBLIC_RESOURCES_PATTERN("/my-public/*", "Public Resources"),
    GROCERY("/grocery", "View Grocery"),
    DASHBOARD("/dashboard", "Dashboard"),
    CREATE_GROCERY("/grocery/create", ""),
    INSERT("/insert", "Insert"),
    UPDATE("/update", "update"),
    DELETE("/delete", "delete"),
    QUERY("/query", "Query"),
    APP_DATA_STATIC_PATTERN("/app_data/*", "App Data"),
    CONTACTS_GROCERIES("/contacts/groceries", ""),
    CONTACTS_POS_NOS("/contacts/posNos", ""),
    CONTACTS_LOCATIONS("/contacts/locations", ""),
    CONTACTS("/contacts", ""),
    CONTACTS_DATE_MIN_MAX("/contacts/dateMinMax", ""),
    CONTACTS_SUMMARY("/contacts/summary", ""),
    CONTACTS_SUMMARY_DETAILS("/contacts/summaryDetails", ""),
    CONTACTS_GROUP_BY_COUNT("/contacts/groupByCount", ""),
    CONTACTS_COUNT_BY_DATE("/contacts/countByDate", "");

    public final String value;
    public final String label;

    MyUris(final String value, String label) {
        this.value = value;
        this.label = label;
    }
}
