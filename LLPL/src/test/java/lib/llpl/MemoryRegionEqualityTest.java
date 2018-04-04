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

package lib.llpl;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

class MemoryRegionEqualityTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);
        MemoryRegion mr = h.allocateMemoryRegion(MemoryRegion.Kind.RAW, 10);
        assert(mr.addr() != 0);
        MemoryRegion mr2 = h.memoryRegionFromAddress(MemoryRegion.Kind.RAW, mr.addr());
        assert(mr.addr() == mr2.addr());
        assert(mr.equals(mr2));

        HashMap<MemoryRegion, Integer> hm = new HashMap<>();
        assert(hm.size() == 0);
        hm.put(mr, 1);
        hm.put(mr2, 2);
        assert(hm.size() == 1);
        assert(hm.get(mr) == 2);

        TreeMap<MemoryRegion, Integer> tm = new TreeMap<>();
        assert(tm.size() == 0);
        tm.put(mr2, 2);
        tm.put(mr, 1);
        assert(tm.size() == 1);
        assert(tm.get(mr2) == 1);

        h.freeMemoryRegion(mr);

        mr = h.allocateMemoryRegion(MemoryRegion.Kind.FLUSHABLE, 10);
        assert(mr.addr() != 0);
        mr2 = h.memoryRegionFromAddress(MemoryRegion.Kind.FLUSHABLE, mr.addr());
        assert(mr.addr() == mr2.addr());
        assert(mr.equals(mr2));

        hm = new HashMap<>();
        assert(hm.size() == 0);
        hm.put(mr, 1);
        hm.put(mr2, 2);
        assert(hm.size() == 1);
        assert(hm.get(mr) == 2);

        tm = new TreeMap<>();
        assert(tm.size() == 0);
        tm.put(mr2, 2);
        tm.put(mr, 1);
        assert(tm.size() == 1);
        assert(tm.get(mr2) == 1);

        h.freeMemoryRegion(mr);

        mr = h.allocateMemoryRegion(MemoryRegion.Kind.TRANSACTIONAL, 10);
        assert(mr.addr() != 0);
        mr2 = h.memoryRegionFromAddress(MemoryRegion.Kind.TRANSACTIONAL, mr.addr());
        assert(mr.addr() == mr2.addr());
        assert(mr.equals(mr2));

        hm = new HashMap<>();
        assert(hm.size() == 0);
        hm.put(mr, 1);
        hm.put(mr2, 2);
        assert(hm.size() == 1);
        assert(hm.get(mr) == 2);

        tm = new TreeMap<>();
        assert(tm.size() == 0);
        tm.put(mr2, 2);
        tm.put(mr, 1);
        assert(tm.size() == 1);
        assert(tm.get(mr2) == 1);

        h.freeMemoryRegion(mr);

        System.out.println("=================================All MemoryRegionEquality tests passed=======================");
    }
}
