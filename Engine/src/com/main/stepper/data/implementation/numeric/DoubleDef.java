package com.main.stepper.data.implementation.numeric;

import com.main.stepper.data.api.AbstractDataDef;

public class DoubleDef extends AbstractDataDef {
    public DoubleDef() {
        super("Double", true, Double.class);
    }

    @Override
    public Double readValue(String data) {
        return  Double.parseDouble(data);
    }
}
