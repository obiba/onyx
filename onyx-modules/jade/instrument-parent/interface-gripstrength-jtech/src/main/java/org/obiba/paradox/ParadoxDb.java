/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.paradox;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.obiba.paradox.format.Int;
import org.obiba.paradox.format.Offset;
import org.obiba.paradox.format.Ptr;
import org.obiba.paradox.format.PxField;
import org.obiba.paradox.format.Short;
import org.obiba.paradox.format.UByte;
import org.obiba.paradox.format.UShort;

public class ParadoxDb implements Closeable, Iterable<ParadoxRecord> {

  private final RandomAccessFile dbFile;

  private final ParadoxDbHeader header;

  private final List<ParadoxDbBlock> blocks;

  public ParadoxDb(File dbFile) throws IOException {
    this.dbFile = new RandomAccessFile(dbFile, "r");
    this.header = readHeader();
    this.blocks = readBlocks();
  }

  public void close() throws IOException {
    this.dbFile.close();
  }

  public ParadoxDbHeader getHeader() {
    return this.header;
  }

  List<ParadoxDbBlock> getBlocks() {
    return blocks;
  }

  @Override
  public Iterator<ParadoxRecord> iterator() {
    return new Iterator<ParadoxRecord>() {

      int nextRecord = 0;

      Iterator<ParadoxDbBlock> b = blocks.iterator();

      Iterator<ParadoxRecord> r;

      @Override
      public boolean hasNext() {
        return nextRecord < header.numRecords;
      }

      @Override
      public ParadoxRecord next() {
        try {
          while((r == null || r.hasNext() == false) && b.hasNext()) {
            r = b.next().readRecords().iterator();
          }
          if(r == null || r.hasNext() == false) throw new NoSuchElementException();
          nextRecord++;
          return r.next();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public String toString() {
    try {
      StringBuilder sb = new StringBuilder();
      for(Field field : pxFields(ParadoxDbHeader.class)) {
        sb.append(String.format("%-20s:%s", field.getName(), field.get(header))).append('\n');
      }
      sb.append("-- Fields --\n");
      for(int i = 0; i < header.numFields; i++) {
        sb.append(String.format("%-20s:%s", header.fieldNames.get(i), header.fieldInfo.get(i))).append('\n');
      }
      sb.append("-- Blocks --\n");
      for(ParadoxDbBlock block : blocks) {
        sb.append(String.format(" %d <- %d(%d:%d) -> %d ", block.prevBlock, block.blockNumber, block.offsetToLastRecord, block.numRecords(), block.nextBlock)).append('\n');
      }
      return sb.toString();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Iterable<Field> pxFields(Class<?> pxObj) {
    List<Field> fields = new ArrayList<Field>();
    for(Field field : pxObj.getDeclaredFields()) {
      field.setAccessible(true);
      for(Annotation annotation : field.getAnnotations()) {
        if(annotation.annotationType().isAnnotationPresent(PxField.class)) {
          field.setAccessible(true);
          fields.add(field);
          continue;
        }
      }
    }
    return fields;
  }

  private ParadoxDbHeader readHeader() throws IOException {
    ParadoxDbHeader header = new ParadoxDbHeader();
    Field fields[] = ParadoxDbHeader.class.getDeclaredFields();
    for(Field field : fields) {
      field.setAccessible(true);
      for(Annotation annotation : field.getAnnotations()) {
        if(annotation.annotationType().isAnnotationPresent(PxField.class)) {
          PxField pxField = annotation.annotationType().getAnnotation(PxField.class);
          Offset offset = field.getAnnotation(Offset.class);
          if(offset == null) throw new IllegalStateException("field " + field.getName() + " must be annotated with @Offset");
          int byteCount = pxField.bytes();
          byte[] bytes = new byte[pxField.bytes()];
          dbFile.seek(offset.value());
          dbFile.readFully(bytes);
          Object value = null;
          switch(byteCount) {
          case 1:
            value = bytes[0];
            break;
          case 2:
            if(pxField.unsigned()) {
              value = (int) (((int) bytes[1]) << 8 | bytes[0]);
            } else {
              value = (short) (((short) bytes[1]) << 8 | bytes[0]);
            }
            break;
          case 4:
            if(pxField.unsigned()) {
              throw new UnsupportedOperationException("no such Paradox type");
            } else {
              value = (int) (((int) bytes[3]) << 24 | ((int) bytes[2]) << 16 | ((int) bytes[1]) << 8 | bytes[0]);
            }
          }
          try {
            field.set(header, value);
          } catch(Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    if(header.fileVersionId > 0x04) {
      dbFile.seek(0x0078);
    }
    readFields(header);
    return header;
  }

  private void readFields(ParadoxDbHeader header) throws IOException {
    header.fieldInfo = new ArrayList<ParadoxFieldInfo>(header.numFields);
    for(int i = 0; i < header.numFields; i++) {
      ParadoxFieldInfo info = new ParadoxFieldInfo();
      info.type = dbFile.readUnsignedByte();
      info.size = dbFile.readUnsignedByte();
      header.fieldInfo.add(info);
    }
    // skip tableNamePtr
    dbFile.skipBytes(4);
    // skip fieldNamePtrArray
    dbFile.skipBytes(4 * header.numFields);
    // Skip tableName
    dbFile.skipBytes(header.fileVersionId > 0x04 ? 261 : 79);

    header.fieldNames = new ArrayList<String>(header.numFields);
    for(int i = 0; i < header.numFields; i++) {
      StringBuilder name = new StringBuilder();
      int c = dbFile.read();
      while(c != 0 && c > 0) {
        name.append((char) c);
        c = dbFile.read();
      }
      if(c < 0) throw new IllegalStateException();
      header.fieldNames.add(name.toString());
    }
  }

  private List<ParadoxDbBlock> readBlocks() throws IOException {
    dbFile.seek(header.headerSize);
    List<ParadoxDbBlock> blocks = new ArrayList<ParadoxDbBlock>(header.fileBlocks);
    for(int i = 0; i < header.fileBlocks; i++) {
      int nextBlock = readUnsignedShort();
      int prevBlock = readUnsignedShort();
      int offsetToLastRecord = readShort();
      blocks.add(new ParadoxDbBlock(i + 1, nextBlock, prevBlock, offsetToLastRecord, dbFile.getFilePointer()));
    }
    return blocks;
  }

  private ParadoxRecord readRecord() throws IOException {
    ParadoxRecord r = new ParadoxRecord(header);
    for(int i = 0; i < header.numFields; i++) {
      ParadoxFieldInfo field = header.fieldInfo.get(i);
      ParadoxFieldType type = field.getType();
      byte[] bytes = new byte[field.size];
      dbFile.readFully(bytes);
      Object value = type.parse(bytes);
      r.setFieldValue(i, value);
    }
    return r;
  }

  private int readUnsignedShort() throws IOException {
    byte b0 = dbFile.readByte();
    byte b1 = dbFile.readByte();
    return (int) (((int) b1) << 8 & 0x0000FF00 | b0 & 0x000000FF);
  }

  private int readShort() throws IOException {
    byte b0 = dbFile.readByte();
    byte b1 = dbFile.readByte();
    return (short) (((short) b1) << 8 & 0xFF00 | b0 & 0x00FF);
  }

  public static class ParadoxDbHeader {

    @Short
    @Offset(0x0000)
    private short recordSize;

    @Short
    @Offset(0x0002)
    private short headerSize;

    @UByte
    @Offset(0x0004)
    private short fileType;

    @UByte
    @Offset(0x0005)
    private short maxTableSize;

    @Int
    @Offset(0x0006)
    private int numRecords;

    @UShort
    @Offset(0x000A)
    private int nextBlock;

    @UShort
    @Offset(0x000C)
    private int fileBlocks;

    @UShort
    @Offset(0x000E)
    private int firstBlock;

    @UShort
    @Offset(0x0010)
    private int lastBlock;

    @UByte
    @Offset(0x0014)
    private short modifiedFlags1;

    @UByte
    @Offset(0x0015)
    private short indexFieldNumber;

    @Ptr
    @Offset(0x0016)
    private int primaryIndexWorkspace;

    @Short
    @Offset(0x0021)
    private short numFields;

    @Short
    @Offset(0x0023)
    private short primaryKeyFields;

    @Int
    @Offset(0x0025)
    private int encryption1;

    @UByte
    @Offset(0x0029)
    private short sortOrder;

    @UByte
    @Offset(0x002A)
    private short modifiedFlags2;

    @UByte
    @Offset(0x002D)
    private short changeCount1;

    @UByte
    @Offset(0x002E)
    private short changeCount2;

    @Ptr
    @Offset(0x0030)
    private int tableNamePtr;

    @Ptr
    @Offset(0x0034)
    private int fldInfoPtr;

    @UByte
    @Offset(0x0038)
    private short writeProtected;

    @UByte
    @Offset(0x0039)
    private short fileVersionId;

    @UShort
    @Offset(0x003A)
    private int maxBlocks;

    @UByte
    @Offset(0x003D)
    private short auxPasswords;

    @Ptr
    @Offset(0x0040)
    private int cryptInfoStartPtr;

    @Ptr
    @Offset(0x0044)
    private int cryptInfoEndPtr;

    @Int
    @Offset(0x0049)
    private int autoInc;

    @UByte
    @Offset(0x004F)
    private short indexUpdateRequired;

    @UByte
    @Offset(0x0055)
    private short refIntegrity;

    private List<ParadoxFieldInfo> fieldInfo;

    private List<String> fieldNames;

    public short getRecordSize() {
      return recordSize;
    }

    public short getHeaderSize() {
      return headerSize;
    }

    public short getFileType() {
      return fileType;
    }

    public short getMaxTableSize() {
      return maxTableSize;
    }

    public int getNumRecords() {
      return numRecords;
    }

    public int getNextBlock() {
      return nextBlock;
    }

    public int getFileBlocks() {
      return fileBlocks;
    }

    public int getFirstBlock() {
      return firstBlock;
    }

    public int getLastBlock() {
      return lastBlock;
    }

    public short getModifiedFlags1() {
      return modifiedFlags1;
    }

    public short getIndexFieldNumber() {
      return indexFieldNumber;
    }

    public int getPrimaryIndexWorkspace() {
      return primaryIndexWorkspace;
    }

    public short getNumFields() {
      return numFields;
    }

    public short getPrimaryKeyFields() {
      return primaryKeyFields;
    }

    public int getEncryption1() {
      return encryption1;
    }

    public short getSortOrder() {
      return sortOrder;
    }

    public short getModifiedFlags2() {
      return modifiedFlags2;
    }

    public short getChangeCount1() {
      return changeCount1;
    }

    public short getChangeCount2() {
      return changeCount2;
    }

    public int getTableNamePtr() {
      return tableNamePtr;
    }

    public int getFldInfoPtr() {
      return fldInfoPtr;
    }

    public short getWriteProtected() {
      return writeProtected;
    }

    public short getFileVersionId() {
      return fileVersionId;
    }

    public int getMaxBlocks() {
      return maxBlocks;
    }

    public short getAuxPasswords() {
      return auxPasswords;
    }

    public int getCryptInfoStartPtr() {
      return cryptInfoStartPtr;
    }

    public int getCryptInfoEndPtr() {
      return cryptInfoEndPtr;
    }

    public int getAutoInc() {
      return autoInc;
    }

    public short getIndexUpdateRequired() {
      return indexUpdateRequired;
    }

    public short getRefIntegrity() {
      return refIntegrity;
    }

    public List<ParadoxFieldInfo> getFieldInfo() {
      return fieldInfo;
    }

    public List<String> getFieldNames() {
      return fieldNames;
    }

  }

  public static class ParadoxFieldInfo {

    int type;

    int size;

    ParadoxFieldType getType() {
      return ParadoxFieldType.forType(this.type);
    }

    @Override
    public String toString() {
      return getType() + "(" + size + ")";
    }
  }

  class ParadoxDbBlock {

    int blockNumber;

    int nextBlock;

    int prevBlock;

    int offsetToLastRecord;

    long fileOffset;

    ParadoxDbBlock(int blockNumber, int nextBlock, int prevBlock, int offsetToLastRecord, long fileOffset) {
      this.blockNumber = blockNumber;
      this.nextBlock = nextBlock;
      this.prevBlock = prevBlock;
      this.offsetToLastRecord = offsetToLastRecord;
      this.fileOffset = fileOffset;
    }

    public int numRecords() {
      // offsetToLastRecord is set to -header.recordSize when the block is empty.
      // this method will thus return 0 in this case.
      return offsetToLastRecord / header.recordSize + 1;
    }

    private List<ParadoxRecord> readRecords() throws IOException {
      int numRecords = numRecords();
      List<ParadoxRecord> records = new ArrayList<ParadoxRecord>(numRecords);
      dbFile.seek(fileOffset);
      for(int i = 0; i < numRecords; i++) {
        records.add(readRecord());
      }
      return records;
    }
  }

}
