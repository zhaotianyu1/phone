/*
 * Copyright (C)  Tony Green, LitePal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tclphone.litepal.crud;



import com.example.tclphone.litepal.LitePal;
import com.example.tclphone.litepal.crud.async.AverageExecutor;
import com.example.tclphone.litepal.crud.async.CountExecutor;
import com.example.tclphone.litepal.crud.async.FindExecutor;
import com.example.tclphone.litepal.crud.async.FindMultiExecutor;
import com.example.tclphone.litepal.tablemanager.Connector;
import com.example.tclphone.litepal.util.BaseUtility;
import com.example.tclphone.litepal.util.DBUtility;

import java.util.List;

/**
 * Allows developers to query tables with cluster style.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class ClusterQuery {

	/**
	 * Representing the selected columns in SQL.
	 */
	String[] mColumns;

	/**
	 * Representing the where clause in SQL.
	 */
	String[] mConditions;

	/**
	 * Representing the order by clause in SQL.
	 */
	String mOrderBy;

	/**
	 * Representing the limit clause in SQL.
	 */
	String mLimit;

	/**
	 * Representing the offset in SQL.
	 */
	String mOffset;

	/**
	 * Do not allow to create instance by developers.
	 */
	ClusterQuery() {
	}

	/**
	 * Declaring to query which columns in table.
	 * 
	 * <pre>
	 * DataSupport.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find all rows with name and age columns in Person table.
	 * 
	 * @param columns
	 *            A String array of which columns to return. Passing null will
	 *            return all columns.
	 * 
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery select(String... columns) {
		mColumns = columns;
		return this;
	}

	/**
	 * Declaring to query which rows in table.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find rows which name is Tom or age greater than 14 in Person
	 * table.
	 * 
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery where(String... conditions) {
		mConditions = conditions;
		return this;
	}

	/**
	 * Declaring how to order the rows queried from table.
	 * 
	 * <pre>
	 * DataSupport.order(&quot;name desc&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find all rows in Person table sorted by name with inverted
	 * order.
	 * 
	 * @param column
	 *            How to order the rows, formatted as an SQL ORDER BY clause.
	 *            Passing null will use the default sort order, which may be
	 *            unordered.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery order(String column) {
		mOrderBy = column;
		return this;
	}

	/**
	 * Limits the number of rows returned by the query.
	 * 
	 * <pre>
	 * DataSupport.limit(2).find(Person.class);
	 * </pre>
	 * 
	 * This will find the top 2 rows in Person table.
	 * 
	 * @param value
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery limit(int value) {
		mLimit = String.valueOf(value);
		return this;
	}

	/**
	 * Declaring the offset of rows returned by the query. This method must be
	 * used with {@link #limit(int)}, or nothing will return.
	 * 
	 * <pre>
	 * DataSupport.limit(1).offset(2).find(Person.class);
	 * </pre>
	 * 
	 * This will find the third row in Person table.
	 * 
	 * @param value
	 *            The offset amount of rows returned by the query.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery offset(int value) {
		mOffset = String.valueOf(value);
		return this;
	}

	/**
	 * Finds multiple records by the cluster parameters. You can use the below
	 * way to finish a complicated query:
	 * 
	 * <pre>
	 * DataSupport.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(1).offset(2)
	 * 		.find(Person.class);
	 * </pre>
	 * 
	 * You can also do the same job with SQLiteDatabase like this:
	 * 
	 * <pre>
	 * getSQLiteDatabase().query(&quot;Person&quot;, &quot;name&quot;, &quot;age &gt; ?&quot;, new String[] { &quot;14&quot; }, null, null, &quot;age&quot;,
	 * 		&quot;2,1&quot;);
	 * </pre>
	 * 
	 * Obviously, the first way is much more semantic.<br>
	 * Note that the associated models won't be loaded by default considering
	 * the efficiency, but you can do that by using
ss, boolean)}.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @return An object list with founded data from database, or an empty list.
	 */
	public <T> List<T> find(Class<T> modelClass) {
		return find(modelClass, false);
	}

    /**
     * Basically same as {@link #find(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @return A FindMultiExecutor instance.
     */
    public <T> FindMultiExecutor findAsync(final Class<T> modelClass) {
        return findAsync(modelClass, false);
    }

	/**
	 * It is mostly same as {@l
	 * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object list with founded data from database, or an empty list.
	 */
	public synchronized <T> List<T> find(Class<T> modelClass, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		String limit;
		if (mOffset == null) {
			limit = mLimit;
		} else {
			if (mLimit == null) {
				mLimit = "0";
			}
			limit = mOffset + "," + mLimit;
		}
		return queryHandler.onFind(modelClass, mColumns, mConditions, mOrderBy, limit, isEager);
	}

    /**
     * Basically same as {@link #find(Class, boolean)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @param isEager
     *            True to load the associated models, false not.
     * @return A FindMultiExecutor instance.
     */
    public <T> FindMultiExecutor findAsync(final Class<T> modelClass, final boolean isEager) {
        final FindMultiExecutor executor = new FindMultiExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final List<T> t = find(modelClass, isEager);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    /**
     * Finds the first record by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * DataSupport.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(1).offset(2)
     * 		.findFirst(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@linean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with founded data from database, or null.
     */
    public <T> T findFirst(Class<T> modelClass) {
        return findFirst(modelClass, false);
    }

    /**
     * Basically same as {@link #findFirst(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    /**
     * It is mostly same as {@link} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with founded data from database, or null.
     */
    public <T> T findFirst(Class<T> modelClass, boolean isEager) {
        List<T> list = find(modelClass, isEager);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Basically same as {@link #findFirst(Class, boolean)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = findFirst(modelClass, isEager);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    /**
     * Finds the last record by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * DataSupport.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(1).offset(2)
     * 		.findLast(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@li
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with founded data from database, or null.
     */
    public <T> T findLast(Class<T> modelClass) {
        return findLast(modelClass, false);
    }

    /**
     * Basically same as {@link #findLast(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    /**
     * It is mostly same as {@linsEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with founded data from database, or null.
     */
    public <T> T findLast(Class<T> modelClass, boolean isEager) {
        List<T> list = find(modelClass, isEager);
        int size = list.size();
        if (size > 0) {
            return list.get(size - 1);
        }
        return null;
    }

    /**
     * Basically same as {@link #findLast(Class, boolean)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = findLast(modelClass, isEager);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

	/**
	 * Count the records.
	 *
	 * <pre>
	 * DataSupport.count(Person.class);
	 * </pre>
	 *
	 * This will count all rows in person table.<br>
	 * You can also specify a where clause when counting.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(Person.class);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @return Count of the specified table.
	 */
	public synchronized int count(Class<?> modelClass) {
		return count(BaseUtility.changeCase(modelClass.getSimpleName()));
	}

    /**
     * Basically same as {@link #count(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @return A CountExecutor instance.
     */
    public CountExecutor countAsync(Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

	/**
	 * Count the records.
	 *
	 * <pre>
	 * DataSupport.count(&quot;person&quot;);
	 * </pre>
	 *
	 * This will count all rows in person table.<br>
	 * You can also specify a where clause when counting.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(&quot;person&quot;);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @return Count of the specified table.
	 */
	public synchronized int count(String tableName) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onCount(tableName, mConditions);
	}

    /**
     * Basically same as {@link #count(String)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @return A CountExecutor instance.
     */
    public CountExecutor countAsync(final String tableName) {
        final CountExecutor executor = new CountExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int count = count(tableName);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(count);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

	/**
	 * Calculates the average value on a given column.
	 *
	 * <pre>
	 * DataSupport.average(Person.class, &quot;age&quot;);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(Person.class, &quot;age&quot;);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param column
	 *            The based on column to calculate.
	 * @return The average value on a given column.
	 */
	public synchronized double average(Class<?> modelClass, String column) {
		return average(BaseUtility.changeCase(modelClass.getSimpleName()), column);
	}

    /**
     * Basically same as {@link #average(Class, String)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param column
     *            The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    public AverageExecutor averageAsync(final Class<?> modelClass, final String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

	/**
	 * Calculates the average value on a given column.
	 *
	 * <pre>
	 * DataSupport.average(&quot;person&quot;, &quot;age&quot;);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(&quot;person&quot;, &quot;age&quot;);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @return The average value on a given column.
	 */
	public synchronized double average(String tableName, String column) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onAverage(tableName, column, mConditions);
	}

    /**
     * Basically same as {@link #average(String, String)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param column
     *            The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    public AverageExecutor averageAsync(final String tableName, final String column) {
        final AverageExecutor executor = new AverageExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final double average = average(tableName, column);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(average);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

	/**
	 * Calculates the maximum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * DataSupport.max(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
	public synchronized <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
		return max(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

    /**
     * Basically same as {@link #max(Class, String, Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor maxAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

	/**
	 * Calculates the maximum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * DataSupport.max(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
	public synchronized <T> T max(String tableName, String columnName, Class<T> columnType) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onMax(tableName, columnName, mConditions, columnType);
	}

    /**
     * Basically same as {@link #max(String, String, Class)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = max(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

	/**
	 * Calculates the minimum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * DataSupport.min(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
	public synchronized <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
		return min(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

    /**
     * Basically same as {@link #min(Class, String, Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor minAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

	/**
	 * Calculates the minimum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * DataSupport.min(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
	public synchronized <T> T min(String tableName, String columnName, Class<T> columnType) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onMin(tableName, columnName, mConditions, columnType);
	}

    /**
     * Basically same as {@link #min(String, String, Class)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = min(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

	/**
	 * Calculates the sum of values on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.sum(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
	public synchronized <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
		return sum(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

    /**
     * Basically same as {@link #sum(Class, String, Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor sumAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
	 * Calculates the sum of values on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.sum(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
	public synchronized <T> T sum(String tableName, String columnName, Class<T> columnType) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onSum(tableName, columnName, mConditions, columnType);
	}

    /**
     * Basically same as {@link #sum(String, String, Class)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public <T> FindExecutor sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = sum(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

}