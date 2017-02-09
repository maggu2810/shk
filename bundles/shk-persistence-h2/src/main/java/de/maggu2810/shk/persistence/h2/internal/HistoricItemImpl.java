/*-
 * #%L
 * shk :: Bundles :: Persistence :: h2
 * %%
 * Copyright (C) 2015 - 2017 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.persistence.h2.internal;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.types.State;

public class HistoricItemImpl implements HistoricItem {

    private final String name;
    private final State state;
    private final Date timestamp;

    /**
     * Create a new historic item.
     *
     * @param name the item name
     * @param state the state
     * @param timestamp the timestamp
     */
    public HistoricItemImpl(final String name, final State state, final Date timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = new Date(timestamp.getTime());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s", DateFormat.getDateTimeInstance().format(timestamp), name, state);
    }

}
