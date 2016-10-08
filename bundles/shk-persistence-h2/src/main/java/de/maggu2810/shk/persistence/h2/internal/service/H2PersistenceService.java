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

    private void addStateClass(final Class<? extends State> stateClass) {
        final List<Class<? extends State>> list = new ArrayList<>();
        list.add(stateClass);
        stateClasses.put(getStateClassKey(stateClass), list);
    }

    private String getStateClassKey(final Class<? extends State> stateClass) {
        return stateClass.getSimpleName();
    }

    @Override
    public String getId() {
        return "h2";
    }

    @Override
    protected State getStateForItem(final Item item) {
        return item.getState();
    }

    @Override
    protected boolean createTable(final Class<? extends State> stateClass, final String tableName) {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s %s, %s %s,  %s %s, PRIMARY KEY(%s));",
                tableName, Column.TIME, SqlType.TIMESTAMP, MyColumn.CLAZZ, SqlType.VARCHAR, Column.VALUE,
                SqlType.VARCHAR, Column.TIME);
        try (final Statement statement = connection.createStatement()) {
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
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 0;
            stmt.setTimestamp(++i, new Timestamp(date.getTime()));
            stmt.setString(++i, getStateClassKey(state.getClass()));
            stmt.setString(++i, state.toString());
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
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 0;
            stmt.setString(++i, getStateClassKey(state.getClass()));
            stmt.setString(++i, state.toString());
            stmt.setTimestamp(++i, new Timestamp(date.getTime()));
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.trace("{}: update failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    public Iterable<HistoricItem> query(final FilterCriteria filter) {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: Query aborted on item {} - H2 not connected!", getId(), filter.getItemName());
            return Collections.emptyList();
        }

        // Get the item name from the filter
        final String itemName = filter.getItemName();

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("SELECT %s, %s, %s FROM %s%s%s%s", Column.TIME, MyColumn.CLAZZ,
                Column.VALUE, getTableName(filter.getItemName()), filterWhere.prepared, getFilterStringOrder(filter),
                getFilterStringLimit(filter));

        try (final PreparedStatement st = connection.prepareStatement(queryString)) {
            int i = 0;
            if (filterWhere.begin) {
                st.setTimestamp(++i, new Timestamp(filter.getBeginDate().getTime()));
            }
            if (filterWhere.end) {
                st.setTimestamp(++i, new Timestamp(filter.getEndDate().getTime()));
            }

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (final ResultSet rs = st.executeQuery()) {
                final List<HistoricItem> items = new ArrayList<>();
                while (rs.next()) {
                    final Date time;
                    final String clazz;
                    final String value;

                    i = 0;
                    time = rs.getTimestamp(++i);
                    clazz = rs.getString(++i);
                    value = rs.getString(++i);
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