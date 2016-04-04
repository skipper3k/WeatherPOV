package com.skipper3k.si.weatherpov.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by skipper3k on 03/04/16.
 *
 */
public class WPOVCity implements Serializable {

    public String id;
    public String name;
    public int temp;
    public int humidity;
    public String description;
    public Date lastUpdated;

    public boolean favoured;

}
