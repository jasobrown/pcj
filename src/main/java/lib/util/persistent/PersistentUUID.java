/* Copyright (C) 2017  Intel Corporation
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 only, as published by the Free Software Foundation.
 * This file has been designated as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied
 * this code.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License version 2 for more details (a copy
 * is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package lib.util.persistent;

import lib.util.persistent.types.*;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A class that represents an immutable universally unique identifier (UUID).
 * A UUID represents a 128-bit value.
 *
 * <p> There exist different variants of these global identifiers.  The methods
 * of this class are for manipulating the Leach-Salz variant, although the
 * constructors allow the creation of any variant of UUID (described below).
 *
 * <p> The layout of a variant 2 (Leach-Salz) UUID is as follows:
 *
 * The most significant long consists of the following unsigned fields:
 * <pre>
 * 0xFFFFFFFF00000000 time_low
 * 0x00000000FFFF0000 time_mid
 * 0x000000000000F000 version
 * 0x0000000000000FFF time_hi
 * </pre>
 * The least significant long consists of the following unsigned fields:
 * <pre>
 * 0xC000000000000000 variant
 * 0x3FFF000000000000 clock_seq
 * 0x0000FFFFFFFFFFFF node
 * </pre>
 *
 * <p> The variant field contains a value which identifies the layout of the
 * {@code UUID}.  The bit layout described above is valid only for a {@code
 * UUID} with a variant value of 2, which indicates the Leach-Salz variant.
 *
 * <p> The version field holds a value that describes the type of this {@code
 * UUID}.  There are four different basic types of UUIDs: time-based, DCE
 * security, name-based, and randomly generated UUIDs.  These types have a
 * version value of 1, 2, 3 and 4, respectively.
 *
 * <p> For more information including algorithms used to create {@code UUID}s,
 * see <a href="http://www.ietf.org/rfc/rfc4122.txt"> <i>RFC&nbsp;4122: A
 * Universally Unique IDentifier (UUID) URN Namespace</i></a>, section 4.2
 * &quot;Algorithms for Creating a Time-Based UUID&quot;.
 *
 * @since   1.5
 */
public final class PersistentUUID extends PersistentImmutableObject implements Comparable<PersistentUUID>, EquatesWith<UUID> {

    // TODO: Update comments to add details for PersistentUUID class instead of UUID
    /*
    * The most significant 64 bits of this UUID.
    *
    * @serial
    */
    private static final LongField MOST_SIG_BITS = new LongField();

    /*
    * The least significant 64 bits of this UUID.
    *
    * @serial
    */
    private static final LongField LEAST_SIG_BITS = new LongField();

    private static final ObjectType<PersistentUUID> TYPE = ObjectType.fromFields(PersistentUUID.class, MOST_SIG_BITS, LEAST_SIG_BITS);

    public PersistentUUID() {
        super(TYPE);
    }

    public PersistentUUID(ObjectPointer<? extends PersistentUUID> pointer) {
        super(pointer);
    }

    /*
     * The random number generator used by this class to create random
     * based UUIDs. In a holder class to defer initialization until needed.
     */
    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    // Constructors and Factories

    /*
     * Private constructor which uses a byte array to construct the new UUID.
     */
    private PersistentUUID(byte[] data) {
        super(TYPE, (PersistentImmutableObject obj) -> {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
            obj.initLongField(MOST_SIG_BITS, msb);
            obj.initLongField(LEAST_SIG_BITS, lsb);
        });
    }

    /**
     * Constructs a new {@code UUID} using the specified data.  {@code
     * mostSigBits} is used for the most significant 64 bits of the {@code
     * UUID} and {@code leastSigBits} becomes the least significant 64 bits of
     * the {@code UUID}.
     *
     * @param  mostSigBits
     *         The most significant bits of the {@code UUID}
     *
     * @param  leastSigBits
     *         The least significant bits of the {@code UUID}
     */
    public PersistentUUID(long mostSigBits, long leastSigBits) {
        super(TYPE, (PersistentImmutableObject obj) -> {
            obj.initLongField(MOST_SIG_BITS, mostSigBits);
            obj.initLongField(LEAST_SIG_BITS, leastSigBits);
        });
    }

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID.
     *
     * The {@code UUID} is generated using a cryptographically strong pseudo
     * random number generator.
     *
     * @return  A randomly generated {@code UUID}
     */
    public static PersistentUUID randomUUID() {
        SecureRandom ng = Holder.numberGenerator;

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6]  &= 0x0f;  /* clear version        */
        randomBytes[6]  |= 0x40;  /* set to version 4     */
        randomBytes[8]  &= 0x3f;  /* clear variant        */
        randomBytes[8]  |= 0x80;  /* set to IETF variant  */
        return new PersistentUUID(randomBytes);
    }

    /**
     * Static factory to retrieve a type 3 (name based) {@code UUID} based on
     * the specified byte array.
     *
     * @param  name
     *         A byte array to be used to construct a {@code UUID}
     *
     * @return  A {@code UUID} generated from the specified array
     */
    public static PersistentUUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported", nsae);
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6]  &= 0x0f;  /* clear version        */
        md5Bytes[6]  |= 0x30;  /* set to version 3     */
        md5Bytes[8]  &= 0x3f;  /* clear variant        */
        md5Bytes[8]  |= 0x80;  /* set to IETF variant  */
        return new PersistentUUID(md5Bytes);
    }

    /**
     * Creates a {@code UUID} from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  name
     *         A string that specifies a {@code UUID}
     *
     * @return  A {@code UUID} with the specified value
     *
     * @throws  IllegalArgumentException
     *          If name does not conform to the string representation as
     *          described in {@link #toString}
     *
     */
    public static PersistentUUID fromString(String name) {
        String[] components = name.split("-");
        if (components.length != 5)
            throw new IllegalArgumentException("Invalid UUID string: "+name);
        for (int i=0; i<5; i++)
            components[i] = "0x"+components[i];

        long mostSigBits = Long.decode(components[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]).longValue();

        long leastSigBits = Long.decode(components[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]).longValue();

        return new PersistentUUID(mostSigBits, leastSigBits);
    }

    // Field Accessor Methods

    /**
     * Returns the least significant 64 bits of this UUID's 128 bit value.
     *
     * @return  The least significant 64 bits of this UUID's 128 bit value
     */
    public long getLeastSignificantBits() {
        return getLongField(LEAST_SIG_BITS);
    }

    /**
     * Returns the most significant 64 bits of this UUID's 128 bit value.
     *
     * @return  The most significant 64 bits of this UUID's 128 bit value
     */
    public long getMostSignificantBits() {
        return getLongField(MOST_SIG_BITS);
    }

    /**
     * The version number associated with this {@code UUID}.  The version
     * number describes how this {@code UUID} was generated.
     *
     * The version number has the following meaning:
     * <ul>
     * <li>1    Time-based UUID
     * <li>2    DCE security UUID
     * <li>3    Name-based UUID
     * <li>4    Randomly generated UUID
     * </ul>
     *
     * @return  The version number of this {@code UUID}
     */
    public int version() {
        // Version is bits masked by 0x000000000000F000 in MS long
        return (int)((getLongField(MOST_SIG_BITS) >> 12) & 0x0f);
    }

    /**
     * The variant number associated with this {@code UUID}.  The variant
     * number describes the layout of the {@code UUID}.
     *
     * The variant number has the following meaning:
     * <ul>
     * <li>0    Reserved for NCS backward compatibility
     * <li>2    <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a>
     * (Leach-Salz), used by this class
     * <li>6    Reserved, Microsoft Corporation backward compatibility
     * <li>7    Reserved for future definition
     * </ul>
     *
     * @return  The variant number of this {@code UUID}
     */
    public int variant() {
        // This field is composed of a varying number of bits.
        // 0    -    -    Reserved for NCS backward compatibility
        // 1    0    -    The IETF aka Leach-Salz variant (used by this class)
        // 1    1    0    Reserved, Microsoft backward compatibility
        // 1    1    1    Reserved for future definition.
        return (int) ((getLongField(LEAST_SIG_BITS) >>> (64 - (getLongField(LEAST_SIG_BITS) >>> 62)))
                & (getLongField(LEAST_SIG_BITS) >> 63));
    }

    /**
     * The timestamp value associated with this UUID.
     *
     * <p> The 60 bit timestamp value is constructed from the time_low,
     * time_mid, and time_hi fields of this {@code UUID}.  The resulting
     * timestamp is measured in 100-nanosecond units since midnight,
     * October 15, 1582 UTC.
     *
     * <p> The timestamp value is only meaningful in a time-based UUID, which
     * has version type 1.  If this {@code UUID} is not a time-based UUID then
     * this method throws UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException
     *         If this UUID is not a version 1 UUID
     * @return The timestamp of this {@code UUID}.
     */
    public long timestamp() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }

        return (getLongField(MOST_SIG_BITS) & 0x0FFFL) << 48
                | ((getLongField(MOST_SIG_BITS) >> 16) & 0x0FFFFL) << 32
                | getLongField(MOST_SIG_BITS) >>> 32;
    }

    /**
     * The clock sequence value associated with this UUID.
     *
     * <p> The 14 bit clock sequence value is constructed from the clock
     * sequence field of this UUID.  The clock sequence field is used to
     * guarantee temporal uniqueness in a time-based UUID.
     *
     * <p> The {@code clockSequence} value is only meaningful in a time-based
     * UUID, which has version type 1.  If this UUID is not a time-based UUID
     * then this method throws UnsupportedOperationException.
     *
     * @return  The clock sequence of this {@code UUID}
     *
     * @throws  UnsupportedOperationException
     *          If this UUID is not a version 1 UUID
     */
    public int clockSequence() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }

        return (int)((getLongField(LEAST_SIG_BITS) & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * The node value associated with this UUID.
     *
     * <p> The 48 bit node value is constructed from the node field of this
     * UUID.  This field is intended to hold the IEEE 802 address of the machine
     * that generated this UUID to guarantee spatial uniqueness.
     *
     * <p> The node value is only meaningful in a time-based UUID, which has
     * version type 1.  If this UUID is not a time-based UUID then this method
     * throws UnsupportedOperationException.
     *
     * @return  The node value of this {@code UUID}
     *
     * @throws  UnsupportedOperationException
     *          If this UUID is not a version 1 UUID
     */
    public long node() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }

        return getLongField(LEAST_SIG_BITS) & 0x0000FFFFFFFFFFFFL;
    }

    // Object Inherited Methods

    /**
     * Returns a {@code String} object representing this {@code UUID}.
     *
     * <p> The UUID string representation is as described by this BNF:
     * <blockquote><pre>
     * {@code
     * UUID                   = <time_low> "-" <time_mid> "-"
     *                          <time_high_and_version> "-"
     *                          <variant_and_sequence> "-"
     *                          <node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               =
     *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     *       | "a" | "b" | "c" | "d" | "e" | "f"
     *       | "A" | "B" | "C" | "D" | "E" | "F"
     * }</pre></blockquote>
     *
     * @return  A string representation of this {@code UUID}
     */
    public String toString() {
        return (digits(getLongField(MOST_SIG_BITS) >> 32, 8) + "-" +
                digits(getLongField(MOST_SIG_BITS) >> 16, 4) + "-" +
                digits(getLongField(MOST_SIG_BITS), 4) + "-" +
                digits(getLongField(LEAST_SIG_BITS) >> 48, 4) + "-" +
                digits(getLongField(LEAST_SIG_BITS), 12));
    }

    /** Returns val represented by the specified number of hex digits. */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * Returns a hash code for this {@code UUID}.
     *
     * @return  A hash code value for this {@code UUID}
     */
    public int hashCode() {
        long hilo = getLongField(MOST_SIG_BITS) ^ getLongField(LEAST_SIG_BITS);
        return ((int)(hilo >> 32)) ^ (int) hilo;
    }

    /**
     * Compares this object to the specified object.  The result is {@code
     * true} if and only if the argument is not {@code null}, is a {@code UUID}
     * object, has the same variant, and contains the same value, bit for bit,
     * as this {@code UUID}.
     *
     * @param  obj
     *         The object to be compared
     *
     * @return  {@code true} if the objects are the same; {@code false}
     *          otherwise
     */
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != PersistentUUID.class))
            return false;
        PersistentUUID id = (PersistentUUID)obj;
        return (getLongField(MOST_SIG_BITS) == id.getMostSignificantBits() &&
                getLongField(LEAST_SIG_BITS) == id.getLeastSignificantBits());
    }

    // Comparison Operations

    /**
     * Compares this UUID with the specified UUID.
     *
     * <p> The first of two UUIDs is greater than the second if the most
     * significant field in which the UUIDs differ is greater for the first
     * UUID.
     *
     * @param  val
     *         {@code UUID} to which this {@code UUID} is to be compared
     *
     * @return  -1, 0 or 1 as this {@code UUID} is less than, equal to, or
     *          greater than {@code val}
     *
     */
    public int compareTo(PersistentUUID val) {
        // The ordering is intentionally set up so that the UUIDs
        // can simply be numerically compared as two numbers
        return (this.getMostSignificantBits() < val.getMostSignificantBits() ? -1 :
                (this.getMostSignificantBits() > val.getMostSignificantBits() ? 1 :
                        (this.getLeastSignificantBits() < val.getLeastSignificantBits() ? -1 :
                                (this.getLeastSignificantBits() > val.getLeastSignificantBits() ? 1 :
                                        0))));
    }

    public int equivalentHash() {
        return hashCode(); 
    }

    public boolean equatesWith(UUID that) {
        return (getLongField(MOST_SIG_BITS) == that.getMostSignificantBits() &&
                getLongField(LEAST_SIG_BITS) == that.getLeastSignificantBits());
    }
}
