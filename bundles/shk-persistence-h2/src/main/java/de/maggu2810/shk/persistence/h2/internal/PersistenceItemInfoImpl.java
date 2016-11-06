/*-
 * #%L
 * shk :: Bundles :: Persistence :: h2
 * %%
 * Copyright (C) 2015 - 2016 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.persistence.h2.internal;

import java.util.Date;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;

public class PersistenceItemInfoImpl implements PersistenceItemInfo {

    private final @NonNull String name;
    private final @NonNull Integer count;
    private final @Nullable Date earliest;
    private final @Nullable Date latest;

    /**
     * Create a new persistence item information.
     *
     * @param name the item name
     * @param count the number of entries
     * @param earliest the date of the first entry (may be null)
     * @param latest the date of the last entry (may be null)
     */
    public PersistenceItemInfoImpl(final @NonNull String name, final @NonNull Integer count,
            final @Nullable Date earliest, final @Nullable Date latest) {
        this.name = name;
        this.count = count;
        this.earliest = DateUtil.clone(earliest);
        this.latest = DateUtil.clone(latest);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable Integer getCount() {
        return count;
    }

    @Override
    public @Nullable Date getEarliest() {
        return DateUtil.clone(earliest);
    }

    @Override
    public @Nullable Date getLatest() {
        return DateUtil.clone(latest);
    }
}