package com.harsha.truckerdriver;

import java.util.Date;

/**
 * Created by LENOVO on 08-01-2018.
 */

public class OldTrip {

    private String status, realName, source, destination, cost, rideDuration;
    private Date date;



    public OldTrip(String status, String realName, String source, String destination, String cost, Date date, String rideDuration) {
        this.status = status;
        this.realName = realName;
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.date = date;
        this.rideDuration = rideDuration;
    }

    public String getStatus() {
        if(status.equals("finished")){
            return status;
        }else {
            status="cancelled";
            return status;
        }

    }

    public String getRealName() {
        return realName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getCost() {
        return cost;
    }

    public Date getdate() {
        return date;
    }

    public String getRideDuration() {
        return rideDuration;
    }
}




