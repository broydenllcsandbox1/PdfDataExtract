package com.broyden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueConfidence {
    private String _value;
    private Float _confidence;

    public ValueConfidence(String value, Float confidence) {
        this._value = value;
        this._confidence = confidence;
    }

    public String getValue() {
        return this._value;
    }

    public String getConfidence() {
        return this._confidence.toString();
    }
}
