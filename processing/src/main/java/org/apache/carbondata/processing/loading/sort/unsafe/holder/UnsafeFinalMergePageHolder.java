/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.carbondata.processing.loading.sort.unsafe.holder;

import org.apache.carbondata.common.logging.LogService;
import org.apache.carbondata.common.logging.LogServiceFactory;
import org.apache.carbondata.processing.loading.row.IntermediateSortTempRow;
import org.apache.carbondata.processing.loading.sort.unsafe.UnsafeCarbonRowPage;
import org.apache.carbondata.processing.loading.sort.unsafe.merger.UnsafeInMemoryIntermediateDataMerger;
import org.apache.carbondata.processing.sort.sortdata.IntermediateSortTempRowComparator;

public class UnsafeFinalMergePageHolder implements SortTempChunkHolder {

  private static final LogService LOGGER =
      LogServiceFactory.getLogService(UnsafeFinalMergePageHolder.class.getName());

  private int counter;

  private int actualSize;

  private long[] mergedAddresses;

  private byte[] rowPageIndexes;

  private UnsafeCarbonRowPage[] rowPages;

  private IntermediateSortTempRowComparator comparator;

  private IntermediateSortTempRow currentRow;

  public UnsafeFinalMergePageHolder(UnsafeInMemoryIntermediateDataMerger merger,
      boolean[] noDictSortColumnMapping) {
    this.actualSize = merger.getEntryCount();
    this.mergedAddresses = merger.getMergedAddresses();
    this.rowPageIndexes = merger.getRowPageIndexes();
    this.rowPages = merger.getUnsafeCarbonRowPages();
    LOGGER.info("Processing unsafe inmemory rows page with size : " + actualSize);
    this.comparator = new IntermediateSortTempRowComparator(noDictSortColumnMapping);
  }

  public boolean hasNext() {
    if (counter < actualSize) {
      return true;
    }
    return false;
  }

  public void readRow() {
    currentRow = rowPages[rowPageIndexes[counter]].getRow(mergedAddresses[counter]);
    counter++;
  }

  public IntermediateSortTempRow getRow() {
    return currentRow;
  }

  @Override public int compareTo(SortTempChunkHolder o) {
    return comparator.compare(currentRow, o.getRow());
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof UnsafeFinalMergePageHolder)) {
      return false;
    }

    UnsafeFinalMergePageHolder o = (UnsafeFinalMergePageHolder) obj;
    return this == o;
  }

  @Override public int hashCode() {
    return super.hashCode();
  }

  public int numberOfRows() {
    return actualSize;
  }

  public void close() {
    for (int i = 0; i < rowPages.length; i++) {
      rowPages[i].freeMemory();
    }
  }
}
