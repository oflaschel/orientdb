package com.orientechnologies.orient.core.storage.impl.memory;

import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrey Lomakin <a href="mailto:lomakin.andrey@gmail.com">Andrey Lomakin</a>
 * @since 6/25/14
 */
public class OMemoryWriteAheadLog extends OAbstractWriteAheadLog {
  private long             counter = 0;
  private List<OWALRecord> records = new ArrayList<OWALRecord>();

  @Override
  public OLogSequenceNumber begin() throws IOException {
    synchronized (syncObject) {
      if (records.isEmpty())
        return null;

      return records.get(0).getLsn();
    }
  }

  @Override
  public OLogSequenceNumber end() throws IOException {
    synchronized (syncObject) {
      if (records.isEmpty())
        return null;

      return records.get(records.size() - 1).getLsn();
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public OLogSequenceNumber log(OWALRecord record) throws IOException {
    OLogSequenceNumber logSequenceNumber;
    synchronized (syncObject) {
      logSequenceNumber = new OLogSequenceNumber(0, counter);
      counter++;

      if (record instanceof OAtomicUnitStartRecord)
        records.clear();

      records.add(record);
      record.setLsn(logSequenceNumber);
    }

    return logSequenceNumber;
  }

  @Override
  public void truncate() throws IOException {
    synchronized (syncObject) {
      records.clear();
    }
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public void close(boolean flush) throws IOException {
  }

  @Override
  public void delete() throws IOException {
    truncate();
  }

  @Override
  public void delete(boolean flush) throws IOException {
    truncate();
  }

  @Override
  public OWALRecord read(OLogSequenceNumber lsn) throws IOException {
    synchronized (syncObject) {
      if (records.isEmpty())
        return null;

      final long index = lsn.getPosition() - records.get(0).getLsn().getPosition();
      if (index < 0 || index >= records.size())
        return null;

      return records.get((int) index);
    }
  }

  @Override
  public OLogSequenceNumber next(OLogSequenceNumber lsn) throws IOException {
    synchronized (syncObject) {
      if (records.isEmpty())
        return null;

      final long index = lsn.getPosition() - records.get(0).getLsn().getPosition() + 1;
      if (index < 0 || index >= records.size())
        return null;

      return new OLogSequenceNumber(0, lsn.getPosition() + 1);
    }
  }

  @Override
  public OLogSequenceNumber getFlushedLSN() {
    return new OLogSequenceNumber(Long.MAX_VALUE, Long.MAX_VALUE);
  }

  @Override
  public void cutTill(OLogSequenceNumber lsn) throws IOException {
    synchronized (syncObject) {
      if (records.isEmpty())
        return;

      long index = records.get(0).getLsn().getPosition() - lsn.getPosition();
      if (index < 0)
        return;

      if (index > records.size())
        index = records.size();

      for (int i = 0; i < index; i++)
        records.remove(0);
    }
  }
}