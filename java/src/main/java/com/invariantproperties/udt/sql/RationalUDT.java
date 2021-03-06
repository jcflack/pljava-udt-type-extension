/*
 * This code was written by Bear Giles <bgiles@coyotesong.com>and he
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Any contributions made by others are licensed to this project under
 * one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Copyright (c) 2012 Bear Giles <bgiles@coyotesong.com>
 */
package com.invariantproperties.udt.sql;

import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.ResourceBundle;

import com.invariantproperties.udt.Rational;

/**
 * Glue that allows Rational numbers to be stored as user-defined types in
 * database.
 * 
 * Design note: this class uses composition, not inheritance, since it should
 * only be used as a container to persist and retrieve a Rational value.
 * 
 * @author bgiles@coyotesong.com
 */
public class RationalUDT implements SQLData {
    private static final ResourceBundle bundle = ResourceBundle
            .getBundle(ComplexUDT.class.getName());
    private static final String TYPE_NAME = bundle.getString("typeName");
    private static final int NULL_POSITION = 1;
    private Rational value;
    private String typeName;

    /**
     * Parse input string.
     */
    public static RationalUDT parse(String input, String typeName)
            throws SQLException {
        // TODO: verify recognized typename.
        Rational value = null;
        try {
            value = Rational.parse(input);
        } catch (IllegalArgumentException e) {
            throw new SQLException(e);
        }
        return new RationalUDT(value);
    }

    /**
     * Constructor taking only numerator.
     * 
     * @param numerator
     * @throws SQLException
     */
    public RationalUDT(long numerator) throws SQLException {
        this(numerator, 1);
    }

    /**
     * Constructor taking numerator and denominator.
     * 
     * @param numerator
     * @param denominator
     * @throws SQLException
     */
    public RationalUDT(long numerator, long denominator) throws SQLException {
        this(new Rational(numerator, denominator));
    }

    /**
     * Constructor taking numerator, denominator and type name.
     * 
     * @param numerator
     * @param denominator
     * @param typeName
     * @throws SQLException
     */
    public RationalUDT(long numerator, long denominator, String typeName)
            throws SQLException {
        this(new Rational(numerator, denominator));
    }

    /**
     * Constructor
     * 
     * @param p
     * @throws SQLException
     */
    protected RationalUDT(Rational p) throws SQLException {
        this(p, TYPE_NAME);
    }

    /**
     * Constructor
     * 
     * @param p
     * @throws SQLException
     */
    protected RationalUDT(Rational p, String typeName) throws SQLException {
        this.value = p;
        this.typeName = typeName;
    }

    /**
     * Get SQL Type Name.
     */
    public String getSQLTypeName() {
        return typeName;
    }

    /**
     * Read object from SQLInput stream.
     */
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        long n = stream.readLong();
        long d = stream.readLong();
        this.value = new Rational(n, d);
        this.typeName = typeName;
    }

    /**
     * Write object to SQLOutput stream.
     */
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeLong(value.getNumerator());
        stream.writeLong(value.getDenominator());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (value == null) ? 0 : value.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if ((value == null) || (o == null)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof RationalUDT)) {
            return false;
        }

        RationalUDT r = (RationalUDT) o;
        if (r.value == null) {
            return false;
        }

        return value.equals(r.value);
    }

    /**
     * Return string representing value. N.B., the toString() contract
     * says that this method should never return null but the SQL contract
     * says that a null object should return a null value. The UDT can
     * never be null, of course, but it might wrap an uninitialized value.
     * Therefore this method breaks the standard java contract and follows
     * the SQL contract.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return (value == null) ? null : value.toString();
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static int compare(RationalUDT p, RationalUDT q) {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return -NULL_POSITION;
        }
        return p.value.compareTo(q.value);
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean lessThan(RationalUDT p, RationalUDT q) {
        return compare(p, q) < 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean lessThanOrEquals(RationalUDT p, RationalUDT q) {
        return compare(p, q) <= 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean equals(RationalUDT p, RationalUDT q) {
        return compare(p, q) == 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean notEquals(RationalUDT p, RationalUDT q) {
        return !equals(p, q);
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean greaterThanOrEquals(RationalUDT p, RationalUDT q) {
        return lessThanOrEquals(q, p);
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean greaterThan(RationalUDT p, RationalUDT q) {
        return lessThan(q, p);
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean lessThan(RationalUDT p, double q) {
        if ((p == null) || (p.value == null)) {
            return false;
        }
        return p.value.compareTo(q) < 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean lessThanOrEquals(RationalUDT p, double q) {
        if ((p == null) || (p.value == null)) {
            return false;
        }
        return p.value.compareTo(q) <= 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean equals(RationalUDT p, double q) {
        if ((p == null) || (p.value == null)) {
            return false;
        }
        return p.value.compareTo(q) == 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean greaterThanOrEquals(RationalUDT p, double q) {
        if ((p == null) || (p.value == null)) {
            return true;
        }
        return p.value.compareTo(q) >= 0;
    }

    /**
     * Static comparison method that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     */
    public static boolean greaterThan(RationalUDT p, double q) {
        if ((p == null) || (p.value == null)) {
            return true;
        }
        return p.value.compareTo(q) > 0;
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param input
     * @return
     * @throws SQLException
     */
    public static RationalUDT newInstance(String input) throws SQLException {
        if (input == null) {
            return null;
        }
        return parse(input, TYPE_NAME);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    public static RationalUDT newInstance(int value) throws SQLException {
        return new RationalUDT(value);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    public static RationalUDT newInstance(Integer value) throws SQLException {
        if (value == null) {
            return null;
        }
        return new RationalUDT(value.longValue());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    public static RationalUDT newInstance(long value) throws SQLException {
        return new RationalUDT(value);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    public static RationalUDT newInstance(Long value) throws SQLException {
        if (value == null) {
            return null;
        }
        return new RationalUDT(value.longValue());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */

    public static Double value(RationalUDT p) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return p.value.doubleValue();
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    public static RationalUDT min(RationalUDT p, RationalUDT q) {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return RationalUDT.lessThanOrEquals(p, q) ? p : q;
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    public static RationalUDT max(RationalUDT p, RationalUDT q) {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return RationalUDT.greaterThanOrEquals(p, q) ? p : q;
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @return
     * @throws SQLException
     */
    public static RationalUDT negate(RationalUDT p) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return new RationalUDT(p.value.negate());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    public static RationalUDT add(RationalUDT p, RationalUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return new RationalUDT(p.value.add(q.value));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    public static RationalUDT subtract(RationalUDT p, RationalUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return new RationalUDT(p.value.subtract(q.value));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    public static RationalUDT multiply(RationalUDT p, RationalUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return new RationalUDT(p.value.multiply(q.value));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    public static RationalUDT divide(RationalUDT p, RationalUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        if (q.value.getNumerator() == 0 && q.value.getDenominator() == 0) {
            throw new SQLException("attempt to divide by zero.");
        }
        return new RationalUDT(p.value.divide(q.value));
    }
}
