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

package de.maggu2810.shk.persistence.h2.internal.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.persistence.h2.internal.HistoricItemImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
        "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" }, //
        justification = "Needs to be checked carefully, but ATM we need to build some stuff (e.g. table name).")
@Component(service = { PersistenceService.class }, immediate = true)
public class H2SqlPersistenceService extends H2AbstractPersistenceService {
    private static class Schema extends H2AbstractPersistenceService.Schema {
        public static final String ITEM = "SMARTHOME";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Create a new H2 persistence service.
     */
    public H2SqlPersistenceService() {
        super(Schema.ITEM);
    }

    @Override
    public @NonNull String getId() {
        return "h2sql";
    }

    private String getSqlType(final Class<? extends State> stateClass) {
        if (stateClass.isAssignableFrom(PercentType.class)) {
            return SqlType.TINYINT;
        } else if (stateClass.isAssignableFrom(DecimalType.class)) {
            return SqlType.DECIMAL;
        } else {
            return SqlType.VARCHAR;
        }
    }

    private void setStateValue(final PreparedStatement stmt, final int pos, final State state) throws SQLException {
        if (state instanceof PercentType) {
            stmt.setInt(pos, ((PercentType) state).intValue());
        } else if (state instanceof DecimalType) {
            stmt.setBigDecimal(pos, ((DecimalType) state).toBigDecimal());
        } else {
            stmt.setString(pos, state.toFullString());
        }
    }

    @Override
    protected @NonNull State getStateForItem(final Item item) {
        // Do some type conversion to ensure we know the data type.
        // This is necessary for items that have multiple types and may return their
        // state in a format that's not preferred or compatible with the H2SQL type.
        // eg. DimmerItem can return OnOffType (ON, OFF), or PercentType (0-100).
        // We need to make sure we cover the best type for serialization.
        if (item instanceof DimmerItem || item instanceof RollershutterItem) {
            final State state = item.getStateAs(PercentType.class);
            if (state == null) {
                logger.warn("Cannot get state for '{}' ({}) as PercentType (got null).", item,
                        item.getClass().getSimpleName());
                throw new IllegalStateException("Got null which is not expected here.");
            }
            return state;
        } else if (item instanceof ColorItem) {
            final State state = item.getStateAs(HSBType.class);
            if (state == null) {
                logger.warn("Cannot get state for '{}' ({}) as HSBType (got null).", item,
                        item.getClass().getSimpleName());
                throw new IllegalStateException("Got null which is not expected here.");
            }
            return state;
        } else {
            // All other items should return the best format by default
            return item.getState();
        }
    }

    @Override
    protected boolean createTable(final Class<? extends State> stateClass, final String tableName) {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s %s, %s %s, PRIMARY KEY(%s));", tableName,
                Column.TIME, SqlType.TIMESTAMP, Column.VALUE, getSqlType(stateClass), Column.TIME);
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (final SQLException ex) {
            logger.error("{}: create table failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    protected boolean insert(final String tableName, final Date date, final State state) {
        final String sql = String.format("INSERT INTO %s (%s, %s) VALUES(?,?);", tableName, Column.TIME, Column.VALUE);
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int cnt = 0;
            stmt.setTimestamp(++cnt, new Timestamp(date.getTime()));
            setStateValue(stmt, ++cnt, state);
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.warn("{}: insert failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    protected boolean update(final String tableName, final Date date, final State state) {
        final String sql = String.format("UPDATE %s SET %s = ? WHERE TIME = ?", tableName, Column.VALUE);
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int cnt = 0;
            setStateValue(stmt, ++cnt, state);
            stmt.setTimestamp(++cnt, new Timestamp(date.getTime()));
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.trace("{}: update failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    public @NonNull Iterable<@NonNull HistoricItem> query(final @NonNull FilterCriteria filter) {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: Query aborted on item {} - H2 not connected!", getId(), filter.getItemName());
            return Collections.emptyList();
        }

        // Get the item name from the filter
        final String itemName = filter.getItemName();
        if (itemName == null) {
            throw new IllegalArgumentException("Filter misses item name.");
        }

        // Get the item name from the filter
        // Also get the Item object so we can determine the type
        Item item = null;
        try {
            if (itemRegistry != null) {
                item = itemRegistry.getItem(itemName);
            }
        } catch (final ItemNotFoundException ex) {
            logger.error("H2SQL: Unable to get item type for {}.", itemName, ex);

            // Set type to null - data will be returned as StringType
            item = null;
        }
        if (item instanceof GroupItem) {
            // For Group Items is BaseItem needed to get correct Type of Value.
            item = GroupItem.class.cast(item).getBaseItem();
        }

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("SELECT %s, %s FROM %s%s%s%s", Column.TIME, Column.VALUE,
                getTableName(filter.getItemName()), filterWhere.prepared, getFilterStringOrder(filter),
                getFilterStringLimit(filter));

        try (PreparedStatement st = getConnection().prepareStatement(queryString)) {
            int cnt = 0;
            if (filterWhere.begin) {
                st.setTimestamp(++cnt, new Timestamp(filter.getBeginDate().getTime()));
            }
            if (filterWhere.end) {
                st.setTimestamp(++cnt, new Timestamp(filter.getEndDate().getTime()));
            }

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (ResultSet rs = st.executeQuery()) {
                final @NonNull List<@NonNull HistoricItem> items = new ArrayList<>();
                while (rs.next()) {
                    final Date time;
                    final String value;

                    cnt = 0;

                    time = rs.getTimestamp(++cnt);
                    if (time == null) {
                        logger.warn("{}: itemName: {}, time must not be null", getId(), itemName);
                        continue;
                    }

                    value = rs.getString(++cnt);
                    if (value == null) {
                        logger.warn("{}: itemName: {}, time: {}, value most nut be null", getId(), itemName, time);
                        continue;
                    }

                    logger.trace("{}: itemName: {}, time: {}, value: {}", getId(), itemName, time, value);

                    final State state;
                    if (item == null) {
                        state = new StringType(value);
                    } else {
                        state = TypeParser.parseState(item.getAcceptedDataTypes(), value);
                        if (state == null) {
                            logger.warn("{}: Cannot parse state (accepted data types: {}, value: {}).", getId(),
                                    item.getAcceptedDataTypes(), value);
                            continue;
                        }
                    }

                    final HistoricItemImpl sqlItem = new HistoricItemImpl(itemName, state, time);
                    items.add(sqlItem);
                }
                return items;
            }

        } catch (final SQLException ex) {
            logger.error("H2: Error running query", ex);
            return Collections.emptyList();
        }
    }
}
