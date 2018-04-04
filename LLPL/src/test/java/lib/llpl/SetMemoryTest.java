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

public class SetMemoryTest {
    public static void main(String[] args) {
        Heap h = Heap.getHeap("/mnt/mem/persistent_pool", 2147483648L);

        MemoryRegion rmr = h.allocateMemoryRegion(MemoryRegion.Kind.RAW, 120);
        MemoryRegion fmr = h.allocateMemoryRegion(MemoryRegion.Kind.FLUSHABLE, 120);
        MemoryRegion tmr = h.allocateMemoryRegion(MemoryRegion.Kind.TRANSACTIONAL, 120);

        h.setMemory(rmr, (byte)0x44, 10, 50);
        for (int i = 0; i < 50; i++) {
            assert(rmr.getByte(10 + i) == (byte)0x44);
        }

        assert(fmr.isFlushed() == true);
        h.setMemory(fmr, (byte)0x88, 30, 50);
        assert(fmr.isFlushed() == false);
        for (int i = 0; i < 50; i++) {
            assert(fmr.getByte(30 + i) == (byte)0x88);
        }

        h.setMemory(tmr, (byte)0xcc, 50, 50);
        for (int i = 0; i < 50; i++) {
            assert(tmr.getByte(50 + i) == (byte)0xcc);
        }
        System.out.println("=================================All SetMemory tests passed==================================");
    }
}