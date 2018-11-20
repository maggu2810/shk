/*-
 * #%L
 * shk - Bundles - Persistence - h2
 * %%
 * Copyright (C) 2015 - 2018 maggu2810
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.types.State;

/**
 * Implementation of {@link HistoricItem}.
 */
public class HistoricItemImpl implements HistoricItem {

    private final @NonNull String name;
    private final @NonNull State state;
    private final @NonNull Date timestamp;

    /**
     * Create a new historic item.
     *
     * @param name the item name
     * @param state the state
     * @param timestamp the timestamp
     */
    public HistoricItemImpl(final @NonNull String name, final @NonNull State state, final @NonNull Date timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = new Date(timestamp.getTime());
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull State getState() {
        return state;
    }

    @Override
    public @NonNull Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s", DateFormat.getDateTimeInstance().format(timestamp), name, state);
    }

}
