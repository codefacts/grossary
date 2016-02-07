package com.imslbd.grossary.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

/**
 * Created by shahadat on 1/30/16.
 */
public class CallCenterService {
    public CallCenterService(HttpClient httpClient) {
    }

    public void callCenterSupervisorQuery(Message<JsonObject> message) {
        String str =
            "SELECT " +
                "SMS_INBOX.Date, " +
                "CALLS.CALL_OPERATOR, " +
                "Count(SMS_INBOX.SMS_ID) AS TotalData, " +
                "Count(CALLS.CALL_ID) AS TotalCalls, " +
                "Nz([TotalData]-[TotalCalls],0) AS Call_Due, " +
                "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS Success, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True,True,Null)) AS Aware, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=False,True,Null)) AS Unaware, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True,True,Null)) AS RecalledTheDesign, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=False,True,Null)) AS NotRecalledTheDesign, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=1,True,Null)) AS Good, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=2,True,Null)) AS OKorAverage, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=3,True,Null)) AS Bad, " +
                "Nz([RecalledTheDesign]-([Good]+[OKorAverage]+[Bad]),0) AS Error " +
                "FROM SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID " +
                "GROUP BY SMS_INBOX.Date, CALLS.CALL_OPERATOR;";

        System.out.println(str);
    }

    public static void main(String... args) {
        String str =
            "SELECT " +
                "SMS_INBOX.Date, " +
                "CALLS.CALL_OPERATOR, " +
                "Count(SMS_INBOX.SMS_ID) AS TotalData, " +
                "Count(CALLS.CALL_ID) AS TotalCalls, " +
                "Nz([TotalData]-[TotalCalls],0) AS Call_Due, " +
                "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS Success, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True,True,Null)) AS Aware, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=False,True,Null)) AS Unaware, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True,True,Null)) AS RecalledTheDesign, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=False,True,Null)) AS NotRecalledTheDesign, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=1,True,Null)) AS Good, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=2,True,Null)) AS OKorAverage, " +
                "Count(IIf([CALL_STATUS_ID]=1 And [Q02_NOTICED_NEW_CHANGE]=True And [Q03_CHANGES_HAS_NOTICED]=True And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=3,True,Null)) AS Bad, " +
                "Nz([RecalledTheDesign]-([Good]+[OKorAverage]+[Bad]),0) AS Error " +
                "FROM SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID " +

                "GROUP BY SMS_INBOX.Date, CALLS.CALL_OPERATOR;";

        System.out.println(str);
    }
}
