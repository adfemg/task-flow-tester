package org.emg.adf.sample.controller.bean;

import java.math.BigDecimal;

import java.util.HashMap;

import oracle.jbo.Key;
import oracle.jbo.domain.Date;

public interface SampleContextInterface {
    void setNoot(String noot);

    void setAap(String aap);

    String getAap();

    String getMies();

    Integer getNummertje();

    Integer getNummerPrim();

    void setBignummer(BigDecimal bignummer);

    BigDecimal getBignummer();

    Key getKey();

    HashMap getMapProperty();

    int getIntje();

    void setCharValue(char charValue);

    char getCharValue();

    short getShortValue();

    double getDoubleValue();

    Date getDateValue();
}
