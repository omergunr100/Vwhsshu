package com.mta.java.stepper.io.implementation;

import com.mta.java.stepper.data.api.IDataDefinition;
import com.mta.java.stepper.io.api.DataNecessity;
import com.mta.java.stepper.io.api.IDataIO;

public class DataIO implements IDataIO {
    private final String name;
    private final String userString;
    private final DataNecessity necessity;
    private final IDataDefinition dataDefinition;

    public DataIO(String name, String userString, DataNecessity necessity, IDataDefinition dataDefinition) {
        this.name = name;
        this.userString = userString;
        this.necessity = necessity;
        this.dataDefinition = dataDefinition;
    }

    public DataIO(String name, String userString, IDataDefinition dataDefinition){
        this(name, userString, DataNecessity.NA, dataDefinition);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUserString() {
        return userString;
    }

    @Override
    public IDataDefinition getDataDefinition() {
        return dataDefinition;
    }

    @Override
    public DataNecessity getNecessity() {
        return necessity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataIO dataIO = (DataIO) o;

        return name.equals(dataIO.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(String o) {
        if(o == null) {
            return 1;
        }
        return name.compareTo(o);
    }
}
