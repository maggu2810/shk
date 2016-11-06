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

import org.checkerframework.checker.nullness.qual.Nullable;

public class DateUtil {
    private DateUtil() {
    }

    /**
     * Create a new Date that contains the same information as the given one.
     *
     * @param date the date object to clone (may be null)
     * @return a new date object that is equal to the given or null if null has been given
     */
    public static @Nullable Date clone(final @Nullable Date date) {
        if (date == null) {
            return null;
        } else {
            return new Date(date.getTime());
        }
    }
}
