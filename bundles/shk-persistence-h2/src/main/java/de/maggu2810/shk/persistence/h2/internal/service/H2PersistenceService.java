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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringListType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.types.UnDefType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.persistence.h2.internal.HistoricItemImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
        "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" }, //
        justification = "Needs to be checked carefully, but ATM we need to build some stuff (e.g. table name).")
@Component(service = { PersistenceService.class }, immediate = true)
public class H2PersistenceService extends H2AbstractPersistenceService {
    private static class MyColumn extends H2AbstractPersistenceService.Column {
        public static final String CLAZZ = "clazz";
    }

    private static class Schema extends H2AbstractPersistenceService.Schema {
        public static final String ITEM = "ESH_ITEM";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, List<Class<? extends State>>> stateClasses = new HashMap<>();

    /**
     * Create a new H2 persistence service.
     */
    public H2PersistenceService() {
        super(Schema.ITEM);

        // Ensure that known types are accessible by the classloader
        addStateClass(DateTimeType.class);
        addStateClass(DecimalType.class);
        addStateClass(HSBType.class);
        addStateClass(OnOffType.class);
        addStateClass(OpenClosedType.class);
        addStateClass(PlayPauseType.class);
        addStateClass(PointType.class);
        addStateClass(RawType.class);
        addStateClass(RewindFastforwardType.class);
        addStateClass(StringListType.class);
        addStateClass(StringType.class);
        addStateClass(UnDefType.class);
        addStateClass(UpDownType.class);
    }

    private void addStateClass(@UnknownInitialization H2PersistenceService this,
            final Class<? extends State> stateClass) {
        final List<Class<? extends State>> list = new ArrayList<>();
        list.add(stateClass);
        stateClasses.put(getStateClassKey(stateClass), list);
    }

    private static String getStateClassKey(final Class<? extends State> stateClass) {
        return stateClass.getSimpleName();
    }

    @Override
    public @NonNull String getId() {
        return "h2";
    }

    private void setStateValue(final PreparedStatement stmt, final int pos, final State state) throws SQLException {
        stmt.setString(pos, state.toFullString());
    }

    @Override
    protected @NonNull State getStateForItem(final Item item) {
        return item.getState();
    }

    @Override
    protected boolean createTable(final Class<? extends State> stateClass, final String tableName) {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s %s, %s %s,  %s %s, PRIMARY KEY(%s));",
                tableName, Column.TIME, SqlType.TIMESTAMP, MyColumn.CLAZZ, SqlType.VARCHAR, Column.VALUE,
                SqlType.VARCHAR, Column.TIME);
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
        final String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES(?,?,?);", tableName, Column.TIME,
                MyColumn.CLAZZ, Column.VALUE);
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int cnt = 0;
            stmt.setTimestamp(++cnt, new Timestamp(date.getTime()));
            stmt.setString(++cnt, getStateClassKey(state.getClass()));
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
        final String sql = String.format("UPDATE %s SET %s = ?, %s = ? WHERE TIME = ?", tableName, MyColumn.CLAZZ,
                Column.VALUE);
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            int cnt = 0;
            stmt.setString(++cnt, getStateClassKey(state.getClass()));
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
        // Get the item name from the filter
        final String itemName = filter.getItemName();
        if (itemName == null) {
            throw new IllegalArgumentException("Filter misses item name.");
        }

        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: Query aborted on item {} - H2 not connected!", getId(), itemName);
            return Collections.emptyList();
        }

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("SELECT %s, %s, %s FROM %s%s%s%s", Column.TIME, MyColumn.CLAZZ,
                Column.VALUE, getTableName(filter.getItemName()), filterWhere.prepared, getFilterStringOrder(filter),
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
                    final String clazz;
                    final String value;

                    cnt = 0;
                    time = rs.getTimestamp(++cnt);
                    if (time == null) {
                        logger.warn("{}: itemName: {}, time must be non null", getId(), itemName);
                        continue;
                    }

                    clazz = rs.getString(++cnt);
                    if (clazz == null) {
                        logger.warn("{}: itemName: {}, clazz must be non null", getId(), itemName);
                        continue;
                    }

                    value = rs.getString(++cnt);
                    if (value == null) {
                        logger.warn("{}: itemName: {}, value must be non null", getId(), itemName);
                        continue;
                    }

                    logger.trace("{}: itemName: {}, time: {}, clazz: {}, value: {}", getId(), itemName, time, clazz,
                            value);

                    final State state;
                    if (!stateClasses.containsKey(clazz) && itemRegistry != null) {
                        try {
                            final Item item = itemRegistry.getItem(itemName);
                            if (item != null) {
                                for (final Class<? extends State> it : item.getAcceptedDataTypes()) {
                                    final String key = getStateClassKey(it);
                                    if (!stateClasses.containsKey(key)) {
                                        addStateClass(it);
                                        logger.warn("Add new state class '{}'", clazz);
                                    }
                                }
                            }
                        } catch (final ItemNotFoundException ex) {
                            logger.warn("{}: Cannot lookup state class because item '{}' is not known.", getId(),
                                    itemName, ex);
                            continue;
                        }
                    }

                    if (stateClasses.containsKey(clazz)) {
                        state = TypeParser.parseState(stateClasses.get(clazz), value);
                        if (state == null) {
                            logger.warn("{}: Cannot parse state (class: {}, state classes: {}, value: {}).", getId(),
                                    clazz, stateClasses.get(clazz), value);
                            continue;
                        }
                    } else {
                        logger.warn("Unknown state class '{}'", clazz);
                        continue;
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
