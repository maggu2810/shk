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

import java.io.File;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.ModifiablePersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.persistence.h2.internal.PersistenceItemInfoImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
        "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" }, //
        justification = "Needs to be checked carefully, but ATM we need to build some stuff (e.g. table name).")
public abstract class H2AbstractPersistenceService implements ModifiablePersistenceService {
    protected static class Column {
        public static final String TIME = "time";
        public static final String VALUE = "value";
    }

    protected static class SqlType {
        public static final String DECIMAL = "DECIMAL";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String TINYINT = "TINYINT";
        public static final String VARCHAR = "VARCHAR";
    }

    protected static class Schema {
        public static final String METAINFO = "ESH_METAINFO";
    }

    protected static class FilterWhere {
        public final boolean begin;
        public final boolean end;
        public String prepared;

        public FilterWhere(final FilterCriteria filter) {
            this.begin = filter.getBeginDate() != null;
            this.end = filter.getEndDate() != null;
            prepared = "";
        }
    }

    private static final String H2_URL = "jdbc:h2:file:";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String itemSchema;

    // TODO: How to add the reference / Require-Capability without a member object or function?
    @Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    protected volatile org.osgi.service.jdbc.DataSourceFactory h2Driver;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected volatile ItemRegistry itemRegistry;

    @Reference
    protected I18nProvider i18nProvider;

    protected Connection connection;
    private final List<String> itemCache = new ArrayList<>();

    private BundleContext bundleContext;

    /**
     * Create a new persistence service.
     *
     * @param itemSchema the name of the schema used to store items
     */
    public H2AbstractPersistenceService(final String itemSchema) {
        this.itemSchema = itemSchema;
    }

    @Activate
    protected void activate(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    protected void deactivate() {
        disconnectFromDatabase();
        this.bundleContext = null;
    }

    @Override
    public String getLabel(final Locale locale) {
        final String key = String.format("%s.label", getId());
        final String dfl = String.format("%s: H2 Embedded Database", getId());
        return i18nProvider.getText(bundleContext.getBundle(), key, dfl, locale);
    }

    @Override
    public void store(final Item item, final String alias) {
        store(item);
    }

    @Override
    public void store(final Item item) {
        store(item, new Date(), getStateForItem(item));
    }

    @Override
    public void store(final Item item, final Date date, final State state) {
        if (state == null) {
            logger.warn("Skip store... Received a null state for item '{}' of type '{}'", item,
                    item.getClass().getSimpleName());
            return;
        }

        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: No connection to database. Can not persist item '{}'", getId(), item.getName());
            return;
        }

        final String tableName = getTableName(item.getName());

        if (!itemCache.contains(item.getName())) {
            itemCache.add(item.getName());
            if (!createTable(state.getClass(), tableName)) {
                logger.error("{}: Could not create table for item '{}'", getId(), item.getName());
                return;
            }
        }

        // Firstly, try an INSERT. This will work 99.9% of the time
        // If the INSERT fails this might be because we tried persisting data too quickly, or it might be
        // because we really want to UPDATE the data.
        // So, let's try an update. If the reason for the exception isn't due to the primary key already
        // existing, then we'll throw another exception.
        // Note that H2 stores times using the Java Date class, so resolution is milliseconds. We really
        // shouldn't be persisting data that quickly!
        if (!insert(tableName, date, state) && !update(tableName, date, state)) {
            logger.error("{}: Could not store item '{}' in database.", getId(), item.getName());
            return;
        }
        logger.debug("{}: Stored item '{}' state '{}'", getId(), item.getName(), state);
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: No connection to database.", getId());
            return Collections.emptySet();
        }

        // Retrieve the table array
        try (final Statement st = connection.createStatement()) {
            final String queryString = String.format(
                    "SELECT TABLE_NAME, ROW_COUNT_ESTIMATE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='%s';",
                    itemSchema);

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (final ResultSet rs = st.executeQuery(queryString)) {

                final Set<PersistenceItemInfo> items = new HashSet<>();
                while (rs.next()) {
                    try (final Statement stTimes = connection.createStatement()) {
                        final String minMax = String.format("SELECT MIN(%s), MAX(%s) FROM %s", Column.TIME, Column.TIME,
                                getTableName(rs.getString(1)));
                        try (final ResultSet rsTimes = stTimes.executeQuery(minMax)) {

                            final Date earliest;
                            final Date latest;
                            if (rsTimes.next()) {
                                earliest = rsTimes.getTimestamp(1);
                                latest = rsTimes.getTimestamp(2);
                            } else {
                                earliest = null;
                                latest = null;
                            }
                            final PersistenceItemInfoImpl item = new PersistenceItemInfoImpl(rs.getString(1),
                                    rs.getInt(2), earliest, latest);
                            items.add(item);
                        }
                    }
                }
                return items;
            }

        } catch (final SQLException ex) {
            logger.error("{}: Error running query", getId(), ex);
            return Collections.emptySet();
        }
    }

    @Override
    public boolean remove(final FilterCriteria filter) throws InvalidParameterException {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: No connection to database.", getId());
            return false;
        }

        if (filter == null || filter.getItemName() == null) {
            throw new InvalidParameterException(
                    "Invalid filter. Filter must be specified and item name must be supplied.");
        }

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("DELETE FROM %s%s;", getTableName(filter.getItemName()),
                filterWhere.prepared);

        // Retrieve the table array
        try (final PreparedStatement st = connection.prepareStatement(queryString)) {
            int i = 0;
            if (filterWhere.begin) {
                st.setTimestamp(++i, new Timestamp(filter.getBeginDate().getTime()));
            }
            if (filterWhere.end) {
                st.setTimestamp(++i, new Timestamp(filter.getEndDate().getTime()));
            }

            st.execute(queryString);
            // final int rowsDeleted = st.getUpdateCount();

            // Do some housekeeping...
            // See how many rows remain - if it's 0, we should remove the table from the database
            try (final ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + getTableName(filter.getItemName()))) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    final String drop = "DROP TABLE " + getTableName(filter.getItemName());
                    st.execute(drop);
                }

                return true;
            }
        } catch (final SQLException ex) {
            logger.error("{}: Error running query", getId(), ex);
            return false;
        }
    }

    /**
     * Checks if we have a database connection.
     *
     * @return true if connection has been established, false otherwise
     */
    private boolean isConnected() {
        // Check if connection is valid
        try {
            if (connection != null && !connection.isValid(5000)) {
                logger.error("{}: Connection is not valid!", getId());
            }
        } catch (final SQLException ex) {
            logger.error("{}: Error while checking connection", getId(), ex);
        }
        return connection != null;
    }

    /**
     * Connects to the database.
     *
     * @return true if the connection has been established, otherwise false
     */
    protected boolean connectToDatabase() {
        // First, check if we're connected
        if (isConnected() == true) {
            return true;
        }

        // We're not connected, so connect
        try {
            logger.info("{}: Connecting to database", getId());

            final String folderName = Paths.get(ConfigConstants.getUserDataFolder(), getId()).toString();

            // Create path for serialization.
            final File folder = new File(folderName);
            if (!folder.exists() && !folder.mkdirs() && !folder.exists()) {
                logger.error("Cannot create directory.");
                return false;
            }

            final String databaseFileName = Paths.get(folderName, "smarthome").toString();

            String url = H2_URL + databaseFileName;

            // Disable logging and defrag on shutdown
            url += ";TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0;DEFRAG_ALWAYS=true;";
            connection = DriverManager.getConnection(url);

            logger.info("{}: Connected to database {}", getId(), databaseFileName);

            try (final Statement statement = connection.createStatement()) {
                for (final String schema : new String[] { itemSchema, Schema.METAINFO }) {
                    statement.execute(String.format("CREATE SCHEMA IF NOT EXISTS %s;", schema));
                }
                // statement.executeUpdate(String.format("SET SCHEMA %s;", Schema.ITEM));
            }
        } catch (final SQLException ex) {
            logger.error("{}: Failed connecting to the database", getId(), ex);
        }

        return isConnected();
    }

    /**
     * Disconnects from the database.
     */
    private void disconnectFromDatabase() {
        logger.debug("{}: Disconnecting from database.", getId());
        if (connection != null) {
            try {
                connection.close();
                logger.debug("{}: Disconnected from database.", getId());
            } catch (final SQLException ex) {
                logger.error("{}: Failed disconnecting from the database", getId(), ex);
            }
            connection = null;
        }
    }

    protected String getTableName(final String itemName) {
        return String.format("%s.\"%s\"", itemSchema, itemName);
    }

    protected FilterWhere getFilterWhere(final FilterCriteria filter) {
        final FilterWhere filterWhere = new FilterWhere(filter);

        if (filterWhere.begin) {
            if (filterWhere.prepared.isEmpty()) {
                filterWhere.prepared += " WHERE";
            } else {
                filterWhere.prepared += " AND";
            }
            filterWhere.prepared += String.format(" %s >= ?", Column.TIME);
        }
        if (filterWhere.end) {
            if (filterWhere.prepared.isEmpty()) {
                filterWhere.prepared += " WHERE";
            } else {
                filterWhere.prepared += " AND";
            }
            filterWhere.prepared += String.format(" %s <= ?", Column.TIME);
        }
        return filterWhere;
    }

    protected String getFilterStringOrder(final FilterCriteria filter) {
        if (filter.getOrdering() == Ordering.ASCENDING) {
            return String.format(" ORDER BY %s ASC", Column.TIME);
        } else {
            return String.format(" ORDER BY %s DESC", Column.TIME);
        }
    }

    protected String getFilterStringLimit(final FilterCriteria filter) {
        if (filter.getPageSize() != 0x7fffffff) {
            return " LIMIT " + filter.getPageSize() + " OFFSET " + filter.getPageNumber() * filter.getPageSize();
        } else {
            return "";
        }
    }

    protected abstract State getStateForItem(final Item item);

    protected abstract boolean createTable(Class<? extends State> stateClass, final String tableName);

    protected abstract boolean insert(final String tableName, final Date date, final State state);

    protected abstract boolean update(final String tableName, final Date date, final State state);
}
